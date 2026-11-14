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
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.ui.preview.FakeMediaSource
import org.pcsoft.app.hocal.app.ui.preview.MediumDetails
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

/**
 * Tests of the generation details component on real controls in a headless window of 400 x 300.
 */
class GenerationDetailsViewTest : ApplicationTest() {

    private lateinit var stage: Stage
    private lateinit var pane: StackPane
    private lateinit var view: GenerationDetails

    override fun start(stage: Stage) {
        this.stage = stage
        view = GenerationDetails()
        pane = StackPane(view)
        stage.scene = Scene(pane, 400.0, 300.0)
        stage.show()
    }

    private val lblModelFlow: Label get() = lookup("#lblModelFlow").query()
    private val lblPrompt: Label get() = lookup("#lblPrompt").query()

    // What the label really draws: the clipped text, which differs from the label text if it is cut.
    private fun drawn(label: Label): String {
        WaitForAsyncUtils.waitForFxEvents()
        return label.lookupAll(".text").filterIsInstance<Text>().single().text
    }

    private fun show(source: FakeMediaSource, number: Int = 0) {
        interact {
            view.mediaSourceProperty().set(source)
            view.mediumProperty().set(HistoryMedium("ref-a", number))
        }
    }

    private fun sourceWith(prompt: String?, model: String? = "model-1", flow: String? = "flow-1") =
        FakeMediaSource().apply { addDetails("ref-a", 0, MediumDetails(prompt, model, flow)) }

    /**
     * Use case: the component is shown without a medium. Both lines are empty.
     */
    @Test
    fun emptyLinesWithoutMedium() {
        assertEquals("", lblModelFlow.text)
        assertEquals("", lblPrompt.text)
    }

    /**
     * Use case: a medium with details is chosen. The first line holds model and flow, the second the prompt;
     * both are single lines that are cut with an ellipsis.
     */
    @Test
    fun linesShowTheDetails() {
        show(sourceWith("A red fox"))

        assertEquals("model-1 · flow-1", lblModelFlow.text)
        assertEquals("A red fox", lblPrompt.text)
        assertEquals("ELLIPSIS", lblPrompt.textOverrun.name)
        assertFalse(lblPrompt.isWrapText)
    }

    /**
     * Use case: a prompt that is longer than the panel. It is cut with `...`, and a wider panel shows more of
     * it, so the length of the cut follows the width of the panel; a short prompt is not cut.
     */
    @Test
    fun longPromptIsCutWithEllipsisAtThePanelWidth() {
        val prompt = "A very long prompt that describes a red fox sitting on a hill in the evening light " +
            "with a forest behind it and a river in front of it " + "x".repeat(200)
        show(sourceWith(prompt))

        val wide = drawn(lblPrompt)
        assertTrue(wide.endsWith("..."), wide)
        assertTrue(prompt.startsWith(wide.removeSuffix("...")))

        interact { view.maxWidth = 150.0 }
        val narrow = drawn(lblPrompt)
        assertTrue(narrow.endsWith("..."), narrow)
        assertTrue(narrow.length < wide.length, "narrow=$narrow wide=$wide")

        interact { view.maxWidth = 400.0 }
        assertEquals(wide, drawn(lblPrompt))

        show(sourceWith("short"))
        assertEquals("short", drawn(lblPrompt))
    }

    /**
     * Use case: a prompt of several lines. It is shown as one line.
     */
    @Test
    fun promptWithLineBreaksIsOneLine() {
        show(sourceWith("first line\nsecond line"))

        assertEquals("first line second line", lblPrompt.text)
    }

    /**
     * Use case: another medium is chosen, one without details. The lines of the former medium disappear.
     */
    @Test
    fun mediumWithoutDetailsEmptiesTheLines() {
        val source = sourceWith("A red fox")
        show(source)

        show(source, number = 3)

        assertEquals("", lblModelFlow.text)
        assertEquals("", lblPrompt.text)
    }

    /**
     * Use case: the details of the shown reference change. Invalidating the reference through the component
     * shows the new details.
     */
    @Test
    fun invalidateShowsTheChangedDetails() {
        val source = sourceWith("before")
        show(source)

        source.addDetails("ref-a", 0, MediumDetails("after", "model-2", null))
        interact { view.invalidate("ref-a") }

        assertEquals("model-2", lblModelFlow.text)
        assertEquals("after", lblPrompt.text)
    }

    /**
     * Use case: the component is put on and off the screen in the three ways - set invisible, removed from
     * the scene, window hidden. While it is off screen a change of the medium is not noticed; when it returns
     * the lines are current, and a further change is noticed again.
     */
    @Test
    fun mediumIsObservedOnlyWhileOnScreen() {
        val source = FakeMediaSource().apply {
            repeat(12) { addDetails("ref-a", it, MediumDetails("prompt $it", null, null)) }
        }
        val root = VBox()
        val tuple = FluentViewLoader.fxmlView(GenerationDetailsView::class.java).root(root).load()
        val viewModel = tuple.viewModel
        viewModel.mediaSource.set(source)
        interact { pane.children.add(root) }
        var next = 0

        fun medium(number: Int) = interact { viewModel.medium.set(HistoryMedium("ref-a", number)) }

        fun checkOffAndOn(label: String, off: () -> Unit, on: () -> Unit) {
            medium(++next)
            assertEquals("prompt $next", viewModel.prompt.get(), "$label: on screen")
            val shown = next
            off()
            medium(++next)
            assertEquals("prompt $shown", viewModel.prompt.get(), "$label: off screen")
            on()
            assertEquals("prompt $next", viewModel.prompt.get(), "$label: back on screen")
            medium(++next)
            assertEquals("prompt $next", viewModel.prompt.get(), "$label: change noticed again")
        }

        checkOffAndOn("invisible", { interact { root.isVisible = false } }, { interact { root.isVisible = true } })
        checkOffAndOn(
            "removed from the scene",
            { interact { pane.children.remove(root) } },
            { interact { pane.children.add(root) } },
        )
        checkOffAndOn("window hidden", { interact { stage.hide() } }, { interact { stage.show() } })
    }
}
