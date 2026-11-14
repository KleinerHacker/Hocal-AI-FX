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

import java.io.InputStream
import java.nio.ByteBuffer

/**
 * The fixed header of encrypted data. Layout (big endian, 78 bytes): magic `HOCALENC`,
 * version, key derivation id, iteration count, salt, base IV, block size, password check value.
 * The whole header is part of the authenticated data of every block.
 */
class EncryptionHeader(
    /** The encryption version. */
    val version: Int,
    /** The key derivation id. */
    val kdf: Int,
    /** The iteration count of the key derivation. */
    val iterations: Int,
    /** The salt of the key derivation. */
    val salt: ByteArray,
    /** The base IV; the IV of a block is derived from it. */
    val iv: ByteArray,
    /** The size of a plain block in bytes. */
    val blockSize: Int,
    /** The password check value. */
    val check: ByteArray,
) {

    /** The header as written to the file. */
    val bytes: ByteArray by lazy {
        ByteBuffer.allocate(SIZE)
            .put(MAGIC).put(version.toByte()).put(kdf.toByte()).putInt(iterations)
            .put(salt).put(iv).putInt(blockSize).put(check)
            .array()
    }

    companion object {
        /** The magic bytes at the start of encrypted data. */
        val MAGIC: ByteArray = "HOCALENC".toByteArray(Charsets.US_ASCII)
        /** The encryption version this library writes and reads. */
        const val VERSION = 1
        /** The key derivation id of PBKDF2-HMAC-SHA256. */
        const val KDF_PBKDF2_SHA256 = 1
        /** The size of the salt in bytes. */
        const val SALT_SIZE = KeyDerivation.SALT_SIZE
        /** The size of the base IV in bytes. */
        const val IV_SIZE = 12
        /** The size of the password check value in bytes. */
        const val CHECK_SIZE = 32
        /** The size of the authentication tag of a block in bytes. */
        const val TAG_BYTES = 16
        /** The size of the header in bytes. */
        const val SIZE = 8 + 1 + 1 + 4 + SALT_SIZE + IV_SIZE + 4 + CHECK_SIZE
        /** The default size of a plain block in bytes. */
        const val DEFAULT_BLOCK_SIZE = 64 * 1024
        /** The smallest accepted block size when reading. */
        const val MIN_BLOCK_SIZE = 1024
        /** The largest accepted block size when reading. */
        const val MAX_BLOCK_SIZE = 16 * 1024 * 1024

        /**
         * Reads and checks a header from the start of encrypted data.
         *
         * @throws CryptoException.TruncatedHeader if the stream ends inside the header.
         * @throws CryptoException.UnsupportedVersion for an unknown version or key derivation.
         * @throws CryptoException.InvalidHeader for a wrong magic or out-of-range parameters.
         */
        fun read(input: InputStream): EncryptionHeader {
            val raw = input.readNBytes(SIZE)
            if (raw.size < SIZE) throw CryptoException.TruncatedHeader("The header of the encrypted data is truncated")
            if (!raw.copyOf(MAGIC.size).contentEquals(MAGIC)) {
                throw CryptoException.InvalidHeader("The data is not encrypted data of this application")
            }
            val buffer = ByteBuffer.wrap(raw)
            buffer.position(MAGIC.size)
            val version = buffer.get().toInt() and 0xff
            if (version != VERSION) throw CryptoException.UnsupportedVersion(version, VERSION)
            val kdf = buffer.get().toInt() and 0xff
            if (kdf != KDF_PBKDF2_SHA256) {
                throw CryptoException.UnsupportedVersion(version, VERSION, "Unknown key derivation $kdf")
            }
            val iterations = buffer.getInt()
            if (iterations !in 1..KeyDerivation.MAX_ITERATIONS) {
                throw CryptoException.InvalidHeader("Invalid iteration count $iterations")
            }
            val salt = ByteArray(SALT_SIZE).also { buffer.get(it) }
            val iv = ByteArray(IV_SIZE).also { buffer.get(it) }
            val blockSize = buffer.getInt()
            if (blockSize !in MIN_BLOCK_SIZE..MAX_BLOCK_SIZE) {
                throw CryptoException.InvalidHeader("Invalid block size $blockSize")
            }
            val check = ByteArray(CHECK_SIZE).also { buffer.get(it) }
            return EncryptionHeader(version, kdf, iterations, salt, iv, blockSize, check)
        }
    }
}
