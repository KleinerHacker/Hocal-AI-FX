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

package org.pcsoft.app.hocal.app.ui.component

import de.saxsys.mvvmfx.ViewModel
import javafx.application.ColorScheme
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.ReadOnlyIntegerWrapper
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.image.Image
import javafx.stage.Screen
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.ui.preview.MediaPreviewRegistry
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.app.ui.preview.MemoryThumbnailCache
import org.pcsoft.app.hocal.app.ui.preview.ThumbnailCache
import org.pcsoft.app.hocal.app.ui.preview.ThumbnailKey
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import java.util.Collections
import java.util.IdentityHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicInteger

/**
 * View model of the project file list.
 *
 * It lists the entries of [folder] (never subfolders) and loads their previews from [mediaSource] in the
 * background, lazily: loading starts when a cell asks for the [preview] of an entry. The entries compare
 * by value, so selection and the load state of the previews work by object identity throughout - two sibling
 * entries of equal content stay two different entries.
 *
 * The folder, the media source and the appearance of the system are observed only between [onShow] and
 * [onHide]. A load result is taken over only if the generation of the loading and the entry (by
 * identity) are still current; a change of the folder or the source and [onHide] start a new generation
 * and cancel the running tasks.
 */
class ProjectFileListViewModel : ViewModel {
    private val selectedFileWrapper = ReadOnlyObjectWrapper<VirtualEntry?>(this, "selectedFile")
    private val darkWrapper = ReadOnlyBooleanWrapper(this, "dark", false)
    private val revisionWrapper = ReadOnlyIntegerWrapper(this, "revision", 0)

    private var observing = false
    private var shownFolder: VirtualFolder? = null

    // Read by the load tasks outside the FX thread, written on the FX thread only.
    @Volatile
    private var generation = 0

    // The load state of the entries on show, keyed by identity: the entries are equal by value.
    private val states = IdentityHashMap<VirtualEntry, ProjectFilePreview>()

    // The source of the last show, to notice that another project was loaded meanwhile.
    private var shownSource: MediaSource? = null

    // Created on the first load, on the FX thread only; null again after a change of the size and after close().
    private var pool: ExecutorService? = null
    private var closed = false

    // Fields: a method reference would be a new object per use and could not be removed again.
    private val folderListener = InvalidationListener { onFolderInvalidated() }
    private val sourceListener = InvalidationListener { onSourceInvalidated() }
    private val appearanceListener = InvalidationListener { onAppearanceChanged() }

    /**
     * The folder whose entries are shown, `null` for an empty list.
     *
     * The caller binds it from outside; a new binding replaces the former one.
     */
    val folder: ObjectProperty<VirtualFolder?> = SimpleObjectProperty(this, "folder")

    /**
     * The source the previews are loaded from, `null` if nothing can be loaded; the entries are shown
     * with placeholder icons then.
     */
    val mediaSource: ObjectProperty<MediaSource?> = SimpleObjectProperty(this, "mediaSource")

    /**
     * The number of threads that load thumbnails at the same time, at least 1.
     *
     * Meant to be set from the settings of the application later. A new value is used for the loads that
     * start afterwards; loads that are running finish on the threads they started on.
     *
     * @throws IllegalArgumentException if the value is less than 1
     */
    var loadThreads: Int = DEFAULT_LOAD_THREADS
        set(value) {
            require(value >= 1) { "loadThreads must be at least 1, was $value" }
            if (field == value) return
            field = value
            pool?.shutdown()
            pool = null
        }

    /** The registry of the preview providers; replaceable for tests. */
    var registry: MediaPreviewRegistry = MediaPreviewRegistry()

    /**
     * The cache of the loaded thumbnails. It keeps them across folder changes and across [onShow] and
     * [onHide], and is cleared when another media source is set. Replaceable, for instance by a cache on disk.
     */
    @Volatile
    var thumbnails: ThumbnailCache = MemoryThumbnailCache()

    /** Whether the system shows the dark appearance, so the dark placeholder icons are used. */
    val dark: ReadOnlyBooleanProperty = darkWrapper.readOnlyProperty

    /** The entry the user selected, or `null` while none is selected. */
    val selectedFile: ReadOnlyObjectProperty<VirtualEntry?> = selectedFileWrapper.readOnlyProperty

    /** Counts the rebuilds of the list, so that the view notices a rebuild even if the entries stay equal. */
    val revision: ReadOnlyIntegerProperty = revisionWrapper.readOnlyProperty

