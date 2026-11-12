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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.storage.StorageFixtures.FIRST
import org.pcsoft.app.hocal.model.storage.StorageFixtures.SECOND
import org.pcsoft.app.hocal.model.storage.StorageFixtures.entries
import org.pcsoft.app.hocal.model.storage.StorageFixtures.json
import org.pcsoft.app.hocal.model.storage.StorageFixtures.zip
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/** Developer tests for reading and writing the project ZIP through streams. */
class ProjectStorageTest {

    private fun read(bytes: ByteArray, limits: StorageLimits = StorageLimits()): ProjectFile =
        ProjectStorage.read(ByteArrayInputStream(bytes), MemoryMedia(), limits)

    private val png = ByteArray(10) { it.toByte() }

    /** A written project is read back with equal project data, references, tree and media content. */
    @Test
    fun roundTrip() {
        val project = StorageFixtures.project()
        val bytes = StorageFixtures.save(project, StorageFixtures.media(project))

        val sink = MemoryMedia()
        val read = ProjectStorage.read(ByteArrayInputStream(bytes), sink)

        assertEquals(project, read)
        StorageFixtures.assertMatching(read, sink)
        assertEquals("png", sink.extensions[MemoryMedia.key(FIRST, 2)])
    }

    /** project.json is the first entry, followed by the media, and every entry is stored uncompressed. */
    @Test
    fun projectJsonFirstAndAllEntriesStored() {
        val project = StorageFixtures.project()
        val bytes = StorageFixtures.save(project, StorageFixtures.media(project))

        val names = ArrayList<String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { stream ->
            while (true) {
                val entry = stream.nextEntry ?: break
                assertEquals(ZipEntry.STORED, entry.method)
                names.add(entry.name)
            }
        }

        assertEquals(
            listOf("project.json", "$FIRST/000.png", "$FIRST/001.png", "$FIRST/002.png", "$SECOND/000.png"),
            names,
        )
    }

    /** Time stamps are written as ISO-8601 text in UTC. */
    @Test
    fun timesAreWrittenAsIsoUtc() {
        val project = StorageFixtures.project()
        val json = String(entries(StorageFixtures.save(project, StorageFixtures.media(project)))["project.json"]!!)

        assertTrue(json.contains("2026-01-02T03:04:05Z"))
    }

    /** Neither reading nor writing closes the stream it is given. */
    @Test
    fun streamsStayOpen() {
        val project = StorageFixtures.project()
        var outputClosed = false
        val output = object : ByteArrayOutputStream() {
            override fun close() {
                outputClosed = true
            }
        }
        ProjectStorage.write(output, project, StorageFixtures.media(project))
        var inputClosed = false
        val input = object : ByteArrayInputStream(output.toByteArray()) {
            override fun close() {
                inputClosed = true
            }
        }
        ProjectStorage.read(input, MemoryMedia())

        assertEquals(false, outputClosed)
        assertEquals(false, inputClosed)
    }

    /** A ZIP cut off inside a medium is reported as damaged. */
    @Test
    fun truncatedZipIsDamaged() {
        val project = StorageFixtures.project()
        val media = StorageFixtures.media(project)
        media.put(FIRST, 0, "png", ByteArray(100_000) { (it % 251).toByte() })
        val bytes = StorageFixtures.save(project, media)

        assertThrows(DamagedProjectFileException::class.java) { read(bytes.copyOf(bytes.size / 2)) }
    }

    /** An input without any ZIP entry has no project.json. */
    @Test
    fun emptyInputIsInvalid() {
        assertThrows(InvalidProjectFileException::class.java) { read(ByteArray(0)) }
    }

    /** A ZIP starting with something else than project.json is invalid. */
    @Test
    fun projectJsonMustBeFirst() {
        val bytes = zip(linkedMapOf("ref-1/000.png" to png, "project.json" to json()))

        assertThrows(InvalidProjectFileException::class.java) { read(bytes) }
    }

    /** A ZIP without project.json is invalid. */
    @Test
    fun missingProjectJson() {
        assertThrows(InvalidProjectFileException::class.java) { read(zip(linkedMapOf("ref-1/000.png" to png))) }
    }

    /** project.json that is no JSON is invalid. */
    @Test
    fun brokenJsonIsInvalid() {
        val bytes = zip(linkedMapOf("project.json" to "{ not json".toByteArray()))

        assertThrows(InvalidProjectFileException::class.java) { read(bytes) }
    }

    /** Entry names that leave the project folder or are not part of the layout are rejected. */
    @Test
    fun unsafeAndUnknownEntryNamesAreInvalid() {
        for (name in listOf("../evil.png", "ref-1/../../evil.png", "/abs/000.png", "ref-1\\000.png", "other.txt", "ref-1/000")) {
            val bytes = zip(linkedMapOf("project.json" to json(), "ref-1/000.png" to png, name to png))

            assertThrows(InvalidProjectFileException::class.java, { read(bytes) }, name)
        }
    }

    /** A file with a newer format version is rejected, even when it holds fields unknown today. */
    @Test
    fun newerFormatVersionIsRejected() {
        val bytes = zip(linkedMapOf("project.json" to json(version = 2, extra = ""","future":true""")))

        val error = assertThrows(UnsupportedFormatVersionException::class.java) { read(bytes) }

        assertEquals(2, error.foundVersion)
        assertEquals(1, error.supportedVersion)
    }

