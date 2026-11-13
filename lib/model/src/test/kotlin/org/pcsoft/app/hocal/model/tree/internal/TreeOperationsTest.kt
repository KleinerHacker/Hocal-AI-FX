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
package org.pcsoft.app.hocal.model.tree.internal

import org.pcsoft.app.hocal.model.tree.DuplicateNameException
import org.pcsoft.app.hocal.model.tree.InvalidNameException
import org.pcsoft.app.hocal.model.tree.NodeNotFoundException
import org.pcsoft.app.hocal.model.tree.RootNodeException
import org.pcsoft.app.hocal.model.tree.TreeCycleException
import org.pcsoft.app.hocal.model.tree.UnknownReferenceException

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.pcsoft.app.hocal.model.storage.internal.MediaStore
import org.pcsoft.app.hocal.model.storage.internal.MemoryMedia
import org.pcsoft.app.hocal.model.storage.internal.ProjectStorage
import org.pcsoft.app.hocal.model.storage.internal.ProjectValidator
import org.pcsoft.app.hocal.model.storage.internal.StorageFixtures
import org.pcsoft.app.hocal.model.storage.internal.StorageFixtures.FIRST
import org.pcsoft.app.hocal.model.storage.internal.StorageFixtures.SECOND
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Developer tests for the operations on the virtual tree. The sample project holds the references
 * FIRST (media a, b, c) and SECOND (medium d); the tree is `root / animals / fox -> FIRST` and
 * `root / wolf -> SECOND`.
 */
class TreeOperationsTest {

    private val project: ProjectFile = StorageFixtures.project()

    private val media: MemoryMedia = StorageFixtures.media(project)

    private val root: VirtualFolder = project.tree

    private val animals = root.children[0] as VirtualFolder

    private val fox = animals.children[0] as VirtualEntry

    private val wolf = root.children[1] as VirtualEntry

    private fun assertValid() = ProjectValidator.validate(project)

    /** Folders and entries are created below a folder, an empty folder is allowed. */
    @Test
    fun createFolderAndEntry() {
        val bears = project.createFolder(root, "bears")
        val bear = project.createEntry(bears, "bear", SECOND)
        project.createFolder(root, "empty")

        assertSame(bears, root.children[2])
        assertSame(bear, bears.children[0])
        assertEquals(SECOND, bear.reference)
        assertTrue((root.children[3] as VirtualFolder).children.isEmpty())
        assertValid()
    }

    /** One reference can be linked by several entries, also in one folder under different names. */
    @Test
    fun oneReferenceManyEntries() {
        project.createEntry(animals, "fox-copy", FIRST)
        project.createEntry(root, "fox-again", FIRST)

        assertEquals(listOf("fox", "fox-copy"), animals.children.map { it.name })
        assertValid()
    }

    /** An entry cannot link a reference the project does not have. */
    @Test
    fun entryNeedsAnExistingReference() {
        val error = assertThrows(UnknownReferenceException::class.java) { project.createEntry(root, "x", "missing") }

        assertEquals("missing", error.reference)
        assertEquals(2, root.children.size)
    }

    /** A name taken by a sibling is rejected, whatever the case, and folders and entries share the names. */
    @Test
    fun duplicateNamesAreRejected() {
        assertThrows(DuplicateNameException::class.java) { project.createFolder(root, "animals") }
        assertThrows(DuplicateNameException::class.java) { project.createFolder(root, "ANIMALS") }
        assertThrows(DuplicateNameException::class.java) { project.createFolder(root, "wolf") }
        assertThrows(DuplicateNameException::class.java) { project.createEntry(animals, "Fox", SECOND) }
        assertEquals(2, root.children.size)
    }

    /** An invalid name is rejected before anything is created. */
    @Test
    fun invalidNamesAreRejected() {
        assertThrows(InvalidNameException::class.java) { project.createFolder(root, "a/b") }
        assertThrows(InvalidNameException::class.java) { project.createEntry(root, "", FIRST) }
        assertEquals(2, root.children.size)
    }

    /** A parent that is not part of the tree is rejected. */
    @Test
    fun parentMustBeInTheTree() {
        assertThrows(NodeNotFoundException::class.java) { project.createFolder(VirtualFolder(), "x") }
    }

