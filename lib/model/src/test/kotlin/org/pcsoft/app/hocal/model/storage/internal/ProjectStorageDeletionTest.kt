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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.storage.internal.StorageFixtures.FIRST
import org.pcsoft.app.hocal.model.storage.internal.StorageFixtures.SECOND
import java.io.ByteArrayInputStream

/** Developer tests: after a medium is deleted, numbers and media still belong together. */
class ProjectStorageDeletionTest {

    /** Removes medium [index] of a reference: its details and its content, all later media move down by one. */
    private fun delete(project: ProjectFile, media: MemoryMedia, uuid: String, index: Int): MemoryMedia {
        project.references.first { it.uuid == uuid }.media.removeAt(index)
        val result = MemoryMedia()
        for (reference in project.references) {
            for (number in reference.media.indices) {
                val old = if (reference.uuid == uuid && number >= index) number + 1 else number
                result.put(reference.uuid, number, media.extension(reference.uuid, old), media.contentOf(reference.uuid, old))
            }
        }
        return result
    }

    private fun roundTrip(project: ProjectFile, media: MemoryMedia): Pair<ProjectFile, MemoryMedia> {
        val sink = MemoryMedia()
        val read = ProjectStorage.read(ByteArrayInputStream(StorageFixtures.save(project, media)), sink)
        return read to sink
    }

    /** Deleting the first, a middle or the last medium leaves the numbers continuous from 000. */
    @Test
    fun numbersStayContinuousAfterDeletion() {
        for ((index, remaining) in listOf(0 to listOf("b", "c"), 1 to listOf("a", "c"), 2 to listOf("a", "b"))) {
            val project = StorageFixtures.project()
            val media = delete(project, StorageFixtures.media(project), FIRST, index)

            val (read, sink) = roundTrip(project, media)

            assertEquals(remaining, read.references[0].media.map { StorageFixtures.tag(it) })
            assertEquals(
                (0 until remaining.size).map { MemoryMedia.key(FIRST, it) },
                sink.contents.keys.filter { it.startsWith(FIRST) },
            )
        }
    }

    /** After a deletion the content of every number still belongs to the details of the same number. */
    @Test
    fun contentAndDetailsStayTogetherAfterDeletion() {
        for (index in 0..2) {
            val project = StorageFixtures.project()
            val media = delete(project, StorageFixtures.media(project), FIRST, index)

            val (read, sink) = roundTrip(project, media)

            StorageFixtures.assertMatching(read, sink)
        }
    }

    /** Several deletions in a row keep the pairing; the other reference is not touched. */
    @Test
    fun repeatedDeletionsKeepThePairing() {
        val project = StorageFixtures.project()
        var media = StorageFixtures.media(project)
        media = delete(project, media, FIRST, 0)
        media = delete(project, media, FIRST, 1)

        val (read, sink) = roundTrip(project, media)

        assertEquals(listOf("b"), read.references[0].media.map { StorageFixtures.tag(it) })
        assertEquals(listOf("d"), read.references[1].media.map { StorageFixtures.tag(it) })
        StorageFixtures.assertMatching(read, sink)
    }

    /** A project whose media list shrank but whose content source still has all media writes only the listed media. */
    @Test
    fun staleContentIsNotWritten() {
        val project = StorageFixtures.project()
        val media = StorageFixtures.media(project)
        project.references[0].media.removeAt(2)

        val (read, sink) = roundTrip(project, media)

        assertEquals(2, read.references[0].media.size)
        assertEquals(3, sink.contents.size)
        StorageFixtures.assertMatching(read, sink)
    }

    /** Deleting the only medium of a reference makes the project invalid; it is not written. */
    @Test
    fun deletingTheLastMediumIsRejected() {
        val project = StorageFixtures.project()
        val media = delete(project, StorageFixtures.media(project), SECOND, 0)

        assertThrows(InvalidProjectFileException::class.java) { StorageFixtures.save(project, media) }
    }
}
