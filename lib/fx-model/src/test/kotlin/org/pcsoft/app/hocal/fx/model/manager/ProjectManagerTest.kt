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
package org.pcsoft.app.hocal.fx.model.manager

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.hocal.fx.model.ChangeRecorder
import org.pcsoft.app.hocal.model.project.Medium
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.pcsoft.app.hocal.model.storage.ProjectFileException
import org.pcsoft.app.hocal.model.storage.WrongPasswordException
import org.pcsoft.app.hocal.model.tree.InvalidNameException
import org.pcsoft.app.hocal.model.tree.UnknownReferenceException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.time.Instant

/** Developer tests for [ProjectManager], the singleton that owns the loaded project. */
class ProjectManagerTest {

    @TempDir
    lateinit var directory: Path

    private val manager = ProjectManager

    private val work: Path get() = directory.resolve("work")

    private val target: Path get() = directory.resolve("sample.hocal")

    @BeforeEach
    fun prepare() {
        manager.resetForTests()
        Files.createDirectories(work)
        manager.workRoot = work
    }

    @AfterEach
    fun cleanUp() {
        manager.resetForTests()
    }

    private fun content(text: String) = text.byteInputStream()

    private fun medium(text: String) = Medium(prompt = text)

    private fun readMedium(uuid: String, number: Int): String =
        manager.openMedium(uuid, number).use { String(it.readAllBytes()) }

    private fun leftovers(): Int = Files.list(work).use { it.count() }.toInt()

    /** A saved project: a reference of two media, linked by `fox` in the folder `animals`. Not dirty afterwards. */
    private fun savedProject(): String {
        manager.createNew("Sample")
        val uuid = manager.addReference(medium("a"), "png", content("content-a"))
        manager.appendMedium(uuid, medium("b"), "jpg", content("content-b"))
        val root = manager.project.get()!!.tree
        val animals = manager.createFolder(root, "animals")
        manager.createEntry(animals, "fox", uuid)
        manager.saveAs(target)
        return uuid
    }

    /** Before anything is loaded the manager is empty and not changed. */
    @Test
    fun initialState() {
        assertFalse(manager.isLoaded)
        assertNull(manager.pathProperty.get())
        assertFalse(manager.dirtyProperty.get())
        assertFalse(manager.encryptedProperty.get())
        assertNull(manager.project.get())
    }

    /** A new project is loaded, has no file, a UUID and a creation time, and counts as changed. */
    @Test
    fun createNewLoadsAnUnsavedProject() {
        manager.createNew("Sample")

        assertTrue(manager.isLoaded)
        assertNull(manager.pathProperty.get())
        assertTrue(manager.dirtyProperty.get())
        assertEquals("Sample", manager.project.projectProperty.name)
        assertFalse(manager.project.projectProperty.uuid.isNullOrEmpty())
        assertNotNull(manager.project.projectProperty.created)
    }

    /** Only one project at a time: new and open both need the previous one closed. */
    @Test
    fun onlyOneProjectAtATime() {
        savedProject()

        assertThrows(IllegalStateException::class.java) { manager.createNew("Other") }
        assertThrows(IllegalStateException::class.java) { manager.open(target) }
    }

    /** Closing a project with unsaved changes needs the approval to discard; without it nothing changes. */
    @Test
    fun closeNeedsApprovalForUnsavedChanges() {
        manager.createNew("Sample")

        assertFalse(manager.close())
        assertTrue(manager.isLoaded)
        assertTrue(manager.close(discardChanges = true))

        assertFalse(manager.isLoaded)
        assertNull(manager.project.get())
        assertFalse(manager.dirtyProperty.get())
        assertEquals(0, leftovers())
    }

    /** A saved project closes without question, its temporary folder is removed, and closing again is harmless. */
    @Test
    fun closeSavedProject() {
        savedProject()

        assertTrue(manager.close())
        assertTrue(manager.close())

        assertFalse(manager.isLoaded)
        assertNull(manager.pathProperty.get())
        assertEquals(0, leftovers())
    }

    /** Save needs a file; save as gives it, clears the changed state, and save then writes to it again. */
    @Test
    fun saveAndSaveAs() {
        manager.createNew("Sample")

        assertThrows(IllegalStateException::class.java) { manager.save() }
        manager.saveAs(target)
        assertEquals(target, manager.pathProperty.get())
        assertFalse(manager.dirtyProperty.get())

        manager.project.projectProperty.name = "Renamed"
        assertTrue(manager.dirtyProperty.get())
        manager.save()
        assertFalse(manager.dirtyProperty.get())
        manager.close()
        manager.open(target)

        assertEquals("Renamed", manager.project.projectProperty.name)
    }

