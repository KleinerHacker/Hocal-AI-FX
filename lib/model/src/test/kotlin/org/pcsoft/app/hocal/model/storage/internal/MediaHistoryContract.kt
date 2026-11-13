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

import org.pcsoft.app.hocal.model.storage.InconsistentHistoryException
import org.pcsoft.app.hocal.model.storage.MediaHistoryFullException
import org.pcsoft.app.hocal.model.storage.MediumNotFoundException
import org.pcsoft.app.hocal.model.storage.OriginalMediumException

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.model.project.Medium
import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.project.Reference
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.pcsoft.app.hocal.model.storage.internal.StorageFixtures.FIRST
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/** The history cases that every [MediaStore] has to pass; subclasses give the store under test. */
abstract class MediaHistoryContract {

    abstract fun newStore(): MediaStore

    private val extensions = listOf("png", "jpg", "webp")

    /** A reference with one medium per tag; content, details and extension are derived from the tag. */
    private fun reference(store: MediaStore, vararg tags: String): Reference {
        tags.forEachIndexed { number, tag ->
            store.accept(FIRST, number, extensions[number % extensions.size], ByteArrayInputStream(StorageFixtures.content(tag)))
        }
        return Reference(FIRST, tags.map { StorageFixtures.medium(it) }.toMutableList())
    }

    private fun tags(reference: Reference) = reference.media.map { StorageFixtures.tag(it) }

    /** Every number holds the content of the details of the same number, and no other medium exists. */
    private fun assertPaired(store: MediaStore, reference: Reference) {
        assertEquals(reference.media.indices.toList(), store.numbers(reference.uuid))
        reference.media.forEachIndexed { number, medium ->
            val content = store.open(reference.uuid, number).use { it.readAllBytes() }
            assertEquals(String(StorageFixtures.content(StorageFixtures.tag(medium))), String(content))
        }
        reference.verifyMedia(store)
    }

    /** A new medium becomes the current one with the next number and keeps its own extension. */
    @Test
    fun appendAddsTheCurrentMedium() {
        val store = newStore()
        val reference = reference(store, "a", "b")
        val medium = StorageFixtures.medium("c")

        val number = reference.appendMedium(store, medium, "webp", ByteArrayInputStream(StorageFixtures.content("c")))

        assertEquals(2, number)
        assertSame(medium, reference.currentMedium)
        assertEquals(2, reference.currentNumber)
        assertEquals("webp", store.extension(FIRST, 2))
        assertEquals("png", store.extension(FIRST, 0))
        assertEquals("jpg", store.extension(FIRST, 1))
        assertPaired(store, reference)
    }

    /** The 1001st medium is rejected and leaves list and store untouched. */
    @Test
    fun appendBeyondTheLimitIsRejected() {
        val store = newStore()
        val reference = Reference(FIRST)
        repeat(1000) {
            store.accept(FIRST, it, "png", ByteArrayInputStream(byteArrayOf(1)))
            reference.media.add(Medium())
        }

        assertThrows(MediaHistoryFullException::class.java) {
            reference.appendMedium(store, Medium(), "png", ByteArrayInputStream(byteArrayOf(2)))
        }

        assertEquals(1000, reference.media.size)
        assertEquals(1000, store.numbers(FIRST).size)
    }

    /** A store that fails leaves the history list untouched. */
    @Test
    fun failedAppendKeepsTheList() {
        val store = newStore()
        val reference = reference(store, "a")
        val failing = object : MediaStore by store {
            override fun accept(reference: String, number: Int, extension: String, content: InputStream) {
                throw IOException("disk gone")
            }
        }

        assertThrows(IOException::class.java) {
            reference.appendMedium(failing, Medium(), "png", ByteArrayInputStream(byteArrayOf(1)))
        }

        assertEquals(listOf("a"), tags(reference))
        assertPaired(store, reference)
    }

    /** Removing any medium but the original closes the gap and keeps content and details together. */
    @Test
    fun removeRenumbersWithoutGap() {
        for ((index, remaining) in listOf(1 to listOf("a", "c", "d"), 2 to listOf("a", "b", "d"), 3 to listOf("a", "b", "c"))) {
            val store = newStore()
            val reference = reference(store, "a", "b", "c", "d")

            reference.removeMedium(store, index)

            assertEquals(remaining, tags(reference))
            assertPaired(store, reference)
        }
    }

