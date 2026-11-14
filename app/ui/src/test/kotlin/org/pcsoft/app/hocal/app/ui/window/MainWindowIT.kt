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

import de.saxsys.mvvmfx.MvvmFX
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.MenuBar
import javafx.scene.control.SplitPane
import javafx.scene.control.TreeCell
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.Messages
import org.pcsoft.app.hocal.app.ui.component.HistoryList
import org.pcsoft.app.hocal.app.ui.component.MediaView
import org.pcsoft.app.hocal.app.ui.component.ProjectFileList
import org.pcsoft.app.hocal.app.ui.component.ProjectTree
import org.pcsoft.app.hocal.app.ui.component.PromptInput
import org.pcsoft.app.hocal.app.ui.preview.pngBytes
import org.pcsoft.app.hocal.fx.model.manager.ProjectManager
import org.pcsoft.app.hocal.model.project.Medium
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.io.ByteArrayInputStream
import java.util.Locale
import java.util.ResourceBundle
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Integration tests starting the complete main window.
 */
class MainWindowIT : ApplicationTest() {

    private lateinit var window: MainWindow

    override fun start(stage: Stage) {
        MvvmFX.setGlobalResourceBundle(ResourceBundle.getBundle(Messages.BUNDLE_NAME, Locale.GERMAN))

        // As the application does: a new project exists before the window.
        ProjectManager.createNew("Test")

        window = MainWindow()
        window.show()
    }

    @AfterEach
    fun releaseWindow() {
        interact {
            window.hide()
            ProjectManager.close(discardChanges = true)
        }
    }

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    private val tree: ProjectTree
        get() = window.scene.root.lookup("#pnlTree") as ProjectTree

    private val fileList: ProjectFileList
        get() = window.scene.root.lookup("#pnlFiles") as ProjectFileList

    private val promptInput: PromptInput
        get() = window.scene.root.lookup("#pnlPromptInput") as PromptInput

    private val rootFolder: VirtualFolder
        get() = ProjectManager.project.treeProperty.get()!!

    private fun addFolder(parent: VirtualFolder, name: String): VirtualFolder =
        fx { ProjectManager.createFolder(parent, name) }

    private fun addFile(parent: VirtualFolder, name: String, rgb: Int): VirtualEntry = fx {
        val reference = ProjectManager.addReference(Medium(), "png", ByteArrayInputStream(pngBytes(60, 40, rgb)))
        ProjectManager.createEntry(parent, name, reference)
    }

    private fun treeRows(): List<String> {
        WaitForAsyncUtils.waitForFxEvents()
        return tree.lookupAll(".tree-cell").filterIsInstance<TreeCell<*>>().filter { !it.isEmpty }
            .sortedBy { it.layoutY }.map { cell ->
                val children = (cell.graphic as HBox).children
                "${(children[1] as Label).text} ${(children[2] as Label).text}"
            }
    }

    private fun fileCells(): List<ListCell<*>> {
        WaitForAsyncUtils.waitForFxEvents()
        return fileList.lookupAll(".list-cell").filterIsInstance<ListCell<*>>().filter { !it.isEmpty }
            .sortedBy { it.layoutY }
    }

    private fun fileNames(): List<String> = fileCells().map { (it.lookup(".file-name") as Label).text }

    private fun previewColors(cell: ListCell<*>): List<Int?> =
        cell.lookupAll(".image-view").filterIsInstance<ImageView>().map { it.image?.pixelReader?.getArgb(5, 5)?.and(0xFFFFFF) }

    private fun selectFolder(folder: VirtualFolder) {
        val item = tree.root.children.first { it.value === folder }
        interact { tree.selectionModel.select(item) }
    }

    private val history: HistoryList
        get() = window.scene.root.lookup("#pnlHistory") as HistoryList

    private val mediaView: MediaView
        get() = window.scene.root.lookup("#pnlImage") as MediaView

    private fun addFileWithMedia(parent: VirtualFolder, name: String, vararg colors: Int): String {
        val reference = fx {
            ProjectManager.addReference(Medium(), "png", ByteArrayInputStream(pngBytes(60, 40, colors.first())))
        }
        colors.drop(1).forEach { appendMedium(reference, it) }
        fx { ProjectManager.createEntry(parent, name, reference) }
        return reference
    }

    private fun appendMedium(reference: String, rgb: Int) {
        fx { ProjectManager.appendMedium(reference, Medium(), "png", ByteArrayInputStream(pngBytes(60, 40, rgb))) }
    }

    private fun historyNumbers(): List<String> {
        WaitForAsyncUtils.waitForFxEvents()
        return history.lookupAll(".list-cell").filterIsInstance<ListCell<*>>().filter { !it.isEmpty }
            .sortedBy { it.layoutY }.map { (it.lookup(".history-number") as Label).text }
    }

