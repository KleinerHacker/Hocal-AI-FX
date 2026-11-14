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

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/** Developer tests for [ZipThumbnailStore]. */
class ZipThumbnailStoreTest {

    @TempDir
    lateinit var directory: Path

    private val file get() = directory.resolve("thumbnails.zip")

    private fun pixels(seed: Int, width: Int = 4, height: Int = 3) =
        ThumbnailPixels(width, height, ByteArray(width * height * 4) { (it * 7 + seed).toByte() })

    private fun key(reference: String = "r1", number: Int = 0, size: Int = 48) = ThumbnailKey(reference, number, size)

    private fun plain(maxBytes: Long = ZipThumbnailStore.DEFAULT_MAX_BYTES, keep: Boolean = false) =
        ZipThumbnailStore(file, null, maxBytes, 1, keep)

    private fun encrypted(password: String = "secret", keep: Boolean = false) =
        ZipThumbnailStore(file, password.toCharArray(), ZipThumbnailStore.DEFAULT_MAX_BYTES, 1000, keep)

    /** Use case: a stored thumbnail is read back with the same pixels. */
    @Test
    fun storedThumbnailIsFound() {
        plain().use {
            it.put(key(), 3, pixels(1))
            assertEquals(pixels(1), it.get(key(), 3))
            assertEquals(1, it.size)
        }
    }

    /** Use case: the key differs in reference, medium number or size. Nothing is found. */
    @Test
    fun unknownKeyIsAMiss() {
        plain().use {
            it.put(key("r1", 0, 48), 1, pixels(1))
            assertNull(it.get(key("r2", 0, 48), 1))
            assertNull(it.get(key("r1", 1, 48), 1))
            assertNull(it.get(key("r1", 0, 96), 1))
        }
    }

    /** Use case: the reference got another number of media. The thumbnail is stale, dropped and not returned. */
    @Test
    fun staleThumbnailIsDropped() {
        plain().use {
            it.put(key(), 1, pixels(1))
            assertNull(it.get(key(), 2))
            assertEquals(0, it.size)
            assertNull(it.get(key(), 1))
        }
    }

    /** Use case: a thumbnail is stored again under the same key. The new one is found. */
    @Test
    fun newerThumbnailReplacesTheOlderOne() {
        plain().use {
            it.put(key(), 1, pixels(1))
            it.put(key(), 1, pixels(2))
            assertEquals(pixels(2), it.get(key(), 1))
            assertEquals(1, it.size)
        }
    }

    /** Use case: a reference is invalidated. All its thumbnails go, those of other references stay. */
    @Test
    fun invalidateDropsOneReference() {
        plain().use {
            it.put(key("r1", 0, 48), 2, pixels(1))
            it.put(key("r1", 1, 96), 2, pixels(2))
            it.put(key("r2", 0, 48), 1, pixels(3))
            it.invalidate("r1")
            assertEquals(1, it.size)
            assertEquals(pixels(3), it.get(key("r2", 0, 48), 1))
        }
    }

    /** Use case: the store is cleared. Nothing is left, the file is empty again and can be used on. */
    @Test
    fun clearDropsEverythingAndResetsTheFile() {
        plain().use {
            it.put(key("r1"), 1, pixels(1))
            it.put(key("r2"), 1, pixels(2))
            it.clear()
            assertEquals(0, it.size)
            assertEquals(0L, it.fileSize)
            assertEquals(0L, Files.size(file))
            it.put(key("r3"), 1, pixels(3))
            assertEquals(pixels(3), it.get(key("r3"), 1))
        }
    }

    /** Use case: the file would grow beyond the limit. It is reset, the oldest thumbnails go, the newest stays. */
    @Test
    fun fileIsResetAtTheLimit() {
        plain(maxBytes = 400).use {
            repeat(10) { n -> it.put(key("r$n"), 1, pixels(n)) }
            assertTrue(it.fileSize <= 400, "size ${it.fileSize}")
            assertNull(it.get(key("r0"), 1))
            assertEquals(pixels(9), it.get(key("r9"), 1))
        }
    }

    /** Use case: the session ends. By default the file is deleted, and the closed store is a miss and keeps quiet. */
    @Test
    fun closeDeletesTheFileAndSilencesTheStore() {
        val store = plain()
        store.put(key(), 1, pixels(1))
        assertTrue(Files.exists(file))

        store.close()
        store.close()

        assertFalse(Files.exists(file))
        assertNull(store.get(key(), 1))
        store.put(key(), 1, pixels(2))
        store.invalidate("r1")
        store.clear()
        assertFalse(Files.exists(file))
    }

