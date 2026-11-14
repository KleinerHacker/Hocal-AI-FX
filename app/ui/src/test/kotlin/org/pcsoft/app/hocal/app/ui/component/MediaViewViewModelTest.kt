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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.ui.preview.FakeMediaSource
import org.pcsoft.app.hocal.app.ui.preview.brokenBytes
import org.pcsoft.app.hocal.app.ui.preview.pngBytes
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Developer tests of the media view model, driven without a view.
 */
class MediaViewViewModelTest {

    companion object {
        private const val RED = 0xFFFF0000.toInt()
        private const val GREEN = 0xFF00FF00.toInt()

        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val viewModel = MediaViewViewModel()
    private val source = FakeMediaSource()

    @AfterEach
    fun closeLoader() {
        fx { viewModel.onHide() }
        viewModel.close()
    }

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    private fun awaitTrue(condition: () -> Boolean) {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { condition() })
    }

    private fun settle() {
        repeat(3) {
            Thread.sleep(50)
            WaitForAsyncUtils.waitForFxEvents()
        }
    }

    private fun show(medium: HistoryMedium?, source: FakeMediaSource? = this.source) = fx {
        viewModel.onShow()
        viewModel.mediaSource.set(source)
        viewModel.medium.set(medium)
    }

    private fun colorOf(image: Image?): Int = fx { image!!.pixelReader.getArgb(0, 0) }

    /**
     * Use case: a chosen medium of a reference with two media. The original (number 0) and the last one
     * (number 1) are each loaded by their number and shown as image, without placeholder.
     */
    @Test
    fun loadsTheChosenMediumByNumber() {
        source.add("ref-a", "png" to pngBytes(8, 8, 0xFF0000), "png" to pngBytes(8, 8, 0x00FF00))

        show(HistoryMedium("ref-a", 0))
        awaitTrue { viewModel.image.get() != null }
        assertEquals(RED, colorOf(viewModel.image.get()))
        assertFalse(viewModel.showsPlaceholder.get())

        fx { viewModel.medium.set(HistoryMedium("ref-a", 1)) }
        awaitTrue { viewModel.image.get() != null && colorOf(viewModel.image.get()) == GREEN }
        assertFalse(viewModel.showsPlaceholder.get())
    }

    /**
     * Use case: nothing is chosen. Without a medium, and with a medium but without a source, the area stays
     * empty: no image and no placeholder.
     */
    @Test
    fun noMediumOrNoSourceStaysEmpty() {
        source.add("ref-a", "png" to pngBytes(8, 8))

        show(null)
        settle()
        assertNull(viewModel.image.get())
        assertFalse(viewModel.showsPlaceholder.get())

        show(HistoryMedium("ref-a", 0), source = null)
        settle()
        assertNull(viewModel.image.get())
        assertFalse(viewModel.showsPlaceholder.get())
        assertEquals(0, source.openRequested.get())
    }

    /**
     * Use case: the medium cannot be shown - a broken image, a missing number or an unknown reference. The
     * placeholder is requested and there is no image and no error.
     */
    @Test
    fun unshowableMediumShowsThePlaceholder() {
        source.add("ref-broken", "png" to brokenBytes())
        source.add("ref-a", "png" to pngBytes(8, 8))

        show(HistoryMedium("ref-broken", 0))
        awaitTrue { viewModel.showsPlaceholder.get() }
        assertNull(viewModel.image.get())

        fx { viewModel.medium.set(HistoryMedium("ref-a", 5)) }
        settle()
        assertTrue(viewModel.showsPlaceholder.get(), "missing number")
        assertNull(viewModel.image.get())

        fx { viewModel.medium.set(HistoryMedium("ref-unknown", 0)) }
        settle()
        assertTrue(viewModel.showsPlaceholder.get(), "unknown reference")

        fx { viewModel.medium.set(null) }
        assertFalse(viewModel.showsPlaceholder.get(), "the placeholder goes with the medium")
    }

    /**
     * Use case: the image is still loading. The area is empty until it arrives, and the image of the former
     * medium is not left standing.
     */
    @Test
    fun loadingStateIsEmptyAndDropsTheOldImage() {
        source.add("ref-a", "png" to pngBytes(8, 8, 0xFF0000))
        source.add("ref-b", "png" to pngBytes(8, 8, 0x00FF00))
        show(HistoryMedium("ref-a", 0))
        awaitTrue { viewModel.image.get() != null }

        val gate = CountDownLatch(1)
        source.gate = gate
        fx { viewModel.medium.set(HistoryMedium("ref-b", 0)) }
        assertNull(viewModel.image.get())
        assertFalse(viewModel.showsPlaceholder.get())

        gate.countDown()
        awaitTrue { viewModel.image.get() != null }
        assertEquals(GREEN, colorOf(viewModel.image.get()))
    }

