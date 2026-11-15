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
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyIntegerProperty
import javafx.beans.property.ReadOnlyIntegerWrapper
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.image.Image
import javafx.stage.Screen
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.ui.AppearanceObserver
import org.pcsoft.app.hocal.app.ui.preview.LoadHandle
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.app.ui.preview.ThumbnailLoader
import org.pcsoft.app.hocal.model.project.VirtualEntry

/**
 * View model of the history list.
 *
 * It lists all media of [file] from number 0 to the last one and loads their previews from [mediaSource]
 * in the background, lazily: loading starts when a cell asks to [load] the preview of a medium.
 *
 * The file is compared by object identity, because an entry compares by value. The selection follows these
 * rules on every rebuild: another file, or a history that grew, selects the last medium; an unchanged
 * count keeps the choice of the user; a history that shrank keeps it as long as its number still exists
 * and selects the last medium otherwise.
 *
 * The file, the media source and the appearance of the system are observed only between [onShow] and
 * [onHide]. A load result is taken over only if its generation is still current; [onHide] starts a new
 * generation, so running loads are not delivered any more.
 */
class HistoryListViewModel : ViewModel {
    private val selectedMediumWrapper = ReadOnlyObjectWrapper<HistoryMedium?>(this, "selectedMedium")
    private val revisionWrapper = ReadOnlyIntegerWrapper(this, "revision", 0)

    private var observing = false
    private var shownFile: VirtualEntry? = null
    private var shownCount = 0

    // Read by the callbacks of the loader on the FX thread, written on the FX thread only.
    private var generation = 0

    private val loader = ThumbnailLoader()

    // Fields: a method reference would be a new object per use and could not be removed again.
    private val fileListener = InvalidationListener { onFileInvalidated() }
    private val sourceListener = InvalidationListener { onSourceInvalidated() }

    /**
     * The file whose media are shown, `null` for an empty list.
     *
     * The caller binds it from outside; a new binding replaces the former one.
     */
    val file: ObjectProperty<VirtualEntry?> = SimpleObjectProperty(this, "file")

    /** The source the previews are loaded from, `null` if nothing can be loaded; placeholder icons are shown then. */
    val mediaSource: ObjectProperty<MediaSource?> = SimpleObjectProperty(this, "mediaSource")

    /** Follows the light or dark appearance of the system, observed only between [onShow] and [onHide]. */
    val appearance: AppearanceObserver = AppearanceObserver()

    /** The medium that is selected, or `null` while none is selected. */
    val selectedMedium: ReadOnlyObjectProperty<HistoryMedium?> = selectedMediumWrapper.readOnlyProperty

    /** Counts the rebuilds of the list, so that the view notices a rebuild even if the media stay equal. */
    val revision: ReadOnlyIntegerProperty = revisionWrapper.readOnlyProperty

    /** The media to show, in the order of their number. */
    var entries: List<HistoryMedium> = emptyList()
        private set

    /** The index in [entries] of the selected medium, set by every rebuild before [revision] changes, or -1. */
    var indexToSelect: Int = -1
        private set

    /**
     * Reports the medium the user selected.
     *
     * @param medium the selected medium, or `null` for no selection
     */
    fun selectMedium(medium: HistoryMedium?) {
        selectedMediumWrapper.set(medium)
    }

    /**
     * Starts loading the preview of a medium, if the view model is observing and a source exists.
     *
     * Called on the FX thread by the cell that shows the medium. The result is delivered on the FX thread,
     * with `null` if the medium has no preview; it is never delivered after the handle was cancelled or after
     * [onHide].
     *
     * @param medium a medium of [entries]
     * @param onLoaded receives the preview, or `null` if there is none
     * @return the handle to cancel the loading, or `null` if nothing is loaded
     */
    fun load(medium: HistoryMedium, onLoaded: (Image?) -> Unit): LoadHandle? {
        if (!observing) return null
        val source = mediaSource.get() ?: return null
        val loadGeneration = generation
        return loader.load(source, medium.reference, medium.number, loadSize()) { image ->
            if (observing && loadGeneration == generation) onLoaded(image)
        }
    }

