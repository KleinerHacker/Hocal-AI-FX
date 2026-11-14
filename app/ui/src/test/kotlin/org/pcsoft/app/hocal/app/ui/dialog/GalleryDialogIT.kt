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

package org.pcsoft.app.hocal.app.ui.dialog

import de.saxsys.mvvmfx.MvvmFX
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.layout.StackPane
import javafx.stage.Modality
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.Messages
import org.pcsoft.app.hocal.app.ui.component.ProjectFileList
import org.pcsoft.app.hocal.app.ui.component.ProjectTree
import org.pcsoft.app.hocal.app.ui.component.entry
import org.pcsoft.app.hocal.app.ui.component.folder
import org.pcsoft.app.hocal.app.ui.preview.FakeMediaSource
import org.pcsoft.app.hocal.app.ui.preview.pngBytes
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.util.Locale
import java.util.ResourceBundle
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Integration tests of the gallery dialog, operated through its real controls in a headless window.
 *
 * The dialog is never shown with `showAndWait` on the test thread: it is shown with `show`, or the
 * blocking call runs inside the FX thread while the test goes on.
 */
class GalleryDialogIT : ApplicationTest() {

    private lateinit var owner: Stage
    private lateinit var dialog: GalleryDialog
    private val source = FakeMediaSource()

    private val rootEntry = referenced("Root.png", "r0")
    private val alpha = referenced("Alpha.png", "r1")
    private val beta = referenced("Beta.png", "r2")
    private val photos = folder("Photos", alpha, beta)
    private val emptyFolder = folder("Empty")
    private val root = folder("Project", rootEntry, photos, emptyFolder)

    @Volatile
    private var finished = false

    @Volatile
    private var result: VirtualEntry? = null

    override fun start(stage: Stage) {
        MvvmFX.setGlobalResourceBundle(ResourceBundle.getBundle(Messages.BUNDLE_NAME, Locale.ENGLISH))

        owner = stage
        stage.scene = Scene(StackPane(), 200.0, 100.0)
        stage.show()
    }

    @AfterEach
    fun releaseDialog() {
        if (::dialog.isInitialized) interact { dialog.hide() }
    }

    private fun referenced(name: String, reference: String): VirtualEntry {
        source.add(reference, "png" to pngBytes(24, 24))
        return entry(name).apply { this.reference = reference }
    }

    private fun create(folder: VirtualFolder?, withSource: Boolean = true) {
        interact {
            dialog = GalleryDialog(owner)
            dialog.folderProperty().set(folder)
            if (withSource) dialog.mediaSourceProperty().set(source)
        }
    }

    private fun open(folder: VirtualFolder? = root, withSource: Boolean = true) {
        create(folder, withSource)
        interact { dialog.show() }
    }

    // Blocks inside the FX thread, as the application does; the test thread goes on.
    private fun openAndAwait() {
        result = null
        finished = false
        create(root)
        Platform.runLater {
            result = dialog.showAndSelect()
            finished = true
        }
        WaitForAsyncUtils.waitForFxEvents()
    }

