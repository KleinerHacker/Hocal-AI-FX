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
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.Messages
import org.pcsoft.app.hocal.app.ui.preview.FakeMediaSource
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.app.ui.preview.brokenBytes
import org.pcsoft.app.hocal.app.ui.preview.pngBytes
import org.pcsoft.app.hocal.fx.model.project.VirtualFolderProperty
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Tests of the project file list component on real cells in a headless window.
 */
class ProjectFileListTest : ApplicationTest() {

    private lateinit var stage: Stage
    private lateinit var pane: StackPane
    private lateinit var list: ProjectFileList
    private val source = FakeMediaSource()

    override fun start(stage: Stage) {
        this.stage = stage
        list = ProjectFileList()
        pane = StackPane(list)
        stage.scene = Scene(pane, 400.0, 300.0)
        stage.show()
    }

    private fun refEntry(name: String, reference: String): VirtualEntry = entry(name).apply { this.reference = reference }

    private fun bind(root: VirtualFolder, withSource: Boolean = true): VirtualFolderProperty {
        val property = VirtualFolderProperty.of(root)
        interact {
            list.folderProperty().bind(property)
            if (withSource) list.mediaSourceProperty().bind(SimpleObjectProperty<MediaSource?>(source))
        }
        return property
    }

    private fun change(property: VirtualFolderProperty, action: () -> Unit) {
        interact {
            action()
            property.refresh()
        }
    }

    private fun cells(): List<ListCell<*>> {
        WaitForAsyncUtils.waitForFxEvents()
        return list.lookupAll(".list-cell").filterIsInstance<ListCell<*>>().filter { !it.isEmpty }.sortedBy { it.layoutY }
    }

    private fun row(cell: ListCell<*>): HBox = cell.graphic as HBox

    private fun originalView(cell: ListCell<*>): ImageView = row(cell).children[0] as ImageView

    private fun currentView(cell: ListCell<*>): ImageView = row(cell).children[1] as ImageView

    private fun nameLabel(cell: ListCell<*>): Label = row(cell).children[2] as Label

    private fun names(): List<String> = cells().map { nameLabel(it).text }

    private fun cellOf(name: String): ListCell<*> = cells().first { nameLabel(it).text == name }

    private fun placeholders(): List<Image> {
        // The preferences belong to the FX thread.
        var dark = false
        interact { dark = Platform.getPreferences().colorScheme == ColorScheme.DARK }
        return HocalIcons.PREVIEW_SIZES.map { HocalIcons.image(HocalIcons.MEDIA_PLACEHOLDER, it, dark) }
    }

    private fun isPlaceholder(view: ImageView): Boolean = view.image in placeholders()