    // The color of the image the view shows, null while it shows none.
    private fun shownColor(): Int? =
        (mediaView.lookup("#imgMedia") as ImageView).image?.pixelReader?.getArgb(5, 5)?.and(0xFFFFFF)

    private fun selectFirstFile() {
        interact { fileList.selectionModel.select(0) }
    }

    private fun awaitTrue(condition: () -> Boolean) {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { condition() })
    }

    private val menuBar: MenuBar
        get() = window.scene.root.lookup(".menu-bar") as MenuBar

    private val splMain: SplitPane
        get() = window.scene.root.lookup("#splMain") as SplitPane

    private val splProject: SplitPane
        get() = window.scene.root.lookup("#splProject") as SplitPane

    private val splContent: SplitPane
        get() = window.scene.root.lookup("#splContent") as SplitPane

    private fun part(id: String): Node = window.scene.root.lookup("#$id")

    /**
     * Use case: starting the application shows a window titled after the product.
     */
    @Test
    fun windowStartsWithTheProductTitle() {
        assertEquals("Hocal AI", window.title)
        assertTrue(window.isShowing)
    }

    /**
     * Use case: the running window carries the application icon in all shipped sizes.
     */
    @Test
    fun windowCarriesTheApplicationIcon() {
        assertEquals(HocalIcons.APP_SIZES, window.icons.map { it.width.toInt() })
    }

    /**
     * Use case: the running window shows the menu bar with its German texts.
     */
    @Test
    fun windowShowsTheGermanMenuBar() {
        val fileMenu = menuBar.menus.single()

        assertEquals("_Datei", fileMenu.text)
        assertEquals(listOf("_Beenden"), fileMenu.items.map { it.text })
    }

    /**
     * Use case: the window opens with a size of 1280 x 800 pixels.
     */
    @Test
    fun windowOpensWithTheDefaultSize() {
        assertEquals(1280.0, window.scene.width, 1.0)
        assertEquals(800.0, window.scene.height, 1.0)
    }

    /**
     * Use case: the window shows the tree above the file list on the left, and the generation details,
     * the media with the prompt input and the history list side by side on the right.
     */
    @Test
    fun windowShowsAllPartsOfTheLayout() {
        listOf("pnlTree", "pnlFiles", "pnlDetails", "pnlMedia", "pnlHistory").forEach {
            assertNotNull(part(it), "part $it is missing")
        }

        assertEquals(Orientation.HORIZONTAL, splMain.orientation)
        assertEquals(Orientation.VERTICAL, splProject.orientation)
        assertEquals(Orientation.HORIZONTAL, splContent.orientation)
        assertEquals(listOf(part("pnlTree"), part("pnlFiles")), splProject.items)
        assertEquals(listOf(part("pnlDetails"), part("pnlMedia"), part("pnlHistory")), splContent.items)
        assertEquals(listOf<Node>(splProject, splContent), splMain.items)
    }

    /**
     * Use case: the window opens with the dividers at 30 % (project side), 50 % (tree and file list)
     * and 20 % / 80 % (content area).
     */
    @Test
    fun dividersStartAtTheDefaultPositions() {
        assertEquals(listOf(0.3), splMain.dividerPositions.toList().map { it.rounded() })
        assertEquals(listOf(0.5), splProject.dividerPositions.toList().map { it.rounded() })
        assertEquals(listOf(0.2, 0.8), splContent.dividerPositions.toList().map { it.rounded() })
    }

    /**
     * Use case: the user enlarges the window, so the project side, the generation details and the
     * history list keep their width and only the media with the prompt input takes the difference.
     *
     * The window is shrunk first and then grown back to its initial size, because the headless
     * screen of the test cannot show a window larger than the initial one.
     */
    @Test
    fun sideAreasKeepTheirWidthWhenTheWindowGrows() {
        interact { window.width -= 400.0 }

        val projectWidth = splProject.width
        val detailsWidth = part("pnlDetails").layoutBounds.width
        val historyWidth = part("pnlHistory").layoutBounds.width
        val mediaWidth = part("pnlMedia").layoutBounds.width

        interact { window.width += 400.0 }

        assertEquals(projectWidth, splProject.width, 1.0)
        assertEquals(detailsWidth, part("pnlDetails").layoutBounds.width, 1.0)
        assertEquals(historyWidth, part("pnlHistory").layoutBounds.width, 1.0)
        assertEquals(mediaWidth + 400.0, part("pnlMedia").layoutBounds.width, 1.0)
    }

    /**
     * Use case: the user shrinks the window, so the side areas keep their width and the media with
     * the prompt input gives up the difference.
     */
    @Test
    fun sideAreasKeepTheirWidthWhenTheWindowShrinks() {
        val projectWidth = splProject.width
        val detailsWidth = part("pnlDetails").layoutBounds.width
        val historyWidth = part("pnlHistory").layoutBounds.width

        interact { window.width -= 200.0 }

        assertEquals(projectWidth, splProject.width, 1.0)
        assertEquals(detailsWidth, part("pnlDetails").layoutBounds.width, 1.0)
        assertEquals(historyWidth, part("pnlHistory").layoutBounds.width, 1.0)
    }

    /**
     * Use case: the media part holds the image above and the prompt input below it, so the input part is
     * shown below the image, inside the media part.
     */
    @Test
    fun promptInputSitsBelowTheImage() {
        val media = part("pnlMedia")
        val image = part("pnlImage")
        val input = part("pnlPromptInput")

        assertNotNull(input, "prompt input is missing")
        assertTrue(input.parent === media || input.parent?.parent === media)
        assertTrue(
            input.localToScene(input.layoutBounds).minY >= image.localToScene(image.layoutBounds).maxY - 1.0,
            "the prompt input must not overlap the image"
        )
        assertEquals(listOf(part("pnlDetails"), media, part("pnlHistory")), splContent.items)
    }

    /**
     * Use case: the window opens with a new, empty project. The tree shows the root folder only, the file
     * list is empty, and nothing fails.
     */
    @Test
    fun newProjectShowsAnEmptyTreeAndAnEmptyList() {
        assertEquals(listOf("${Messages["component.projectTree.root"]} (0)"), treeRows())
        assertTrue(fileCells().isEmpty())
    }

    /**
     * Use case: the project holds folders and files. The tree shows the folders with the number of their
     * children, selecting a folder lists its files, and their previews are loaded from the project - in the
     * background, although the project belongs to the FX thread.
     */
    @Test
    fun filledProjectShowsFoldersFilesAndPreviews() {
        val photos = addFolder(rootFolder, "Photos")
        addFolder(rootFolder, "Videos")
        addFile(photos, "A.png", 0xFF0000)
        addFile(photos, "B.png", 0x00FF00)

        val root = Messages["component.projectTree.root"]
        assertEquals(listOf("$root (2)", "Photos (2)", "Videos (0)"), treeRows())

        selectFolder(photos)
        assertEquals(listOf("A.png", "B.png"), fileNames())
        awaitTrue { fileCells().map { previewColors(it) } == listOf(listOf(0xFF0000, 0xFF0000), listOf(0x00FF00, 0x00FF00)) }
    }

    /**
     * Use case: the user selects another folder. The file list shows the files of that folder only.
     */
    @Test
    fun selectingAnotherFolderChangesTheList() {
        val photos = addFolder(rootFolder, "Photos")
        val videos = addFolder(rootFolder, "Videos")
        addFile(photos, "A.png", 0xFF0000)
        addFile(videos, "Clip.png", 0x0000FF)

        selectFolder(photos)
        assertEquals(listOf("A.png"), fileNames())

        selectFolder(videos)
        assertEquals(listOf("Clip.png"), fileNames())
    }

    /**
     * Use case: a file is added to the folder that is shown. The tree and the file list follow the project
     * without the user selecting anything again.
     */
    @Test
    fun changeInTheSelectedFolderAppearsInTheList() {
        val photos = addFolder(rootFolder, "Photos")
        addFile(photos, "A.png", 0xFF0000)
        selectFolder(photos)
        assertEquals(listOf("A.png"), fileNames())

        addFile(photos, "B.png", 0x00FF00)

        assertEquals(listOf("A.png", "B.png"), fileNames())
        assertTrue("Photos (2)" in treeRows())
    }

    /**
     * Use case: the project is new and nothing is selected. The history list is empty and the image view
     * shows no image.
     */
    @Test
    fun noSelectionShowsAnEmptyHistoryAndNoImage() {
        assertTrue(historyNumbers().isEmpty())
        assertEquals(null, shownColor())
    }

    /**
     * Use case: the user selects a file with three media. The history list shows all of them in order, the
     * last one is selected, and the image view shows it.
     */
    @Test
    fun selectingAFileShowsItsHistoryWithTheLastMediumSelected() {
        val photos = addFolder(rootFolder, "Photos")
        addFileWithMedia(photos, "A.png", 0xFF0000, 0x00FF00, 0x0000FF)
        selectFolder(photos)

        selectFirstFile()

        assertEquals(listOf("000", "001", "002"), historyNumbers())
        awaitTrue { history.selectionModel.selectedIndex == 2 }
        awaitTrue { shownColor() == 0x0000FF }
    }

    /**
     * Use case: the user selects another medium in the history list. The image view shows that medium.
     */
    @Test
    fun selectingAnotherMediumChangesTheImage() {
        val photos = addFolder(rootFolder, "Photos")
        addFileWithMedia(photos, "A.png", 0xFF0000, 0x00FF00, 0x0000FF)
        selectFolder(photos)
        selectFirstFile()
        awaitTrue { shownColor() == 0x0000FF }

        interact { history.selectionModel.select(0) }

        awaitTrue { shownColor() == 0xFF0000 }
    }

    /**
     * Use case: a medium is appended to the file that is shown, for instance by a generation. The history
     * list gets the new entry, selects it, and the image view shows the new medium.
     */
    @Test
    fun appendedMediumIsSelectedAndShown() {
        val photos = addFolder(rootFolder, "Photos")
        val reference = addFileWithMedia(photos, "A.png", 0xFF0000, 0x00FF00)
        selectFolder(photos)
        selectFirstFile()
        awaitTrue { shownColor() == 0x00FF00 }

        appendMedium(reference, 0x0000FF)

        awaitTrue { historyNumbers() == listOf("000", "001", "002") }
        awaitTrue { history.selectionModel.selectedIndex == 2 }
        awaitTrue { shownColor() == 0x0000FF }
    }

    /**
     * Use case: the prompt input offers the gallery the folders of the project, and loads the previews of
     * its chips from the project: it is bound to the root folder and to the media source of the window.
     */
    @Test
    fun promptInputGetsTheFolderAndTheMediaSource() {
        assertSame(rootFolder, promptInput.folderProperty().get())
        assertNotNull(promptInput.mediaSourceProperty().get())
        assertSame(promptInput.mediaSourceProperty().get(), fileList.mediaSourceProperty().get())
    }

    private fun detailsLines(): Pair<String, String> {
        WaitForAsyncUtils.waitForFxEvents()
        val details = window.scene.root.lookup("#pnlDetails")
        return (details.lookup("#lblModelFlow") as Label).text to (details.lookup("#lblPrompt") as Label).text
    }

    private fun addFileWithDetails(parent: VirtualFolder, name: String, vararg media: Medium): String {
        val reference = fx {
            ProjectManager.addReference(media.first(), "png", ByteArrayInputStream(pngBytes(60, 40, 0xFF0000)))
        }
        media.drop(1).forEach { medium ->
            fx { ProjectManager.appendMedium(reference, medium, "png", ByteArrayInputStream(pngBytes(60, 40, 0x00FF00))) }
        }
        fx { ProjectManager.createEntry(parent, name, reference) }
        return reference
    }

    /**
     * Use case: nothing is selected. The generation details at the top of the left part of the content area are
     * empty. A file whose media carry no details leaves them empty as well.
     */
    @Test
    fun detailsAreEmptyWithoutSelectionAndWithoutDetails() {
        assertEquals("" to "", detailsLines())

        val photos = addFolder(rootFolder, "Photos")
        addFileWithMedia(photos, "A.png", 0xFF0000)
        selectFolder(photos)
        selectFirstFile()
        awaitTrue { shownColor() == 0xFF0000 }

        assertEquals("" to "", detailsLines())
    }

    /**
     * Use case: the user selects a file and then another medium of its history. The details show model, flow
     * and prompt of the selected medium: the last one after selecting the file, the chosen one afterwards.
     */
    @Test
    fun detailsFollowTheSelectedMedium() {
        val photos = addFolder(rootFolder, "Photos")
        addFileWithDetails(
            photos, "A.png",
            Medium("A red fox", "model-1", "text-to-image"),
            Medium("The fox gets a hat", "model-2", "image-to-image"),
        )
        selectFolder(photos)

        selectFirstFile()
        awaitTrue { detailsLines() == ("model-2 · image-to-image" to "The fox gets a hat") }

        interact { history.selectionModel.select(0) }
        awaitTrue { detailsLines() == ("model-1 · text-to-image" to "A red fox") }
    }

    /**
     * Use case: a medium is appended to the shown file, for instance by a generation. The new medium is
     * selected and the details show its data.
     */
    @Test
    fun appendedMediumShowsItsDetails() {
        val photos = addFolder(rootFolder, "Photos")
        val reference = addFileWithDetails(photos, "A.png", Medium("A red fox", "model-1", "text-to-image"))
        selectFolder(photos)
        selectFirstFile()
        awaitTrue { detailsLines() == ("model-1 · text-to-image" to "A red fox") }

        fx {
            ProjectManager.appendMedium(
                reference, Medium("A blue fox", "model-3", null), "png", ByteArrayInputStream(pngBytes(60, 40, 0x0000FF)),
            )
        }

        awaitTrue { detailsLines() == ("model-3" to "A blue fox") }
    }

    /**
     * Use case: the user chooses Exit in the File menu, so the main window closes.
     */
    @Test
    fun exitClosesTheWindow() {
        interact { menuBar.menus.single().items.single().fire() }

        assertFalse(window.isShowing)
    }

    private fun Double.rounded(): Double = Math.round(this * 100.0) / 100.0
}