    private fun awaitFinished() {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { finished })
    }

    private val tree: ProjectTree
        get() = dialog.dialogPane.lookup("#trvProject") as ProjectTree

    private val list: ProjectFileList
        get() = dialog.dialogPane.lookup("#lstFiles") as ProjectFileList

    private val ok: Button
        get() = dialog.dialogPane.lookupButton(ButtonType.OK) as Button

    private val cancel: Button
        get() = dialog.dialogPane.lookupButton(ButtonType.CANCEL) as Button

    private fun selectFolder(name: String) {
        interact { tree.selectionModel.select(tree.root.children.first { it.value.name == name }) }
        WaitForAsyncUtils.waitForFxEvents()
    }

    private fun selectRoot() {
        interact { tree.selectionModel.select(tree.root) }
        WaitForAsyncUtils.waitForFxEvents()
    }

    private fun selectEntry(index: Int) {
        interact { list.selectionModel.select(index) }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * Use case: the dialog opens with the title of the gallery, shows the tree and the list, and offers
     * exactly the standard buttons OK and Cancel.
     */
    @Test
    fun dialogShowsTreeListTitleAndButtons() {
        open()

        assertTrue(dialog.isShowing)
        assertEquals(Messages["dialog.gallery.title"], dialog.title)
        assertTrue(tree.isVisible && tree.scene != null)
        assertTrue(list.isVisible && list.scene != null)
        assertEquals(listOf(ButtonType.OK, ButtonType.CANCEL), dialog.dialogPane.buttonTypes)
        assertNull(dialog.graphic)
        assertNull(dialog.headerText)
        assertTrue(dialog.isResizable)
    }

    /**
     * Use case: a dialog with an owner window is window modal to it, a dialog without owner is
     * application modal.
     */
    @Test
    fun dialogIsModal() {
        create(root)
        assertEquals(Modality.WINDOW_MODAL, dialog.modality)
        assertSame(owner, dialog.owner)

        interact { dialog = GalleryDialog(null) }
        assertEquals(Modality.APPLICATION_MODAL, dialog.modality)
        assertNull(dialog.owner)
    }

    /**
     * Use case: the dialog gets a project; the tree shows the root folder and its subfolders.
     */
    @Test
    fun rootAndSubfoldersAppearInTheTree() {
        open()
        WaitForAsyncUtils.waitForFxEvents()

        assertSame(root, tree.root.value)
        assertEquals(listOf("Photos", "Empty"), tree.root.children.map { it.value.name })
    }

    /**
     * Use case: the user selects folders in the tree; the list shows the entries of the selected folder
     * only, no folders.
     */
    @Test
    fun listShowsTheEntriesOfTheSelectedFolder() {
        open()

        selectRoot()
        assertEquals(listOf("Root.png"), list.items.map { it.name })

        selectFolder("Photos")
        assertEquals(listOf("Alpha.png", "Beta.png"), list.items.map { it.name })
        assertSame(alpha, list.items[0])

        selectFolder("Empty")
        assertTrue(list.items.isEmpty())
    }

    /**
     * Use case: the user only selects a folder and no entry. Folders are no choice, so nothing is chosen
     * and the OK button stays disabled, while Cancel is always active.
     */
    @Test
    fun foldersCannotBeChosen() {
        open()

        selectFolder("Photos")

        assertNull(list.selectionModel.selectedItem)
        assertNull(dialog.selectedEntryProperty().get())
        assertTrue(ok.isDisable)
        assertFalse(cancel.isDisable)
    }

    /**
     * Use case: the user selects an entry in the list; the OK button becomes active. Switching to another
     * folder clears the choice, so the button is disabled again.
     */
    @Test
    fun selectingAnEntryEnablesOk() {
        open()
        selectFolder("Photos")

        selectEntry(1)
        assertSame(beta, dialog.selectedEntryProperty().get())
        assertFalse(ok.isDisable)

        selectFolder("Empty")
        assertNull(dialog.selectedEntryProperty().get())
        assertTrue(ok.isDisable)
    }

    /**
     * Use case: the user selects an entry and confirms with OK; the dialog closes and hands over exactly
     * the selected entry of the project.
     */
    @Test
    fun okReturnsTheSelectedEntry() {
        openAndAwait()
        selectFolder("Photos")
        selectEntry(0)

        interact { ok.fire() }
        awaitFinished()

        assertSame(alpha, result)
        assertFalse(dialog.isShowing)
    }

    /**
     * Use case: the user selects an entry but cancels; the dialog closes and hands over nothing.
     */
    @Test
    fun cancelReturnsNull() {
        openAndAwait()
        selectFolder("Photos")
        selectEntry(0)

        interact { cancel.fire() }
        awaitFinished()

        assertNull(result)
        assertFalse(dialog.isShowing)
    }

    /**
     * Use case: the user closes the dialog with ESC or the X button of the window (both close the
     * dialog); this counts as cancel even with an entry selected, and hands over nothing.
     */
    @Test
    fun closingTheWindowReturnsNull() {
        openAndAwait()
        selectFolder("Photos")
        selectEntry(1)

        interact { dialog.close() }
        awaitFinished()

        assertNull(result)
        assertFalse(dialog.isShowing)
    }

    /**
     * Use case: the dialog is opened without root folder and without media source; the tree is empty,
     * the list shows nothing and the OK button is disabled.
     */
    @Test
    fun emptyDialogShowsAnEmptyTreeAndDisabledOk() {
        open(folder = null, withSource = false)
        WaitForAsyncUtils.waitForFxEvents()

        assertNull(tree.root)
        assertTrue(list.items.isEmpty())
        assertTrue(ok.isDisable)
        assertFalse(cancel.isDisable)
    }

    /**
     * Use case: the dialog is closed; the file list ends the thread pool of its previews. Proven by an
     * entry that is shown after the dialog was hidden and shown again: while the dialog was open the
     * previews were requested from the source, afterwards no further request reaches it.
     */
    @Test
    fun closingTheDialogEndsThePreviewPool() {
        open()
        selectFolder("Photos")
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { source.openRequested.get() >= 1 })

        interact { dialog.hide() }
        val requested = source.openRequested.get()

        val later = referenced("Later.png", "r10")
        val laterRoot = folder("Later project", later)
        interact {
            dialog.show()
            dialog.folderProperty().set(laterRoot)
        }
        selectRoot()
        Thread.sleep(300)
        WaitForAsyncUtils.waitForFxEvents()

        assertSame(later, list.items.single())
        assertEquals(requested, source.openRequested.get())
    }
}
