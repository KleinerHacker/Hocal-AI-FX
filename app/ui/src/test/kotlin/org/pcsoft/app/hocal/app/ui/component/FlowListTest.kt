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

import de.saxsys.mvvmfx.MvvmFX
import javafx.collections.FXCollections
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.Messages
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle

/**
 * Developer tests for [FlowList], driven through its real [ListView].
 */
class FlowListTest : ApplicationTest() {

    private lateinit var flowList: FlowList

    // The list of the surrounding window; the component follows it without a copy.
    private val flows = FXCollections.observableArrayList("Text to Image", "Image to Image", "Upscale")

    @Suppress("UNCHECKED_CAST")
    private val listView: ListView<String>
        get() = flowList.lookup(".list-view") as ListView<String>

    override fun start(stage: Stage) {
        // No fallback, so the English base bundle is used no matter which locale the build runs under.
        MvvmFX.setGlobalResourceBundle(
            ResourceBundle.getBundle(
                Messages.BUNDLE_NAME,
                Locale.ROOT,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            )
        )

        flowList = FlowList()
        flowList.bindFlows(flows)
        stage.scene = Scene(flowList, 300.0, 400.0)
        stage.show()
    }

    /**
     * Use case: the window hands its flows over, so the list shows them in the same order.
     */
    @Test
    fun listShowsTheHandedOverFlows() {
        assertEquals(listOf("Text to Image", "Image to Image", "Upscale"), listView.items.toList())
        assertNull(flowList.selectedFlow.value)
    }

    /**
     * Use case: the user clicks a flow in the list, so the component reports it as the picked flow.
     */
    @Test
    fun clickingAFlowReportsItAsSelected() {
        clickOn("Upscale")

        assertEquals("Upscale", flowList.selectedFlow.value)
    }

    /**
     * Use case: the user clears the pick in the list, so the component reports that nothing is
     * selected any more.
     */
    @Test
    fun clearingTheSelectionReportsNoFlow() {
        clickOn("Image to Image")
        interact { listView.selectionModel.clearSelection() }

        assertNull(flowList.selectedFlow.value)
    }

    /**
     * Use case: the window adds, removes and replaces flows in its list, so the shown list follows
     * every change without another hand-over.
     */
    @Test
    fun listFollowsChangesOfTheWindowList() {
        interact {
            flows += "Inpaint"
            flows.remove("Image to Image")
            flows[0] = "Text to Video"
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("Text to Video", "Upscale", "Inpaint"), listView.items.toList())
    }

    /**
     * Use case: the window hands over another list, so the old pick is dropped and the new flows are
     * shown.
     */
    @Test
    fun handingOverAnotherListClearsThePick() {
        clickOn("Upscale")

        interact { flowList.bindFlows(FXCollections.observableArrayList("Outpaint")) }

        assertEquals(listOf("Outpaint"), listView.items.toList())
        assertNull(flowList.selectedFlow.value)
    }

    /**
     * Use case: no flow is available, so the list shows a hint instead of an empty area.
     */
    @Test
    fun emptyListShowsThePlaceholder() {
        interact { flows.clear() }

        assertEquals("No flows available", (listView.placeholder as Label).text)
    }
}
