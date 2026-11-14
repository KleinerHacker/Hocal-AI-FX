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

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.beans.binding.BooleanBinding
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ListView
import org.pcsoft.app.hocal.app.ui.showingBinding
import java.net.URL
import java.util.ResourceBundle

/**
 * View of the history list, living in the list control itself (`fx:root`).
 *
 * It shows the media of the view model, reports the selection of the user back to it and ties the
 * observation of the model to the screen state of the component.
 */
class HistoryListView : FxmlView<HistoryListViewModel>, Initializable {
    @FXML
    private lateinit var pnlRoot: ListView<HistoryMedium>

    @InjectViewModel
    private lateinit var viewModel: HistoryListViewModel

    // Kept as a field: a garbage collected binding stops notifying, and the registration would stay.
    private lateinit var showing: BooleanBinding

    // True while the view itself changes the selection, so that it is not reported back as a user choice.
    private var syncing = false

    /**
     * Initializes the view after the FXML file has been loaded.
     *
     * Sets the cell factory, follows the media and the selection, and registers the view model only
     * while the component is on screen.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        pnlRoot.setCellFactory { HistoryListCell({ viewModel.appearance.dark.get() }) { medium, onLoaded -> viewModel.load(medium, onLoaded) } }

        // The placeholder icons follow the appearance of the system, so the rows are drawn again when it changes.
        viewModel.appearance.dark.addListener { _, _, _ -> pnlRoot.refresh() }
        // The revision changes on every rebuild, even if the media stay equal by value.
        viewModel.revision.addListener { _, _, _ -> applyMedia() }
        applyMedia()

        pnlRoot.selectionModel.selectedItemProperty().addListener { _, _, item ->
            if (!syncing) viewModel.selectMedium(item)
        }

        showing = pnlRoot.showingBinding()
        showing.addListener { _, _, onScreen -> if (onScreen) viewModel.onShow() else viewModel.onHide() }
    }

    /** Shows the media of the view model and restores the selection it asks for. */
    private fun applyMedia() {
        syncing = true
        try {
            pnlRoot.items.setAll(viewModel.entries)
            pnlRoot.selectionModel.clearSelection()
            if (viewModel.indexToSelect >= 0) {
                pnlRoot.selectionModel.select(viewModel.indexToSelect)
                pnlRoot.scrollTo(viewModel.indexToSelect)
            }
            pnlRoot.refresh()
        } finally {
            syncing = false
        }
    }
}
