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
import java.nio.ByteBuffer

/**
 * The fixed header of an encrypted project file. Layout (big endian, 78 bytes): magic `HOCALENC`,
 * version, key derivation id, iteration count, salt, base IV, block size, password check value.
 * The whole header is part of the authenticated data of every block.
 */
internal class EncryptionHeader(
    val version: Int,
    val kdf: Int,
    val iterations: Int,
    val salt: ByteArray,
    val iv: ByteArray,
    val blockSize: Int,
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
        val MAGIC: ByteArray = "HOCALENC".toByteArray(Charsets.US_ASCII)
        const val VERSION = 1
        const val KDF_PBKDF2_SHA256 = 1
        const val SALT_SIZE = 16
        const val IV_SIZE = 12
        const val CHECK_SIZE = 32
        const val TAG_BYTES = 16
        const val SIZE = 8 + 1 + 1 + 4 + SALT_SIZE + IV_SIZE + 4 + CHECK_SIZE
        const val DEFAULT_ITERATIONS = 600_000
        const val MAX_ITERATIONS = 10_000_000
        const val DEFAULT_BLOCK_SIZE = 64 * 1024
        const val MIN_BLOCK_SIZE = 1024
        const val MAX_BLOCK_SIZE = 16 * 1024 * 1024

        /**
         * Reads and checks a header from the start of an encrypted file.
         *
         * @throws DamagedProjectFileException if the stream ends inside the header.
         * @throws UnsupportedEncryptionVersionException for an unknown version or key derivation.
         * @throws InvalidProjectFileException for a wrong magic or out-of-range parameters.
         */
        fun read(input: InputStream): EncryptionHeader {
            val raw = input.readNBytes(SIZE)
            if (raw.size < SIZE) throw DamagedProjectFileException("The header of the encrypted file is truncated")
            if (!raw.copyOf(MAGIC.size).contentEquals(MAGIC)) {
                throw InvalidProjectFileException("The file is not an encrypted project file")
            }
            val buffer = ByteBuffer.wrap(raw)
            buffer.position(MAGIC.size)
            val version = buffer.get().toInt() and 0xff
            if (version != VERSION) throw UnsupportedEncryptionVersionException(version, VERSION)
            val kdf = buffer.get().toInt() and 0xff
            if (kdf != KDF_PBKDF2_SHA256) {
                throw UnsupportedEncryptionVersionException(version, VERSION, "Unknown key derivation $kdf")
            }
            val iterations = buffer.getInt()
            if (iterations !in 1..MAX_ITERATIONS) {
                throw InvalidProjectFileException("Invalid iteration count $iterations")
            }
            val salt = ByteArray(SALT_SIZE).also { buffer.get(it) }
            val iv = ByteArray(IV_SIZE).also { buffer.get(it) }
            val blockSize = buffer.getInt()
            if (blockSize !in MIN_BLOCK_SIZE..MAX_BLOCK_SIZE) {
                throw InvalidProjectFileException("Invalid block size $blockSize")
            }
            val check = ByteArray(CHECK_SIZE).also { buffer.get(it) }
            return EncryptionHeader(version, kdf, iterations, salt, iv, blockSize, check)
        }
    }
}
