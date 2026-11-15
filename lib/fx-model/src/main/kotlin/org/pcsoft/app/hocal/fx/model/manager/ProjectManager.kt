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

import javafx.application.Platform
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
import org.pcsoft.app.hocal.model.storage.internal.DirectoryMediaStore
import org.pcsoft.app.hocal.model.storage.MediaHistoryException
import org.pcsoft.app.hocal.model.storage.internal.ProjectStorage
import org.pcsoft.app.hocal.model.storage.internal.StorageLimits
import org.pcsoft.app.hocal.model.storage.internal.appendMedium
import org.pcsoft.app.hocal.model.storage.internal.removeMedium
import org.pcsoft.app.hocal.model.tree.TreeException
import org.pcsoft.app.hocal.model.tree.UnknownReferenceException
import org.pcsoft.app.hocal.model.tree.internal.createEntry
import org.pcsoft.app.hocal.model.tree.internal.createFolder
import org.pcsoft.app.hocal.model.tree.internal.delete
import org.pcsoft.app.hocal.model.tree.internal.move
import org.pcsoft.app.hocal.model.tree.internal.orphansOf
import org.pcsoft.app.hocal.model.tree.internal.removeReference
import org.pcsoft.app.hocal.model.tree.internal.rename
import org.pcsoft.app.hocal.thumbnail.ThumbnailCache
import org.pcsoft.app.hocal.thumbnail.ThumbnailKey
import org.pcsoft.app.hocal.thumbnail.ThumbnailPixels
import org.pcsoft.app.hocal.thumbnail.ThumbnailStorage
import org.pcsoft.app.hocal.thumbnail.ZipThumbnailStore
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

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
 * and the media history live in the packages `...model.storage.internal` and `...model.tree.internal`
 * of `hocal-model`, which that module exports to `hocal-fx-model` only. They are reached through this
 * manager only.
 *
 * The media of the open project live in a temporary folder for the time it is open and are removed
 * again on [close]. A password given to [open] or [saveAs] is kept in memory so [save] does not ask
 * again, and is overwritten on [close]. The password is changed only by [saveAs]. An empty password is refused,
 * no password means unencrypted.
 *
 * While the project is encrypted, its media in the temporary folder are encrypted as well, with a random key that
 * lives in memory only, see [DirectoryMediaStore]. Media of an unencrypted project stay plain there.
 *
 * If [thumbnailStorage] is [ThumbnailStorage.DISK], the thumbnails of the open project are kept in a file `thumbnails-<n>.zip` in the same
 * temporary folder, see [thumbnailCache]. It is encrypted with the password of the project if there is one, renewed
 * empty when [saveAs] changes the password, and removed with the folder on [close]. By default the thumbnails are kept
 * in memory only and no file is made.
 *
 * All members belong to the FX thread; see [threadCheck]. [open], [save] and [saveAs] read and write the file and
 * block the caller. [openAsync], [saveAsync] and [saveAsAsync] do the same in the background and report the
 * [FileOperationResult] on the FX thread; [busyProperty] is `true` meanwhile, a second file operation is answered
 * with [FileOperationResult.Busy], and every change of the project and [close] are refused.
 */
object ProjectManager {

    /** The property model of the loaded project, its value is `null` while no project is loaded. */
    val project: ProjectFileProperty = ProjectFileProperty()

    private val loadedWrapper = ReadOnlyBooleanWrapper(false)

    private val pathWrapper = ReadOnlyObjectWrapper<Path?>(null)

    private val dirtyWrapper = ReadOnlyBooleanWrapper(false)

    private val encryptedWrapper = ReadOnlyBooleanWrapper(false)

    private val busyWrapper = ReadOnlyBooleanWrapper(false)

    /** Whether a project is loaded. */
    val loadedProperty: ReadOnlyBooleanProperty = loadedWrapper.readOnlyProperty

    /** The file of the loaded project, `null` for a new project that was not saved yet. */
    val pathProperty: ReadOnlyObjectProperty<Path?> = pathWrapper.readOnlyProperty

    /** Whether the loaded project has unsaved changes. */
    val dirtyProperty: ReadOnlyBooleanProperty = dirtyWrapper.readOnlyProperty

    /** Whether the loaded project is saved encrypted, that is whether a password is held for it. */
    val encryptedProperty: ReadOnlyBooleanProperty = encryptedWrapper.readOnlyProperty

    /** Whether a file operation runs in the background, see [openAsync], [saveAsync] and [saveAsAsync]. */
    val busyProperty: ReadOnlyBooleanProperty = busyWrapper.readOnlyProperty