    /** The entries to show, in the order of the model; the objects of the model itself. */
    var entries: List<VirtualEntry> = emptyList()
        private set

    /** The index in [entries] of the selected entry, set by every rebuild before [revision] changes, or -1. */
    var indexToSelect: Int = -1
        private set

    /**
     * Reports the entry the user selected.
     *
     * @param file the selected entry, or `null` for no selection
     */
    fun selectFile(file: VirtualEntry?) {
        selectedFileWrapper.set(file)
    }

    /**
     * Returns the previews of an entry and starts loading them, if the view model is observing, a
     * source exists and loading did not start yet.
     *
     * Called on the FX thread by the cell that shows the entry.
     *
     * @param entry an entry of [entries]
     * @return the previews, whose images are set when loaded
     */
    fun preview(entry: VirtualEntry): ProjectFilePreview {
        val state = states.getOrPut(entry) { ProjectFilePreview() }
        if (observing && !state.started) startLoading(entry, state)
        return state
    }

    /**
     * Tells that the media of a reference changed, for instance because a medium of its history was deleted
     * or appended: the thumbnails of the reference are dropped from the cache and every entry that links it
     * is loaded anew, also while it is shown. Its images show the placeholder until the new ones are loaded.
     *
     * The cache recognizes a changed number of media by itself, but not a change that keeps the number - a
     * medium removed and another appended. Whoever changes the media calls this method in every case.
     *
     * May be called from any thread. While the component is not on screen only the cache is dropped; the
     * entries are loaded anew when it is shown again.
     *
     * @param reference UUID of the reference whose media changed
     */
    fun invalidate(reference: String) {
        thumbnails.invalidate(reference)
        if (Platform.isFxApplicationThread()) reloadReference(reference) else Platform.runLater { reloadReference(reference) }
    }

    private fun reloadReference(reference: String) {
        // The cache is dropped once more: a load that was running while the media changed may have stored a stale image.
        thumbnails.invalidate(reference)
        states.entries.filter { (entry, _) -> entry.reference == reference }.forEach { (entry, state) ->
            state.reset()
            if (observing) startLoading(entry, state)
        }
    }

    /**
     * Starts observing the folder, the source and the appearance, and builds the list anew.
     *
     * Called when the component comes on screen. Calling it twice registers only once.
     */
    fun onShow() {
        if (!observing) {
            observing = true
            folder.addListener(folderListener)
            mediaSource.addListener(sourceListener)
            Platform.getPreferences().colorSchemeProperty().addListener(appearanceListener)
        }
        // Reading validates the properties, so that their next change is reported.
        adoptSource()
        darkWrapper.set(isDarkAppearance())
        resetLoading()
        shownFolder = folder.get()
        rebuild()
    }

    /**
     * Stops observing and cancels the running load tasks. The list stays as it is; the pool stays usable.
     *
     * Called when the component leaves the screen. Calling it twice is harmless.
     */
    fun onHide() {
        if (observing) {
            observing = false
            folder.removeListener(folderListener)
            mediaSource.removeListener(sourceListener)
            Platform.getPreferences().colorSchemeProperty().removeListener(appearanceListener)
        }
        cancelAll()
    }

    /**
     * Cancels all loading and ends the pool of the load threads for good.
     *
     * To be called when the view model is not used any more; the previews cannot be loaded afterwards.
     */
    fun close() {
        onHide()
        closed = true
        pool?.shutdownNow()
        pool = null
    }

    private fun isDarkAppearance(): Boolean = Platform.getPreferences().colorScheme == ColorScheme.DARK

    private fun onAppearanceChanged() {
        // Reading again is a must: a property reports the next change only after its value was read.
        Platform.getPreferences().colorScheme
        onFx { darkWrapper.set(isDarkAppearance()) }
    }

    private fun onFolderInvalidated() {
        // Reading again is a must: a property reports the next change only after its value was read.
        folder.get()
        onFx { rebuild() }
    }

    private fun onSourceInvalidated() {
        mediaSource.get()
        onFx {
            adoptSource()
            resetLoading()
            rebuild()
        }
    }

    // Another source means another project, so its thumbnails must not be taken for those of the former one.
    private fun adoptSource() {
        val source = mediaSource.get()
        if (source !== shownSource) {
            thumbnails.clear()
            shownSource = source
        }
    }

    private fun onFx(action: () -> Unit) {
        if (Platform.isFxApplicationThread()) {
            if (observing) action()
        } else {
            Platform.runLater { if (observing) action() }
        }
    }