    /** A saved project is read back with equal project, tree and media, numbers and media still matching. */
    @Test
    fun saveCloseOpenRoundTrip() {
        val uuid = savedProject()
        val before = manager.project.get().toString()
        manager.close()

        manager.open(target)

        assertEquals(before, manager.project.get().toString())
        assertEquals(target, manager.pathProperty.get())
        assertFalse(manager.dirtyProperty.get())
        assertEquals("content-a", readMedium(uuid, 0))
        assertEquals("content-b", readMedium(uuid, 1))
        assertEquals("png", manager.mediumExtension(uuid, 0))
        assertEquals("jpg", manager.mediumExtension(uuid, 1))
    }

    /** Saving sets the time of the last change and the application version and leaves the creation time alone. */
    @Test
    fun saveSetsModifiedAndApplicationVersion() {
        manager.applicationVersion = "9.9.9"
        manager.createNew("Sample")
        val created = manager.project.projectProperty.created

        manager.saveAs(target)

        assertEquals(created, manager.project.projectProperty.created)
        assertEquals("9.9.9", manager.project.projectProperty.applicationVersion)
        assertTrue(!manager.project.projectProperty.modified!!.isBefore(created!!))
        assertTrue(manager.project.projectProperty.modified!!.isBefore(Instant.now().plusSeconds(60)))
    }

    /** The password stays with the project for `save`, `saveAs` without a password switches back to unencrypted. */
    @Test
    fun passwordIsKeptAndChangedOnlyBySaveAs() {
        savedProject()
        val secret = "correct horse".toCharArray()

        manager.saveAs(target, secret)
        assertTrue(manager.encryptedProperty.get())
        assertEquals("correct horse", String(secret))
        manager.project.projectProperty.description = "changed"
        manager.save()
        assertEquals("HOCALENC", String(Files.readAllBytes(target), 0, 8))
        manager.close()
        manager.open(target, "correct horse".toCharArray())
        assertEquals("changed", manager.project.projectProperty.description)
        assertTrue(manager.encryptedProperty.get())

        manager.saveAs(directory.resolve("plain.hocal"))

        assertFalse(manager.encryptedProperty.get())
        assertEquals("PK", String(Files.readAllBytes(directory.resolve("plain.hocal")), 0, 2))
    }

    /** A wrong password, a damaged file and a missing file leave the manager closed and no temporary folder behind. */
    @Test
    fun failedOpenLeavesTheManagerClosed() {
        savedProject()
        manager.saveAs(target, "secret".toCharArray())
        manager.close()
        val damaged = directory.resolve("damaged.hocal")
        Files.write(damaged, ByteArray(100) { 7 })

        assertThrows(WrongPasswordException::class.java) { manager.open(target, "wrong".toCharArray()) }
        assertThrows(ProjectFileException::class.java) { manager.open(damaged) }
        assertThrows(NoSuchFileException::class.java) { manager.open(directory.resolve("none.hocal")) }

        assertFalse(manager.isLoaded)
        assertNull(manager.project.get())
        assertEquals(0, leftovers())
    }

    /** Every operation that changes the project marks it as changed. */
    @Test
    fun everyChangeMarksTheProjectAsChanged() {
        val uuid = savedProject()
        val root = manager.project.get()!!.tree
        val animals = root.children[0] as VirtualFolder
        val fox = animals.children[0] as VirtualEntry
        val steps: List<() -> Unit> = listOf(
            { manager.createFolder(root, "bears") },
            { manager.createEntry(root, "fox-too", uuid) },
            { manager.rename(fox, "fuchs") },
            { manager.move(fox, root) },
            { manager.appendMedium(uuid, medium("c"), "png", content("content-c")) },
            { manager.removeMedium(uuid, 1) },
            { manager.addReference(medium("z"), "png", content("content-z")) },
            { manager.delete(root.children.last()) },
            { manager.removeReference(uuid) },
        )

        for (step in steps) {
            manager.save()
            assertFalse(manager.dirtyProperty.get())

            step()

            assertTrue(manager.dirtyProperty.get())
        }
    }

