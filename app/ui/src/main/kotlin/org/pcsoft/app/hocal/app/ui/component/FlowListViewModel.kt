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
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * View model of [FlowList].
 *
 * The list of flow names is the single input of the component and is handed over through [bind]; it
 * is taken as the list itself and not copied, so the component never owns a state of its own.
 * [selectedFlow] is the single output and reports which flow the user picked.
 */
class FlowListViewModel : ViewModel {

    private val flowsWrapper: ReadOnlyListWrapper<String> =
        ReadOnlyListWrapper(this, "flows", FXCollections.observableArrayList())

    private val selectedFlowWrapper: ReadOnlyObjectWrapper<String?> =
        ReadOnlyObjectWrapper(this, "selectedFlow", null)

    /** Flow names the list shows, empty until [bind] was called. */
    val flows: ReadOnlyListProperty<String> get() = flowsWrapper.readOnlyProperty

    /** The flow the user picked in the list, absent while nothing is selected. */
    val selectedFlow: ReadOnlyObjectProperty<String?> get() = selectedFlowWrapper.readOnlyProperty

    /**
     * Lets the list follow [flows] instead of the list it followed before.
     *
     * The selection points into the list that was just replaced, so it is cleared.
     *
     * @param flows the flow names of the surrounding window
     */
    internal fun bind(flows: ObservableList<String>) {
        flowsWrapper.set(flows)
        selectedFlowWrapper.value = null
    }

    /**
     * Takes over the flow the user picked in the list.
     *
     * Called by [FlowListView] only; the outside world reads [selectedFlow].
     *
     * @param flow the picked flow, `null` when the selection was cleared
     */
    internal fun select(flow: String?) {
        selectedFlowWrapper.value = flow
    }
}