    // A new generation: results of tasks that are still running are not taken over any more.
    private fun cancelAll() {
        generation++
        states.values.forEach { it.future?.cancel(true) }
    }

    private fun resetLoading() {
        cancelAll()
        states.clear()
    }

    private fun rebuild() {
        val newFolder = folder.get()
        if (newFolder !== shownFolder) {
            resetLoading()
            shownFolder = newFolder
        }

        val newEntries = newFolder?.children?.filterIsInstance<VirtualEntry>() ?: emptyList()
        val current: MutableSet<VirtualEntry> = Collections.newSetFromMap(IdentityHashMap())
        current.addAll(newEntries)
        states.entries.removeIf { (entry, state) ->
            (entry !in current).also { if (it) state.future?.cancel(true) }
        }

        val selected = selectedFileWrapper.get()
        val index = if (selected == null) -1 else newEntries.indexOfFirst { it === selected }
        if (selected != null && index < 0) selectedFileWrapper.set(null)

        entries = newEntries
        indexToSelect = index
        revisionWrapper.set(revisionWrapper.get() + 1)
    }

    // The larger file keeps the preview sharp on a HiDPI screen, where it is scaled down to the display size.
    private fun loadSize(): Int = if (Screen.getPrimary().outputScaleX > 1.0) HocalIcons.PREVIEW_SIZES.last() else HocalIcons.PREVIEW_SIZES.first()

    private fun startLoading(entry: VirtualEntry, state: ProjectFilePreview) {
        state.started = true
        val source = mediaSource.get() ?: return
        val executor = pool() ?: return
        val size = loadSize()
        val loadGeneration = generation
        val reference = entry.reference
        val registry = registry
        val loadId = state.loadId
        // False as soon as a newer loading of the entry started, the folder changed or the list left the screen.
        val isCurrent = { loadGeneration == generation && state.loadId == loadId }

        try {
            state.future = executor.submit {
                try {
                    val count = try { source.mediaCount(reference) } catch (_: Exception) { 0 }
                    if (count <= 0 || !isCurrent()) return@submit

                    val original = loadThumbnail(registry, source, reference, 0, count, size, isCurrent)
                    if (count == 1) {
                        // A single medium is original and current at once, so it is loaded once.
                        publish(entry, state, isCurrent) {
                            state.setOriginal(original)
                            state.setCurrent(original)
                        }
                        return@submit
                    }
                    publish(entry, state, isCurrent) { state.setOriginal(original) }
                    if (!isCurrent()) return@submit
                    val current = loadThumbnail(registry, source, reference, count - 1, count, size, isCurrent)
                    publish(entry, state, isCurrent) { state.setCurrent(current) }
                } catch (_: Exception) {
                    // A broken medium never disturbs the list: the placeholder stays.
                }
            }
        } catch (_: RejectedExecutionException) {
            // The pool is closed: no preview can be loaded any more.
        }
    }

    // Asks the cache first; a thumbnail that had to be loaded is stored, unless it is stale already or failed.
    private fun loadThumbnail(
        registry: MediaPreviewRegistry,
        source: MediaSource,
        reference: String,
        number: Int,
        mediaCount: Int,
        size: Int,
        isCurrent: () -> Boolean,
    ): Image? {
        val cache = thumbnails
        val key = ThumbnailKey(reference, number, size)
        cache.get(key, mediaCount)?.let { return it }

        val image = registry.load(source, reference, number, size)
        if (image != null && isCurrent()) cache.put(key, mediaCount, image)
        return image
    }

    private fun publish(entry: VirtualEntry, state: ProjectFilePreview, isCurrent: () -> Boolean, apply: () -> Unit) {
        Platform.runLater {
            if (observing && isCurrent() && states[entry] === state) apply()
        }
    }

    // The pool of the current size, created on demand; none once the view model is closed.
    private fun pool(): ExecutorService? {
        if (closed) return null
        return pool ?: createPool().also { pool = it }
    }

    private fun createPool(): ExecutorService {
        val counter = AtomicInteger()
        return Executors.newFixedThreadPool(loadThreads) { task ->
            Thread(task, "hocal-preview-${counter.incrementAndGet()}").apply { isDaemon = true }
        }
    }

    companion object {
        /** The number of threads that load thumbnails when no other number is set. */
        const val DEFAULT_LOAD_THREADS: Int = 5
    }
}
