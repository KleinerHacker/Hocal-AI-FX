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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.pcsoft.app.hocal.app.ui.preview.FakeMediaSource
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.app.ui.preview.MediumDetails
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable

/**
 * Developer tests of the generation details view model, driven without a view.
 */
class GenerationDetailsViewModelTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val viewModel = GenerationDetailsViewModel()
    private val source = FakeMediaSource()

    @AfterEach
    fun hide() {
        fx { viewModel.onHide() }
    }

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    private fun show(medium: HistoryMedium?, source: MediaSource? = this.source) = fx {
        viewModel.onShow()
        viewModel.mediaSource.set(source)
        viewModel.medium.set(medium)
    }

    /**
     * Use case: a medium with model, flow and prompt is chosen. The first line holds model and flow, the
     * second the prompt.
     */
    @Test
    fun showsModelFlowAndPrompt() {
        source.addDetails("ref-a", 0, MediumDetails("A red fox", "model-1", "text-to-image"))

        show(HistoryMedium("ref-a", 0))

        assertEquals("model-1 · text-to-image", viewModel.modelFlow.get())
        assertEquals("A red fox", viewModel.prompt.get())
    }

    /**
     * Use case: details with missing parts. Model or flow alone stand without separator; a missing prompt
     * leaves the second line empty.
     */
    @ParameterizedTest
    @CsvSource(
        value = ["model-1,NIL,model-1", "NIL,flow-1,flow-1", "model-1,flow-1,model-1 · flow-1", "NIL,NIL,''"],
        nullValues = ["NIL"],
    )
    fun missingPartsLeaveNoSeparator(model: String?, flow: String?, expected: String) {
        source.addDetails("ref-a", 0, MediumDetails(null, model, flow))

        show(HistoryMedium("ref-a", 0))

        assertEquals(expected, viewModel.modelFlow.get())
        assertEquals("", viewModel.prompt.get())
    }

    /**
     * Use case: a prompt with line breaks, tabs and runs of blanks. The second line is a single line: every
     * run of white space is one blank, the ends are trimmed.
     */
    @Test
    fun promptBecomesASingleLine() {
        source.addDetails("ref-a", 0, MediumDetails("  A red\n\nfox\t on  a hill \r\n", null, null))

        show(HistoryMedium("ref-a", 0))

        assertEquals("A red fox on a hill", viewModel.prompt.get())
    }

    /**
     * Use case: a medium without details, no medium at all, and a medium without a source. All lines are
     * empty.
     */
    @Test
    fun withoutDetailsTheLinesAreEmpty() {
        source.addDetails("ref-a", 0, MediumDetails("A red fox", "model-1", "flow-1"))
        show(HistoryMedium("ref-a", 0))

        fx { viewModel.medium.set(HistoryMedium("ref-a", 5)) }
        assertEquals("", viewModel.modelFlow.get())
        assertEquals("", viewModel.prompt.get())

        fx { viewModel.medium.set(HistoryMedium("ref-a", 0)) }
        assertEquals("A red fox", viewModel.prompt.get())
        fx { viewModel.medium.set(null) }
        assertEquals("", viewModel.prompt.get())

        fx { viewModel.medium.set(HistoryMedium("ref-a", 0)) }
        fx { viewModel.mediaSource.set(null) }
        assertEquals("", viewModel.modelFlow.get())
        assertEquals("", viewModel.prompt.get())
    }

    /**
     * Use case: the user selects another medium of the history. The lines follow, each medium with its own
     * details.
     */
    @Test
    fun changingTheMediumChangesTheLines() {
        source.addDetails("ref-a", 0, MediumDetails("first", "model-1", "flow-1"))
        source.addDetails("ref-a", 1, MediumDetails("second", "model-2", "flow-2"))
        show(HistoryMedium("ref-a", 0))

        fx { viewModel.medium.set(HistoryMedium("ref-a", 1)) }

        assertEquals("model-2 · flow-2", viewModel.modelFlow.get())
        assertEquals("second", viewModel.prompt.get())
    }

    /**
     * Use case: another project, so another source, is bound. The details come from the new source.
     */
    @Test
    fun changingTheSourceReadsAgain() {
        source.addDetails("ref-a", 0, MediumDetails("old", null, null))
        show(HistoryMedium("ref-a", 0))
        val other = FakeMediaSource().apply { addDetails("ref-a", 0, MediumDetails("new", null, null)) }

        fx { viewModel.mediaSource.set(other) }

        assertEquals("new", viewModel.prompt.get())
    }

    /**
     * Use case: the shown reference changed. Invalidating it reads the details anew, from any thread;
     * another reference leaves the lines untouched.
     */
    @Test
    fun invalidateReadsTheShownReferenceAgain() {
        source.addDetails("ref-a", 0, MediumDetails("before", null, null))
        show(HistoryMedium("ref-a", 0))
        source.addDetails("ref-a", 0, MediumDetails("after", null, null))

        viewModel.invalidate("ref-b")
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals("before", viewModel.prompt.get())

        viewModel.invalidate("ref-a")
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals("after", viewModel.prompt.get())
    }

    /**
     * Use case: the source fails. The failure is not shown, the lines stay empty.
     */
    @Test
    fun failingSourceLeavesTheLinesEmpty() {
        val failing = object : MediaSource {
            override fun mediaCount(reference: String) = 1
            override fun extension(reference: String, number: Int) = "png"
            override fun open(reference: String, number: Int) = throw IllegalStateException("boom")
            override fun details(reference: String, number: Int): MediumDetails = throw IllegalStateException("boom")
        }

        show(HistoryMedium("ref-a", 0), failing)

        assertEquals("", viewModel.modelFlow.get())
        assertEquals("", viewModel.prompt.get())
    }

    /**
     * Use case: the view model is not observing. A change of the medium is not noticed until it is shown
     * again, then it is read at once; calling the two methods twice is harmless.
     */
    @Test
    fun observesOnlyBetweenShowAndHide() {
        source.addDetails("ref-a", 0, MediumDetails("first", null, null))
        source.addDetails("ref-a", 1, MediumDetails("second", null, null))
        show(HistoryMedium("ref-a", 0))
        fx { viewModel.onShow() }

        fx { viewModel.onHide() }
        fx { viewModel.onHide() }
        fx { viewModel.medium.set(HistoryMedium("ref-a", 1)) }
        assertEquals("first", viewModel.prompt.get())

        fx { viewModel.onShow() }
        assertEquals("second", viewModel.prompt.get())
    }
}
