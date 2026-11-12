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

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.hocal.model.storage.StorageFixtures.FIRST
import org.pcsoft.app.hocal.model.storage.StorageFixtures.SECOND
import java.io.ByteArrayInputStream
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

/** The history cases against the directory store, plus the cases of the directory itself. */
class MediaHistoryDirectoryTest : MediaHistoryContract() {

    @TempDir
    lateinit var directory: Path

    private var counter = 0

    override fun newStore(): MediaStore = DirectoryMediaStore(directory.resolve("store-${counter++}"))

    private fun input(vararg bytes: Byte) = ByteArrayInputStream(bytes)

    /** A medium is stored as `<uuid>/<NNN>.<extension>` and read back unchanged. */
    @Test
    fun mediumLivesInItsFile() {
        val store = DirectoryMediaStore(directory)

        store.accept(FIRST, 7, "png", input(1, 2, 3))

        assertArrayEquals(byteArrayOf(1, 2, 3), Files.readAllBytes(directory.resolve(FIRST).resolve("007.png")))
        assertArrayEquals(byteArrayOf(1, 2, 3), store.open(FIRST, 7).use { it.readAllBytes() })
        assertEquals("png", store.extension(FIRST, 7))
    }

    /** No temporary file stays behind after a medium was stored. */
    @Test
    fun noTemporaryFileStaysBehind() {
        val store = DirectoryMediaStore(directory)

        store.accept(FIRST, 0, "png", input(1))

        assertEquals(listOf("000.png"), Files.list(directory.resolve(FIRST)).use { s -> s.map { it.fileName.toString() }.toList() })
    }

    /** Storing a medium under a taken number replaces it, also with another extension. */
    @Test
    fun storingReplacesTheMediumOfTheSameNumber() {
        val store = DirectoryMediaStore(directory)
        store.accept(FIRST, 0, "png", input(1))

        store.accept(FIRST, 0, "jpg", input(2))

        assertEquals(listOf(0), store.numbers(FIRST))
        assertEquals("jpg", store.extension(FIRST, 0))
    }

    /** Moving keeps the extension and refuses a taken target number. */
    @Test
    fun moveKeepsTheExtensionAndRefusesTakenNumbers() {
        val store = DirectoryMediaStore(directory)
        store.accept(FIRST, 1, "webp", input(1))
        store.accept(FIRST, 2, "png", input(2))

        assertThrows(FileAlreadyExistsException::class.java) { store.move(FIRST, 1, 2) }
        store.move(FIRST, 1, 0)

        assertEquals("webp", store.extension(FIRST, 0))
        assertEquals(listOf(0, 2), store.numbers(FIRST))
    }

    /** A missing medium is reported as missing file. */
    @Test
    fun missingMediumIsReported() {
        val store = DirectoryMediaStore(directory)

        assertThrows(NoSuchFileException::class.java) { store.open(FIRST, 0) }
        assertThrows(NoSuchFileException::class.java) { store.extension(FIRST, 0) }
        assertThrows(NoSuchFileException::class.java) { store.delete(FIRST, 0) }
        assertThrows(NoSuchFileException::class.java) { store.move(FIRST, 0, 1) }
        assertEquals(emptyList<Int>(), store.numbers(SECOND))
    }

    /** Names that could leave the directory, numbers out of range and bad extensions are refused. */
    @Test
    fun pathTricksAreRefused() {
        val store = DirectoryMediaStore(directory)

        assertThrows(IllegalArgumentException::class.java) { store.accept("../x", 0, "png", input(1)) }
        assertThrows(IllegalArgumentException::class.java) { store.accept("a/b", 0, "png", input(1)) }
        assertThrows(IllegalArgumentException::class.java) { store.accept(FIRST, 1000, "png", input(1)) }
        assertThrows(IllegalArgumentException::class.java) { store.accept(FIRST, -1, "png", input(1)) }
        assertThrows(IllegalArgumentException::class.java) { store.accept(FIRST, 0, "../png", input(1)) }
        assertThrows(IllegalArgumentException::class.java) { store.open("..", 0) }
    }

    /** Foreign files in a reference folder are not counted as media. */
    @Test
    fun foreignFilesAreIgnored() {
        val store = DirectoryMediaStore(directory)
        store.accept(FIRST, 0, "png", input(1))
        Files.writeString(directory.resolve(FIRST).resolve("notes.txt"), "x")
        Files.writeString(directory.resolve(FIRST).resolve(".medium-1.tmp"), "x")

        assertEquals(listOf(0), store.numbers(FIRST))
    }
}
