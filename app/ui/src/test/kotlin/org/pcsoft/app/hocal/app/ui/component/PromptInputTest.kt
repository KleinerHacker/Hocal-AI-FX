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
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.controlsfx.control.SegmentedButton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.Messages
import org.pcsoft.app.hocal.app.ui.preview.FakeMediaSource
import org.pcsoft.app.hocal.app.ui.preview.pngBytes
import org.pcsoft.app.hocal.app.ui.prompt.FlowOption
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Tests of the prompt input component on real controls in a headless window.
 */
class PromptInputTest : ApplicationTest() {

    private lateinit var stage: Stage
    private lateinit var pane: StackPane
    private lateinit var input: PromptInput

    override fun start(stage: Stage) {
        this.stage = stage
        input = PromptInput()
        pane = StackPane(input)
        stage.scene = Scene(pane, 600.0, 400.0)
        stage.show()
    }

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    private fun <T : javafx.scene.Node> find(id: String): T = lookup(id).query()

    private val btnAdd: Button get() = find("#btnAdd")
    private val btnSend: Button get() = find("#btnSend")
    private val txtPrompt: TextArea get() = find("#txtPrompt")
    private val segFlows: SegmentedButton get() = find("#segFlows")

    @Suppress("UNCHECKED_CAST")
    private val cmbModel: ComboBox<Any> get() = find<ComboBox<*>>("#cmbModel") as ComboBox<Any>

    private fun chips(): List<PromptImageChip> {
        WaitForAsyncUtils.waitForFxEvents()
        return lookup(".prompt-chip").queryAll<PromptImageChip>().toList()
    }

    private fun galleryReturns(name: String, reference: String, source: FakeMediaSource? = null) {
        interact {
            input.mediaSourceProperty().set(source)
            input.galleryOpener = { _, _, _ -> entry(name).apply { this.reference = reference } }
        }
    }

    /**
     * Use case: the component is shown. Every control the user works with exists: the add button, the
     * model box, the flow bar, the parameter placeholder, the prompt area and the send button.
     */
    @Test
    fun allControlsArePresent() {
        assertNotNull(btnAdd)
        assertNotNull(cmbModel)
        assertNotNull(segFlows)
        assertNotNull(find<Region>("#pnlParameters"))
        assertNotNull(txtPrompt)
        assertNotNull(btnSend)
        assertNotNull(find<javafx.scene.Node>("#pnlChips"))
    }

    /**
     * Use case: the add and the send button show an icon only. Each has an image as graphic and no text,
     * and its tooltip and accessible text carry the message text.
     */
    @Test
    fun buttonsShowIconsOnlyWithTooltips() {
        listOf(btnAdd to "component.promptInput.add.tooltip", btnSend to "component.promptInput.send.tooltip").forEach { (button, key) ->
            assertTrue(button.graphic is ImageView, "the graphic must be an icon")
            assertTrue(button.text.isNullOrEmpty(), "the button must not show text")
            assertEquals(Messages[key], button.tooltip.text)
            assertEquals(Messages[key], button.accessibleText)
        }
        assertEquals(Messages["component.promptInput.prompt.hint"], txtPrompt.promptText)
        assertEquals(Messages["component.promptInput.model.prompt"], cmbModel.promptText)
    }

    /**
     * Use case: the text of the prompt travels in both directions. Text written into the area stands in
     * the property of the component, and text written into the property stands in the area.
     */
    @Test
    fun promptTextIsBoundInBothDirections() {
        interact { txtPrompt.text = "A red fox" }
        assertEquals("A red fox", input.promptProperty().get())

        interact { input.promptProperty().set("A blue whale") }
        assertEquals("A blue whale", txtPrompt.text)
    }

    /**
     * Use case: the user types into the prompt area. The send button is disabled before and enabled
     * after the first characters; deleting the text disables it again.
     */
    @Test
    fun sendButtonActivatesWhileTyping() {
        assertTrue(btnSend.isDisable)

        clickOn(txtPrompt).write("Fox")
        assertFalse(btnSend.isDisable)

        interact { txtPrompt.text = "  " }
        assertTrue(btnSend.isDisable)
    }

    /**
     * Use case: the user clicks the send button of a non-empty prompt. The send handler is called once.
     */
    @Test
    fun clickOnSendCallsTheHandler() {
        var calls = 0
        interact {
            input.sendHandler = { calls++ }
            input.promptProperty().set("A red fox")
        }

        clickOn(btnSend)

        assertEquals(1, calls)
    }

