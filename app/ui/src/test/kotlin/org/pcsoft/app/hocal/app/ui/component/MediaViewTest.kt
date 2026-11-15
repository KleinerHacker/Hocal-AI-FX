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
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.ui.preview.FakeMediaSource
import org.pcsoft.app.hocal.app.ui.preview.brokenBytes
import org.pcsoft.app.hocal.app.ui.preview.pngBytes
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Tests of the media view component on real controls in a headless window of 400 x 300.
 */
class MediaViewTest : ApplicationTest() {

    private lateinit var stage: Stage
    private lateinit var pane: StackPane
    private lateinit var view: MediaView

    override fun start(stage: Stage) {
        this.stage = stage
        view = MediaView()
        pane = StackPane(view)
        stage.scene = Scene(pane, 400.0, 300.0)
        stage.show()
    }

    @AfterEach
    fun closeView() {
        view.close()
    }

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    private val imgMedia: ImageView get() = lookup("#imgMedia").query()
    private val imgPlaceholder: ImageView get() = lookup("#imgPlaceholder").query()

    private fun awaitTrue(condition: () -> Boolean) {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { condition() })
    }

    private fun show(reference: String, source: FakeMediaSource) {
        interact {
            view.mediaSourceProperty().set(source)
            view.mediumProperty().set(HistoryMedium(reference, 0))
        }
    }

    /**
     * Use case: the component is shown without a medium. The area is empty: neither an image nor the
     * placeholder is visible.
     */
    @Test
    fun emptyAreaWithoutMedium() {
        assertNull(imgMedia.image)
        assertFalse(imgPlaceholder.isVisible)
    }

    /**
     * Use case: a medium is chosen. After loading, the image appears in the image view; its pixels carry the
     * color of the medium, and the placeholder stays hidden.
     */
    @Test
    fun imageAppearsAfterLoading() {
        val source = FakeMediaSource().apply { add("ref-a", "png" to pngBytes(20, 10, 0xFF0000)) }

        show("ref-a", source)

        awaitTrue { imgMedia.image != null }
        assertEquals(0xFFFF0000.toInt(), fx { imgMedia.image.pixelReader.getArgb(0, 0) })
        assertFalse(imgPlaceholder.isVisible)
    }

    /**
     * Use case: the component gets smaller than the window. The image view follows the size of the
     * component, keeps the aspect ratio and is smoothed; the component may shrink to nothing.
     */
    @Test
    fun imageFitsTheComponent() {
        val source = FakeMediaSource().apply { add("ref-a", "png" to pngBytes(20, 10)) }
        show("ref-a", source)
        awaitTrue { imgMedia.image != null }
        assertEquals(400.0, imgMedia.fitWidth)
        assertEquals(300.0, imgMedia.fitHeight)

        interact {
            view.maxWidth = 200.0
            view.maxHeight = 100.0
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(200.0, imgMedia.fitWidth)
        assertEquals(100.0, imgMedia.fitHeight)
        assertTrue(imgMedia.isPreserveRatio)
        assertTrue(imgMedia.isSmooth)
        assertEquals(0.0, view.minWidth)
        assertEquals(0.0, view.minHeight)
        assertNotNull(view.clip)
    }

    /**
     * Use case: the medium cannot be shown. The placeholder is visible with a fixed size of 96 x 96 in the
     * middle, also when the component changes its size; the image view stays empty. A good medium hides it.
     */
    @Test
    fun placeholderIsFixedAndVisibleOnlyInThePlaceholderState() {
        val source = FakeMediaSource().apply {
            add("ref-broken", "png" to brokenBytes())
            add("ref-a", "png" to pngBytes(20, 10))
        }
        show("ref-broken", source)
        awaitTrue { imgPlaceholder.isVisible }
        assertNull(imgMedia.image)
        assertEquals(96.0, imgPlaceholder.layoutBounds.width)
        assertEquals(96.0, imgPlaceholder.layoutBounds.height)

        interact {
            view.maxWidth = 200.0
            view.maxHeight = 100.0
        }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(96.0, imgPlaceholder.layoutBounds.width)
        assertEquals(96.0, imgPlaceholder.layoutBounds.height)

        interact { view.mediumProperty().set(HistoryMedium("ref-a", 0)) }
        awaitTrue { imgMedia.image != null }
        assertFalse(imgPlaceholder.isVisible)
    }

    /**
     * Use case: the media of the shown reference change. Invalidating the reference through the component
     * loads the medium again and the new image replaces the old one.
     */
    @Test
    fun invalidateShowsTheChangedMedium() {
        val source = FakeMediaSource().apply { add("ref-a", "png" to pngBytes(20, 10, 0xFF0000)) }
        show("ref-a", source)
        awaitTrue { imgMedia.image != null }

        source.add("ref-a", "png" to pngBytes(20, 10, 0x00FF00))
        interact { view.invalidate("ref-a") }

        awaitTrue { imgMedia.image != null && fx { imgMedia.image.pixelReader.getArgb(0, 0) } == 0xFF00FF00.toInt() }
    }

    /**
     * Use case: the component is put on and off the screen in the three ways - set invisible, removed
     * from the scene, window hidden. The appearance is observed exactly while it is on screen, and
     * observing is restored each time it returns.
     */
    @Test
    fun appearanceIsObservedOnlyWhileOnScreen() {
        val root = StackPane()
        val tuple = FluentViewLoader.fxmlView(MediaViewView::class.java).root(root).load()
        val viewModel = tuple.viewModel
        assertEquals(0, viewModel.appearance.registrations)
        interact { pane.children.add(root) }
        assertEquals(1, viewModel.appearance.registrations)

        interact { root.isVisible = false }
        assertEquals(0, viewModel.appearance.registrations, "invisible")
        interact { root.isVisible = true }
        assertEquals(1, viewModel.appearance.registrations)

        interact { pane.children.remove(root) }
        assertEquals(0, viewModel.appearance.registrations, "removed from the scene")
        interact { pane.children.add(root) }
        assertEquals(1, viewModel.appearance.registrations)

        interact { stage.hide() }
        assertEquals(0, viewModel.appearance.registrations, "window hidden")
        interact { stage.show() }
        assertEquals(1, viewModel.appearance.registrations)

        viewModel.close()
    }
}
