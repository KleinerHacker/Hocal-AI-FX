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

import javafx.scene.control.Label
import javafx.scene.control.TreeCell
import javafx.scene.layout.HBox
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * Cell of the project tree: the folder name followed by the number of its direct children.
 *
 * The name and the number are two labels of their own; the number carries the style class
 * `child-count`, which the stylesheet paints gray. A root folder without a name is shown with the
 * given replacement text.
 *
 * @param rootText text shown for a root folder with an empty name
 */
class ProjectTreeCell(private val rootText: String) : TreeCell<VirtualFolder>() {
    private val lblName = Label().apply { styleClass.add("folder-name") }
    private val lblCount = Label().apply { styleClass.add("child-count") }
    private val content = HBox(4.0, lblName, lblCount)

    init {
        // The item may stay equal (by value) while the tree item is exchanged, so the item alone is not a trigger.
        treeItemProperty().addListener { _, _, _ -> refreshContent() }
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
        lblName.text = if (isRoot && folder.name.isEmpty()) rootText else folder.name
        lblCount.text = "(${folder.children.size})"
        graphic = content
    }
}