    /** Use case: the file is kept. It is a valid ZIP of uncompressed entries with right checksums and the pixels inside. */
    @Test
    fun keptFileIsAValidStoredZip() {
        plain(keep = true).use {
            it.put(key("r1", 0, 48), 5, pixels(1))
            it.put(key("r2", 1, 96), 6, pixels(2, 2, 2))
        }

        ZipFile(file.toFile()).use { zip ->
            val entries = zip.entries().toList().sortedBy { it.name }
            assertEquals(listOf("r1/0-48.px", "r2/1-96.px"), entries.map { it.name })
            for (entry in entries) {
                assertEquals(ZipEntry.STORED, entry.method)
                val bytes = zip.getInputStream(entry).readAllBytes()
                assertEquals(entry.size, bytes.size.toLong())
                assertEquals(entry.crc, CRC32().also { crc -> crc.update(bytes) }.value)
            }
            val first = zip.getInputStream(entries[0]).readAllBytes()
            assertArrayEquals(pixels(1).bytes, first.copyOfRange(12, first.size))
        }
    }

    /** Use case: a kept file of an empty store is a valid, empty ZIP. */
    @Test
    fun keptFileOfAnEmptyStoreIsValid() {
        plain(keep = true).close()
        ZipFile(file.toFile()).use { assertEquals(0, it.size()) }
    }

    /** Use case: the file is damaged from outside. The damaged thumbnail is a miss, the store does not fail. */
    @Test
    fun damagedPlainEntryIsAMiss() {
        plain().use {
            it.put(key(), 1, pixels(1))
            it.put(key("r2"), 1, pixels(2))
            flipLastByteOfTheFile()
            assertNull(it.get(key("r2"), 1))
            assertEquals(pixels(1), it.get(key(), 1))
        }
    }

    /** Use case: the file was cut off from outside. Every thumbnail is a miss and the store does not fail. */
    @Test
    fun cutOffFileIsAMiss() {
        plain().use {
            it.put(key(), 1, pixels(1))
            java.nio.channels.FileChannel.open(file, java.nio.file.StandardOpenOption.WRITE).use { channel -> channel.truncate(10) }
            assertNull(it.get(key(), 1))
        }
    }

    /** Use case: a project with a password. The thumbnails are read back; the password array stays untouched. */
    @Test
    fun encryptedRoundTrip() {
        val password = "secret".toCharArray()
        ZipThumbnailStore(file, password, ZipThumbnailStore.DEFAULT_MAX_BYTES, 1000).use {
            it.put(key(), 4, pixels(1))
            assertEquals(pixels(1), it.get(key(), 4))
            assertNull(it.get(key(), 5))
        }
        assertEquals("secret", String(password))
    }

    /** Use case: an encrypted file shows neither the reference in the entry names nor the pixel bytes. */
    @Test
    fun encryptedFileRevealsNothing() {
        val marker = ThumbnailPixels(8, 8, ByteArray(8 * 8 * 4) { 0x5A })
        encrypted(keep = true).use { it.put(key("my-secret-reference"), 1, marker) }

        val content = Files.readAllBytes(file)
        assertFalse(String(content, Charsets.ISO_8859_1).contains("my-secret-reference"))
        assertFalse(String(content, Charsets.ISO_8859_1).contains("Z".repeat(32)))
        ZipFile(file.toFile()).use { zip ->
            val entry = zip.entries().toList().single()
            assertTrue(entry.name.endsWith(".bin"))
            assertFalse(entry.name.contains("my-secret-reference"))
            assertEquals(ZipEntry.STORED, entry.method)
        }
    }

    /** Use case: a changed byte in an encrypted entry is detected. The thumbnail is a miss. */
    @Test
    fun manipulatedEncryptedEntryIsAMiss() {
        encrypted().use {
            it.put(key(), 1, pixels(1))
            it.put(key("r2"), 1, pixels(2))
            flipLastByteOfTheFile()
            assertNull(it.get(key("r2"), 1))
            assertEquals(pixels(1), it.get(key(), 1))
        }
    }

    /** Use case: two stores of the same password do not share anything; every file has its own key. */
    @Test
    fun everyStoreHasItsOwnKey() {
        val other = directory.resolve("other.zip")
        encrypted().use { first ->
            ZipThumbnailStore(other, "secret".toCharArray(), ZipThumbnailStore.DEFAULT_MAX_BYTES, 1000).use { second ->
                first.put(key(), 1, pixels(1))
                assertNull(second.get(key(), 1))
            }
        }
    }

    /** Use case: load threads and the FX thread use the store at once. No access fails and the data stays right. */
    @Test
    fun concurrentAccessStaysConsistent() {
        val pool = Executors.newFixedThreadPool(8)
        try {
            encrypted().use { store ->
                val tasks = (0 until 8).map { worker ->
                    pool.submit(Callable {
                        repeat(100) { i ->
                            val k = key("w$worker", i % 5)
                            store.put(k, 1, pixels(worker + i))
                            val found = store.get(k, 1)
                            assertTrue(found == null || found.width == 4)
                            if (i % 37 == 0) store.invalidate("w${(worker + 1) % 8}")
                        }
                    })
                }
                tasks.forEach { it.get(60, TimeUnit.SECONDS) }
            }
        } finally {
            pool.shutdownNow()
        }
    }

    private fun flipLastByteOfTheFile() {
        val bytes = Files.readAllBytes(file)
        bytes[bytes.size - 1] = (bytes[bytes.size - 1].toInt() xor 0x01).toByte()
        Files.write(file, bytes)
    }
}
