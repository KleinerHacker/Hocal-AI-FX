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
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import org.pcsoft.app.hocal.app.Messages
import org.pcsoft.app.hocal.app.ui.showingBinding
import org.pcsoft.app.hocal.model.project.VirtualFolder
import java.net.URL
import java.util.ResourceBundle

/**
 * View of the project tree, living in the tree control itself (`fx:root`).
 *
 * It shows the root item of the view model, reports the selection of the user back to it and ties the
 * observation of the project model to the screen state of the component.
 */
class ProjectTreeView : FxmlView<ProjectTreeViewModel>, Initializable {
    @FXML
    private lateinit var pnlRoot: TreeView<VirtualFolder>

    @InjectViewModel
    private lateinit var viewModel: ProjectTreeViewModel

    // Kept as a field: a garbage collected binding stops notifying, and the registration would stay.
    private lateinit var showing: BooleanBinding

    // True while the view itself changes the selection, so that it is not reported back as a user choice.
    private var syncing = false

    /**
     * Initializes the view after the FXML file has been loaded.
     *
     * Sets the cell factory, follows the root item and the selection, and registers the view model at
     * the project model only while the component is on screen.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val rootText = Messages["component.projectTree.root"]
        pnlRoot.setCellFactory { ProjectTreeCell(rootText) { viewModel.dark.get() } }

        // The icons follow the appearance of the system, so the rows are drawn again when it changes.
        viewModel.dark.addListener { _, _, _ -> pnlRoot.refresh() }
        viewModel.rootItem.addListener { _, _, item -> applyRoot(item) }
        applyRoot(viewModel.rootItem.get())

        pnlRoot.selectionModel.selectedItemProperty().addListener { _, _, item ->
            if (!syncing) viewModel.selectFolder(item?.value)
        }

        showing = pnlRoot.showingBinding()
        showing.addListener { _, _, onScreen -> if (onScreen) viewModel.onShow() else viewModel.onHide() }
    }

    /** Shows the given root item and restores the selection the view model asks for. */
    private fun applyRoot(item: TreeItem<VirtualFolder>?) {
        syncing = true
        try {
            pnlRoot.root = item
            pnlRoot.selectionModel.clearSelection()
            viewModel.itemToSelect?.let { pnlRoot.selectionModel.select(it) }
            pnlRoot.refresh()
        } finally {
            syncing = false
        }
    }
}