    /** The parent of a node is found by identity; the root has none; a stranger is not found. */
    @Test
    fun parentOf() {
        assertSame(animals, project.parentOf(fox))
        assertSame(root, project.parentOf(animals))
        assertEquals(null, project.parentOf(root))
        assertThrows(NodeNotFoundException::class.java) { project.parentOf(VirtualEntry().also { it.name = "fox" }) }
    }

    /** A node is renamed; a change of the case alone is allowed. */
    @Test
    fun renameKeepsTheNodeAndAllowsCaseChange() {
        project.rename(fox, "fuchs")
        project.rename(fox, "FUCHS")

        assertEquals("FUCHS", fox.name)
        assertSame(fox, animals.children[0])
        assertValid()
    }

    /** Renaming to a sibling name, to an invalid name, or renaming the root is rejected. */
    @Test
    fun renameRejectsBadTargets() {
        project.createEntry(animals, "wolf", SECOND)

        assertThrows(DuplicateNameException::class.java) { project.rename(fox, "WOLF") }
        assertThrows(InvalidNameException::class.java) { project.rename(fox, "a|b") }
        assertThrows(RootNodeException::class.java) { project.rename(root, "x") }
        assertThrows(NodeNotFoundException::class.java) { project.rename(VirtualFolder(), "x") }
        assertEquals("fox", fox.name)
    }

    /** A node moves into another folder and stays the same object. */
    @Test
    fun moveIntoAnotherFolder() {
        val bears = project.createFolder(root, "bears")

        project.move(fox, bears)
        project.move(animals, bears)

        assertEquals(listOf(wolf, bears).map { it.name }, root.children.map { it.name })
        assertEquals(listOf("fox"), bears.children.filterIsInstance<VirtualEntry>().map { it.name })
        assertSame(animals, bears.children.filterIsInstance<VirtualFolder>().single())
        assertValid()
    }

    /** Moving into the folder a node is in changes nothing. */
    @Test
    fun moveIntoTheSameFolderChangesNothing() {
        project.move(fox, animals)

        assertSame(fox, animals.children.single())
    }

    /** A name taken at the target stops the move and changes nothing. */
    @Test
    fun moveRejectsNameCollision() {
        project.createEntry(root, "fox", FIRST)

        assertThrows(DuplicateNameException::class.java) { project.move(fox, root) }
        assertSame(fox, animals.children.single())
    }

    /** A folder cannot move into itself or into a subfolder; the root and strangers cannot move. */
    @Test
    fun moveRejectsCyclesRootAndStrangers() {
        val deeper = project.createFolder(animals, "deeper")

        assertThrows(TreeCycleException::class.java) { project.move(animals, animals) }
        assertThrows(TreeCycleException::class.java) { project.move(animals, deeper) }
        assertThrows(RootNodeException::class.java) { project.move(root, animals) }
        assertThrows(NodeNotFoundException::class.java) { project.move(fox, VirtualFolder()) }
        assertThrows(NodeNotFoundException::class.java) { project.move(VirtualEntry(), animals) }
        assertSame(animals, root.children[0])
        assertValid()
    }

    /** `orphansOf` names the references that lose their last entry and changes nothing. */
    @Test
    fun orphansOfNamesTheLostReferences() {
        val before = root.toString()

        assertEquals(listOf(FIRST), project.orphansOf(fox))
        assertEquals(listOf(FIRST), project.orphansOf(animals))
        assertEquals(listOf(FIRST, SECOND), project.orphansOf(root))
        assertEquals(before, root.toString())
        project.createEntry(root, "fox-copy", FIRST)
        assertEquals(emptyList<String>(), project.orphansOf(fox))
        assertThrows(NodeNotFoundException::class.java) { project.orphansOf(VirtualEntry()) }
    }

    /** Deleting an entry whose reference has another entry deletes without question and keeps reference and media. */
    @Test
    fun deleteKeepsReferenceWithAnotherEntry() {
        project.createEntry(root, "fox-copy", FIRST)

        assertTrue(project.delete(fox, media))

        assertTrue(animals.children.isEmpty())
        assertEquals(listOf(FIRST, SECOND), project.references.map { it.uuid })
        assertEquals(listOf(0, 1, 2), media.numbers(FIRST))
        assertValid()
    }

