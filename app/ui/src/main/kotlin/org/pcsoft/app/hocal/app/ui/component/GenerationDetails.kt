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
import javafx.scene.layout.VBox
import org.pcsoft.app.hocal.app.ui.preview.MediaSource

/**
 * The generation details of a medium: in the first line its model and flow, in the second the beginning of
 * its prompt, cut with `...` where the panel is too narrow.
 *
 * The component holds no project data of its own: the properties are bound by the caller. Without a medium,
 * or for a medium without details, the lines are empty. Additional images used for a generation are not
 * part of the details and not shown. The details are read only while the component is on screen.
 *
 * The content is defined in `GenerationDetailsView.fxml`, which is loaded with this node as its root.
 */
class GenerationDetails : VBox() {
    /** The view model, reachable for the tests of the module. */
    internal val viewModel: GenerationDetailsViewModel

    init {
        FluentViewLoader.fxmlView(GenerationDetailsView::class.java).root(this).load().apply {
            this@GenerationDetails.viewModel = viewModel
        }
    }

    /**
     * The medium whose details are shown.
     *
     * @return the property of the medium, `null` for none
     */
    fun mediumProperty(): ObjectProperty<HistoryMedium?> = viewModel.medium

    /**
     * The source the details are read from.
     *
     * @return the property of the media source
     */
    fun mediaSourceProperty(): ObjectProperty<MediaSource?> = viewModel.mediaSource

    /**
     * Tells that the media of a reference changed: if the shown medium belongs to it, its details are read anew.
     *
     * @param reference UUID of the reference whose media changed
     */
    fun invalidate(reference: String) {
        viewModel.invalidate(reference)
    }
}
