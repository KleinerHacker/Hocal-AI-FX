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
package org.pcsoft.app.hocal.model.storage.internal

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.hocal.model.storage.internal.StorageFixtures.FIRST
import org.pcsoft.app.hocal.model.storage.internal.StorageFixtures.SECOND
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

/** Developer tests for the encryption of the media in a [DirectoryMediaStore]. */
class DirectoryMediaStoreEncryptionTest {

    @TempDir
    lateinit var directory: Path

    private val pattern = "SECRET-PATTERN-".repeat(40)

    private fun input(text: String) = ByteArrayInputStream(text.toByteArray())

    private fun files(): List<Path> =
        Files.walk(directory).use { stream -> stream.filter { Files.isRegularFile(it) }.toList() }

    private fun names(): List<String> = files().map { it.fileName.toString() }.sorted()

    private fun anyFileContains(text: String): Boolean =
        files().any { String(Files.readAllBytes(it), Charsets.ISO_8859_1).contains(text) }

    private fun read(store: MediaStore, reference: String, number: Int): ByteArray =
        store.open(reference, number).use { it.readAllBytes() }

    /** An encrypted store keeps a medium as `<NNN>.<extension>.enc` without any readable part of the content. */
    @Test
    fun encryptedStoreKeepsNoPlaintext() {
        val store = DirectoryMediaStore(directory, encrypted = true)

        store.accept(FIRST, 0, "png", input(pattern))

        assertTrue(store.isEncrypted)
        assertEquals(listOf("000.png.enc"), names())
        assertFalse(anyFileContains("SECRET-PATTERN"))
    }

    /** A medium of several blocks is read back byte by byte, with its extension and its number. */
    @Test
    fun encryptedMediumRoundTrip() {
        val bytes = ByteArray(200_000) { (it * 31).toByte() }
        val store = DirectoryMediaStore(directory, encrypted = true)

        store.accept(FIRST, 3, "jpg", ByteArrayInputStream(bytes))

        assertArrayEquals(bytes, read(store, FIRST, 3))
        assertEquals("jpg", store.extension(FIRST, 3))
        assertEquals(listOf(3), store.numbers(FIRST))
        assertEquals(listOf("003.jpg.enc"), names())
    }

    /** An empty medium is possible as well and no temporary file stays behind. */
    @Test
    fun encryptedEmptyMedium() {
        val store = DirectoryMediaStore(directory, encrypted = true)

        store.accept(FIRST, 0, "png", input(""))

        assertEquals(0, read(store, FIRST, 0).size)
        assertEquals(listOf("000.png.enc"), names())
    }

    /** Another store has another random key and cannot read the media of the first one. */
    @Test
    fun wrongKeyCannotRead() {
        DirectoryMediaStore(directory, encrypted = true).accept(FIRST, 0, "png", input(pattern))
        val other = DirectoryMediaStore(directory, encrypted = true)

        assertThrows(IOException::class.java) { other.open(FIRST, 0) }
    }

    /** A manipulated file is reported as I/O problem while it is read, and never delivers changed data. */
    @Test
    fun manipulatedMediumFailsWhileReading() {
        val store = DirectoryMediaStore(directory, encrypted = true)
        store.accept(FIRST, 0, "png", input(pattern))
        val file = files().single()
        val bytes = Files.readAllBytes(file)
        bytes[bytes.size - 5] = (bytes[bytes.size - 5].toInt() xor 0x55).toByte()
        Files.write(file, bytes)

        assertThrows(IOException::class.java) { store.open(FIRST, 0).use { it.readAllBytes() } }
    }

    /** A cut off file is reported as I/O problem as well. */
    @Test
    fun cutOffMediumFailsWhileReading() {
        val store = DirectoryMediaStore(directory, encrypted = true)
        store.accept(FIRST, 0, "png", input(pattern))
        val file = files().single()
        Files.write(file, Files.readAllBytes(file).copyOf(100))

        assertThrows(IOException::class.java) { store.open(FIRST, 0).use { it.readAllBytes() } }
    }

    /** Without encryption nothing changes: the medium is plain in `<NNN>.<extension>`. */
    @Test
    fun plainStoreStaysPlain() {
        val store = DirectoryMediaStore(directory)

        store.accept(FIRST, 0, "png", input(pattern))

        assertFalse(store.isEncrypted)
        assertEquals(listOf("000.png"), names())
        assertTrue(anyFileContains("SECRET-PATTERN"))
        assertEquals(pattern, String(read(store, FIRST, 0)))
    }

