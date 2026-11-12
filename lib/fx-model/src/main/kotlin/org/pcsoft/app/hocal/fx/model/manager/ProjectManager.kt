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

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import org.pcsoft.app.hocal.fx.model.project.ProjectFileProperty
import org.pcsoft.app.hocal.model.project.Medium
import org.pcsoft.app.hocal.model.project.ProjectData
import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.project.Reference
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.pcsoft.app.hocal.model.project.VirtualNode
import org.pcsoft.app.hocal.model.storage.DirectoryMediaStore
import org.pcsoft.app.hocal.model.storage.MediaHistoryException
import org.pcsoft.app.hocal.model.storage.ProjectStorage
import org.pcsoft.app.hocal.model.storage.StorageLimits
import org.pcsoft.app.hocal.model.storage.appendMedium
import org.pcsoft.app.hocal.model.storage.removeMedium
import org.pcsoft.app.hocal.model.tree.TreeException
import org.pcsoft.app.hocal.model.tree.UnknownReferenceException
import org.pcsoft.app.hocal.model.tree.createEntry
import org.pcsoft.app.hocal.model.tree.createFolder
import org.pcsoft.app.hocal.model.tree.delete
import org.pcsoft.app.hocal.model.tree.move
import org.pcsoft.app.hocal.model.tree.orphansOf
import org.pcsoft.app.hocal.model.tree.removeReference
import org.pcsoft.app.hocal.model.tree.rename
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.UUID

/**
 * The one place that owns the project the user works on: it loads, creates, saves and closes it and
 * is the only way to change it. Exactly one project is loaded at a time.
 *
 * What a user interface binds to is [project], the property model of the loaded project, and the
 * read-only properties [loadedProperty], [pathProperty], [dirtyProperty] and [encryptedProperty].
 * Every change - an operation of this manager or an edit of a field through a property model of
 * [project] - marks the project as changed ([dirtyProperty]) until it is saved.
 *
 * Nothing else may read or write the project file or edit the tree: the storage, the tree operations
 * and the media history are reached through this manager only.
 *
 * The media of the open project live in a temporary folder for the time it is open and are removed
 * again on [close]. A password given to [open] or [saveAs] is kept in memory so [save] does not ask
 * again, and is overwritten on [close]. The password is changed only by [saveAs].
 *
 * All members belong to the FX thread; see [threadCheck]. Reading and writing a file blocks the caller.
 */
object ProjectManager {

    /** The property model of the loaded project, its value is `null` while no project is loaded. */
    val project: ProjectFileProperty = ProjectFileProperty()

    private val loadedWrapper = ReadOnlyBooleanWrapper(false)

    private val pathWrapper = ReadOnlyObjectWrapper<Path?>(null)

    private val dirtyWrapper = ReadOnlyBooleanWrapper(false)

    private val encryptedWrapper = ReadOnlyBooleanWrapper(false)

    /** Whether a project is loaded. */
    val loadedProperty: ReadOnlyBooleanProperty = loadedWrapper.readOnlyProperty

    /** The file of the loaded project, `null` for a new project that was not saved yet. */
    val pathProperty: ReadOnlyObjectProperty<Path?> = pathWrapper.readOnlyProperty

    /** Whether the loaded project has unsaved changes. */
    val dirtyProperty: ReadOnlyBooleanProperty = dirtyWrapper.readOnlyProperty

    /** Whether the loaded project is saved encrypted, that is whether a password is held for it. */
    val encryptedProperty: ReadOnlyBooleanProperty = encryptedWrapper.readOnlyProperty

    /** Whether a project is loaded. */
    val isLoaded: Boolean get() = loadedWrapper.get()

    /** The version of the application, written into the project on every save. Set by the application at start. */
    var applicationVersion: String = ""

    /**
     * Tells whether the calling thread is the FX thread. `javafx.base` has no `Platform`, so the application
     * sets this at start, for instance to `{ Platform.isFxApplicationThread() }`. Every operation fails with
     * [IllegalStateException] if it answers `false`.
     */
    var threadCheck: () -> Boolean = { true }

    /** The folder the temporary folders of the open project are created in, `null` for the system default. */
    var workRoot: Path? = null

    private var store: DirectoryMediaStore? = null

    private var sessionDirectory: Path? = null

    private var password: CharArray? = null

    // While the project is loaded or saved the manager itself changes the property models, which must not count as an edit.
    private var quiet = false

    init {
        // An invalidation listener that reads the value again: a property that was not read after it reported
        // stays invalid and would not report the next change.
        project.addListener {
            project.get()
            if (!quiet) dirtyWrapper.set(true)
        }
    }