    /** An entry linking a reference that does not exist is invalid when reading and when writing. */
    @Test
    fun danglingLinkIsInvalid() {
        val tree = """{"type":"folder","name":"","children":[{"type":"entry","name":"x","reference":"missing"}]}"""
        val bytes = zip(linkedMapOf("project.json" to json(tree = tree), "ref-1/000.png" to png))
        val project = StorageFixtures.project()
        project.references.removeAt(1)

        assertThrows(InvalidProjectFileException::class.java) { read(bytes) }
        assertThrows(InvalidProjectFileException::class.java) {
            StorageFixtures.save(project, StorageFixtures.media(project))
        }
    }

    /** A reference without any medium is invalid when reading and when writing. */
    @Test
    fun referenceWithoutMediumIsInvalid() {
        val bytes = zip(linkedMapOf("project.json" to json(references = """[{"uuid":"ref-1","media":[]}]""")))
        val project = StorageFixtures.project()
        project.references[1].media.clear()

        assertThrows(InvalidProjectFileException::class.java) { read(bytes) }
        assertThrows(InvalidProjectFileException::class.java) {
            StorageFixtures.save(project, StorageFixtures.media(project))
        }
    }

    /** More than 1000 media of one reference cannot be numbered and are invalid. */
    @Test
    fun tooManyMediaAreInvalid() {
        val project = StorageFixtures.project()
        repeat(1000) { project.references[0].media.add(StorageFixtures.medium("x")) }

        assertThrows(InvalidProjectFileException::class.java) {
            StorageFixtures.save(project, MemoryMedia())
        }
    }

    /** Fewer media than details, more media than details and a doubled medium are all invalid. */
    @Test
    fun mediaCountMustMatchDetails() {
        val two = json(references = """[{"uuid":"ref-1","media":[{},{}]}]""")

        assertThrows(InvalidProjectFileException::class.java) {
            read(zip(linkedMapOf("project.json" to two, "ref-1/000.png" to png)))
        }
        assertThrows(InvalidProjectFileException::class.java) {
            read(zip(linkedMapOf("project.json" to json(), "ref-1/000.png" to png, "ref-1/001.png" to png)))
        }
        assertThrows(InvalidProjectFileException::class.java) {
            read(zip(linkedMapOf("project.json" to two, "ref-1/000.png" to png, "ref-1/000.jpg" to png)))
        }
    }

    /** A medium of a reference that project.json does not know is invalid. */
    @Test
    fun mediumOfUnknownReferenceIsInvalid() {
        val bytes = zip(linkedMapOf("project.json" to json(), "ref-1/000.png" to png, "ref-2/000.png" to png))

        assertThrows(InvalidProjectFileException::class.java) { read(bytes) }
    }

    /** Two references with the same UUID are invalid. */
    @Test
    fun duplicateReferenceIsInvalid() {
        val references = """[{"uuid":"ref-1","media":[{}]},{"uuid":"ref-1","media":[{}]}]"""
        val bytes = zip(linkedMapOf("project.json" to json(references = references), "ref-1/000.png" to png))

        assertThrows(InvalidProjectFileException::class.java) { read(bytes) }
    }

    /** Directory entries in the ZIP are ignored. */
    @Test
    fun directoryEntriesAreIgnored() {
        val bytes = zip(linkedMapOf("project.json" to json(), "ref-1/" to ByteArray(0), "ref-1/000.png" to png))

        assertEquals("ref-1", read(bytes).references[0].uuid)
    }

    /** A medium above the size limit is reported as damaged. */
    @Test
    fun oversizedMediumIsDamaged() {
        val bytes = zip(linkedMapOf("project.json" to json(), "ref-1/000.png" to png))

        assertThrows(DamagedProjectFileException::class.java) { read(bytes, StorageLimits(maxMediumBytes = 5)) }
    }

    /** A project.json above the size limit is reported as damaged. */
    @Test
    fun oversizedProjectJsonIsDamaged() {
        val bytes = zip(linkedMapOf("project.json" to json(), "ref-1/000.png" to png))

        assertThrows(DamagedProjectFileException::class.java) { read(bytes, StorageLimits(maxProjectFileBytes = 10)) }
    }

    /** More entries than the limit allows are reported as damaged. */
    @Test
    fun tooManyEntriesAreDamaged() {
        val bytes = zip(linkedMapOf("project.json" to json(), "ref-1/000.png" to png, "ref-1/000.jpg" to png))

        assertThrows(DamagedProjectFileException::class.java) { read(bytes, StorageLimits(maxEntries = 2)) }
    }

    /** The limits must be positive. */
    @Test
    fun limitsMustBePositive() {
        assertThrows(IllegalArgumentException::class.java) { StorageLimits(maxProjectFileBytes = 0) }
        assertThrows(IllegalArgumentException::class.java) { StorageLimits(maxMediumBytes = 0) }
        assertThrows(IllegalArgumentException::class.java) { StorageLimits(maxEntries = 0) }
    }

    /** A medium whose extension is not plain letters and digits cannot be written. */
    @Test
    fun invalidExtensionIsRejectedOnWrite() {
        val project = StorageFixtures.project()
        val media = StorageFixtures.media(project)
        media.extensions[MemoryMedia.key(FIRST, 0)] = "../x"

        assertThrows(InvalidProjectFileException::class.java) { StorageFixtures.save(project, media) }
    }

    /** The content written for a medium is the content delivered by the source, byte by byte. */
    @Test
    fun mediaContentSurvivesUnchanged() {
        val project = StorageFixtures.project()
        val media = StorageFixtures.media(project)
        media.put(FIRST, 1, "png", ByteArray(200_000) { (it * 7).toByte() })

        val sink = MemoryMedia()
        ProjectStorage.read(ByteArrayInputStream(StorageFixtures.save(project, media)), sink)

        assertArrayEquals(media.contentOf(FIRST, 1), sink.contentOf(FIRST, 1))
    }
}
