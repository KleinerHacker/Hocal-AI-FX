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

import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.scene.control.TreeItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.fx.model.project.VirtualFolderProperty
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable

/**
 * Developer tests of the project tree view model, driven without a view.
 *
 * The view model dispatches to the FX thread, so every action runs there.
 */
class ProjectTreeViewModelTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val viewModel = ProjectTreeViewModel()

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    private fun flatten(item: TreeItem<VirtualFolder>?): List<TreeItem<VirtualFolder>> =
        if (item == null) emptyList() else listOf(item) + item.children.flatMap { flatten(it) }

    private fun values(item: TreeItem<VirtualFolder>): List<VirtualFolder> = item.children.map { it.value }

    private fun bind(root: VirtualFolder, show: Boolean = true): VirtualFolderProperty {
        val property = VirtualFolderProperty.of(root)
        fx {
            viewModel.bindTree(property)
            if (show) viewModel.onShow()
        }
        return property
    }

    private fun change(property: VirtualFolderProperty, action: () -> Unit) {
        fx {
            action()
            property.refresh()
        }
    }

    /**
     * Use case: a view model nobody bound a model to offers an empty tree and no selection, without
     * any error, also when it is shown and hidden.
     */
    @Test
    fun emptyTreeWithoutBinding() {
        fx {
            viewModel.onShow()
            viewModel.onHide()
        }

        assertNull(viewModel.rootItem.get())
        assertNull(viewModel.selectedFolder.get())
        assertNull(viewModel.itemToSelect)
    }

    /**
     * Use case: the tree is built from the folders of the model only. Entries do not appear, the root
     * folder is the topmost item and subfolders follow in the order of the model.
     */
    @Test
    fun treeMirrorsTheFoldersOnly() {
        val raw = folder("Raw", entry("Sunset.raw"))
        val photos = folder("Photos", entry("Sunset.png"), raw)
        val videos = folder("Videos")
        val root = folder("Project", entry("Readme.txt"), photos, videos)

        bind(root, show = false)

        val rootItem = viewModel.rootItem.get()!!
        assertSame(root, rootItem.value)
        assertEquals(listOf(photos, videos), values(rootItem))
        assertSame(photos, rootItem.children[0].value)
        assertSame(videos, rootItem.children[1].value)
        assertEquals(listOf(raw), values(rootItem.children[0]))
        assertTrue(rootItem.children[1].children.isEmpty())
        assertEquals(4, flatten(rootItem).size)
    }

    /**
     * Use case: a freshly shown tree has its root folder opened and every subfolder closed.
     */
    @Test
    fun rootStartsExpandedAndSubfoldersCollapsed() {
        val root = folder("Project", folder("Photos", folder("Raw")))

        bind(root)

        val rootItem = viewModel.rootItem.get()!!
        assertTrue(rootItem.isExpanded)
        assertFalse(rootItem.children[0].isExpanded)
    }

    /**
     * Use case: while the component is shown, a change in the model - here a new subfolder - is taken
     * over by building the tree anew.
     */
    @Test
    fun modelChangeIsTakenOverWhileShown() {
        val root = folder("Project", folder("Photos"))
        val property = bind(root)
        val before = viewModel.rootItem.get()

        val music = folder("Music")
        change(property) { root.children.add(music) }

        val after = viewModel.rootItem.get()!!
        assertTrue(before !== after)
        assertEquals(2, after.children.size)
        assertSame(music, after.children[1].value)
    }

    /**
     * Use case: a model change that arrives on another thread is handed to the FX thread and shown
     * there.
     */
    @Test
    fun changeFromAnotherThreadIsTakenOver() {
        val root = folder("Project")
        val property = bind(root)

        root.children.add(folder("Music"))
        property.refresh()
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(1, viewModel.rootItem.get()!!.children.size)
    }

    /**
     * Use case: the model holds no folder at all, either from the start or after the folder was
     * removed. The tree is empty and nothing fails.
     */
    @Test
    fun nullModelGivesAnEmptyTree() {
        val property = bind(folder("Project", folder("Photos")))
        assertTrue(viewModel.rootItem.get() != null)

        fx { property.set(null) }

        assertNull(viewModel.rootItem.get())
        assertNull(viewModel.selectedFolder.get())
    }

    /**
     * Use case: a folder is selected and the model changes elsewhere; the same folder is still selected
     * afterwards and the view is told which new item stands for it.
     */
    @Test
    fun selectionSurvivesTheRebuild() {
        val photos = folder("Photos", folder("Raw"))
        val root = folder("Project", photos)
        val property = bind(root)
        fx { viewModel.selectFolder(photos) }

        change(property) { root.children.add(folder("Music")) }

        assertSame(photos, viewModel.selectedFolder.get())
        assertSame(photos, viewModel.itemToSelect!!.value)
        assertSame(viewModel.rootItem.get()!!.children[0], viewModel.itemToSelect)
    }

    /**
     * Use case: the selected folder is deleted from the model; nothing is selected afterwards.
     */
    @Test
    fun selectionOfARemovedFolderIsCleared() {
        val photos = folder("Photos")
        val root = folder("Project", photos, folder("Music"))
        val property = bind(root)
        fx { viewModel.selectFolder(photos) }

        change(property) { root.children.remove(photos) }

        assertNull(viewModel.selectedFolder.get())
        assertNull(viewModel.itemToSelect)
    }

    /**
     * Use case: the user opened some folders, the model changes; the opened folders are open again, the
     * others closed, and the root keeps its state, closed included.
     */
    @Test
    fun expansionSurvivesTheRebuild() {
        val raw = folder("Raw", folder("Deep"))
        val photos = folder("Photos", raw)
        val videos = folder("Videos", folder("Clips"))
        val root = folder("Project", photos, videos)
        val property = bind(root)
        val items = flatten(viewModel.rootItem.get())
        items.first { it.value === photos }.isExpanded = true
        items.first { it.value === raw }.isExpanded = true

        change(property) { root.children.add(folder("Music")) }

        val after = flatten(viewModel.rootItem.get())
        assertTrue(after.first { it.value === photos }.isExpanded)
        assertTrue(after.first { it.value === raw }.isExpanded)
        assertFalse(after.first { it.value === videos }.isExpanded)

        after.first { it.value === root }.isExpanded = false
        change(property) { root.children.add(folder("Pictures")) }
        assertFalse(viewModel.rootItem.get()!!.isExpanded)
    }

    /**
     * Use case: two sibling folders are built exactly alike, so the model considers them equal. The
     * selection and the opened state belong to the one the user chose, not to its twin.
     */
    @Test
    fun selectionAndExpansionFollowIdentityNotEquality() {
        val first = folder("Twin", entry("Same.png"))
        val second = folder("Twin", entry("Same.png"))
        assertEquals(first, second)
        val root = folder("Project", first, second)
        val property = bind(root)
        fx { viewModel.selectFolder(second) }
        flatten(viewModel.rootItem.get()).last().isExpanded = true

        change(property) { root.children.add(folder("Other")) }

        val after = viewModel.rootItem.get()!!
        assertSame(second, viewModel.selectedFolder.get())
        assertSame(after.children[1], viewModel.itemToSelect)
        assertFalse(after.children[0].isExpanded)
        assertTrue(after.children[1].isExpanded)
    }

    /**
     * Use case: the wrapped root folder is exchanged, as when a new project is opened. The tree shows
     * the new folders and the former selection is gone.
     */
    @Test
    fun exchangingTheRootObjectShowsTheNewTree() {
        val oldPhotos = folder("Photos")
        val property = bind(folder("Old", oldPhotos))
        fx { viewModel.selectFolder(oldPhotos) }

        val newRoot = folder("New", folder("Music"))
        fx { property.set(newRoot) }

        val rootItem = viewModel.rootItem.get()!!
        assertSame(newRoot, rootItem.value)
        assertTrue(rootItem.isExpanded)
        assertNull(viewModel.selectedFolder.get())
    }

    /**
     * Use case: another property is bound. The tree shows its folder; the former model is not observed
     * any more and stays untouched, also when it changes afterwards.
     */
    @Test
    fun switchingTheBoundPropertyReleasesTheOldModel() {
        val oldRoot = folder("Old", folder("Photos"))
        val oldProperty = bind(oldRoot)
        val newRoot = folder("New")
        val newProperty = bind(newRoot)
        val shown = viewModel.rootItem.get()!!
        assertSame(newRoot, shown.value)

        change(oldProperty) { oldRoot.children.add(folder("Music")) }

        assertSame(shown, viewModel.rootItem.get())
        assertEquals(2, oldRoot.children.size)

        change(newProperty) { newRoot.children.add(folder("Videos")) }
        assertEquals(1, viewModel.rootItem.get()!!.children.size)
    }

    /**
     * Use case: the component leaves the screen. A model change is not taken over, the tree stays as
     * it was; showing it again builds it from the current model and observes it once more.
     */
    @Test
    fun hidingStopsTheObservationAndShowingRestoresIt() {
        val root = folder("Project")
        val property = bind(root)

        fx { viewModel.onHide() }
        change(property) { root.children.add(folder("Hidden")) }
        assertTrue(viewModel.rootItem.get()!!.children.isEmpty())

        fx { viewModel.onShow() }
        assertEquals(1, viewModel.rootItem.get()!!.children.size)

        change(property) { root.children.add(folder("Later")) }
        assertEquals(2, viewModel.rootItem.get()!!.children.size)
    }

    /**
     * Use case: show is reported twice and hide once, as can happen on quick screen changes. The
     * registration exists only once, so one hide is enough to stop the observation.
     */
    @Test
    fun doubleShowRegistersOnlyOnce() {
        val root = folder("Project")
        val property = bind(root)
        fx {
            viewModel.onShow()
            viewModel.onHide()
            viewModel.onHide()
        }

        change(property) { root.children.add(folder("Hidden")) }

        assertTrue(viewModel.rootItem.get()!!.children.isEmpty())
    }

    /**
     * Use case: the selected folder is offered read-only and is empty at first; it only changes through
     * the selection reported by the view.
     */
    @Test
    fun selectedFolderIsReadOnlyAndFollowsTheReportedSelection() {
        val photos = folder("Photos")
        bind(folder("Project", photos))

        assertTrue(viewModel.selectedFolder is ReadOnlyObjectProperty<*>)
        assertFalse(viewModel.selectedFolder is ObjectProperty<*>)
        assertNull(viewModel.selectedFolder.get())

        fx { viewModel.selectFolder(photos) }
        assertSame(photos, viewModel.selectedFolder.get())

        fx { viewModel.selectFolder(null) }
        assertNull(viewModel.selectedFolder.get())
    }
}
