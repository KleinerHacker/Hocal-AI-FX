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
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.collections.ObservableList
import javafx.scene.layout.BorderPane

/**
 * Shows the flows the user can run as a list and reports which one is picked.
 *
 * The component owns no data of its own. [bindFlows] hands it the list of flow names to show, and
 * [selectedFlow] reports every change of the selection back, so the surrounding window decides what
 * to do with it.
 */
class FlowList : BorderPane() {

    private val viewModel: FlowListViewModel

    init {
        FluentViewLoader.fxmlView(FlowListView::class.java).let {
            it.root(this)
            it.load().let {
                viewModel = it.viewModel
            }
        }
    }

    /**
     * Hands the flow names to show to the list.
     *
     * The list follows [flows] - an entry added, removed or replaced there shows up here as well.
     *
     * @param flows the flow names of the surrounding window
     */
    fun bindFlows(flows: ObservableList<String>) = viewModel.bind(flows)

    /** The flow the user picked in the list, absent while nothing is selected. */
    val selectedFlow: ReadOnlyObjectProperty<String?> by viewModel::selectedFlow
}