    /** Whether a file operation runs in the background. */
    val isBusy: Boolean get() = busyWrapper.get()

    /** Whether a project is loaded. */
    val isLoaded: Boolean get() = loadedWrapper.get()

    /**
     * Where the thumbnails of the open project are kept: [ThumbnailStorage.MEMORY] by default, so no file is made, or
     * [ThumbnailStorage.DISK] for a file in its temporary folder as well. Meant to be set from the settings of the
     * application.
     *
     * Switching it while a project is loaded makes or removes the file at once; the thumbnails in the file are lost.
     */
    var thumbnailStorage: ThumbnailStorage = ThumbnailStorage.MEMORY
        set(value) {
            checkThread()
            if (field == value) return
            field = value
            if (isLoaded) renewThumbnailStore()
        }

    /**
     * The thumbnail store of the open project: the very same object for the whole run, so it may be held by anyone.
     * It forwards to the file store of the project that is loaded now, see [thumbnailStorage]; while no project is
     * loaded, or the storage is [ThumbnailStorage.MEMORY], it has nothing and stores nothing. It is thread-safe, unlike the manager itself.
     */
    val thumbnailCache: ThumbnailCache<ThumbnailPixels> get() = thumbnails

    /** The version of the application, written into the project on every save. Set by the application at start. */
    var applicationVersion: String = ""

    /**
     * Tells whether the calling thread is the FX thread; every operation fails with [IllegalStateException] if it
     * answers `false`. It asks the FX toolkit and belongs to the manager: only the tests of this library replace
     * it, as they run without a toolkit.
     */
    internal var threadCheck: () -> Boolean = { Platform.isFxApplicationThread() }

    /** Runs the file access of the asynchronous operations; only the tests of this library replace it. */
    internal var runner: BackgroundRunner = TaskRunner

    /** The folder the temporary folders of the open project are created in, `null` for the system default. */
    var workRoot: Path? = null

    private var store: DirectoryMediaStore? = null

    private var sessionDirectory: Path? = null

    private var password: CharArray? = null

    private val thumbnails = ForwardingThumbnailCache()