    /** The extension moves with its medium when the numbers shift. */
    @Test
    fun extensionsFollowTheirMedia() {
        val store = newStore()
        val reference = reference(store, "a", "b", "c")

        reference.removeMedium(store, 1)

        assertEquals("png", store.extension(FIRST, 0))
        assertEquals("webp", store.extension(FIRST, 1))
    }

    /** Removing the current medium makes its predecessor current. */
    @Test
    fun removingTheCurrentMediumMakesThePredecessorCurrent() {
        val store = newStore()
        val reference = reference(store, "a", "b", "c")

        reference.removeMedium(store, 2)

        assertEquals("b", StorageFixtures.tag(reference.currentMedium))
        assertEquals(1, reference.currentNumber)
    }

    /** The original `000` cannot be removed, so the last medium never is either. */
    @Test
    fun originalCannotBeRemoved() {
        val store = newStore()
        val reference = reference(store, "a", "b")

        assertThrows(OriginalMediumException::class.java) { reference.removeMedium(store, 0) }
        reference.removeMedium(store, 1)
        assertThrows(OriginalMediumException::class.java) { reference.removeMedium(store, 0) }

        assertEquals(listOf("a"), tags(reference))
        assertPaired(store, reference)
    }

    /** A number that does not exist is rejected without a change. */
    @Test
    fun unknownNumberIsRejected() {
        val store = newStore()
        val reference = reference(store, "a", "b")

        assertThrows(MediumNotFoundException::class.java) { reference.removeMedium(store, 2) }
        assertThrows(MediumNotFoundException::class.java) { reference.removeMedium(store, -1) }

        assertPaired(store, reference)
    }

    /** `verify` accepts a consistent history. */
    @Test
    fun verifyAcceptsConsistentHistory() {
        val store = newStore()

        reference(store, "a", "b").verifyMedia(store)
    }

    /** `verify` reports a gap, an extra medium and a reference without medium. */
    @Test
    fun verifyReportsInconsistencies() {
        val gap = newStore()
        val gapReference = reference(gap, "a", "b", "c")
        gap.delete(FIRST, 1)
        val extra = newStore()
        val extraReference = reference(extra, "a")
        extra.accept(FIRST, 1, "png", ByteArrayInputStream(byteArrayOf(1)))
        val none = newStore()

        assertThrows(InconsistentHistoryException::class.java) { gapReference.verifyMedia(gap) }
        assertThrows(InconsistentHistoryException::class.java) { extraReference.verifyMedia(extra) }
        assertThrows(InconsistentHistoryException::class.java) { Reference(FIRST).verifyMedia(none) }
        assertThrows(InconsistentHistoryException::class.java) { Reference(FIRST).currentMedium }
    }

    /** After appending and removing, the project is saved and read back with numbers and media still matching. */
    @Test
    fun savedHistoryKeepsNumbersAndMedia() {
        val store = newStore()
        val reference = reference(store, "a", "b", "c")
        reference.removeMedium(store, 1)
        reference.appendMedium(store, StorageFixtures.medium("d"), "png", ByteArrayInputStream(StorageFixtures.content("d")))
        reference.removeMedium(store, 1)
        val project = ProjectFile(
            references = mutableListOf(reference),
            tree = VirtualFolder().apply { children.add(VirtualEntry().apply { name = "x"; this.reference = FIRST }) },
        )

        val out = ByteArrayOutputStream()
        ProjectStorage.write(out, project, store)
        val sink = MemoryMedia()
        val read = ProjectStorage.read(ByteArrayInputStream(out.toByteArray()), sink)

        assertEquals(listOf("a", "d"), tags(read.references[0]))
        StorageFixtures.assertMatching(read, sink)
    }

    /** Removing a reference removes all its media and leaves other references alone; an unknown one is no error. */
    @Test
    fun deleteReferenceRemovesAllMedia() {
        val store = newStore()
        reference(store, "a", "b", "c")
        store.accept(StorageFixtures.SECOND, 0, "png", ByteArrayInputStream(byteArrayOf(1)))

        store.deleteReference(FIRST)
        store.deleteReference("unknown")

        assertEquals(emptyList<Int>(), store.numbers(FIRST))
        assertEquals(listOf(0), store.numbers(StorageFixtures.SECOND))
    }
}
