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

package org.pcsoft.app.hocal.app.ui.window

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import org.pcsoft.app.hocal.app.ui.component.FlowList
import java.net.URL
import java.util.ResourceBundle

/**
 * View of the application main window, holding the menu bar, the flow list and the content area.
 *
 * Texts come from the global resource bundle via the `%key` syntax of the FXML file.
 */
class MainWindowView : FxmlView<MainWindowViewModel>, Initializable {
    @FXML
    private lateinit var pnlRoot: BorderPane
    @FXML
    private lateinit var flowList: FlowList
    @FXML
    private lateinit var lblSelection: Label

    @InjectViewModel
    private lateinit var viewModel: MainWindowViewModel

    /**
     * Initializes the view after the FXML file has been loaded.
     *
     * Hands the flows of the window to the flow list, follows the flow picked there and shows the
     * text for the current selection.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        flowList.bindFlows(viewModel.flows)
        viewModel.selectedFlow.bind(flowList.selectedFlow)

        lblSelection.textProperty().bind(viewModel.selectionText)
    }

    /**
     * Exits the application by hiding the main window.
     *
     * This action is triggered by the Exit menu item. The window is hidden rather than closed
     * to allow for proper cleanup and potential "are you sure" dialogs in the future.
     */
    @FXML
    private fun actionExit() {
        pnlRoot.scene.window.hide()
    }
}