    /** A rejected change and a refused delete leave the project unchanged and not marked as changed. */
    @Test
    fun rejectedChangesLeaveTheProjectClean() {
        val uuid = savedProject()
        val root = manager.project.get()!!.tree
        val fox = (root.children[0] as VirtualFolder).children[0]

        assertThrows(InvalidNameException::class.java) { manager.createFolder(root, "a/b") }
        assertThrows(UnknownReferenceException::class.java) { manager.appendMedium("missing", medium("x"), "png", content("x")) }
        assertEquals(listOf(uuid), manager.orphansOf(fox))
        assertFalse(manager.delete(fox))

        assertFalse(manager.dirtyProperty.get())
        assertEquals(1, manager.project.get()!!.references.size)
    }

    /** Deleting the last entry of a reference with the orphan removal allowed removes reference and media. */
    @Test
    fun deleteWithOrphanRemoval() {
        val uuid = savedProject()
        val root = manager.project.get()!!.tree
        val animals = root.children[0] as VirtualFolder

        assertTrue(manager.delete(animals, removeOrphans = true))

        assertTrue(manager.project.get()!!.references.isEmpty())
        assertThrows(NoSuchFileException::class.java) { manager.openMedium(uuid, 0) }
        assertTrue(manager.dirtyProperty.get())
    }

    /** The property models show the change of an operation, and the whole project reports it on the root. */
    @Test
    fun propertyModelsFollowTheOperations() {
        savedProject()
        val recorder = ChangeRecorder()
        recorder.watch("file", manager.project)
        recorder.watch("tree", manager.project.treeProperty)
        val root = manager.project.get()!!.tree

        manager.createFolder(root, "bears")

        assertEquals(listOf("animals", "bears"), manager.project.tree!!.children.map { it.name })
        assertTrue(recorder.countOf("file") >= 1)
        assertTrue(recorder.countOf("tree") >= 1)
    }

    /** An edit through a field property of the project marks it as changed and reaches the plain object. */
    @Test
    fun fieldEditThroughThePropertyModelMarksAsChanged() {
        savedProject()
        assertFalse(manager.dirtyProperty.get())

        manager.project.projectProperty.nameProperty.set("Edited")

        assertTrue(manager.dirtyProperty.get())
        assertEquals("Edited", manager.project.get()!!.project.name)
    }

    /** The properties report their changes: loaded, path and changed state. */
    @Test
    fun propertiesReportTheirChanges() {
        val loaded = mutableListOf<Boolean>()
        val paths = mutableListOf<Path?>()
        val dirty = mutableListOf<Boolean>()
        manager.loadedProperty.addListener { _, _, value -> loaded += value }
        manager.pathProperty.addListener { _, _, value -> paths += value }
        manager.dirtyProperty.addListener { _, _, value -> dirty += value }

        manager.createNew("Sample")
        manager.saveAs(target)
        manager.close()

        assertEquals(listOf(true, false), loaded)
        assertEquals(listOf(target, null), paths)
        assertEquals(listOf(true, false), dirty)
    }

    /** Without a loaded project every change and every read of the media fails. */
    @Test
    fun noProjectNoOperations() {
        val folder = VirtualFolder()

        assertThrows(IllegalStateException::class.java) { manager.createFolder(folder, "x") }
        assertThrows(IllegalStateException::class.java) { manager.addReference(medium("x"), "png", content("x")) }
        assertThrows(IllegalStateException::class.java) { manager.openMedium("x", 0) }
        assertThrows(IllegalStateException::class.java) { manager.save() }
    }

    /** When the thread check answers `false` every operation fails and changes nothing. */
    @Test
    fun threadCheckBlocksEveryOperation() {
        savedProject()
        val root = manager.project.get()!!.tree
        manager.threadCheck = { false }

        assertThrows(IllegalStateException::class.java) { manager.createFolder(root, "x") }
        assertThrows(IllegalStateException::class.java) { manager.save() }
        assertThrows(IllegalStateException::class.java) { manager.close() }
        assertThrows(IllegalStateException::class.java) { manager.createNew("x") }

        manager.threadCheck = { true }
        assertEquals(1, root.children.size)
        assertFalse(manager.dirtyProperty.get())
    }

    /** A medium that was written to the file is the same one after reading, byte by byte. */
    @Test
    fun mediaContentSurvivesUnchanged() {
        manager.createNew("Sample")
        val bytes = ByteArray(100_000) { (it * 13).toByte() }
        val uuid = manager.addReference(medium("big"), "png", bytes.inputStream())
        manager.saveAs(target)
        manager.close()

        manager.open(target)

        assertArrayEquals(bytes, manager.openMedium(uuid, 0).use { it.readAllBytes() })
    }
}
