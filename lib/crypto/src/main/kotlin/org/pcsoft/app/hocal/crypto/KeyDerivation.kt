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

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Derives a 256 bit key from a password with PBKDF2-HMAC-SHA256. */
object KeyDerivation {

    /** The default iteration count of the key derivation. */
    const val DEFAULT_ITERATIONS = 600_000

    /** The highest iteration count accepted when reading, a guard against absurd values in a damaged header. */
    const val MAX_ITERATIONS = 10_000_000

    /** The size of a salt in bytes. */
    const val SALT_SIZE = 16

    private val random = SecureRandom()

    /** A new random salt of [SALT_SIZE] bytes. */
    fun newSalt(): ByteArray = ByteArray(SALT_SIZE).also { random.nextBytes(it) }

    /**
     * Derives the key. The password array is not changed. The caller MUST overwrite the result when done with it.
     *
     * @param password The password.
     * @param salt The salt.
     * @param iterations The iteration count, 1 to [MAX_ITERATIONS].
     * @return The 32 key bytes.
     */
    fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int = DEFAULT_ITERATIONS): ByteArray {
        require(iterations in 1..MAX_ITERATIONS) { "Iteration count out of range" }
        val spec = PBEKeySpec(password, salt, iterations, 256)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
