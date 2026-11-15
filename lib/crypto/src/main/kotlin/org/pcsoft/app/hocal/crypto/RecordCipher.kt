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
package org.pcsoft.app.hocal.crypto

import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Seals single records with AES-256-GCM. The key is derived from the password once, so a record is sealed and
 * opened quickly; every record has its own random IV. Thread safe.
 *
 * The password array is not kept. [close] overwrites the key bytes.
 *
 * @param password The password; the array is not changed.
 * @param salt The salt of the key derivation.
 * @param iterations The iteration count of the key derivation.
 */
class RecordCipher @JvmOverloads constructor(
    password: CharArray,
    salt: ByteArray,
    iterations: Int = KeyDerivation.DEFAULT_ITERATIONS,
) : AutoCloseable {

    private val random = SecureRandom()

    @Volatile
    private var key: ByteArray? = KeyDerivation.deriveKey(password, salt, iterations)

    /**
     * Seals a record.
     *
     * @param plain The content.
     * @param associated Data that is authenticated but not stored; opening needs the same data.
     * @return The IV, followed by the encrypted content and the authentication tag.
     * @throws IllegalStateException if the cipher is closed.
     */
    @JvmOverloads
    fun seal(plain: ByteArray, associated: ByteArray = ByteArray(0)): ByteArray {
        val iv = ByteArray(IV_SIZE).also { random.nextBytes(it) }
        val cipher = cipher(Cipher.ENCRYPT_MODE, iv, associated)
        return iv + cipher.doFinal(plain)
    }

    /**
     * Opens a sealed record.
     *
     * @param sealed What [seal] returned.
     * @param associated The same data that was given to [seal].
     * @return The content.
     * @throws CryptoException.Manipulated if the record was changed, damaged or sealed with another key or data.
     * @throws IllegalStateException if the cipher is closed.
     */
    @JvmOverloads
    fun open(sealed: ByteArray, associated: ByteArray = ByteArray(0)): ByteArray {
        if (sealed.size < IV_SIZE + TAG_SIZE) throw CryptoException.Manipulated("The record is cut off")
        val cipher = cipher(Cipher.DECRYPT_MODE, sealed.copyOfRange(0, IV_SIZE), associated)
        try {
            return cipher.doFinal(sealed, IV_SIZE, sealed.size - IV_SIZE)
        } catch (e: GeneralSecurityException) {
            throw CryptoException.Manipulated("The record is manipulated, damaged or sealed with another key", e)
        }
    }

    /**
     * A keyed fingerprint of [data] (HMAC-SHA256, domain separated from the sealing). The same data gives the
     * same fingerprint, but it reveals nothing about the data without the key.
     *
     * @throws IllegalStateException if the cipher is closed.
     */
    fun fingerprint(data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(requireKey(), "HmacSHA256"))
        mac.update(FINGERPRINT_CONTEXT)
        return mac.doFinal(data)
    }

    /** Overwrites the key bytes; the cipher cannot be used afterwards. Closing twice is allowed. */
    override fun close() {
        key?.fill(0)
        key = null
    }

    private fun requireKey(): ByteArray = checkNotNull(key) { "The cipher is closed" }

    private fun cipher(mode: Int, iv: ByteArray, associated: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(mode, SecretKeySpec(requireKey(), "AES"), GCMParameterSpec(TAG_SIZE * 8, iv))
        cipher.updateAAD(associated)
        return cipher
    }

    private companion object {
        const val IV_SIZE = 12
        const val TAG_SIZE = 16
        val FINGERPRINT_CONTEXT = "hocal-record-fingerprint".toByteArray(Charsets.US_ASCII)
    }
}
