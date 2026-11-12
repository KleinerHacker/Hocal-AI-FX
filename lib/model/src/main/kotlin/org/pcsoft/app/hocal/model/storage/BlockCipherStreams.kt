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
import java.io.PushbackInputStream
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.SecretKey

/**
 * Encrypts what is written, block by block. A block is as large as the header says, the last one is
 * written on [close], even when it is full. Never closes the wrapped stream.
 */
internal class BlockCipherOutputStream(
    private val output: OutputStream,
    private val key: SecretKey,
    private val header: EncryptionHeader,
) : OutputStream() {

    private val buffer = ByteArray(header.blockSize)
    private var filled = 0
    private var counter = 0L
    private var closed = false

    override fun write(value: Int) {
        write(byteArrayOf(value.toByte()), 0, 1)
    }

    override fun write(source: ByteArray, offset: Int, length: Int) {
        check(!closed) { "Stream closed" }
        var position = offset
        var left = length
        while (left > 0) {
            if (filled == buffer.size) writeBlock(last = false)
            val count = minOf(left, buffer.size - filled)
            System.arraycopy(source, position, buffer, filled, count)
            filled += count
            position += count
            left -= count
        }
    }

    override fun flush() {
        output.flush()
    }

    override fun close() {
        if (closed) return
        writeBlock(last = true)
        closed = true
        output.flush()
    }

    private fun writeBlock(last: Boolean) {
        val cipher = ProjectCipher.blockCipher(Cipher.ENCRYPT_MODE, key, header, counter, last)
        output.write(cipher.doFinal(buffer, 0, filled))
        filled = 0
        counter++
    }
}

/**
 * Decrypts block by block and verifies every block before its data is handed out. A failed check,
 * also caused by a cut-off or reordered file, is reported as [ManipulatedProjectFileException].
 * Never closes the wrapped stream.
 */
internal class BlockCipherInputStream(
    input: InputStream,
    private val key: SecretKey,
    private val header: EncryptionHeader,
) : InputStream() {

    private val source = PushbackInputStream(input, 1)
    private val encrypted = ByteArray(header.blockSize + EncryptionHeader.TAG_BYTES)
    private var plain = ByteArray(0)
    private var position = 0
    private var counter = 0L
    private var finished = false

    override fun read(): Int {
        val one = ByteArray(1)
        return if (read(one, 0, 1) < 0) -1 else one[0].toInt() and 0xff
    }

    override fun read(target: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        while (position >= plain.size) {
            if (finished) return -1
            nextBlock()
        }
        val count = minOf(length, plain.size - position)
        System.arraycopy(plain, position, target, offset, count)
        position += count
        return count
    }

    override fun close() {
        // the wrapped stream stays open
    }

    private fun nextBlock() {
        val read = source.readNBytes(encrypted, 0, encrypted.size)
        var last = true
        if (read == encrypted.size) {
            val probe = source.read()
            if (probe >= 0) {
                source.unread(probe)
                last = false
            }
        }
        if (read < EncryptionHeader.TAG_BYTES) {
            throw ManipulatedProjectFileException("The encrypted file is cut off")
        }
        try {
            plain = ProjectCipher.blockCipher(Cipher.DECRYPT_MODE, key, header, counter, last)
                .doFinal(encrypted, 0, read)
        } catch (e: GeneralSecurityException) {
            throw ManipulatedProjectFileException("The encrypted file is manipulated, damaged or cut off", e)
        }
        position = 0
        counter++
        if (last) finished = true
    }
}