    /**
     * Use case: the user switches the medium while the first one still loads. The first load is cancelled,
     * its late result is dropped, and only the image of the second medium is ever shown.
     */
    @Test
    fun changeCancelsTheLoadAndDiscardsTheLateResult() {
        source.add("ref-a", "png" to pngBytes(8, 8, 0xFF0000))
        source.add("ref-b", "png" to pngBytes(8, 8, 0x00FF00))
        val gate = CountDownLatch(1)
        source.gate = gate
        val seen = mutableListOf<Int>()
        fx { viewModel.image.addListener { _, _, image -> if (image != null) seen += image.pixelReader.getArgb(0, 0) } }

        show(HistoryMedium("ref-a", 0))
        awaitTrue { source.openRequested.get() >= 1 }
        fx { viewModel.medium.set(HistoryMedium("ref-b", 0)) }
        gate.countDown()

        awaitTrue { viewModel.image.get() != null }
        settle()
        assertEquals(GREEN, colorOf(viewModel.image.get()))
        assertEquals(listOf(GREEN), fx { seen.toList() })
    }

    /**
     * Use case: the caller exchanges the media source, for instance another project is opened. The cache is
     * emptied, so the same reference is loaded from the new source and not taken from the cache.
     */
    @Test
    fun sourceChangeLoadsFromTheNewSource() {
        val other = FakeMediaSource()
        source.add("ref-a", "png" to pngBytes(8, 8, 0xFF0000))
        other.add("ref-a", "png" to pngBytes(8, 8, 0x00FF00))
        show(HistoryMedium("ref-a", 0))
        awaitTrue { viewModel.image.get() != null }
        assertEquals(RED, colorOf(viewModel.image.get()))

        fx { viewModel.mediaSource.set(other) }
        awaitTrue { viewModel.image.get() != null && colorOf(viewModel.image.get()) == GREEN }
        assertEquals(1, other.openRequested.get())
    }

    /**
     * Use case: the media of the shown reference change, for instance a new version was generated. The
     * cache of the reference is dropped and the medium is loaded again.
     */
    @Test
    fun invalidateReloadsTheShownReference() {
        source.add("ref-a", "png" to pngBytes(8, 8, 0xFF0000))
        show(HistoryMedium("ref-a", 0))
        awaitTrue { viewModel.image.get() != null }
        assertEquals(RED, colorOf(viewModel.image.get()))

        source.add("ref-a", "png" to pngBytes(8, 8, 0x00FF00))
        fx { viewModel.invalidate("ref-a") }
        awaitTrue { viewModel.image.get() != null && colorOf(viewModel.image.get()) == GREEN }
    }

    /**
     * Use case: the media of another reference change. The shown medium is neither reloaded nor cleared.
     */
    @Test
    fun invalidateOfAnotherReferenceChangesNothing() {
        source.add("ref-a", "png" to pngBytes(8, 8, 0xFF0000))
        show(HistoryMedium("ref-a", 0))
        awaitTrue { viewModel.image.get() != null }
        val opened = source.openRequested.get()

        fx { viewModel.invalidate("ref-other") }
        settle()

        assertEquals(opened, source.openRequested.get())
        assertEquals(RED, colorOf(viewModel.image.get()))
    }

    /**
     * Use case: a medium is already set when the component comes on screen. Showing loads it, and showing a
     * second time registers nothing twice: one change of the medium starts exactly one load.
     */
    @Test
    fun showLoadsTheCurrentMediumAndDoubleShowRegistersOnce() {
        source.add("ref-a", "png" to pngBytes(8, 8, 0xFF0000))
        source.add("ref-b", "png" to pngBytes(8, 8, 0x00FF00))
        fx {
            viewModel.mediaSource.set(source)
            viewModel.medium.set(HistoryMedium("ref-a", 0))
        }
        settle()
        assertEquals(0, source.openRequested.get(), "not observed before the first show")

        fx {
            viewModel.onShow()
            viewModel.onShow()
        }
        awaitTrue { viewModel.image.get() != null }
        assertEquals(1, viewModel.appearance.registrations)
        val opened = source.openRequested.get()

        fx { viewModel.medium.set(HistoryMedium("ref-b", 0)) }
        awaitTrue { viewModel.image.get() != null && colorOf(viewModel.image.get()) == GREEN }
        settle()
        assertEquals(opened + 1, source.openRequested.get())
    }

    /**
     * Use case: the component leaves the screen. The appearance registration is released, a running load is
     * cancelled and its result never arrives, and a change of the medium is not observed any more.
     */
    @Test
    fun hideStopsObservingAndCancelsTheLoad() {
        source.add("ref-a", "png" to pngBytes(8, 8, 0xFF0000))
        source.add("ref-b", "png" to pngBytes(8, 8, 0x00FF00))
        val gate = CountDownLatch(1)
        source.gate = gate
        show(HistoryMedium("ref-a", 0))
        awaitTrue { source.openRequested.get() >= 1 }

        fx { viewModel.onHide() }
        assertEquals(0, viewModel.appearance.registrations)
        gate.countDown()
        settle()
        assertNull(viewModel.image.get(), "the late result of the cancelled load is dropped")
        assertFalse(viewModel.showsPlaceholder.get())

        val opened = source.openRequested.get()
        fx { viewModel.medium.set(HistoryMedium("ref-b", 0)) }
        settle()
        assertEquals(opened, source.openRequested.get(), "no longer observed")
        assertNull(viewModel.image.get())

        fx { viewModel.onShow() }
        awaitTrue { viewModel.image.get() != null }
        assertNotNull(viewModel.image.get())
        assertEquals(1, viewModel.appearance.registrations)
    }
}
