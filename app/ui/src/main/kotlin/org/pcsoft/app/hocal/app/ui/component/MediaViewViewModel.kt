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
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.image.Image
import org.pcsoft.app.hocal.app.ui.AppearanceObserver
import org.pcsoft.app.hocal.app.ui.preview.LoadHandle
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.app.ui.preview.ThumbnailLoader

/**
 * View model of the media view.
 *
 * It holds the medium to show and the source to load it from; both are bound by the caller. The image is
 * loaded in the background, a change of the medium cancels the running load and drops its late result. The
 * view shows [image] while there is one, and the placeholder while [showsPlaceholder] is `true`.
 *
 * [medium] and [mediaSource] are observed only between [onShow] and [onHide], as is the appearance.
 */
class MediaViewViewModel : ViewModel {
    private val imageWrapper = ReadOnlyObjectWrapper<Image?>(this, "image", null)
    private val placeholderWrapper = ReadOnlyBooleanWrapper(this, "showsPlaceholder", false)
    private val loader = ThumbnailLoader()

    private var observing = false
    private var handle: LoadHandle? = null

    // Fields: a method reference would be a new object per use and could not be removed again.
    private val mediumListener = InvalidationListener {
        // Reading again is a must: a property reports the next change only after its value was read.
        medium.get()
        load()
    }
    private val sourceListener = InvalidationListener {
        mediaSource.get()
        // Another source means another project: its images must not be taken for those of the former one.
        loader.clear()
        load()
    }

    /** The medium to show, `null` while there is none. */
    val medium: ObjectProperty<HistoryMedium?> = SimpleObjectProperty(this, "medium")

    /** The source the medium is loaded from. */
    val mediaSource: ObjectProperty<MediaSource?> = SimpleObjectProperty(this, "mediaSource")

    /** Follows the appearance of the system for the placeholder icon, see [onShow] and [onHide]. */
    val appearance: AppearanceObserver = AppearanceObserver()

    /** The loaded image; `null` while there is no medium, the image still loads, or it cannot be shown. */
    val image: ReadOnlyObjectProperty<Image?> = imageWrapper.readOnlyProperty

    /** `true` if the medium cannot be shown, so the placeholder icon takes the place of the image. */
    val showsPlaceholder: ReadOnlyBooleanProperty = placeholderWrapper.readOnlyProperty

    /**
     * Starts observing the medium, the source and the appearance, and loads the current medium.
     * Calling it twice registers only once.
     */
    fun onShow() {
        if (!observing) {
            observing = true
            medium.addListener(mediumListener)
            mediaSource.addListener(sourceListener)
        }
        appearance.start()
        load()
    }

    /** Stops observing and cancels a running load. Calling it twice is harmless. */
    fun onHide() {
        if (observing) {
            observing = false
            medium.removeListener(mediumListener)
            mediaSource.removeListener(sourceListener)
        }
        appearance.stop()
        cancel()
    }

    /**
     * Tells that the media of a reference changed: the cache of the reference is dropped and, if the shown
     * medium belongs to it and the view is on screen, the medium is loaded anew. May be called from any thread.
     *
     * @param reference UUID of the reference whose media changed
     */
    fun invalidate(reference: String) {
        loader.invalidate(reference)
        val reload = { if (observing && medium.get()?.reference == reference) load() }
        if (Platform.isFxApplicationThread()) reload() else Platform.runLater(reload)
    }

    /** Ends the load thread of this view model; the view model is not used afterwards. */
    fun close() {
        cancel()
        loader.close()
    }

    private fun cancel() {
        handle?.cancel()
        handle = null
    }

    private fun load() {
        cancel()
        imageWrapper.set(null)
        placeholderWrapper.set(false)
        val current = medium.get() ?: return
        val source = mediaSource.get() ?: return
        handle = loader.load(source, current.reference, current.number, LOAD_SIZE) { loaded ->
            imageWrapper.set(loaded)
            placeholderWrapper.set(loaded == null)
        }
    }

    companion object {
        /** The longest edge in pixels an image is decoded with; the view scales it to the available space. */
        const val LOAD_SIZE: Int = 1024
    }
}