    /**
     * Creates an empty project with a new UUID and the current time as creation time. It has no file and is
     * changed ([dirtyProperty] is `true`) until it is saved.
     *
     * @param name The name of the project.
     * @throws IllegalStateException if a project is loaded, close it first.
     */
    fun createNew(name: String) {
        checkThread()
        check(!isLoaded) { "A project is loaded, close it first" }
        val now = Instant.now()
        val file = ProjectFile(
            ProjectData(
                name = name,
                uuid = UUID.randomUUID().toString(),
                created = now,
                modified = now,
                applicationVersion = applicationVersion,
            ),
        )
        val directory = newSessionDirectory()
        attach(file, directory, path = null, password = null)
        dirtyWrapper.set(true)
    }

    /**
     * Opens a project file; the media are read into the temporary folder. If this fails the manager stays closed.
     *
     * @param path The file.
     * @param password The password of an encrypted file, `null` for an unencrypted one. The array is not changed.
     * @throws IllegalStateException if a project is loaded, close it first.
     * @throws org.pcsoft.app.hocal.model.storage.ProjectFileException if the content is not valid, the password is
     *   missing or wrong, or the file is damaged.
     * @throws java.io.IOException if the file cannot be read.
     */
    fun open(path: Path, password: CharArray? = null) {
        checkThread()
        check(!isLoaded) { "A project is loaded, close it first" }
        val directory = newSessionDirectory()
        try {
            val file = ProjectStorage.read(path, DirectoryMediaStore(directory), StorageLimits(), password)
            attach(file, directory, path, password)
        } catch (e: Throwable) {
            directory.toFile().deleteRecursively()
            throw e
        }
    }

    /**
     * Saves the project to its file with the password it was opened or last saved with, and sets the time of
     * the last change and the application version.
     *
     * @throws IllegalStateException if no project is loaded or the project has no file yet, use [saveAs].
     * @throws java.io.IOException if writing fails; an existing file stays untouched.
     */
    fun save() {
        checkThread()
        val target = pathWrapper.get()
        checkNotNull(target) { "The project has no file yet, use saveAs" }
        write(target, password)
    }

    /**
     * Saves the project to a new file; the file and the password become those of the project. This is the only
     * way to change the password or to switch between the encrypted and the unencrypted form.
     *
     * @param path The new file.
     * @param password The password, `null` to save unencrypted. The array is not changed.
     * @throws IllegalStateException if no project is loaded.
     * @throws java.io.IOException if writing fails; an existing file stays untouched.
     */
    fun saveAs(path: Path, password: CharArray? = null) {
        checkThread()
        write(path, password)
        pathWrapper.set(path)
        keepPassword(password)
    }

    /**
     * Closes the project and removes its temporary folder.
     *
     * @param discardChanges Whether unsaved changes may be thrown away.
     * @return `false` and nothing changed if there are unsaved changes and [discardChanges] is not set, else `true`.
     */
    fun close(discardChanges: Boolean = false): Boolean {
        checkThread()
        if (!isLoaded) return true
        if (dirtyWrapper.get() && !discardChanges) return false
        release()
        return true
    }

    // ---- tree --------------------------------------------------------------------------------------------------

    /** Creates a folder, see [org.pcsoft.app.hocal.model.tree.createFolder]. */
    fun createFolder(parent: VirtualFolder, name: String): VirtualFolder = change { file, _ -> file.createFolder(parent, name) }

    /** Creates an entry linking a reference, see [org.pcsoft.app.hocal.model.tree.createEntry]. */
    fun createEntry(parent: VirtualFolder, name: String, reference: String): VirtualEntry =
        change { file, _ -> file.createEntry(parent, name, reference) }

    /** Renames a folder or an entry, see [org.pcsoft.app.hocal.model.tree.rename]. */
    fun rename(node: VirtualNode, name: String) = change { file, _ -> file.rename(node, name) }

    /** Moves a folder or an entry, see [org.pcsoft.app.hocal.model.tree.move]. */
    fun move(node: VirtualNode, target: VirtualFolder) = change { file, _ -> file.move(node, target) }

    /** The references that would lose their last entry if [node] were deleted, see [org.pcsoft.app.hocal.model.tree.orphansOf]. */
    fun orphansOf(node: VirtualNode): List<String> {
        checkThread()
        return requireProject().orphansOf(node)
    }

    /**
     * Deletes a folder or an entry. If a reference would lose its last entry, nothing is changed and `false` is
     * returned, unless [removeOrphans] is set, see [org.pcsoft.app.hocal.model.tree.delete].
     */
    fun delete(node: VirtualNode, removeOrphans: Boolean = false): Boolean =
        change<Boolean>(changed = { it }) { file, store -> file.delete(node, store, removeOrphans) }

    /** Removes a reference with its entries and media, see [org.pcsoft.app.hocal.model.tree.removeReference]. */
    fun removeReference(uuid: String) = change { file, store -> file.removeReference(store, uuid) }

