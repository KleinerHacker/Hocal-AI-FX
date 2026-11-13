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

import org.pcsoft.app.hocal.model.storage.InvalidProjectFileException

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.hocal.model.storage.internal.StorageFixtures.FIRST
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path

/** Developer tests for the file variants of [ProjectStorage]. */
class ProjectStorageFileTest {

    @TempDir
    lateinit var directory: Path

    /** A project saved to a file is read back from that file. */
    @Test
    fun fileRoundTrip() {
        val project = StorageFixtures.project()
        val target = directory.resolve("sample.hocal")

        ProjectStorage.write(target, project, StorageFixtures.media(project))
        val sink = MemoryMedia()

        assertEquals(project, ProjectStorage.read(target, sink))
        StorageFixtures.assertMatching(project, sink)
    }

    /** Saving over an existing file replaces it and leaves no temporary file behind. */
    @Test
    fun saveReplacesExistingFile() {
        val target = directory.resolve("sample.hocal")
        Files.writeString(target, "old")
        val project = StorageFixtures.project()

        ProjectStorage.write(target, project, StorageFixtures.media(project))

        assertEquals(project, ProjectStorage.read(target, MemoryMedia()))
        assertEquals(listOf("sample.hocal"), Files.list(directory).use { stream -> stream.map { it.fileName.toString() }.toList() })
    }

    /** A save that fails midway keeps the old file untouched and removes the temporary file. */
    @Test
    fun abortedSaveKeepsOldFile() {
        val target = directory.resolve("sample.hocal")
        Files.writeString(target, "old")
        val project = StorageFixtures.project()
        val media = StorageFixtures.media(project)
        val failing = object : MediaSource {
            override fun extension(reference: String, number: Int) = media.extension(reference, number)
            override fun open(reference: String, number: Int): InputStream {
                if (reference == FIRST && number == 1) throw IOException("disk gone")
                return media.open(reference, number)
            }
        }

        assertThrows(IOException::class.java) { ProjectStorage.write(target, project, failing) }

        assertEquals("old", Files.readString(target))
        assertEquals(listOf("sample.hocal"), Files.list(directory).use { stream -> stream.map { it.fileName.toString() }.toList() })
    }

    /** An invalid project is rejected before any file is touched or created. */
    @Test
    fun invalidProjectTouchesNoFile() {
        val target = directory.resolve("sample.hocal")
        Files.writeString(target, "old")
        val project = StorageFixtures.project()
        project.references[0].media.clear()

        assertThrows(InvalidProjectFileException::class.java) {
            ProjectStorage.write(target, project, StorageFixtures.media(project))
        }

        assertEquals("old", Files.readString(target))
        assertEquals(1L, Files.list(directory).use { it.count() })
    }

    /** Reading a file that does not exist reports the missing file. */
    @Test
    fun missingFileIsReported() {
        assertThrows(NoSuchFileException::class.java) {
            ProjectStorage.read(directory.resolve("none.hocal"), MemoryMedia())
        }
    }

    /** The bytes of a file written twice from the same project are readable both times. */
    @Test
    fun savingTwiceWorks() {
        val project = StorageFixtures.project()
        val target = directory.resolve("sample.hocal")
        val media = StorageFixtures.media(project)

        ProjectStorage.write(target, project, media)
        val first = Files.readAllBytes(target)
        ProjectStorage.write(target, project, media)

        assertEquals(project, ProjectStorage.read(ByteArrayInputStream(first), MemoryMedia()))
        assertArrayEquals(StorageFixtures.entries(first)["project.json"], StorageFixtures.entries(Files.readAllBytes(target))["project.json"])
    }
}
