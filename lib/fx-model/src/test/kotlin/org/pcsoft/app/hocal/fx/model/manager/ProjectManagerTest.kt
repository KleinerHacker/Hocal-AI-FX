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
import org.pcsoft.app.hocal.model.storage.PasswordRequiredException
import org.pcsoft.app.hocal.model.storage.ProjectFileException
import org.pcsoft.app.hocal.model.storage.WrongPasswordException
import org.pcsoft.app.hocal.model.tree.InvalidNameException
import org.pcsoft.app.hocal.model.tree.UnknownReferenceException
import org.pcsoft.app.hocal.thumbnail.ThumbnailKey
import org.pcsoft.app.hocal.thumbnail.ThumbnailPixels
import org.pcsoft.app.hocal.thumbnail.ThumbnailStorage
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

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
        manager.thumbnailStorage = ThumbnailStorage.DISK
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

    /** The answer follows the form the project was saved in, and `open` agrees with it. */
    @Test
    fun passwordRequiredFollowsTheSavedForm() {
        savedProject()
        val plain = directory.resolve("plain.hocal")
        manager.saveAs(plain)
        manager.saveAs(target, "secret".toCharArray())
        manager.close()

        assertTrue(manager.isPasswordRequired(target))
        assertFalse(manager.isPasswordRequired(plain))
        assertThrows(PasswordRequiredException::class.java) { manager.open(target) }
        manager.open(target, "secret".toCharArray())
        assertTrue(manager.isLoaded)
    }

    /** The question needs no loaded project and changes nothing, also while a project with changes is loaded. */
    @Test
    fun passwordRequiredChangesNothing() {
        savedProject()
        manager.saveAs(target, "secret".toCharArray())
        manager.project.projectProperty.name = "Edited"

        assertTrue(manager.isPasswordRequired(target))
        assertFalse(manager.isPasswordRequired(directory.resolve("none-yet").also { Files.write(it, ByteArray(3)) }))

        assertTrue(manager.isLoaded)
        assertTrue(manager.dirtyProperty.get())
        assertEquals(target, manager.pathProperty.get())
        assertEquals("Edited", manager.project.projectProperty.name)
        manager.close(discardChanges = true)
        assertTrue(manager.isPasswordRequired(target))
        assertFalse(manager.isLoaded)
    }

    /** A file that does not exist is an I/O problem, and the thread check applies like for every operation. */
    @Test
    fun passwordRequiredReportsMissingFileAndChecksTheThread() {
        assertThrows(NoSuchFileException::class.java) { manager.isPasswordRequired(directory.resolve("none.hocal")) }

        manager.threadCheck = { false }
        assertThrows(IllegalStateException::class.java) { manager.isPasswordRequired(target) }
    }

    private val key = ThumbnailKey("ref", 0, 48)

    private fun marker() = ThumbnailPixels(8, 8, ByteArray(8 * 8 * 4) { 0x5A })

    private fun thumbnailFiles(): List<Path> =
        Files.walk(work).use { stream -> stream.filter { it.fileName.toString().startsWith("thumbnails-") }.toList() }

    private fun thumbnailFile(): Path? = thumbnailFiles().singleOrNull()

    /** By default no thumbnail file is made: the store has nothing, and nothing is left in the temporary folder. */
    @Test
    fun thumbnailsStayInMemoryByDefault() {
        manager.resetForTests()
        manager.workRoot = work
        assertEquals(ThumbnailStorage.MEMORY, manager.thumbnailStorage)

        manager.createNew("Sample")
        manager.thumbnailCache.put(key, 1, marker())

        assertNull(manager.thumbnailCache.get(key, 1))
        assertTrue(thumbnailFiles().isEmpty())
    }

    /** Switching the file on while a project is loaded makes the file, switching it off removes it again. */
    @Test
    fun switchingTheFileWhileAProjectIsLoaded() {
        manager.thumbnailStorage = ThumbnailStorage.MEMORY
        manager.createNew("Sample")
        assertTrue(thumbnailFiles().isEmpty())

        manager.thumbnailStorage = ThumbnailStorage.DISK
        manager.thumbnailCache.put(key, 1, marker())
        assertEquals(marker(), manager.thumbnailCache.get(key, 1))
        assertEquals(1, thumbnailFiles().size)

        manager.thumbnailStorage = ThumbnailStorage.MEMORY
        assertTrue(thumbnailFiles().isEmpty())
        assertNull(manager.thumbnailCache.get(key, 1))
    }

    /** Setting the storage that is already set changes nothing: the same file stays and keeps its thumbnails. */
    @Test
    fun settingTheSameStorageKeepsTheFile() {
        manager.createNew("Sample")
        manager.thumbnailCache.put(key, 1, marker())
        val before = thumbnailFile()

        manager.thumbnailStorage = ThumbnailStorage.DISK

        assertEquals(before, thumbnailFile())
        assertEquals(marker(), manager.thumbnailCache.get(key, 1))
    }

    /** Switching on a loaded encrypted project makes a file that is encrypted with the password of the project. */
    @Test
    fun switchingOnForAnEncryptedProjectEncryptsTheFile() {
        manager.thumbnailStorage = ThumbnailStorage.MEMORY
        manager.createNew("Sample")
        manager.saveAs(target, "secret".toCharArray())

        manager.thumbnailStorage = ThumbnailStorage.DISK
        manager.thumbnailCache.put(key, 1, marker())

        assertEquals(marker(), manager.thumbnailCache.get(key, 1))
        assertFalse(clearText())
    }

    /** The switch belongs to the FX thread like every other member. */
    @Test
    fun switchChecksTheThread() {
        manager.threadCheck = { false }

        assertThrows(IllegalStateException::class.java) { manager.thumbnailStorage = ThumbnailStorage.MEMORY }
    }

    /** Without a loaded project the thumbnail store has nothing and stores nothing. */
    @Test
    fun thumbnailCacheIsEmptyWithoutAProject() {
        manager.thumbnailCache.put(key, 1, marker())

        assertNull(manager.thumbnailCache.get(key, 1))
    }

    /** A loaded project has a thumbnail file in its temporary folder and stores thumbnails in it. */
    @Test
    fun thumbnailsAreStoredWhileAProjectIsLoaded() {
        manager.createNew("Sample")

        manager.thumbnailCache.put(key, 1, marker())

        assertEquals(marker(), manager.thumbnailCache.get(key, 1))
        assertNotNull(thumbnailFile())
    }

    /** Closing removes the thumbnail file with the temporary folder; the store object stays the same and is empty. */
    @Test
    fun closeRemovesTheThumbnailFile() {
        val store = manager.thumbnailCache
        manager.createNew("Sample")
        manager.thumbnailCache.put(key, 1, marker())

        manager.close(discardChanges = true)

        assertEquals(0, leftovers())
        assertTrue(store === manager.thumbnailCache)
        assertNull(manager.thumbnailCache.get(key, 1))
    }

    /** The thumbnails of a closed project are not seen by the next project, also not when it is the same file. */
    @Test
    fun anotherSessionStartsWithAnEmptyStore() {
        savedProject()
        manager.thumbnailCache.put(key, 1, marker())
        manager.close()

        manager.open(target)

        assertNull(manager.thumbnailCache.get(key, 1))
    }

    /** Saving under the same password keeps the thumbnails; a new password or the other form starts a new, empty store. */
    @Test
    fun saveAsRenewsTheStoreOnlyWhenThePasswordChanges() {
        savedProject()
        manager.thumbnailCache.put(key, 1, marker())

        manager.saveAs(directory.resolve("copy.hocal"))
        assertEquals(marker(), manager.thumbnailCache.get(key, 1))

        manager.saveAs(directory.resolve("secret.hocal"), "secret".toCharArray())
        assertNull(manager.thumbnailCache.get(key, 1))

        manager.thumbnailCache.put(key, 1, marker())
        manager.saveAs(directory.resolve("secret-again.hocal"), "secret".toCharArray())
        assertEquals(marker(), manager.thumbnailCache.get(key, 1))

        manager.saveAs(directory.resolve("other.hocal"), "other".toCharArray())
        assertNull(manager.thumbnailCache.get(key, 1))

        manager.thumbnailCache.put(key, 1, marker())
        manager.saveAs(directory.resolve("plain.hocal"))
        assertNull(manager.thumbnailCache.get(key, 1))
    }

    /** The thumbnail file of an encrypted project does not hold the pixels in the clear; that of a plain project does. */
    @Test
    fun thumbnailFileIsEncryptedWithTheProjectPassword() {
        val pattern = "Z".repeat(32)
        manager.createNew("Sample")
        manager.thumbnailCache.put(key, 1, marker())
        assertTrue(String(Files.readAllBytes(thumbnailFile()!!), Charsets.ISO_8859_1).contains(pattern))

        manager.saveAs(target, "secret".toCharArray())
        manager.thumbnailCache.put(key, 1, marker())

        assertEquals(marker(), manager.thumbnailCache.get(key, 1))
        assertEquals(1, thumbnailFiles().size)
        assertFalse(String(Files.readAllBytes(thumbnailFile()!!), Charsets.ISO_8859_1).contains(pattern))
    }

    private fun clearText(): Boolean =
        String(Files.readAllBytes(thumbnailFile()!!), Charsets.ISO_8859_1).contains("Z".repeat(32))

    /** An encrypted project that is opened with its password keeps its thumbnails encrypted with that password. */
    @Test
    fun openedEncryptedProjectEncryptsItsThumbnails() {
        savedProject()
        manager.saveAs(target, "secret".toCharArray())
        manager.close()

        manager.open(target, "secret".toCharArray())
        manager.thumbnailCache.put(key, 1, marker())

        assertEquals(marker(), manager.thumbnailCache.get(key, 1))
        assertFalse(clearText())
    }

    /** Plain and encrypted sessions follow each other without sharing a thumbnail, and each keeps its own form. */
    @Test
    fun sessionsOfBothFormsDoNotShareThumbnails() {
        val encrypted = directory.resolve("secret.hocal")
        savedProject()
        manager.saveAs(encrypted, "secret".toCharArray())
        manager.thumbnailCache.put(key, 1, marker())
        manager.close()

        manager.open(target)
        assertNull(manager.thumbnailCache.get(key, 1))
        manager.thumbnailCache.put(key, 1, marker())
        assertTrue(clearText())
        manager.close()

        manager.open(encrypted, "secret".toCharArray())
        assertNull(manager.thumbnailCache.get(key, 1))
        manager.thumbnailCache.put(key, 1, marker())
        assertFalse(clearText())
        manager.close()

        assertEquals(0, leftovers())
    }

    /** A failed open - wrong password - leaves no thumbnail file and no store behind. */
    @Test
    fun failedOpenLeavesNoThumbnailFile() {
        savedProject()
        manager.saveAs(target, "secret".toCharArray())
        manager.close()

        assertThrows(WrongPasswordException::class.java) { manager.open(target, "wrong".toCharArray()) }
        manager.thumbnailCache.put(key, 1, marker())

        assertEquals(0, leftovers())
        assertNull(manager.thumbnailCache.get(key, 1))
    }

    /** Threads that use the store while `saveAs` renews it never fail, and the store works afterwards. */
    @Test
    fun renewingTheStoreIsSafeUnderParallelAccess() {
        savedProject()
        val running = AtomicBoolean(true)
        val pool = Executors.newFixedThreadPool(4)
        try {
            val workers = (0 until 4).map { worker ->
                pool.submit(Callable {
                    var rounds = 0
                    while (running.get()) {
                        val k = ThumbnailKey("w$worker", rounds % 4, 48)
                        manager.thumbnailCache.put(k, 1, marker())
                        manager.thumbnailCache.get(k, 1)
                        if (rounds % 25 == 0) manager.thumbnailCache.invalidate("w$worker")
                        rounds++
                    }
                    rounds
                })
            }
            manager.saveAs(directory.resolve("a.hocal"), "one".toCharArray())
            manager.saveAs(directory.resolve("b.hocal"))
            manager.saveAs(directory.resolve("c.hocal"), "two".toCharArray())
            running.set(false)
            workers.forEach { assertTrue(it.get(30, TimeUnit.SECONDS) > 0) }
        } finally {
            running.set(false)
            pool.shutdownNow()
        }

        manager.thumbnailCache.put(key, 1, marker())
        assertEquals(marker(), manager.thumbnailCache.get(key, 1))
        assertEquals(1, thumbnailFiles().size)
    }
}