    /** Deleting the last entry of a reference changes nothing and answers `false`. */
    @Test
    fun deleteOfTheLastEntryIsRefusedWithoutOrphanRemoval() {
        val before = root.toString()

        assertFalse(project.delete(fox, media))

        assertEquals(before, root.toString())
        assertEquals(listOf(FIRST, SECOND), project.references.map { it.uuid })
        assertEquals(listOf(0, 1, 2), media.numbers(FIRST))
    }

    /** With the orphan removal allowed, the entry, the reference and its media are removed. */
    @Test
    fun deleteWithOrphanRemoval() {
        assertTrue(project.delete(fox, media, removeOrphans = true))

        assertTrue(animals.children.isEmpty())
        assertEquals(listOf(SECOND), project.references.map { it.uuid })
        assertEquals(emptyList<Int>(), media.numbers(FIRST))
        assertEquals(listOf(0), media.numbers(SECOND))
        assertValid()
    }

    /** A folder is deleted with everything below it; two entries of one reference inside count once. */
    @Test
    fun deleteFolderWithSeveralEntriesOfOneReference() {
        project.createEntry(animals, "fox-copy", FIRST)

        assertEquals(listOf(FIRST), project.orphansOf(animals))
        assertFalse(project.delete(animals, media))
        assertTrue(project.delete(animals, media, removeOrphans = true))

        assertEquals(listOf("wolf"), root.children.map { it.name })
        assertEquals(listOf(SECOND), project.references.map { it.uuid })
        assertValid()
    }

    /** The root cannot be deleted and a stranger is not found. */
    @Test
    fun deleteRejectsRootAndStrangers() {
        assertThrows(RootNodeException::class.java) { project.delete(root, media) }
        assertThrows(NodeNotFoundException::class.java) { project.delete(VirtualEntry(), media) }
    }

    /** A failing store leaves a consistent project; only unused media stay behind. */
    @Test
    fun failingStoreLeavesAConsistentProject() {
        val failing = object : MediaStore by media {
            override fun deleteReference(reference: String) {
                throw IOException("disk gone")
            }
        }

        assertThrows(IOException::class.java) { project.delete(fox, failing, removeOrphans = true) }

        assertValid()
        assertEquals(listOf(SECOND), project.references.map { it.uuid })
    }

    /** Removing a reference removes all its entries in every folder, the reference and its media. */
    @Test
    fun removeReferenceRemovesEntriesAndMedia() {
        val bears = project.createFolder(root, "bears")
        project.createEntry(bears, "fox-too", FIRST)
        project.createEntry(root, "fox-root", FIRST)

        project.removeReference(media, FIRST)

        assertEquals(listOf("animals", "wolf", "bears"), root.children.map { it.name })
        assertTrue(animals.children.isEmpty())
        assertTrue(bears.children.isEmpty())
        assertEquals(listOf(SECOND), project.references.map { it.uuid })
        assertEquals(emptyList<Int>(), media.numbers(FIRST))
        assertEquals(listOf(0), media.numbers(SECOND))
        assertValid()
    }

    /** Removing a reference the project does not have is rejected. */
    @Test
    fun removeUnknownReferenceIsRejected() {
        assertThrows(UnknownReferenceException::class.java) { project.removeReference(media, "missing") }
    }

    /** After a series of operations the project is saved as ZIP and read back, numbers and media still match. */
    @Test
    fun savedProjectAfterOperationsKeepsNumbersAndMedia() {
        val bears = project.createFolder(root, "bears")
        project.createEntry(bears, "bear", SECOND)
        project.rename(wolf, "Wolf")
        project.move(fox, bears)
        project.delete(animals, media)
        project.removeReference(media, FIRST)

        val out = ByteArrayOutputStream()
        ProjectStorage.write(out, project, media)
        val sink = MemoryMedia()
        val read = ProjectStorage.read(ByteArrayInputStream(out.toByteArray()), sink)

        assertEquals(project, read)
        assertEquals(listOf(SECOND), read.references.map { it.uuid })
        StorageFixtures.assertMatching(read, sink)
    }
}