    /**
     * Use case: the user clicks the add button and the gallery returns an entry. A chip with the name of
     * the entry appears, and its preview shows the image after loading; the folder and the source of the
     * caller reach the opener.
     */
    @Test
    fun addingAnImageShowsAChipWithPreview() {
        val source = FakeMediaSource().apply { add("r1", "png" to pngBytes(60, 60)) }
        var seenSource: Any? = null
        interact {
            input.mediaSourceProperty().set(source)
            input.galleryOpener = { _, _, mediaSource ->
                seenSource = mediaSource
                entry("Sunrise.png").apply { reference = "r1" }
            }
        }

        clickOn(btnAdd)

        val chip = chips().single()
        assertEquals("Sunrise.png", chip.nameLabel.text)
        assertSame(source, seenSource)
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { fx { chip.previewView.image } != null })
        assertEquals(listOf(PromptImage("r1", "Sunrise.png")), input.imagesProperty().toList())
    }

    /**
     * Use case: the gallery returns the same reference twice. Only one chip exists.
     */
    @Test
    fun duplicateImageShowsOneChip() {
        galleryReturns("Sunrise.png", "r1")

        clickOn(btnAdd)
        clickOn(btnAdd)

        assertEquals(1, chips().size)
    }

    /**
     * Use case: the user cancels the gallery dialog. No chip appears.
     */
    @Test
    fun cancelledGalleryShowsNoChip() {
        interact { input.galleryOpener = { _, _, _ -> null } }

        clickOn(btnAdd)

        assertTrue(chips().isEmpty())
    }

    /**
     * Use case: the user clicks the remove button of a chip. The chip disappears and the image leaves
     * the list; the remove button shows an icon and a tooltip.
     */
    @Test
    fun removeButtonRemovesTheChip() {
        galleryReturns("Sunrise.png", "r1")
        clickOn(btnAdd)
        val remove = chips().single().removeButton
        assertTrue(remove.graphic is ImageView)
        assertEquals(Messages["component.promptInput.remove.tooltip"], remove.tooltip.text)

        clickOn(remove)

        assertTrue(chips().isEmpty())
        assertTrue(input.imagesProperty().isEmpty())
    }

    /**
     * Use case: the user chooses another model in the model box. The flow bar shows the flows of that
     * model, and the property of the chosen model follows.
     */
    @Test
    fun modelBoxChangesTheFlowBar() {
        assertEquals(listOf("Text to Image", "Image to Image", "Upscale"), segFlows.buttons.map { it.text })

        interact { cmbModel.selectionModel.select(2) }

        assertEquals(listOf("Text to Image"), segFlows.buttons.map { it.text })
        assertEquals("Placeholder model 3", input.selectedModelProperty().get()?.name)
        assertTrue(segFlows.buttons.single().isSelected)
    }

    /**
     * Use case: the caller chooses a model and a flow through the properties. The model box shows the
     * model, the flow bar selects the matching toggle, and only that one.
     */
    @Test
    fun propertiesChangeTheControls() {
        val model = input.modelProvider.models()[1]

        interact {
            input.selectedModelProperty().set(model)
            input.selectedFlowProperty().set(FlowOption("image-to-image", "Image to Image"))
        }

        assertEquals(model, cmbModel.value)
        assertEquals(listOf(false, true), segFlows.buttons.map { it.isSelected })
    }

    /**
     * Use case: the user clicks a toggle of the flow bar. The property of the chosen flow follows.
     */
    @Test
    fun clickOnAToggleChoosesTheFlow() {
        clickOn(segFlows.buttons[2])

        assertEquals("Upscale", input.selectedFlowProperty().get()?.name)
    }

    /**
     * Use case: the component is put on and off the screen in the three ways - set invisible, removed
     * from the scene, window hidden. The appearance is observed exactly while it is on screen, and
     * observing is restored each time it returns.
     */
    @Test
    fun appearanceIsObservedOnlyWhileOnScreen() {
        val root = VBox()
        val tuple = FluentViewLoader.fxmlView(PromptInputView::class.java).root(root).load()
        val viewModel = tuple.viewModel
        assertEquals(0, viewModel.appearanceRegistrations)
        interact { pane.children.add(root) }
        assertEquals(1, viewModel.appearanceRegistrations)

        interact { root.isVisible = false }
        assertEquals(0, viewModel.appearanceRegistrations, "invisible")
        interact { root.isVisible = true }
        assertEquals(1, viewModel.appearanceRegistrations)

        interact { pane.children.remove(root) }
        assertEquals(0, viewModel.appearanceRegistrations, "removed from the scene")
        interact { pane.children.add(root) }
        assertEquals(1, viewModel.appearanceRegistrations)

        interact { stage.hide() }
        assertEquals(0, viewModel.appearanceRegistrations, "window hidden")
        interact { stage.show() }
        assertEquals(1, viewModel.appearanceRegistrations)
    }
}
