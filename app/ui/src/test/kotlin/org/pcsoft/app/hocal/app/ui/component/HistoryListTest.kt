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

import javafx.application.ColorScheme
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.ui.RefreshableObjectProperty
import org.pcsoft.app.hocal.app.ui.preview.FakeMediaSource
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.app.ui.preview.brokenBytes
import org.pcsoft.app.hocal.app.ui.preview.pngBytes
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Tests of the history list component on real cells in a headless window.
 */
class HistoryListTest : ApplicationTest() {

    private companion object {
        const val RED = 0xFF0000
        const val GREEN = 0x00FF00
        const val BLUE = 0x0000FF
    }

    private lateinit var stage: Stage
    private lateinit var pane: StackPane
    private lateinit var list: HistoryList
    private val source = FakeMediaSource()
    private val fileProperty = RefreshableObjectProperty<VirtualEntry>(this, "file")

    override fun start(stage: Stage) {
        this.stage = stage
        list = HistoryList()
        pane = StackPane(list)
        stage.scene = Scene(pane, 400.0, 300.0)
        stage.show()
    }

    @AfterEach
    fun closeList() {
        source.gate?.countDown()
        interact { list.close() }
    }

    private fun refEntry(name: String, reference: String): VirtualEntry = entry(name).apply { this.reference = reference }

    private fun bind(file: VirtualEntry?, withSource: Boolean = true) {
        interact {
            fileProperty.set(file)
            list.fileProperty().bind(fileProperty)
            if (withSource) list.mediaSourceProperty().bind(SimpleObjectProperty<MediaSource?>(source))
        }
    }

    private fun refresh() {
        interact { fileProperty.refresh() }
    }

    private fun cells(): List<ListCell<*>> {
        WaitForAsyncUtils.waitForFxEvents()
        return list.lookupAll(".list-cell").filterIsInstance<ListCell<*>>().filter { !it.isEmpty }.sortedBy { it.layoutY }
    }

    private fun row(cell: ListCell<*>): HBox = cell.graphic as HBox

    private fun previewView(cell: ListCell<*>): ImageView = row(cell).children[0] as ImageView

    private fun numberLabel(cell: ListCell<*>): Label = row(cell).children[1] as Label

    private fun numbers(): List<String> = cells().map { numberLabel(it).text }

    private fun placeholders(): List<Image> {
        // The preferences belong to the FX thread.
        var dark = false
        interact { dark = Platform.getPreferences().colorScheme == ColorScheme.DARK }
        return HocalIcons.PREVIEW_SIZES.map { HocalIcons.image(HocalIcons.MEDIA_PLACEHOLDER, it, dark) }
    }

    private fun isPlaceholder(view: ImageView): Boolean = view.image in placeholders()

    private fun centerColor(view: ImageView): Int? {
        val image = view.image ?: return null
        if (isPlaceholder(view)) return null
        return image.pixelReader.getArgb((image.width / 2).toInt(), (image.height / 2).toInt()) and 0xFFFFFF
    }

