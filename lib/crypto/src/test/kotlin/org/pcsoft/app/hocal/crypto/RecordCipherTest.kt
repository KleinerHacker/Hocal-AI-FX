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
package org.pcsoft.app.hocal.crypto

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Developer tests for [RecordCipher]. */
class RecordCipherTest {

    private val salt = KeyDerivation.newSalt()

    private fun cipher(password: String = "secret", salt: ByteArray = this.salt) =
        RecordCipher(password.toCharArray(), salt, 1000)

    /** A sealed record is opened to the same content. */
    @Test
    fun roundTrip() {
        cipher().use {
            val plain = "thumbnail".toByteArray()
            assertArrayEquals(plain, it.open(it.seal(plain)))
        }
    }

    /** An empty record can be sealed and opened. */
    @Test
    fun emptyRecordRoundTrips() {
        cipher().use { assertEquals(0, it.open(it.seal(ByteArray(0))).size) }
    }

    /** Every record has its own IV, so sealing the same content twice gives different bytes. */
    @Test
    fun everySealIsDifferent() {
        cipher().use {
            val plain = "same".toByteArray()
            assertFalse(it.seal(plain).contentEquals(it.seal(plain)))
        }
    }

    /** A cipher with another password cannot open the record. */
    @Test
    fun wrongKeyIsManipulated() {
        val sealed = cipher().use { it.seal("data".toByteArray()) }
        cipher("other").use {
            assertThrows(CryptoException.Manipulated::class.java) { it.open(sealed) }
        }
    }

    /** A cipher with another salt cannot open the record. */
    @Test
    fun otherSaltIsManipulated() {
        val sealed = cipher().use { it.seal("data".toByteArray()) }
        cipher(salt = KeyDerivation.newSalt()).use {
            assertThrows(CryptoException.Manipulated::class.java) { it.open(sealed) }
        }
    }

    /** A changed byte, in the IV, the content or the tag, is detected. */
    @Test
    fun flippedByteIsManipulated() {
        cipher().use {
            val sealed = it.seal("data to protect".toByteArray())
            for (index in listOf(0, 13, sealed.size - 1)) {
                val changed = sealed.copyOf().also { copy -> copy[index] = (copy[index].toInt() xor 1).toByte() }
                assertThrows(CryptoException.Manipulated::class.java) { it.open(changed) }
            }
        }
    }

    /** A cut-off record is reported as manipulated, also when it is shorter than IV and tag. */
    @Test
    fun truncatedRecordIsManipulated() {
        cipher().use {
            val sealed = it.seal("data".toByteArray())
            assertThrows(CryptoException.Manipulated::class.java) { it.open(sealed.copyOf(sealed.size - 1)) }
            assertThrows(CryptoException.Manipulated::class.java) { it.open(ByteArray(5)) }
        }
    }

    /** Other associated data than at sealing is detected, the same data is accepted. */
    @Test
    fun associatedDataIsAuthenticated() {
        cipher().use {
            val sealed = it.seal("data".toByteArray(), "name-a".toByteArray())
            assertArrayEquals("data".toByteArray(), it.open(sealed, "name-a".toByteArray()))
            assertThrows(CryptoException.Manipulated::class.java) { it.open(sealed, "name-b".toByteArray()) }
            assertThrows(CryptoException.Manipulated::class.java) { it.open(sealed) }
        }
    }

    /** The fingerprint is stable for equal data, differs for other data and for another key. */
    @Test
    fun fingerprintIsKeyedAndStable() {
        val data = "reference".toByteArray()
        cipher().use {
            assertArrayEquals(it.fingerprint(data), it.fingerprint(data))
            assertFalse(it.fingerprint(data).contentEquals(it.fingerprint("other".toByteArray())))
            assertEquals(32, it.fingerprint(data).size)
        }
        val first = cipher().use { it.fingerprint(data) }
        cipher("other").use { assertFalse(first.contentEquals(it.fingerprint(data))) }
    }

    /** After closing, every operation is refused and closing twice is harmless. */
    @Test
    fun closedCipherIsUnusable() {
        val cipher = cipher()
        val sealed = cipher.seal("data".toByteArray())
        cipher.close()
        cipher.close()
        assertThrows(IllegalStateException::class.java) { cipher.seal(ByteArray(1)) }
        assertThrows(IllegalStateException::class.java) { cipher.open(sealed) }
        assertThrows(IllegalStateException::class.java) { cipher.fingerprint(ByteArray(1)) }
    }

    /** The password array of the caller is not changed. */
    @Test
    fun passwordStaysUntouched() {
        val password = "secret".toCharArray()
        RecordCipher(password, salt, 1000).close()
        assertEquals("secret", String(password))
    }

    /** Several threads can seal and open at the same time. */
    @Test
    fun worksConcurrently() {
        val pool = Executors.newFixedThreadPool(4)
        try {
            cipher().use { cipher ->
                val results = (0 until 40).map { n ->
                    pool.submit<Boolean> {
                        val plain = "record-$n".toByteArray()
                        cipher.open(cipher.seal(plain)).contentEquals(plain)
                    }
                }
                assertTrue(results.all { it.get(10, TimeUnit.SECONDS) })
            }
        } finally {
            pool.shutdownNow()
        }
    }

    private fun assertTrue(value: Boolean) = org.junit.jupiter.api.Assertions.assertTrue(value)
}
