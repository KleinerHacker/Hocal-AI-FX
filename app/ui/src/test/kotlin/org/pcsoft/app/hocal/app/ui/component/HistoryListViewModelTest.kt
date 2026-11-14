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

import javafx.scene.image.Image
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.ui.RefreshableObjectProperty
import org.pcsoft.app.hocal.app.ui.preview.FakeMediaSource
import org.pcsoft.app.hocal.app.ui.preview.pngBytes
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Developer tests of the history list view model, driven without a view.
 *
 * The view model dispatches to the FX thread, so every action runs there.
 */
class HistoryListViewModelTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val viewModel = HistoryListViewModel()
    private val source = FakeMediaSource()
    private val fileProperty = RefreshableObjectProperty<VirtualEntry>(this, "file")

    @AfterEach
    fun closeViewModel() {
        fx { viewModel.close() }
    }

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    private fun refEntry(name: String, reference: String): VirtualEntry = entry(name).apply { this.reference = reference }

    private fun addMedia(reference: String, count: Int) {
        source.add(reference, *Array(count) { "png" to pngBytes(10, 10, it * 40) })
    }

    private fun show(file: VirtualEntry?, withSource: Boolean = true) {
        fx {
            fileProperty.set(file)
            viewModel.file.bind(fileProperty)
            if (withSource) viewModel.mediaSource.set(source)
            viewModel.onShow()
        }
    }

    private fun refresh() {
        fx { fileProperty.refresh() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    private fun switchTo(file: VirtualEntry?) {
        fx { fileProperty.set(file) }
        WaitForAsyncUtils.waitForFxEvents()
    }

    private fun numbers(): List<Int> = viewModel.entries.map { it.number }

    private fun awaitTrue(condition: () -> Boolean) {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { condition() })
    }

    /**
     * Use case: a view model with no file and no source offers an empty list without any error, also when
     * it is shown and hidden.
     */
    @Test
    fun emptyWithoutFileAndSource() {
        fx {
            viewModel.onShow()
            viewModel.onHide()
        }

        assertTrue(viewModel.entries.isEmpty())
        assertNull(viewModel.selectedMedium.get())
        assertEquals(-1, viewModel.indexToSelect)
    }

    /**
     * Use case: a file with three media is shown. All media are listed from 000 to 002 in the order of
     * their number, each of the file's reference, and the labels have three digits.
     */
    @Test
    fun listsAllMediaInOrder() {
        addMedia("r1", 3)
        show(refEntry("Sunset.png", "r1"))

        assertEquals(listOf(HistoryMedium("r1", 0), HistoryMedium("r1", 1), HistoryMedium("r1", 2)), viewModel.entries)
        assertEquals(listOf("000", "001", "002"), viewModel.entries.map { it.label })
    }

    /**
     * Use case: a file is shown for the first time. The last medium is preselected, the index to select
     * and the selected medium agree.
     */
    @Test
    fun lastMediumIsPreselected() {
        addMedia("r1", 3)
        show(refEntry("Sunset.png", "r1"))

        assertEquals(HistoryMedium("r1", 2), viewModel.selectedMedium.get())
        assertEquals(2, viewModel.indexToSelect)
    }

    /**
     * Use case: the user selected an early medium, then another file is chosen. The list shows the media
     * of the new file and its last medium is selected.
     */
    @Test
    fun fileChangeSelectsTheLastMedium() {
        addMedia("r1", 3)
        addMedia("r2", 2)
        show(refEntry("One.png", "r1"))
        fx { viewModel.selectMedium(HistoryMedium("r1", 0)) }

        switchTo(refEntry("Two.png", "r2"))

        assertEquals(listOf(0, 1), numbers())
        assertEquals(HistoryMedium("r2", 1), viewModel.selectedMedium.get())
        assertEquals(1, viewModel.indexToSelect)
    }

    /**
     * Use case: a medium is appended to the history of the shown file while the user had selected an early
     * one. After the project change is reported the new last medium is selected.
     */
    @Test
    fun growingHistorySelectsTheNewLastMedium() {
        addMedia("r1", 2)
        show(refEntry("Sunset.png", "r1"))
        fx { viewModel.selectMedium(HistoryMedium("r1", 0)) }

        addMedia("r1", 3)
        refresh()

        assertEquals(listOf(0, 1, 2), numbers())
        assertEquals(HistoryMedium("r1", 2), viewModel.selectedMedium.get())
    }

    /**
     * Use case: the project reports a change that leaves the history as it is. The list is rebuilt, which
     * the revision shows, and the choice of the user stays.
     */
    @Test
    fun unchangedCountKeepsTheChoiceOfTheUser() {
        addMedia("r1", 3)
        show(refEntry("Sunset.png", "r1"))
        fx { viewModel.selectMedium(HistoryMedium("r1", 0)) }
        val revision = viewModel.revision.get()

        refresh()

        assertTrue(viewModel.revision.get() > revision)
        assertEquals(HistoryMedium("r1", 0), viewModel.selectedMedium.get())
        assertEquals(0, viewModel.indexToSelect)
    }

    /**
     * Use case: the history shrinks and the selected number does not exist any more. The last medium of the
     * shrunk history is selected.
     */
    @Test
    fun shrinkingHistoryWithMissingNumberSelectsTheLastMedium() {
        addMedia("r1", 3)
        show(refEntry("Sunset.png", "r1"))

        addMedia("r1", 2)
        refresh()

        assertEquals(listOf(0, 1), numbers())
        assertEquals(HistoryMedium("r1", 1), viewModel.selectedMedium.get())
    }

    /**
     * Use case: the history shrinks but the selected number still exists. The choice of the user stays.
     */
    @Test
    fun shrinkingHistoryKeepsAnExistingChoice() {
        addMedia("r1", 3)
        show(refEntry("Sunset.png", "r1"))
        fx { viewModel.selectMedium(HistoryMedium("r1", 0)) }

        addMedia("r1", 2)
        refresh()

        assertEquals(HistoryMedium("r1", 0), viewModel.selectedMedium.get())
    }

    /**
     * Use case: no file, no source, or a file whose reference has no media. The list is empty, nothing is
     * selected and nothing fails.
     */
    @Test
    fun emptyForNoFileNoSourceAndZeroMedia() {
        addMedia("r1", 3)
        show(refEntry("Sunset.png", "r1"))
        assertEquals(3, viewModel.entries.size)

        switchTo(null)
        assertTrue(viewModel.entries.isEmpty())
        assertNull(viewModel.selectedMedium.get())

        switchTo(refEntry("Empty.png", "unknown"))
        assertTrue(viewModel.entries.isEmpty())
        assertNull(viewModel.selectedMedium.get())

        switchTo(refEntry("Sunset.png", "r1"))
        assertEquals(3, viewModel.entries.size)
        fx { viewModel.mediaSource.set(null) }
        WaitForAsyncUtils.waitForFxEvents()
        assertTrue(viewModel.entries.isEmpty())
        assertNull(viewModel.selectedMedium.get())
    }

    /**
     * Use case: two entries equal by value (same name and reference) but different objects. Choosing the
     * second one counts as a change of the file, so the last medium is selected although the user had
     * selected the first medium of the former one.
     */
    @Test
    fun equalEntriesOfDifferentIdentityAreAFileChange() {
        addMedia("r1", 3)
        val first = refEntry("Same.png", "r1")
        val second = refEntry("Same.png", "r1")
        assertEquals(first, second)
        show(first)
        fx { viewModel.selectMedium(HistoryMedium("r1", 0)) }

        switchTo(second)

        assertEquals(HistoryMedium("r1", 2), viewModel.selectedMedium.get())
    }

    /**
     * Use case: the component is shown twice in a row. The appearance is registered only once, and hiding
     * it once removes the registration.
     */
    @Test
    fun doubleShowRegistersOnce() {
        addMedia("r1", 1)
        show(refEntry("Sunset.png", "r1"))
        fx { viewModel.onShow() }

        assertEquals(1, viewModel.appearance.registrations)

        fx { viewModel.onHide() }

        assertEquals(0, viewModel.appearance.registrations)
    }

    /**
     * Use case: the appearance is observed only between show and hide: before the first show and after
     * hide there is no registration, between them exactly one.
     */
    @Test
    fun appearanceIsRegisteredOnlyWhileShown() {
        addMedia("r1", 1)
        assertEquals(0, viewModel.appearance.registrations)

        show(refEntry("Sunset.png", "r1"))
        assertEquals(1, viewModel.appearance.registrations)

        fx { viewModel.onHide() }
        assertEquals(0, viewModel.appearance.registrations)
    }

    /**
     * Use case: after hide a change of the file is not noticed; after the next show the list is current,
     * and further changes are noticed again.
     */
    @Test
    fun hideStopsTheObservation() {
        addMedia("r1", 2)
        addMedia("r2", 4)
        show(refEntry("One.png", "r1"))
        fx { viewModel.onHide() }

        switchTo(refEntry("Two.png", "r2"))
        assertEquals(2, viewModel.entries.size)

        fx { viewModel.onShow() }
        assertEquals(4, viewModel.entries.size)

        addMedia("r2", 5)
        refresh()
        assertEquals(5, viewModel.entries.size)
    }

    /**
     * Use case: a cell asks for the preview of a medium while the list is shown. The image of the medium is
     * delivered on the FX thread.
     */
    @Test
    fun loadDeliversThePreview() {
        addMedia("r1", 2)
        show(refEntry("Sunset.png", "r1"))
        val result = AtomicReference<Image?>()

        val handle = fx { viewModel.load(HistoryMedium("r1", 1)) { result.set(it) } }

        assertNotNull(handle)
        awaitTrue { result.get() != null }
    }

    /**
     * Use case: a preview is requested while the list is not on screen, or without a source. Nothing is
     * loaded and no handle is returned, so the cell keeps its placeholder.
     */
    @Test
    fun loadDoesNothingWhileHiddenOrWithoutSource() {
        addMedia("r1", 1)
        assertNull(fx { viewModel.load(HistoryMedium("r1", 0)) { } })

        show(refEntry("Sunset.png", "r1"), withSource = false)
        assertNull(fx { viewModel.load(HistoryMedium("r1", 0)) { } })
    }

    /**
     * Use case: a preview is still loading when the list leaves the screen. The result is not delivered
     * any more once the loading is released.
     */
    @Test
    fun hideCancelsRunningLoads() {
        addMedia("r1", 1)
        val gate = CountDownLatch(1)
        source.gate = gate
        show(refEntry("Sunset.png", "r1"))
        val result = AtomicReference<Image?>()
        fx { viewModel.load(HistoryMedium("r1", 0)) { result.set(it) } }
        awaitTrue { source.openRequested.get() == 1 }

        fx { viewModel.onHide() }
        gate.countDown()
        awaitTrue { source.closed.get() == 1 }
        repeat(3) {
            Thread.sleep(50)
            WaitForAsyncUtils.waitForFxEvents()
        }

        assertNull(result.get())
    }

    /**
     * Use case: the media of a reference changed. The list is rebuilt, which the revision shows, and the
     * new number of media is taken over.
     */
    @Test
    fun invalidateRebuildsTheList() {
        addMedia("r1", 2)
        show(refEntry("Sunset.png", "r1"))
        val revision = viewModel.revision.get()

        addMedia("r1", 3)
        viewModel.invalidate("r1")
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(viewModel.revision.get() > revision)
        assertEquals(3, viewModel.entries.size)
        assertEquals(HistoryMedium("r1", 2), viewModel.selectedMedium.get())
    }
}