    /**
     * Tells that the media of a reference changed: the previews of the reference are dropped from the cache
     * and, while observing, the list is rebuilt, which loads the previews of the rows anew.
     *
     * May be called from any thread. While the component is not on screen only the cache is dropped; the
     * list is rebuilt when it is shown again.
     *
     * @param reference UUID of the reference whose media changed
     */
    fun invalidate(reference: String) {
        loader.invalidate(reference)
        onFx {
            // Once more: a load that was running while the media changed may have stored a stale image.
            loader.invalidate(reference)
            rebuild()
        }
    }

    /**
     * Starts observing the file, the source and the appearance, and builds the list anew.
     *
     * Called when the component comes on screen. Calling it twice registers only once.
     */
    fun onShow() {
        if (!observing) {
            observing = true
            file.addListener(fileListener)
            mediaSource.addListener(sourceListener)
        }
        appearance.start()
        // Reading validates the properties, so that their next change is reported.
        file.get()
        adoptSource()
        generation++
        rebuild()
    }

    /**
     * Stops observing and cancels the running loads. The list stays as it is.
     *
     * Called when the component leaves the screen. Calling it twice is harmless.
     */
    fun onHide() {
        if (observing) {
            observing = false
            file.removeListener(fileListener)
            mediaSource.removeListener(sourceListener)
        }
        appearance.stop()
        generation++
    }

    /**
     * Cancels all loading and ends the thread pool of the previews for good.
     *
     * To be called when the view model is not used any more; the previews cannot be loaded afterwards.
     */
    fun close() {
        onHide()
        loader.close()
    }

    private var shownSource: MediaSource? = null

    // Another source means another project, so its thumbnails must not be taken for those of the former one.
    private fun adoptSource() {
        val source = mediaSource.get()
        if (source !== shownSource) {
            loader.clear()
            shownSource = source
        }
    }

    private fun onFileInvalidated() {
        // Reading again is a must: a property reports the next change only after its value was read.
        file.get()
        onFx { rebuild() }
    }

    private fun onSourceInvalidated() {
        mediaSource.get()
        onFx {
            adoptSource()
            generation++
            rebuild()
        }
    }

    private fun onFx(action: () -> Unit) {
        if (Platform.isFxApplicationThread()) {
            if (observing) action()
        } else {
            Platform.runLater { if (observing) action() }
        }
    }

    private fun mediaCountOf(entry: VirtualEntry?, source: MediaSource?): Int {
        if (entry == null || source == null) return 0
        return try {
            source.mediaCount(entry.reference)
        } catch (_: Exception) {
            0
        }
    }

    private fun rebuild() {
        val entry = file.get()
        val count = mediaCountOf(entry, mediaSource.get())
        // By identity: entries compare by value, so an equal entry of another file must count as a change.
        val fileChanged = entry !== shownFile

        val media = if (entry == null) emptyList() else List(count) { HistoryMedium(entry.reference, it) }
        val selected = selectedMediumWrapper.get()
        val index = when {
            media.isEmpty() -> -1
            fileChanged || count > shownCount -> media.lastIndex
            else -> media.indexOf(selected).takeIf { it >= 0 } ?: media.lastIndex
        }

        shownFile = entry
        shownCount = count
        entries = media
        indexToSelect = index
        selectedMediumWrapper.set(media.getOrNull(index))
        revisionWrapper.set(revisionWrapper.get() + 1)
    }

    // The larger file keeps the preview sharp on a HiDPI screen, where it is scaled down to the display size.
    private fun loadSize(): Int =
        if (Screen.getPrimary().outputScaleX > 1.0) HocalIcons.PREVIEW_SIZES.last() else HocalIcons.PREVIEW_SIZES.first()
}
