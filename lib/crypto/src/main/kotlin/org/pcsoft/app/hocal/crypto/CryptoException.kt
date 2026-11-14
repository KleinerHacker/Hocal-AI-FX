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

/**
 * Base of all errors the encryption can report for the encrypted data itself. Plain I/O problems are reported as
 * [java.io.IOException] and are not part of this hierarchy.
 */
sealed class CryptoException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The data is encrypted but no password was given. */
    class PasswordRequired : CryptoException("The data is encrypted, a password is required")

    /** The given password does not match the encrypted data. */
    class WrongPassword : CryptoException("The password does not match the encrypted data")

    /** The encrypted content failed its authentication: it was manipulated, damaged or cut off. */
    class Manipulated(message: String, cause: Throwable? = null) : CryptoException(message, cause)

    /** The header of the encrypted data is cut off. */
    class TruncatedHeader(message: String) : CryptoException(message)

    /** The header of the encrypted data has a wrong magic or out-of-range parameters. */
    class InvalidHeader(message: String) : CryptoException(message)

    /**
     * The encrypted data uses an encryption version or key derivation this library does not know.
     *
     * @property foundVersion The encryption version found in the data.
     * @property supportedVersion The newest encryption version this library can read.
     */
    class UnsupportedVersion(
        val foundVersion: Int,
        val supportedVersion: Int,
        detail: String = "Encryption version $foundVersion is not supported, the newest known is $supportedVersion",
    ) : CryptoException(detail)
}
