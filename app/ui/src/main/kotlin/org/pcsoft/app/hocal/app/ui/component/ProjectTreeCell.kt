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

import javafx.beans.value.ChangeListener
import javafx.scene.control.Label
import javafx.scene.control.TreeCell
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.stage.Screen
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * Cell of the project tree: the folder icon, the folder name and the number of its direct children.
 *
 * The icon shows a closed folder while the node is collapsed and an open one while it is expanded, in
 * the variant for the light or the dark appearance. The name and the number are two labels of their own;
 * the number carries the style class `child-count`, which the stylesheet paints gray. A root folder
 * without a name is shown with the given replacement text.
 *
 * @param rootText text shown for a root folder with an empty name
 * @param dark tells whether the dark appearance is active, asked every time the cell is updated
 */
class ProjectTreeCell(private val rootText: String, private val dark: () -> Boolean) : TreeCell<VirtualFolder>() {
    private val icon = ImageView().apply {
        fitWidth = ICON_SIZE
        fitHeight = ICON_SIZE
        isPreserveRatio = true
        isSmooth = true
    }
    private val lblName = Label().apply { styleClass.add("folder-name") }
    private val lblCount = Label().apply { styleClass.add("child-count") }
    private val content = HBox(4.0, icon, lblName, lblCount)

    // Belongs to the tree item of the cell, so it lives and dies with the tree.
    private val expandedListener = ChangeListener<Boolean> { _, _, _ -> refreshContent() }

    init {
        // The item may stay equal (by value) while the tree item is exchanged, so the item alone is not a trigger.
        treeItemProperty().addListener { _, old, new ->
            old?.expandedProperty()?.removeListener(expandedListener)
            new?.expandedProperty()?.addListener(expandedListener)
            refreshContent()
        }
    }

    /**
     * Updates the cell for the given folder.
     *
     * @param item the folder of the cell, or `null`
     * @param empty `true` if the cell holds no item
     */
    override fun updateItem(item: VirtualFolder?, empty: Boolean) {
        super.updateItem(item, empty)
        refreshContent()
    }

    private fun refreshContent() {
        val folder = treeItem?.value
        text = null
        if (isEmpty || folder == null) {
            graphic = null
            return
        }

        val isRoot = treeItem.parent == null
        val name = if (treeItem.isExpanded) HocalIcons.FOLDER_OPEN else HocalIcons.FOLDER_CLOSED
        icon.image = HocalIcons.image(name, iconFileSize(), dark())
        lblName.text = if (isRoot && folder.name.isEmpty()) rootText else folder.name
        lblCount.text = "(${folder.children.size})"
        graphic = content
    }

    // The larger file keeps the icon sharp on a HiDPI screen, where it is scaled down to the display size.
    private fun iconFileSize(): Int = if (Screen.getPrimary().outputScaleX > 1.0) HocalIcons.FOLDER_SIZES.last() else HocalIcons.FOLDER_SIZES.first()

    private companion object {
        const val ICON_SIZE = 16.0
    }
}
