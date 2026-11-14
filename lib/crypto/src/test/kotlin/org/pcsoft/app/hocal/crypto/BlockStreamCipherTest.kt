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

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/** Developer tests for [BlockStreamCipher] and the header it writes. */
class BlockStreamCipherTest {

    private val password = "secret".toCharArray()

    private fun encrypt(plain: ByteArray, pw: CharArray = password): ByteArray {
        val bytes = ByteArrayOutputStream()
        BlockStreamCipher.openOutput(bytes, pw, 1000).use { it.write(plain) }
        return bytes.toByteArray()
    }

    private fun decrypt(bytes: ByteArray, pw: CharArray? = password): ByteArray =
        BlockStreamCipher.openInput(ByteArrayInputStream(bytes), pw).readAllBytes()

    /** Data of several blocks is written and read back unchanged. */
    @Test
    fun roundTripOverSeveralBlocks() {
        val plain = ByteArray(EncryptionHeader.DEFAULT_BLOCK_SIZE * 2 + 100) { (it * 7).toByte() }
        assertArrayEquals(plain, decrypt(encrypt(plain)))
    }

    /** Empty data and data of exactly one block are written and read back unchanged. */
    @Test
    fun emptyAndExactBlockRoundTrip() {
        assertEquals(0, decrypt(encrypt(ByteArray(0))).size)
        val one = ByteArray(EncryptionHeader.DEFAULT_BLOCK_SIZE) { it.toByte() }
        assertArrayEquals(one, decrypt(encrypt(one)))
    }

    /** The encrypted data starts with the magic, so the form can be recognised. */
    @Test
    fun startsWithMagic() {
        assertArrayEquals(EncryptionHeader.MAGIC, encrypt(ByteArray(3)).copyOf(EncryptionHeader.MAGIC.size))
    }

    /** No password is reported as required, another one as wrong. */
    @Test
    fun missingAndWrongPassword() {
        val bytes = encrypt(ByteArray(10))
        assertThrows(CryptoException.PasswordRequired::class.java) { decrypt(bytes, null) }
        assertThrows(CryptoException.WrongPassword::class.java) { decrypt(bytes, "other".toCharArray()) }
    }

    /** A changed data byte, a cut-off end and a missing header are reported with their own error. */
    @Test
    fun damageIsReported() {
        val bytes = encrypt(ByteArray(100))
        val flipped = bytes.copyOf().also { it[bytes.size - 1] = (it[bytes.size - 1].toInt() xor 1).toByte() }
        assertThrows(CryptoException.Manipulated::class.java) { decrypt(flipped) }
        assertThrows(CryptoException.Manipulated::class.java) { decrypt(bytes.copyOf(EncryptionHeader.SIZE + 3)) }
        assertThrows(CryptoException.TruncatedHeader::class.java) { decrypt(bytes.copyOf(10)) }
        assertThrows(CryptoException.InvalidHeader::class.java) { decrypt(ByteArray(EncryptionHeader.SIZE)) }
    }

    /** An unknown version in the header is reported as unsupported. */
    @Test
    fun unknownVersionIsUnsupported() {
        val bytes = encrypt(ByteArray(10)).also { it[EncryptionHeader.MAGIC.size] = 9 }
        val error = assertThrows(CryptoException.UnsupportedVersion::class.java) { decrypt(bytes) }
        assertEquals(9, error.foundVersion)
    }

    /** An out-of-range iteration count is refused when writing, the password stays untouched. */
    @Test
    fun iterationsAreChecked() {
        assertThrows(IllegalArgumentException::class.java) {
            BlockStreamCipher.openOutput(ByteArrayOutputStream(), password, 0)
        }
        encrypt(ByteArray(1))
        assertEquals("secret", String(password))
    }
}
