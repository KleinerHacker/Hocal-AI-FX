/*
 * Copyright (c) KleinerHacker alias Pfeiffer C Soft 2026.
 * This work is licensed under the Apache License, Version 2.0.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations.
 */
package org.pcsoft.app.hocal.model.storage

import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Key derivation and stream creation of the encrypted project file: AES-256-GCM, applied block by
 * block, key from the password with PBKDF2-HMAC-SHA256.
 */
internal object ProjectCipher {

    private val random = SecureRandom()

    private val checkText = "hocal-password-check".toByteArray(Charsets.US_ASCII)

    /**
     * Writes the header to [output] and returns the stream that encrypts everything written to it.
     * The returned stream MUST be closed to write the last block; closing does not close [output].
     * The password array is not changed.
     */
    fun openOutput(output: OutputStream, password: CharArray, iterations: Int): OutputStream {
        require(iterations in 1..EncryptionHeader.MAX_ITERATIONS) { "Iteration count out of range" }
        val salt = ByteArray(EncryptionHeader.SALT_SIZE).also { random.nextBytes(it) }
        val iv = ByteArray(EncryptionHeader.IV_SIZE).also { random.nextBytes(it) }
        val keyBytes = deriveKey(password, salt, iterations)
        try {
            val header = EncryptionHeader(
                EncryptionHeader.VERSION, EncryptionHeader.KDF_PBKDF2_SHA256, iterations, salt, iv,
                EncryptionHeader.DEFAULT_BLOCK_SIZE, checkOf(keyBytes),
            )
            output.write(header.bytes)
            return BlockCipherOutputStream(output, SecretKeySpec(keyBytes, "AES"), header)
        } finally {
            keyBytes.fill(0)
        }
    }

    /**
     * Reads the header from [input], checks the password and returns the stream that decrypts the rest.
     * Every block is verified before its data is passed on.
     *
     * @throws PasswordRequiredException if [password] is `null`.
     * @throws WrongPasswordException if the password does not match.
     */
    fun openInput(input: InputStream, password: CharArray?): InputStream {
        val header = EncryptionHeader.read(input)
        if (password == null) throw PasswordRequiredException()
        val keyBytes = deriveKey(password, header.salt, header.iterations)
        try {
            if (!MessageDigest.isEqual(checkOf(keyBytes), header.check)) throw WrongPasswordException()
            return BlockCipherInputStream(input, SecretKeySpec(keyBytes, "AES"), header)
        } finally {
            keyBytes.fill(0)
        }
    }

    /** The cipher of one block: unique IV per block, header and last-block flag as authenticated data. */
    fun blockCipher(mode: Int, key: SecretKey, header: EncryptionHeader, counter: Long, last: Boolean): Cipher {
        val iv = header.iv.copyOf()
        for (i in 0 until 8) {
            iv[EncryptionHeader.IV_SIZE - 8 + i] =
                (iv[EncryptionHeader.IV_SIZE - 8 + i].toInt() xor ((counter ushr (56 - 8 * i)).toInt() and 0xff)).toByte()
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(mode, key, GCMParameterSpec(EncryptionHeader.TAG_BYTES * 8, iv))
        cipher.updateAAD(header.bytes)
        cipher.updateAAD(byteArrayOf(if (last) 1 else 0))
        return cipher
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, 256)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun checkOf(keyBytes: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(keyBytes, "HmacSHA256"))
        return mac.doFinal(checkText)
    }
}