    // ---- media -------------------------------------------------------------------------------------------------

    /**
     * Adds a reference with a first medium, the original `000`.
     *
     * @param medium The details of the medium.
     * @param extension The file extension of the medium, without dot.
     * @param content The content of the medium; it is not closed.
     * @return the UUID of the new reference.
     */
    fun addReference(medium: Medium, extension: String, content: InputStream): String = change { file, store ->
        val uuid = UUID.randomUUID().toString()
        store.accept(uuid, 0, extension, content)
        file.references.add(Reference(uuid, mutableListOf(medium)))
        uuid
    }

    /**
     * Appends a medium as the new current one of a reference, see [org.pcsoft.app.hocal.model.storage.appendMedium].
     *
     * @return the number of the new medium.
     * @throws org.pcsoft.app.hocal.model.tree.UnknownReferenceException if the project has no such reference.
     */
    fun appendMedium(uuid: String, medium: Medium, extension: String, content: InputStream): Int =
        change { file, store -> referenceOf(file, uuid).appendMedium(store, medium, extension, content) }

    /**
     * Removes a medium of a reference, the following ones move down, see [org.pcsoft.app.hocal.model.storage.removeMedium].
     *
     * @throws org.pcsoft.app.hocal.model.tree.UnknownReferenceException if the project has no such reference.
     */
    fun removeMedium(uuid: String, number: Int) =
        change { file, store -> referenceOf(file, uuid).removeMedium(store, number) }

    /** Opens the content of a medium; the caller closes the stream. */
    fun openMedium(uuid: String, number: Int): InputStream {
        checkThread()
        requireProject()
        return store!!.open(uuid, number)
    }

    /** The file extension of a medium, without dot. */
    fun mediumExtension(uuid: String, number: Int): String {
        checkThread()
        requireProject()
        return store!!.extension(uuid, number)
    }

    /** Closes without a question and sets everything back to the start, for tests. */
    internal fun resetForTests() {
        if (isLoaded) release()
        applicationVersion = ""
        threadCheck = { true }
        workRoot = null
    }

    // ---- internals ---------------------------------------------------------------------------------------------

    private fun checkThread() = check(threadCheck()) { "Must be called on the FX thread" }

    private fun requireProject(): ProjectFile {
        check(isLoaded) { "No project is loaded" }
        return checkNotNull(project.get()) { "No project is loaded" }
    }

    private fun referenceOf(file: ProjectFile, uuid: String): Reference =
        file.references.firstOrNull { it.uuid == uuid } ?: throw UnknownReferenceException(uuid)

    /**
     * Runs a change. If it succeeded the property models read the project again and the project counts as
     * changed, unless [changed] says nothing was changed. A rejected change - a tree or history rule - leaves
     * everything as it was; a failure of the store counts as a change, because the project may be half changed.
     */
    private fun <T> change(changed: (T) -> Boolean = { true }, block: (ProjectFile, DirectoryMediaStore) -> T): T {
        checkThread()
        val file = requireProject()
        val result = try {
            block(file, store!!)
        } catch (e: TreeException) {
            throw e
        } catch (e: MediaHistoryException) {
            throw e
        } catch (e: Throwable) {
            markChanged()
            throw e
        }
        if (changed(result)) markChanged()
        return result
    }

    private fun markChanged() {
        project.refresh()
        dirtyWrapper.set(true)
    }

    private fun write(target: Path, password: CharArray?) {
        val file = requireProject()
        quiet = true
        try {
            file.project.modified = Instant.now()
            file.project.applicationVersion = applicationVersion
            project.refresh()
            ProjectStorage.write(target, file, store!!, password)
        } finally {
            quiet = false
        }
        dirtyWrapper.set(false)
    }

    private fun attach(file: ProjectFile, directory: Path, path: Path?, password: CharArray?) {
        quiet = true
        try {
            project.set(file)
        } finally {
            quiet = false
        }
        sessionDirectory = directory
        store = DirectoryMediaStore(directory)
        pathWrapper.set(path)
        keepPassword(password)
        dirtyWrapper.set(false)
        loadedWrapper.set(true)
    }

    private fun release() {
        quiet = true
        try {
            project.set(null)
        } finally {
            quiet = false
        }
        keepPassword(null)
        sessionDirectory?.toFile()?.deleteRecursively()
        sessionDirectory = null
        store = null
        pathWrapper.set(null)
        dirtyWrapper.set(false)
        loadedWrapper.set(false)
    }

    private fun keepPassword(newPassword: CharArray?) {
        password?.fill('\u0000')
        password = newPassword?.copyOf()
        encryptedWrapper.set(password != null)
    }

    private fun newSessionDirectory(): Path {
        val root = workRoot
        return if (root != null) Files.createTempDirectory(root, "hocal-project-") else Files.createTempDirectory("hocal-project-")
    }
}
