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
package org.pcsoft.app.hocal.thumbnail

import org.pcsoft.app.hocal.crypto.CryptoException
import org.pcsoft.app.hocal.crypto.KeyDerivation
import org.pcsoft.app.hocal.crypto.RecordCipher
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.zip.CRC32
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * A [ThumbnailCache] in a file that is a ZIP without compression (method STORED): every thumbnail is an entry that
 * is appended when it is stored and read directly from its position, without unpacking.
 *
 * The index of the entries lives in memory; the ZIP directory is written only on [close] with `keepFile`, so a kept file
 * opens with every ZIP reader. The cache is meant for one session: with the default the file is deleted on [close].
 * An entry that is replaced or invalidated stays in the file as dead space until the file is reset, which happens when
 * it would grow beyond [maxBytes] or on [clear].
 *
 * With a password every entry is sealed with a [RecordCipher] whose key is derived once, and the entry name is a keyed
 * fingerprint of the key, so neither content nor reference can be read from the file. The password array is not kept;
 * the key bytes are overwritten on [close].
 *
 * Writing is serialized, reading goes on in parallel. A failure of the file counts as no thumbnail; it never reaches
 * the caller. After [close] every call is a miss or has no effect.
 *
 * @param file the file; it is created, an existing one is replaced
 * @param password the password to encrypt with, `null` for plain entries; the array is not changed
 * @param maxBytes the size from which the file is reset
 * @param iterations the iteration count of the key derivation
 * @param keepFile whether [close] writes the ZIP directory and keeps the file instead of deleting it
 * @throws IOException if the file cannot be created
 */
