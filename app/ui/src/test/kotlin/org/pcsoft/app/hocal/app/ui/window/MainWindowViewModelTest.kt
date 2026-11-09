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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.Messages
import java.text.MessageFormat

/**
 * Developer tests for [MainWindowViewModel].
 *
 * Expected texts are read through [Messages], so the tests hold under every locale the build runs in.
 */
class MainWindowViewModelTest {

    /**
     * Use case: the window opens with the example flows and no pick, so the content area asks the
     * user to choose a flow.
     */
    @Test
    fun startsWithTheExampleFlowsAndAHint() {
        val viewModel = MainWindowViewModel()

        assertEquals(
            listOf(
                Messages["window.main.flow.textToImage"],
                Messages["window.main.flow.imageToImage"],
                Messages["window.main.flow.upscale"]
            ),
            viewModel.flows.toList()
        )
        assertNull(viewModel.selectedFlow.value)
        assertEquals(Messages["window.main.selection.none"], viewModel.selectionText.get())
    }

    /**
     * Use case: the user picks a flow, so the content area names it; dropping the pick brings the hint
     * back.
     */
    @Test
    fun selectionTextFollowsThePickedFlow() {
        val viewModel = MainWindowViewModel()

        viewModel.selectedFlow.value = "Upscale"
        assertEquals(
            MessageFormat.format(Messages["window.main.selection.selected"], "Upscale"),
            viewModel.selectionText.get()
        )

        viewModel.selectedFlow.value = null
        assertEquals(Messages["window.main.selection.none"], viewModel.selectionText.get())
    }
}