    private var thumbnailStore: ZipThumbnailStore? = null

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
        checkNotBusy()
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
        attach(file, directory, DirectoryMediaStore(directory), path = null, password = null)
        dirtyWrapper.set(true)
    }

    /**
     * Tells whether a project file needs a password to be opened. A user interface asks this first, shows the
     * password dialog only if the answer is `true`, and then calls [open] with the password. A file that is no
     * project file at all answers `false`; [open] reports what is wrong with it.
     *
     * Only the start of the file is read. No project has to be loaded, and nothing is changed.
     *
     * @param path The file.
     * @return `true` if the file is encrypted, else `false`.
     * @throws java.io.IOException if the file does not exist or cannot be read.
     */
    fun isPasswordRequired(path: Path): Boolean {
        checkThread()
        return ProjectStorage.isEncrypted(path)
    }

    /**
     * Opens a project file; the media are read into the temporary folder. If this fails the manager stays closed.
     * Ask [isPasswordRequired] before to know whether a password is needed.
     *
     * @param path The file.
     * @param password The password of an encrypted file, `null` for an unencrypted one. The array is not changed.
     * @throws IllegalStateException if a project is loaded, close it first, or a file operation runs.
     * @throws IllegalArgumentException if the password is empty.
     * @throws org.pcsoft.app.hocal.model.storage.PasswordRequiredException if the file is encrypted and no password
     *   is given.
     * @throws org.pcsoft.app.hocal.model.storage.WrongPasswordException if the password does not match.
     * @throws org.pcsoft.app.hocal.model.storage.ProjectFileException if the content is not valid or the file is
     *   damaged.
     * @throws java.io.IOException if the file cannot be read.
     */
    fun open(path: Path, password: CharArray? = null) {
        checkThread()
        checkNotBusy()
        requireValid(password)
        check(!isLoaded) { "A project is loaded, close it first" }
        val directory = newSessionDirectory()
        val mediaStore = DirectoryMediaStore(directory, encrypted = password != null)
        try {
            val file = ProjectStorage.read(path, mediaStore, StorageLimits(), password)
            attach(file, directory, mediaStore, path, password)
        } catch (e: Throwable) {
            mediaStore.close()
            directory.toFile().deleteRecursively()
            throw e
        }
    }

    /**
     * Opens a project file in the background and replaces the loaded project, if there is one, once the new one
     * is read. If this fails nothing changes: the loaded project, its path and its password stay as they were.
     * The caller asks about unsaved changes before. The window of the project is not touched while reading, so a
     * user interface locks itself with [busyProperty].
     *
     * @param path The file.
     * @param password The password of an encrypted file, `null` for an unencrypted one. The array is copied and
     *   not changed; the caller overwrites its own array.
     * @param onDone Called on the FX thread with [FileOperationResult.Success], [FileOperationResult.Failure] or,
     *   at once, [FileOperationResult.Busy].
     * @throws IllegalArgumentException if the password is empty.
     */
    fun openAsync(path: Path, password: CharArray? = null, onDone: (FileOperationResult) -> Unit) {
        checkThread()
        requireValid(password)
        if (busyWrapper.get()) {
            onDone(FileOperationResult.Busy)
            return
        }
        val secret = password?.copyOf()
        val directory = try {
            newSessionDirectory()
        } catch (e: IOException) {
            secret?.fill('\u0000')
            onDone(FileOperationResult.Failure(e.toFileError(writing = false), e))
            return
        }
        val created = AtomicReference<DirectoryMediaStore?>()
        busyWrapper.set(true)
        runner.run({
            val mediaStore = DirectoryMediaStore(directory, encrypted = secret != null)
            created.set(mediaStore)
            ProjectStorage.read(path, mediaStore, StorageLimits(), secret)
        }) { result ->
            busyWrapper.set(false)
            val outcome = try {
                val file = result.getOrThrow()
                if (isLoaded) release()
                attach(file, directory, created.get()!!, path, secret)
                FileOperationResult.Success
            } catch (e: Throwable) {
                created.get()?.close()
                directory.toFile().deleteRecursively()
                FileOperationResult.Failure(e.toFileError(writing = false), e)
            }
            secret?.fill('\u0000')
            onDone(outcome)
        }
    }

    /**
     * Saves the project to its file with the password it was opened or last saved with, and sets the time of
     * the last change and the application version.
     *
     * @throws IllegalStateException if no project is loaded or the project has no file yet, use [saveAs], or a file
     *   operation runs.
     * @throws java.io.IOException if writing fails; an existing file stays untouched.
     */
    fun save() {
        checkThread()
        checkNotBusy()
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
     * @throws IllegalStateException if no project is loaded or a file operation runs.
     * @throws IllegalArgumentException if the password is empty.
     * @throws java.io.IOException if writing fails; an existing file stays untouched.
     */
    fun saveAs(path: Path, password: CharArray? = null) {
        checkThread()
        checkNotBusy()
        requireValid(password)
        requireProject()
        if (password != null) store!!.enableEncryption()
        write(path, password)
        adopt(path, password)
    }

    /**
     * Saves the project to its file in the background, like [save]. The project must not be changed meanwhile,
     * which the manager enforces while [busyProperty] is `true`.
     *
     * @param onDone Called on the FX thread with the result, at once with [FileOperationResult.Busy].
     * @throws IllegalStateException if no project is loaded or the project has no file yet, use [saveAsAsync].
     */
    fun saveAsync(onDone: (FileOperationResult) -> Unit) {
        checkThread()
        requireProject()
        val target = checkNotNull(pathWrapper.get()) { "The project has no file yet, use saveAsAsync" }
        startWrite(target, saveAs = false, secret = password?.copyOf(), onDone = onDone)
    }

    /**
     * Saves the project to a new file in the background, like [saveAs]. File, password and thumbnails of the
     * project change only if writing worked.
     *
     * @param password The password, `null` to save unencrypted. The array is copied and not changed.
     * @param onDone Called on the FX thread with the result, at once with [FileOperationResult.Busy].
     * @throws IllegalStateException if no project is loaded.
     * @throws IllegalArgumentException if the password is empty.
     */
    fun saveAsAsync(path: Path, password: CharArray? = null, onDone: (FileOperationResult) -> Unit) {
        checkThread()
        requireValid(password)
        requireProject()
        startWrite(path, saveAs = true, secret = password?.copyOf(), onDone = onDone)
    }

    /**
     * Closes the project and removes its temporary folder.
     *
     * @param discardChanges Whether unsaved changes may be thrown away.
     * @return `false` and nothing changed if there are unsaved changes and [discardChanges] is not set, else `true`.
     * @throws IllegalStateException if a file operation runs.
     */
    fun close(discardChanges: Boolean = false): Boolean {
        checkThread()
        checkNotBusy()
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
        busyWrapper.set(false)
        runner = SynchronousRunner
        if (isLoaded) release()
        applicationVersion = ""
        threadCheck = { true }
        workRoot = null
        thumbnailStorage = ThumbnailStorage.MEMORY
    }

    // ---- internals ---------------------------------------------------------------------------------------------

    private fun checkThread() = check(threadCheck()) { "Must be called on the FX thread" }

    private fun checkNotBusy() = check(!busyWrapper.get()) { "A file operation is running" }

    private fun requireValid(password: CharArray?) =
        require(password == null || password.isNotEmpty()) { "The password must not be empty, use no password instead" }

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
        checkNotBusy()
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
        val file = beforeWrite()
        ProjectStorage.write(target, file, store!!, password)
        dirtyWrapper.set(false)
    }

    // Sets the time of the last change and the version into the project; on the FX thread, before the file is written.
    private fun beforeWrite(): ProjectFile {
        val file = requireProject()
        quiet = true
        try {
            file.project.modified = Instant.now()
            file.project.applicationVersion = applicationVersion
            project.refresh()
        } finally {
            quiet = false
        }
        return file
    }

    // File and password become those of the project; a changed password starts a new, empty thumbnail store.
    private fun adopt(path: Path, newPassword: CharArray?) {
        pathWrapper.set(path)
        val sameSecret = this.password?.contentEquals(newPassword) ?: (newPassword == null)
        keepPassword(newPassword)
        if (!sameSecret) renewThumbnailStore()
    }

    private fun startWrite(target: Path, saveAs: Boolean, secret: CharArray?, onDone: (FileOperationResult) -> Unit) {
        if (busyWrapper.get()) {
            secret?.fill('\u0000')
            onDone(FileOperationResult.Busy)
            return
        }
        val file = beforeWrite()
        val mediaStore = store!!
        busyWrapper.set(true)
        runner.run({
            if (saveAs && secret != null) mediaStore.enableEncryption()
            ProjectStorage.write(target, file, mediaStore, secret)
        }) { result ->
            busyWrapper.set(false)
            val outcome = result.fold(
                onSuccess = {
                    dirtyWrapper.set(false)
                    if (saveAs) adopt(target, secret)
                    FileOperationResult.Success
                },
                onFailure = { FileOperationResult.Failure(it.toFileError(writing = true), it) },
            )
            secret?.fill('\u0000')
            onDone(outcome)
        }
    }

    private fun attach(file: ProjectFile, directory: Path, mediaStore: DirectoryMediaStore, path: Path?, password: CharArray?) {
        quiet = true
        try {
            project.set(file)
        } finally {
            quiet = false
        }
        sessionDirectory = directory
        store = mediaStore
        pathWrapper.set(path)
        keepPassword(password)
        renewThumbnailStore()
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
        closeThumbnailStore()
        store?.close()
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

    // The old store is closed after the new one is in place, so a thumbnail that is looked up meanwhile finds a store.
    // Every store has a file of its own: closing the old one deletes its file, which must not be the one of the new.
    private fun renewThumbnailStore() {
        val directory = sessionDirectory
        val fresh = try {
            directory?.takeIf { thumbnailStorage == ThumbnailStorage.DISK }?.let { ZipThumbnailStore(it.resolve("$THUMBNAIL_PREFIX-${++thumbnailCounter}.zip"), password) }
        } catch (_: IOException) {
            null
        }
        val old = thumbnailStore
        thumbnailStore = fresh
        thumbnails.target = fresh
        old?.close()
    }

    private fun closeThumbnailStore() {
        thumbnails.target = null
        thumbnailStore?.close()
        thumbnailStore = null
    }

    /** Forwards to the current store; no store means no thumbnail. */
    private class ForwardingThumbnailCache : ThumbnailCache<ThumbnailPixels> {
        @Volatile
        var target: ThumbnailCache<ThumbnailPixels>? = null

        override fun get(key: ThumbnailKey, mediaCount: Int): ThumbnailPixels? = target?.get(key, mediaCount)

        override fun put(key: ThumbnailKey, mediaCount: Int, value: ThumbnailPixels) {
            target?.put(key, mediaCount, value)
        }

        override fun invalidate(reference: String) {
            target?.invalidate(reference)
        }

        override fun clear() {
            target?.clear()
        }
    }

    private const val THUMBNAIL_PREFIX = "thumbnails"

    private var thumbnailCounter = 0

    private fun newSessionDirectory(): Path {
        val root = workRoot
        return if (root != null) Files.createTempDirectory(root, "hocal-project-") else Files.createTempDirectory("hocal-project-")
    }
}
