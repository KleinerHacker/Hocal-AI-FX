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

/** Developer tests for [KeyDerivation]. */
class KeyDerivationTest {

    /** The same password, salt and iteration count always derive the same 32 bytes. */
    @Test
    fun sameInputGivesSameKey() {
        val salt = KeyDerivation.newSalt()
        val first = KeyDerivation.deriveKey("secret".toCharArray(), salt, 1000)
        val second = KeyDerivation.deriveKey("secret".toCharArray(), salt, 1000)
        assertEquals(32, first.size)
        assertArrayEquals(first, second)
    }

    /** A different password, salt or iteration count derives another key. */
    @Test
    fun otherInputGivesOtherKey() {
        val salt = KeyDerivation.newSalt()
        val key = KeyDerivation.deriveKey("secret".toCharArray(), salt, 1000)
        assertFalse(key.contentEquals(KeyDerivation.deriveKey("other".toCharArray(), salt, 1000)))
        assertFalse(key.contentEquals(KeyDerivation.deriveKey("secret".toCharArray(), KeyDerivation.newSalt(), 1000)))
        assertFalse(key.contentEquals(KeyDerivation.deriveKey("secret".toCharArray(), salt, 1001)))
    }

    /** The password array of the caller is not changed by the derivation. */
    @Test
    fun passwordStaysUntouched() {
        val password = "secret".toCharArray()
        KeyDerivation.deriveKey(password, KeyDerivation.newSalt(), 1000)
        assertEquals("secret", String(password))
    }

    /** A new salt has the documented size and is random. */
    @Test
    fun saltHasSizeAndIsRandom() {
        val first = KeyDerivation.newSalt()
        assertEquals(KeyDerivation.SALT_SIZE, first.size)
        assertFalse(first.contentEquals(KeyDerivation.newSalt()))
    }

    /** An iteration count outside of the allowed range is refused. */
    @Test
    fun iterationsOutOfRangeAreRefused() {
        val salt = KeyDerivation.newSalt()
        assertThrows(IllegalArgumentException::class.java) { KeyDerivation.deriveKey("a".toCharArray(), salt, 0) }
        assertThrows(IllegalArgumentException::class.java) {
            KeyDerivation.deriveKey("a".toCharArray(), salt, KeyDerivation.MAX_ITERATIONS + 1)
        }
    }
}
