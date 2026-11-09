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
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ListView
import java.net.URL
import java.util.ResourceBundle

/**
 * View of [FlowList], showing the flow names as a list.
 *
 * The list view shows the list of the view model itself, so it follows every change of the bound
 * flows without a listener of its own.
 */
class FlowListView : FxmlView<FlowListViewModel>, Initializable {
    @FXML
    private lateinit var lstFlows: ListView<String>

    @InjectViewModel
    private lateinit var viewModel: FlowListViewModel

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        lstFlows.itemsProperty().bind(viewModel.flows)

        lstFlows.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            viewModel.select(selected)
        }
    }
}
