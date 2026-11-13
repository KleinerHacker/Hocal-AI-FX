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

import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.TreeCell
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.Messages
import org.pcsoft.app.hocal.fx.model.project.VirtualFolderProperty
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

/**
 * Tests of the project tree component on real controls in a headless window.
 */
class ProjectTreeTest : ApplicationTest() {

    private lateinit var stage: Stage
    private lateinit var pane: StackPane
    private lateinit var tree: ProjectTree

    override fun start(stage: Stage) {
        this.stage = stage
        tree = ProjectTree()
        pane = StackPane(tree)
        stage.scene = Scene(pane, 400.0, 300.0)
        stage.show()
    }

    private fun bind(root: VirtualFolder): VirtualFolderProperty {
        val property = VirtualFolderProperty.of(root)
        interact { tree.folderProperty().bind(property) }
        return property
    }

    private fun change(property: VirtualFolderProperty, action: () -> Unit) {
        interact {
            action()
            property.refresh()
        }
    }

    private fun cells(): List<TreeCell<*>> {
        WaitForAsyncUtils.waitForFxEvents()
        return tree.lookupAll(".tree-cell").filterIsInstance<TreeCell<*>>().filter { !it.isEmpty }.sortedBy { it.layoutY }
    }

    private fun nameLabel(cell: TreeCell<*>): Label = (cell.graphic as HBox).children[0] as Label

    private fun countLabel(cell: TreeCell<*>): Label = (cell.graphic as HBox).children[1] as Label

    private fun rows(): List<String> = cells().map { "${nameLabel(it).text} ${countLabel(it).text}" }

    private fun cellOf(name: String): TreeCell<*> = cells().first { nameLabel(it).text == name }

    /**
     * Use case: a project with entries and subfolders is shown. Only the root and its folders appear as
     * rows, entries do not, and every row shows the number of the direct children of its folder -
     * entries and subfolders counted together.
     */
    @Test
    fun showsOnlyFoldersWithTheirDirectChildCount() {
        bind(folder("Project", entry("Readme.txt"), folder("Photos", entry("A.png"), entry("B.png"), folder("Raw")), folder("Videos")))

        assertEquals(listOf("Project (3)", "Photos (3)", "Videos (0)"), rows())
    }

    /**
     * Use case: the child count is a label of its own with the style class child-count, shown in gray,
     * and it is not part of the text of the folder name.
     */
    @Test
    fun childCountIsAGrayLabelOfItsOwn() {
        bind(folder("Project", folder("Photos")))
        interact { tree.applyCss() }

        val cell = cellOf("Photos")
        assertEquals("Photos", nameLabel(cell).text)
        assertTrue("child-count" in countLabel(cell).styleClass)
        assertFalse("child-count" in nameLabel(cell).styleClass)
        assertEquals(Color.GRAY, countLabel(cell).textFill)
    }

    /**
     * Use case: a root folder without a name, as in a fresh project, is shown with the text of the
     * message bundle instead of an empty row.
     */
    @Test
    fun rootWithEmptyNameShowsTheBundleText() {
        bind(folder("", folder("Photos")))

        assertEquals(listOf("${Messages["component.projectTree.root"]} (1)", "Photos (0)"), rows())
    }

    /**
     * Use case: the user clicks a folder, so it becomes the selected folder of the component; with no
     * click nothing is selected.
     */
    @Test
    fun clickingAFolderSelectsIt() {
        val photos = folder("Photos")
        val videos = folder("Videos")
        bind(folder("Project", photos, videos))
        assertNull(tree.selectedFolderProperty().get())

        clickOn(cellOf("Photos"))
        assertSame(photos, tree.selectedFolderProperty().get())

        clickOn(cellOf("Videos"))
        assertSame(videos, tree.selectedFolderProperty().get())
    }

    /**
     * Use case: the model changes while the folder is selected - here a folder is added. The rows are
     * rebuilt with the new count and the folder stays selected.
     */
    @Test
    fun modelChangeIsShownAndKeepsTheSelection() {
        val photos = folder("Photos")
        val root = folder("Project", photos)
        val property = bind(root)
        clickOn(cellOf("Photos"))

        change(property) { root.children.add(folder("Music")) }

        assertEquals(listOf("Project (2)", "Photos (0)", "Music (0)"), rows())
        assertSame(photos, tree.selectedFolderProperty().get())
        assertSame(photos, tree.selectionModel.selectedItem.value)
    }

    /**
     * Use case: without a bound model, or with a model holding no folder, the tree is empty and nothing
     * fails.
     */
    @Test
    fun emptyTreeWithoutModelOrFolder() {
        assertTrue(rows().isEmpty())
        assertNull(tree.root)

        val property = bind(folder("Project"))
        interact { property.set(null) }

        assertNull(tree.root)
        assertTrue(rows().isEmpty())
    }

    /**
     * Use case: another project is bound. The tree shows the new project, a change in the former one is
     * ignored and leaves it untouched.
     */
    @Test
    fun exchangingTheBoundObjectShowsTheNewOneOnly() {
        val oldRoot = folder("Old", folder("Photos"))
        val oldProperty = bind(oldRoot)
        val newRoot = folder("New", folder("Music"), folder("Videos"))
        bind(newRoot)

        assertEquals(listOf("New (2)", "Music (0)", "Videos (0)"), rows())

        change(oldProperty) { oldRoot.children.add(folder("Ignored")) }

        assertEquals(listOf("New (2)", "Music (0)", "Videos (0)"), rows())
        assertEquals(2, oldRoot.children.size)
    }

    private fun assertChangeIsIgnoredUntilBack(
        root: VirtualFolder,
        property: VirtualFolderProperty,
        leave: () -> Unit,
        back: () -> Unit,
    ) {
        leave()
        change(property) { root.children.add(folder("WhileAway")) }
        assertEquals(0, tree.root.children.size, "a change while off screen must not be taken over")

        back()
        assertEquals(1, tree.root.children.size, "returning shows the current model")

        change(property) { root.children.add(folder("After")) }
        assertEquals(2, tree.root.children.size, "a further change must be noticed again")
    }

    /**
     * Use case: the component is set invisible. A model change meanwhile is not taken over; when it is
     * visible again the tree shows the current model and notices further changes.
     */
    @Test
    fun invisibleComponentDoesNotObserveTheModel() {
        val root = folder("Project")
        val property = bind(root)

        assertChangeIsIgnoredUntilBack(root, property, { interact { tree.isVisible = false } }, { interact { tree.isVisible = true } })
    }

    /**
     * Use case: the component is removed from the scene. A model change meanwhile is not taken over; when
     * it is added again the tree shows the current model and notices further changes.
     */
    @Test
    fun componentRemovedFromTheSceneDoesNotObserveTheModel() {
        val root = folder("Project")
        val property = bind(root)

        assertChangeIsIgnoredUntilBack(root, property, { interact { pane.children.remove(tree) } }, { interact { pane.children.add(tree) } })
    }

    /**
     * Use case: the window of the component is hidden. A model change meanwhile is not taken over; when
     * the window is shown again the tree shows the current model and notices further changes. The window
     * keeps its size, as the headless screen cannot show a larger one.
     */
    @Test
    fun hiddenWindowDoesNotObserveTheModel() {
        val root = folder("Project")
        val property = bind(root)

        assertChangeIsIgnoredUntilBack(root, property, { interact { stage.hide() } }, { interact { stage.show() } })
    }
}
