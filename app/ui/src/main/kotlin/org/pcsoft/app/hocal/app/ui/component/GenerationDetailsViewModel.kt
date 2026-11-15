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
import javafx.beans.property.ReadOnlyStringProperty
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.hocal.app.ui.preview.MediaSource

/**
 * View model of the generation details.
 *
 * It holds the medium and the source the details are read from; both are bound by the caller. The two lines
 * the view shows are [modelFlow] and [prompt]: both are empty while there is no medium, no source, or no
 * detail. The details are read in memory on the FX thread, so there is no background loading.
 *
 * [medium] and [mediaSource] are observed only between [onShow] and [onHide]; showing the component again
 * reads the details anew.
 */
class GenerationDetailsViewModel : ViewModel {
    private val modelFlowWrapper = ReadOnlyStringWrapper(this, "modelFlow", "")
    private val promptWrapper = ReadOnlyStringWrapper(this, "prompt", "")

    private var observing = false

    // Fields: a method reference would be a new object per use and could not be removed again.
    private val mediumListener = InvalidationListener {
        // Reading again is a must: a property reports the next change only after its value was read.
        medium.get()
        refresh()
    }
    private val sourceListener = InvalidationListener {
        mediaSource.get()
        refresh()
    }

    /** The medium whose details are shown, `null` while there is none. */
    val medium: ObjectProperty<HistoryMedium?> = SimpleObjectProperty(this, "medium")

    /** The source the details are read from. */
    val mediaSource: ObjectProperty<MediaSource?> = SimpleObjectProperty(this, "mediaSource")

    /** The first line: model and flow separated by [SEPARATOR]; only the one that exists if the other is missing. */
    val modelFlow: ReadOnlyStringProperty = modelFlowWrapper.readOnlyProperty

    /** The second line: the prompt on a single line, line breaks and runs of blanks become one blank. */
    val prompt: ReadOnlyStringProperty = promptWrapper.readOnlyProperty

    /**
     * Starts observing the medium and the source, and reads the details of the current medium.
     * Calling it twice registers only once.
     */
    fun onShow() {
        if (!observing) {
            observing = true
            medium.addListener(mediumListener)
            mediaSource.addListener(sourceListener)
        }
        refresh()
    }

    /** Stops observing. The lines stay as they are. Calling it twice is harmless. */
    fun onHide() {
        if (observing) {
            observing = false
            medium.removeListener(mediumListener)
            mediaSource.removeListener(sourceListener)
        }
    }

    /**
     * Tells that the media of a reference changed: if the shown medium belongs to it and the component is on
     * screen, the details are read anew. May be called from any thread.
     *
     * @param reference UUID of the reference whose media changed
     */
    fun invalidate(reference: String) {
        val reload = { if (observing && medium.get()?.reference == reference) refresh() }
        if (Platform.isFxApplicationThread()) reload() else Platform.runLater(reload)
    }

    private fun refresh() {
        val current = medium.get()
        val source = mediaSource.get()
        val details = if (current == null || source == null) {
            null
        } else {
            try {
                source.details(current.reference, current.number)
            } catch (_: Exception) {
                null
            }
        }
        modelFlowWrapper.set(listOfNotNull(details?.model, details?.flow).joinToString(SEPARATOR))
        promptWrapper.set(details?.prompt?.trim()?.replace(WHITESPACE, " ") ?: "")
    }

    companion object {
        /** Separates model and flow in the first line. */
        const val SEPARATOR: String = " · "

        private val WHITESPACE = Regex("\\s+")
    }
}