    private fun awaitTrue(condition: () -> Boolean) {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { condition() })
    }

    private fun tooltipOf(view: ImageView): String = (view.properties["javafx.scene.control.Tooltip"] as Tooltip).text

    /**
     * Use case: a folder with entries and a subfolder is shown. Only the entries appear as rows, in the
     * order of the model; the subfolder does not.
     */
    @Test
    fun showsOnlyEntriesInModelOrder() {
        bind(folder("Project", entry("B.png"), folder("Photos"), entry("A.png")), withSource = false)

        assertEquals(listOf("B.png", "A.png"), names())
    }

    /**
     * Use case: a row of an entry with two media is built as the original preview, the current preview
     * and the name, in this order; the previews are shown in the display size 48 x 48 with aspect ratio
     * kept, the name label carries the style class file-name, and both previews differ.
     */
    @Test
    fun rowIsBuiltFromOriginalCurrentAndName() {
        source.add("r1", "png" to pngBytes(100, 100, 0xFF0000), "png" to pngBytes(100, 100, 0x0000FF))
        bind(folder("Project", refEntry("Sunset.png", "r1")))
        val cell = cellOf("Sunset.png")
        awaitTrue { !isPlaceholder(originalView(cell)) && !isPlaceholder(currentView(cell)) }

        assertEquals(3, row(cell).children.size)
        assertNotSame(originalView(cell).image, currentView(cell).image)
        listOf(originalView(cell), currentView(cell)).forEach {
            assertEquals(48.0, it.fitWidth)
            assertEquals(48.0, it.fitHeight)
            assertTrue(it.isPreserveRatio)
        }
        assertTrue("file-name" in nameLabel(cell).styleClass)
    }

    /**
     * Use case: an entry whose reference has a single medium shows the same image twice, loaded once.
     */
    @Test
    fun singleMediumShowsTheSameImageTwice() {
        source.add("r1", "png" to pngBytes(100, 50))
        bind(folder("Project", refEntry("Sunset.png", "r1")))
        val cell = cellOf("Sunset.png")
        awaitTrue { !isPlaceholder(currentView(cell)) }

        assertSame(originalView(cell).image, currentView(cell).image)
        assertEquals(1, source.openRequested.get())
    }

    /**
     * Use case: the two previews carry tooltips with the texts of the message bundle, one for the
     * original and one for the current medium.
     */
    @Test
    fun previewsCarryTheBundleTooltips() {
        bind(folder("Project", entry("Sunset.png")), withSource = false)
        val cell = cellOf("Sunset.png")

        assertEquals(Messages["component.projectFileList.original"], tooltipOf(originalView(cell)))
        assertEquals(Messages["component.projectFileList.current"], tooltipOf(currentView(cell)))
    }

    /**
     * Use case: without a media source the entries are shown with their names and placeholder icons.
     */
    @Test
    fun withoutSourceShowsPlaceholders() {
        bind(folder("Project", refEntry("Sunset.png", "r1")), withSource = false)
        val cell = cellOf("Sunset.png")

        assertTrue(isPlaceholder(originalView(cell)))
        assertTrue(isPlaceholder(currentView(cell)))
    }

    /**
     * Use case: one entry has a broken image, another a type no provider knows, a third a good image.
     * The broken and the unknown one keep their placeholders, the good one gets its preview, and the
     * list stays intact.
     */
    @Test
    fun brokenAndUnknownMediaShowPlaceholders() {
        source.add("broken", "png" to brokenBytes())
        source.add("unknown", "xyz" to pngBytes(10, 10))
        source.add("good", "png" to pngBytes(10, 10))
        bind(folder("Project", refEntry("Broken", "broken"), refEntry("Unknown", "unknown"), refEntry("Good", "good")))
        awaitTrue { !isPlaceholder(currentView(cellOf("Good"))) }
        awaitTrue { source.closed.get() >= 2 }
        Thread.sleep(100)
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("Broken", "Unknown", "Good"), names())
        assertTrue(isPlaceholder(originalView(cellOf("Broken"))))
        assertTrue(isPlaceholder(currentView(cellOf("Broken"))))
        assertTrue(isPlaceholder(originalView(cellOf("Unknown"))))
        assertTrue(isPlaceholder(currentView(cellOf("Unknown"))))
    }

    /**
     * Use case: the user clicks an entry, so it becomes the selected file of the component; with no click
     * nothing is selected. Two entries equal by value are told apart.
     */
    @Test
    fun clickingAnEntrySelectsIt() {
        val first = entry("Same.png")
        val second = entry("Same.png")
        val other = entry("Other.png")
        bind(folder("Project", first, second, other), withSource = false)
        assertNull(list.selectedFileProperty().get())

        clickOn(cells()[1])
        assertSame(second, list.selectedFileProperty().get())

        clickOn(cells()[2])
        assertSame(other, list.selectedFileProperty().get())
    }

    /**
     * Use case: the model changes while an entry is selected - an entry is added. The rows are rebuilt and
     * the same entry stays selected.
     */
    @Test
    fun modelChangeIsShownAndKeepsTheSelection() {
        val a = entry("A.png")
        val root = folder("Project", a)
        val property = bind(root, withSource = false)
        clickOn(cellOf("A.png"))

        change(property) { root.children.add(entry("B.png")) }

        assertEquals(listOf("A.png", "B.png"), names())
        assertSame(a, list.selectedFileProperty().get())
        assertSame(a, list.selectionModel.selectedItem)
    }

    /**
     * Use case: another folder is bound. The list shows the entries of the new folder only, a change in
     * the former folder is ignored and leaves it untouched, and the selection of the former folder is gone.
     */
    @Test
    fun exchangingTheBoundFolderShowsTheNewOneOnly() {
        val oldRoot = folder("Old", entry("Old.png"))
        val oldProperty = bind(oldRoot, withSource = false)
        clickOn(cellOf("Old.png"))
        val newRoot = folder("New", entry("New1.png"), entry("New2.png"))
        bind(newRoot, withSource = false)

        assertEquals(listOf("New1.png", "New2.png"), names())
        assertNull(list.selectedFileProperty().get())

        change(oldProperty) { oldRoot.children.add(entry("Ignored.png")) }

        assertEquals(listOf("New1.png", "New2.png"), names())
        assertEquals(2, oldRoot.children.size)
    }

    /**
     * Use case: without a folder the list is empty and nothing fails, also after the folder was removed.
     */
    @Test
    fun emptyListWithoutFolder() {
        assertTrue(names().isEmpty())

        val property = bind(folder("Project", entry("A.png")), withSource = false)
        interact { property.set(null) }

        assertTrue(names().isEmpty())
    }

    private fun assertChangeIsIgnoredUntilBack(
        root: VirtualFolder,
        property: VirtualFolderProperty,
        leave: () -> Unit,
        back: () -> Unit,
    ) {
        leave()
        change(property) { root.children.add(entry("WhileAway.png")) }
        assertEquals(0, list.items.size, "a change while off screen must not be taken over")

        back()
        assertEquals(1, list.items.size, "returning shows the current model")

        change(property) { root.children.add(entry("After.png")) }
        assertEquals(2, list.items.size, "a further change must be noticed again")
    }

    /**
     * Use case: the component is set invisible. A model change meanwhile is not taken over; when it is
     * visible again the list shows the current model and notices further changes.
     */
    @Test
    fun invisibleComponentDoesNotObserveTheModel() {
        val root = folder("Project")
        val property = bind(root, withSource = false)

        assertChangeIsIgnoredUntilBack(root, property, { interact { list.isVisible = false } }, { interact { list.isVisible = true } })
    }

    /**
     * Use case: the component is removed from the scene. A model change meanwhile is not taken over; when
     * it is added again the list shows the current model and notices further changes.
     */
    @Test
    fun componentRemovedFromTheSceneDoesNotObserveTheModel() {
        val root = folder("Project")
        val property = bind(root, withSource = false)

        assertChangeIsIgnoredUntilBack(root, property, { interact { pane.children.remove(list) } }, { interact { pane.children.add(list) } })
    }

    /**
     * Use case: the window of the component is hidden. A model change meanwhile is not taken over; when
     * the window is shown again the list shows the current model and notices further changes. The window
     * keeps its size, as the headless screen cannot show a larger one.
     */
    @Test
    fun hiddenWindowDoesNotObserveTheModel() {
        val root = folder("Project")
        val property = bind(root, withSource = false)

        assertChangeIsIgnoredUntilBack(root, property, { interact { stage.hide() } }, { interact { stage.show() } })
    }
}