    /** A plain medium that starts like encrypted data is no encrypted medium: the file name decides. */
    @Test
    fun plainMediumWithTheMagicIsNotTakenForEncrypted() {
        val store = DirectoryMediaStore(directory)
        val bytes = "HOCALENC".toByteArray() + ByteArray(200) { 9 }

        store.accept(FIRST, 0, "png", ByteArrayInputStream(bytes))

        assertArrayEquals(bytes, read(store, FIRST, 0))
    }

    /** Switching the encryption on encrypts the media that are there, in several references, and keeps them readable. */
    @Test
    fun enableEncryptionEncryptsExistingMedia() {
        val store = DirectoryMediaStore(directory)
        store.accept(FIRST, 0, "png", input(pattern))
        store.accept(FIRST, 1, "jpg", input("second"))
        store.accept(SECOND, 0, "gif", input("third"))

        store.enableEncryption()

        assertTrue(store.isEncrypted)
        assertEquals(listOf("000.gif.enc", "000.png.enc", "001.jpg.enc"), names())
        assertFalse(anyFileContains("SECRET-PATTERN"))
        assertEquals(pattern, String(read(store, FIRST, 0)))
        assertEquals("second", String(read(store, FIRST, 1)))
        assertEquals("third", String(read(store, SECOND, 0)))
        assertEquals(listOf(0, 1), store.numbers(FIRST))
        assertEquals("jpg", store.extension(FIRST, 1))
    }

    /** After switching on, new media are encrypted too; switching on again changes nothing. */
    @Test
    fun enableEncryptionIsRepeatable() {
        val store = DirectoryMediaStore(directory)
        store.accept(FIRST, 0, "png", input(pattern))
        store.enableEncryption()

        store.accept(FIRST, 1, "png", input(pattern))
        store.enableEncryption()

        assertEquals(listOf("000.png.enc", "001.png.enc"), names())
        assertEquals(pattern, String(read(store, FIRST, 0)))
        assertEquals(pattern, String(read(store, FIRST, 1)))
    }

    /** An interrupted encryption left plain and encrypted file: the encrypted one is read, the next call cleans up. */
    @Test
    fun interruptedEncryptionTakesUpAgain() {
        val store = DirectoryMediaStore(directory)
        store.accept(FIRST, 0, "png", input(pattern))
        store.enableEncryption()
        Files.write(directory.resolve(FIRST).resolve("000.png"), pattern.toByteArray())

        assertEquals(listOf(0), store.numbers(FIRST))
        assertEquals(pattern, String(read(store, FIRST, 0)))
        store.enableEncryption()

        assertEquals(listOf("000.png.enc"), names())
        assertFalse(anyFileContains("SECRET-PATTERN"))
    }

    /** Deleting, moving and replacing keep working on encrypted media and keep them encrypted. */
    @Test
    fun deleteMoveAndReplaceOfEncryptedMedia() {
        val store = DirectoryMediaStore(directory, encrypted = true)
        store.accept(FIRST, 0, "png", input("first"))
        store.accept(FIRST, 1, "jpg", input("second"))

        store.move(FIRST, 1, 2)
        assertEquals(listOf(0, 2), store.numbers(FIRST))
        assertEquals("second", String(read(store, FIRST, 2)))
        assertEquals("jpg", store.extension(FIRST, 2))
        assertThrows(FileAlreadyExistsException::class.java) { store.move(FIRST, 0, 2) }

        store.accept(FIRST, 0, "gif", input("replaced"))
        assertEquals("gif", store.extension(FIRST, 0))
        assertEquals("replaced", String(read(store, FIRST, 0)))

        store.delete(FIRST, 2)
        assertThrows(NoSuchFileException::class.java) { store.open(FIRST, 2) }
        store.deleteReference(FIRST)

        assertEquals(emptyList<String>(), names())
    }

    /** Closing the store overwrites the key: the encrypted media cannot be read any more. */
    @Test
    fun closeRemovesTheKey() {
        val store = DirectoryMediaStore(directory, encrypted = true)
        store.accept(FIRST, 0, "png", input(pattern))

        store.close()

        assertFalse(store.isEncrypted)
        assertThrows(IllegalStateException::class.java) { store.open(FIRST, 0) }
    }

    /** A store without media and without folder can be switched on without failing. */
    @Test
    fun enableEncryptionWithoutMedia() {
        val store = DirectoryMediaStore(directory.resolve("missing"))

        store.enableEncryption()

        assertTrue(store.isEncrypted)
    }
}
