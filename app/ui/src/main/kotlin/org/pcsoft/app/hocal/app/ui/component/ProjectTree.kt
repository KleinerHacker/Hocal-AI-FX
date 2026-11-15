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
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.scene.control.TreeView
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * Tree of the virtual folders of a project.
 *
 * Only folders are shown, never entries. The root folder is the topmost node; behind every folder name
 * stands, in gray, the number of its direct children (entries and subfolders). The component holds no
 * data of its own: its [folderProperty] is bound to the tree property of the project model, and the tree
 * is rebuilt whenever that property reports a change. The user's choice is reported by
 * [selectedFolderProperty].
 *
 * The content is defined in `ProjectTreeView.fxml`, which is loaded with this node as its root.
 */
class ProjectTree : TreeView<VirtualFolder>() {
    private val viewModel: ProjectTreeViewModel

    init {
        FluentViewLoader.fxmlView(ProjectTreeView::class.java).root(this).load().apply {
            this@ProjectTree.viewModel = viewModel
        }
    }

    /**
     * The root folder of the tree, the single interface of the component.
     *
     * Bind it to the tree property of the open project, for instance
     * `projectTree.folderProperty().bind(ProjectManager.project.treeProperty)`. The tree is empty as long
     * as the property holds no folder. Binding another source releases the former one.
     *
     * @return the property of the root folder
     */
    fun folderProperty(): ObjectProperty<VirtualFolder?> = viewModel.folder

    /**
     * The folder the user selected, or `null` while none is selected.
     *
     * @return the read-only property of the selected folder
     */
    fun selectedFolderProperty(): ReadOnlyObjectProperty<VirtualFolder?> = viewModel.selectedFolder
}
