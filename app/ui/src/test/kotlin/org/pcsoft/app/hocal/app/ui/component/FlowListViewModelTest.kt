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

import javafx.collections.FXCollections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests for [FlowListViewModel].
 */
class FlowListViewModelTest {

    /**
     * Use case: a freshly created component shows no flows and reports no selection until the window
     * hands its list over.
     */
    @Test
    fun startsEmptyWithoutSelection() {
        val viewModel = FlowListViewModel()

        assertTrue(viewModel.flows.isEmpty())
        assertNull(viewModel.selectedFlow.value)
    }

    /**
     * Use case: the list handed over is taken as it is, not copied, so a flow the window adds later
     * shows up without another hand-over.
     */
    @Test
    fun bindTakesTheListItself() {
        val viewModel = FlowListViewModel()
        val flows = FXCollections.observableArrayList("Text to Image")

        viewModel.bind(flows)
        flows += "Upscale"

        assertSame(flows, viewModel.flows.get())
        assertEquals(listOf("Text to Image", "Upscale"), viewModel.flows.toList())
    }

    /**
     * Use case: the flow the user picks is reported as the selection, and clearing the pick reports
     * that nothing is selected any more.
     */
    @Test
    fun selectReportsThePickedFlow() {
        val viewModel = FlowListViewModel()
        viewModel.bind(FXCollections.observableArrayList("Text to Image", "Upscale"))

        viewModel.select("Upscale")
        assertEquals("Upscale", viewModel.selectedFlow.value)

        viewModel.select(null)
        assertNull(viewModel.selectedFlow.value)
    }

    /**
     * Use case: the window hands over another list of flows, so a pick from the old list is no longer
     * reported as the selection.
     */
    @Test
    fun bindingAnotherListClearsTheSelection() {
        val viewModel = FlowListViewModel()
        viewModel.bind(FXCollections.observableArrayList("Text to Image"))
        viewModel.select("Text to Image")

        viewModel.bind(FXCollections.observableArrayList("Upscale"))

        assertNull(viewModel.selectedFlow.value)
        assertEquals(listOf("Upscale"), viewModel.flows.toList())
    }
}