class ZipThumbnailStore @JvmOverloads constructor(
    private val file: Path,
    password: CharArray? = null,
    private val maxBytes: Long = DEFAULT_MAX_BYTES,
    iterations: Int = KeyDerivation.DEFAULT_ITERATIONS,
    private val keepFile: Boolean = false,
) : ThumbnailCache<ThumbnailPixels>, AutoCloseable {

    private class Entry(
        val name: ByteArray,
        val headerOffset: Long,
        val dataOffset: Long,
        val size: Int,
        val crc: Long,
        val mediaCount: Int,
    )

    private val lock = ReentrantReadWriteLock()
    private val index = HashMap<ThumbnailKey, Entry>()
    private val cipher: RecordCipher? = password?.let { RecordCipher(it, KeyDerivation.newSalt(), iterations) }
    private val channel: FileChannel
    private var end = 0L
    private var closed = false

    init {
        try {
            channel = FileChannel.open(
                file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.READ, StandardOpenOption.WRITE,
            )
        } catch (e: IOException) {
            cipher?.close()
            throw e
        }
    }

    /** The number of thumbnails held now. */
    val size: Int
        get() = lock.read { index.size }

    /** The size of the file now, dead space included. */
    val fileSize: Long
        get() = lock.read { end }

    override fun get(key: ThumbnailKey, mediaCount: Int): ThumbnailPixels? {
        var stale = false
        val result = lock.read {
            if (closed) return null
            val entry = index[key] ?: return null
            if (entry.mediaCount != mediaCount) {
                stale = true
                null
            } else {
                readEntry(entry, mediaCount)
            }
        }
        if (stale) lock.write { if (index[key]?.mediaCount != mediaCount) index.remove(key) }
        return result
    }

    override fun put(key: ThumbnailKey, mediaCount: Int, value: ThumbnailPixels) {
        // The cipher is closed together with the store, possibly by another thread while this call is on its way.
        val name: ByteArray
        val data: ByteArray
        try {
            name = entryName(key)
            val payload = encode(mediaCount, value)
            data = cipher?.seal(payload, name) ?: payload
        } catch (_: IllegalStateException) {
            return
        }
        val crc = CRC32().also { it.update(data) }.value
        lock.write {
            if (closed) return
            try {
                val total = LOCAL_HEADER_SIZE + name.size + data.size
                if (end + total > maxBytes || index.size >= MAX_ENTRIES) reset()
                val buffer = ByteBuffer.allocate(total).order(ByteOrder.LITTLE_ENDIAN)
                buffer.putInt(LOCAL_SIGNATURE).putShort(VERSION).putShort(FLAGS).putShort(0).putShort(0).putShort(DATE)
                    .putInt(crc.toInt()).putInt(data.size).putInt(data.size)
                    .putShort(name.size.toShort()).putShort(0).put(name).put(data).flip()
                writeFully(buffer, end)
                index[key] = Entry(name, end, end + LOCAL_HEADER_SIZE + name.size, data.size, crc, mediaCount)
                end += total
            } catch (_: IOException) {
                index.remove(key)
            }
        }
    }

    override fun invalidate(reference: String) {
        lock.write { index.keys.removeIf { it.reference == reference } }
    }

    override fun clear() {
        lock.write {
            if (closed) return
            try {
                reset()
            } catch (_: IOException) {
                index.clear()
            }
        }
    }

    /**
     * Closes the file and overwrites the key. With `keepFile` the ZIP directory is written first and the file stays,
     * else the file is deleted. Closing twice is allowed.
     */
    override fun close() {
        lock.write {
            if (closed) return
            closed = true
            try {
                if (keepFile) writeDirectory()
            } catch (_: IOException) {
                // a file nobody can read is no worse than a deleted one
            } finally {
                try {
                    channel.close()
                } catch (_: IOException) {
                }
                cipher?.close()
                index.clear()
                if (!keepFile) {
                    try {
                        Files.deleteIfExists(file)
                    } catch (_: IOException) {
                    }
                }
            }
        }
    }

    private fun entryName(key: ThumbnailKey): ByteArray {
        val plain = "${key.reference}/${key.number}-${key.size}.px".toByteArray(Charsets.UTF_8)
        val sealing = cipher ?: return plain
        return (sealing.fingerprint(plain).joinToString("") { "%02x".format(it) } + ".bin").toByteArray(Charsets.US_ASCII)
    }

    private fun encode(mediaCount: Int, pixels: ThumbnailPixels): ByteArray =
        ByteBuffer.allocate(PAYLOAD_HEADER_SIZE + pixels.bytes.size)
            .putInt(mediaCount).putInt(pixels.width).putInt(pixels.height).put(pixels.bytes).array()

    private fun readEntry(entry: Entry, mediaCount: Int): ThumbnailPixels? = try {
        val buffer = ByteBuffer.allocate(entry.size)
        var position = entry.dataOffset
        while (buffer.hasRemaining()) {
            val read = channel.read(buffer, position)
            if (read < 0) throw IOException("The file ends inside an entry")
            position += read
        }
        val data = buffer.array()
        val payload = if (cipher != null) {
            cipher.open(data, entry.name)
        } else {
            if (CRC32().also { it.update(data) }.value != entry.crc) throw IOException("Checksum mismatch")
            data
        }
        decode(payload, mediaCount)
    } catch (_: IOException) {
        null
    } catch (_: CryptoException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: java.nio.BufferUnderflowException) {
        null
    }

    private fun decode(payload: ByteArray, mediaCount: Int): ThumbnailPixels? {
        val buffer = ByteBuffer.wrap(payload)
        if (buffer.getInt() != mediaCount) return null
        val width = buffer.getInt()
        val height = buffer.getInt()
        return ThumbnailPixels(width, height, payload.copyOfRange(PAYLOAD_HEADER_SIZE, payload.size))
    }

    private fun reset() {
        index.clear()
        channel.truncate(0)
        end = 0
    }

    private fun writeFully(buffer: ByteBuffer, start: Long) {
        var position = start
        while (buffer.hasRemaining()) position += channel.write(buffer, position)
    }

    private fun writeDirectory() {
        val entries = index.values.sortedBy { it.headerOffset }
        val directorySize = entries.sumOf { CENTRAL_HEADER_SIZE + it.name.size } + END_SIZE
        val buffer = ByteBuffer.allocate(directorySize).order(ByteOrder.LITTLE_ENDIAN)
        for (entry in entries) {
            buffer.putInt(CENTRAL_SIGNATURE).putShort(VERSION).putShort(VERSION).putShort(FLAGS).putShort(0)
                .putShort(0).putShort(DATE).putInt(entry.crc.toInt()).putInt(entry.size).putInt(entry.size)
                .putShort(entry.name.size.toShort()).putShort(0).putShort(0).putShort(0).putShort(0)
                .putInt(0).putInt(entry.headerOffset.toInt()).put(entry.name)
        }
        val directoryStart = end
        buffer.putInt(END_SIGNATURE).putShort(0).putShort(0).putShort(entries.size.toShort())
            .putShort(entries.size.toShort()).putInt(directorySize - END_SIZE).putInt(directoryStart.toInt())
            .putShort(0).flip()
        writeFully(buffer, directoryStart)
    }

    /** The limits and the constants of the file format. */
    companion object {
        /** The size of the file from which it is reset, 64 MB. */
        const val DEFAULT_MAX_BYTES: Long = 64L * 1024 * 1024

        private const val MAX_ENTRIES = 60_000
        private const val LOCAL_SIGNATURE = 0x04034b50
        private const val CENTRAL_SIGNATURE = 0x02014b50
        private const val END_SIGNATURE = 0x06054b50
        private const val LOCAL_HEADER_SIZE = 30
        private const val CENTRAL_HEADER_SIZE = 46
        private const val END_SIZE = 22
        private const val PAYLOAD_HEADER_SIZE = 12
        private const val VERSION: Short = 20

        // bit 11: the entry names are UTF-8
        private const val FLAGS: Short = 0x0800

        // 1980-01-01, the earliest date a ZIP can hold
        private const val DATE: Short = 33
    }
}
