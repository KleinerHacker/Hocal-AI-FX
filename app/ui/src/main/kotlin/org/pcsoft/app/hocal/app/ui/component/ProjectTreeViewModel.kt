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

import de.saxsys.mvvmfx.ViewModel
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeItem
import org.pcsoft.app.hocal.model.project.VirtualFolder
import java.util.Collections
import java.util.IdentityHashMap

/**
 * View model of the project tree.
 *
 * It builds the tree items from the folder of [folder] and nothing else; it keeps no copy of the data.
 * The folders compare by value, so selection and expansion are tracked by object identity throughout -
 * two sibling folders of equal content stay two different folders.
 *
 * [folder] is bound from outside to the tree property of the project model, which reports every change
 * inside the tree as an invalidation. The project model is global, therefore the view model reacts to
 * [folder] only between [onShow] and [onHide], and the view calls the two according to the screen state
 * of the component.
 */
class ProjectTreeViewModel : ViewModel {
    private val selectedFolderWrapper = ReadOnlyObjectWrapper<VirtualFolder?>(this, "selectedFolder")
    private val rootItemWrapper = ReadOnlyObjectWrapper<TreeItem<VirtualFolder>?>(this, "rootItem")

    private var observing = false

    // A field: a method reference would be a new object per use and could not be removed again.
    private val modelListener = InvalidationListener { onModelInvalidated() }

    /**
     * The root folder of the tree to show, `null` for an empty tree.
     *
     * The property is the single interface to the data: the caller binds it to the property model of
     * the project. A new binding replaces the former one, which is released by JavaFX.
     */
    val folder: ObjectProperty<VirtualFolder?> = SimpleObjectProperty(this, "folder")

    /** The folder the user selected, or `null` while none is selected. */
    val selectedFolder: ReadOnlyObjectProperty<VirtualFolder?> = selectedFolderWrapper.readOnlyProperty

    /** The root item of the tree to show, or `null` for an empty tree. */
    val rootItem: ReadOnlyObjectProperty<TreeItem<VirtualFolder>?> = rootItemWrapper.readOnlyProperty

    /**
     * The item of the new tree that stands for the selected folder, set by every rebuild before
     * [rootItem] is changed, or `null` if the selected folder is gone.
     */
    var itemToSelect: TreeItem<VirtualFolder>? = null
        private set

    /**
     * Reports the folder the user selected.
     *
     * @param folder the selected folder, or `null` for no selection
     */
    fun selectFolder(folder: VirtualFolder?) {
        selectedFolderWrapper.set(folder)
    }

    /**
     * Starts observing [folder] and builds the tree from its current state.
     *
     * Called when the component comes on screen. Calling it twice registers only once.
     */
    fun onShow() {
        if (!observing) {
            observing = true
            folder.addListener(modelListener)
        }
        rebuild()
    }

    /**
     * Stops observing [folder]. The tree stays as it is.
     *
     * Called when the component leaves the screen. Calling it twice is harmless.
     */
    fun onHide() {
        if (observing) {
            observing = false
            folder.removeListener(modelListener)
        }
    }

    private fun onModelInvalidated() {
        // Reading again is a must: a property reports the next change only after its value was read.
        folder.get()
        if (Platform.isFxApplicationThread()) {
            if (observing) rebuild()
        } else {
            Platform.runLater { if (observing) rebuild() }
        }
    }

    private fun rebuild() {
        val oldRoot = rootItemWrapper.get()
        val expanded: MutableSet<VirtualFolder> = Collections.newSetFromMap(IdentityHashMap())
        oldRoot?.let { collectExpanded(it, expanded) }

        val rootFolder = folder.get()
        val newRoot = rootFolder?.let { buildItem(it, expanded) }
        // The root starts expanded; the same root folder keeps the state it had.
        newRoot?.isExpanded = if (oldRoot != null && oldRoot.value === rootFolder) oldRoot.isExpanded else true

        val selected = selectedFolderWrapper.get()
        val selectedItem = if (selected != null && newRoot != null) findItem(newRoot, selected) else null
        if (selected != null && selectedItem == null) selectedFolderWrapper.set(null)

        itemToSelect = selectedItem
        rootItemWrapper.set(newRoot)
    }

    private fun buildItem(folder: VirtualFolder, expanded: Set<VirtualFolder>): TreeItem<VirtualFolder> {
        val item = TreeItem(folder)
        item.children.setAll(folder.children.filterIsInstance<VirtualFolder>().map { buildItem(it, expanded) })
        item.isExpanded = expanded.contains(folder)
        return item
    }

    private fun collectExpanded(item: TreeItem<VirtualFolder>, target: MutableSet<VirtualFolder>) {
        if (item.isExpanded) target.add(item.value)
        item.children.forEach { collectExpanded(it, target) }
    }

    private fun findItem(item: TreeItem<VirtualFolder>, folder: VirtualFolder): TreeItem<VirtualFolder>? {
        if (item.value === folder) return item
        for (child in item.children) {
            findItem(child, folder)?.let { return it }
        }
        return null
    }
}