    private fun awaitTrue(condition: () -> Boolean) {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { condition() })
    }

    private fun addColored(reference: String, vararg colors: Int) {
        source.add(reference, *colors.map { "png" to pngBytes(100, 100, it) }.toTypedArray())
    }

    private fun registrations(): Int = list.viewModel.appearance.registrations

    /**
     * Use case: a file with three media is shown. Each medium is a row with its number 000, 001, 002 in
     * order, the preview in the display size 48 x 48 with aspect ratio kept, and the number label carries
     * the style class history-number. After loading, each preview shows the color of its own medium.
     */
    @Test
    fun rowsShowNumbersAndPreviewsOfTheirMedia() {
        addColored("r1", RED, GREEN, BLUE)
        bind(refEntry("Sunset.png", "r1"))
        awaitTrue { cells().size == 3 && cells().all { !isPlaceholder(previewView(it)) } }

        assertEquals(listOf("000", "001", "002"), numbers())
        assertEquals(listOf(RED, GREEN, BLUE), cells().map { centerColor(previewView(it)) })
        cells().forEach {
            assertEquals(48.0, previewView(it).fitWidth)
            assertEquals(48.0, previewView(it).fitHeight)
            assertTrue(previewView(it).isPreserveRatio)
            assertTrue("history-number" in numberLabel(it).styleClass)
        }
    }

    /**
     * Use case: the media are slow to load. Until the loading is released every row shows the placeholder
     * icon; afterwards it shows the real preview.
     */
    @Test
    fun placeholderIsShownUntilThePreviewIsLoaded() {
        addColored("r1", RED, GREEN)
        val gate = CountDownLatch(1)
        source.gate = gate
        bind(refEntry("Sunset.png", "r1"))

        assertEquals(2, cells().size)
        assertTrue(cells().all { isPlaceholder(previewView(it)) })

        gate.countDown()
        awaitTrue { cells().all { !isPlaceholder(previewView(it)) } }

        assertEquals(listOf(RED, GREEN), cells().map { centerColor(previewView(it)) })
    }

    /**
     * Use case: one medium is broken, another is good. The broken one keeps its placeholder, the good one
     * gets its preview, and the list stays intact.
     */
    @Test
    fun brokenMediumKeepsThePlaceholder() {
        source.add("r1", "png" to brokenBytes(), "png" to pngBytes(100, 100, GREEN))
        bind(refEntry("Sunset.png", "r1"))
        awaitTrue { cells().size == 2 && !isPlaceholder(previewView(cells()[1])) }
        awaitTrue { source.closed.get() >= 2 }
        Thread.sleep(100)
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("000", "001"), numbers())
        assertTrue(isPlaceholder(previewView(cells()[0])))
        assertEquals(GREEN, centerColor(previewView(cells()[1])))
    }

    /**
     * Use case: a file is shown for the first time. The last medium is preselected, in the list and in the
     * selected medium of the component.
     */
    @Test
    fun lastMediumIsPreselected() {
        addColored("r1", RED, GREEN, BLUE)
        bind(refEntry("Sunset.png", "r1"))
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(2, list.selectionModel.selectedIndex)
        assertEquals(HistoryMedium("r1", 2), list.selectedMediumProperty().get())
    }

    /**
     * Use case: the user clicks a row. Its medium becomes the selected medium of the component.
     */
    @Test
    fun clickingARowSelectsItsMedium() {
        addColored("r1", RED, GREEN, BLUE)
        bind(refEntry("Sunset.png", "r1"))

        clickOn(cells()[0])
        assertEquals(HistoryMedium("r1", 0), list.selectedMediumProperty().get())

        clickOn(cells()[1])
        assertEquals(HistoryMedium("r1", 1), list.selectedMediumProperty().get())
    }

    /**
     * Use case: the user selected an early medium and the project reports a change that leaves the history
     * as it is. The list is rebuilt and the choice of the user stays.
     */
    @Test
    fun selectionStaysAfterRefreshWithUnchangedHistory() {
        addColored("r1", RED, GREEN, BLUE)
        bind(refEntry("Sunset.png", "r1"))
        clickOn(cells()[0])

        refresh()
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(3, list.items.size)
        assertEquals(0, list.selectionModel.selectedIndex)
        assertEquals(HistoryMedium("r1", 0), list.selectedMediumProperty().get())
    }

    /**
     * Use case: a medium is appended while the user had selected an early one. The new row appears and
     * becomes the selection.
     */
    @Test
    fun growingHistorySelectsTheNewLastRow() {
        addColored("r1", RED, GREEN)
        bind(refEntry("Sunset.png", "r1"))
        clickOn(cells()[0])

        addColored("r1", RED, GREEN, BLUE)
        refresh()
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("000", "001", "002"), numbers())
        assertEquals(HistoryMedium("r1", 2), list.selectedMediumProperty().get())
    }

    /**
     * Use case: without a file the list is empty and nothing is selected; the same holds after the file was
     * removed again.
     */
    @Test
    fun emptyListWithoutFile() {
        assertTrue(numbers().isEmpty())
        assertNull(list.selectedMediumProperty().get())

        addColored("r1", RED)
        bind(refEntry("Sunset.png", "r1"))
        assertEquals(1, cells().size)

        interact { fileProperty.set(null) }

        assertTrue(numbers().isEmpty())
        assertNull(list.selectedMediumProperty().get())
    }

    /**
     * Use case: the media of a reference are replaced by others with the same count. Without telling it the
     * old previews stay cached; after invalidate the rows load the new media.
     */
    @Test
    fun invalidateLoadsThePreviewsAnew() {
        addColored("r1", RED, GREEN)
        bind(refEntry("Sunset.png", "r1"))
        awaitTrue { cells().size == 2 && cells().all { !isPlaceholder(previewView(it)) } }
        assertEquals(listOf(RED, GREEN), cells().map { centerColor(previewView(it)) })

        addColored("r1", BLUE, BLUE)
        interact { list.invalidate("r1") }
        awaitTrue { cells().all { centerColor(previewView(it)) == BLUE } }

        assertEquals(listOf(BLUE, BLUE), cells().map { centerColor(previewView(it)) })
        assertEquals(1, list.selectionModel.selectedIndex)
    }

    private fun assertChangeIsIgnoredUntilBack(leave: () -> Unit, back: () -> Unit) {
        addColored("r1", RED, GREEN)
        bind(refEntry("Sunset.png", "r1"))
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(1, registrations(), "observing while on screen")
        assertEquals(2, list.items.size)

        leave()
        assertEquals(0, registrations(), "no registration off screen")
        addColored("r1", RED, GREEN, BLUE)
        refresh()
        assertEquals(2, list.items.size, "a change while off screen must not be taken over")

        back()
        assertEquals(1, registrations(), "returning registers again")
        assertEquals(3, list.items.size, "returning shows the current history")

        addColored("r1", RED, GREEN, BLUE, RED)
        refresh()
        assertEquals(4, list.items.size, "a further change must be noticed again")
    }

    /**
     * Use case: the component is set invisible. It stops observing, takes over no change meanwhile and
     * notices changes again when visible.
     */
    @Test
    fun invisibleComponentStopsObserving() {
        assertChangeIsIgnoredUntilBack({ interact { list.isVisible = false } }, { interact { list.isVisible = true } })
    }

    /**
     * Use case: the component is removed from the scene. It stops observing, takes over no change meanwhile
     * and notices changes again when added back.
     */
    @Test
    fun componentRemovedFromTheSceneStopsObserving() {
        assertChangeIsIgnoredUntilBack({ interact { pane.children.remove(list) } }, { interact { pane.children.add(list) } })
    }

    /**
     * Use case: the window of the component is hidden. It stops observing, takes over no change meanwhile
     * and notices changes again when the window is shown. The window keeps its size.
     */
    @Test
    fun hiddenWindowStopsObserving() {
        assertChangeIsIgnoredUntilBack({ interact { stage.hide() } }, { interact { stage.show() } })
    }
}
