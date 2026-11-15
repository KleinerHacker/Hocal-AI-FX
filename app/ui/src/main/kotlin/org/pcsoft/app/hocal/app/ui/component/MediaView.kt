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

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.property.ObjectProperty
import javafx.scene.layout.StackPane
import org.pcsoft.app.hocal.app.ui.preview.MediaSource

/**
 * The image view of the chosen medium: it shows the medium as an image that fills the available space.
 *
 * The component holds no project data of its own: the properties are bound by the caller. Without a
 * medium or a source the area stays empty; a medium that cannot be shown is replaced by a placeholder icon.
 * Loading happens in the background, and only while the component is on screen.
 *
 * The content is defined in `MediaViewView.fxml`, which is loaded with this node as its root.
 */
class MediaView : StackPane() {
    private val viewModel: MediaViewViewModel

    init {
        FluentViewLoader.fxmlView(MediaViewView::class.java).root(this).load().apply {
            this@MediaView.viewModel = viewModel
        }
    }

    /**
     * The medium to show.
     *
     * @return the property of the medium, `null` for none
     */
    fun mediumProperty(): ObjectProperty<HistoryMedium?> = viewModel.medium

    /**
     * The source the medium is loaded from; another source empties the cache and loads again.
     *
     * @return the property of the media source
     */
    fun mediaSourceProperty(): ObjectProperty<MediaSource?> = viewModel.mediaSource

    /**
     * Tells that the media of a reference changed: if the shown medium belongs to it, it is loaded anew.
     *
     * @param reference UUID of the reference whose media changed
     */
    fun invalidate(reference: String) {
        viewModel.invalidate(reference)
    }

    /** Ends the load thread of the component; call it when the component is not used any more. */
    fun close() {
        viewModel.close()
    }
}
