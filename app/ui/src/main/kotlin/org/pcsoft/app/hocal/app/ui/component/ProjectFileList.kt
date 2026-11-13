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
import javafx.scene.control.ListView
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * List of the entries of a virtual folder, each row with the preview of the original medium, the
 * preview of the current medium and the entry name.
 *
 * Only entries are shown, never subfolders. The component holds no data of its own: [folderProperty]
 * and [mediaSourceProperty] are bound by the caller, and the list is rebuilt whenever the folder reports
 * a change. The previews are loaded in the background, only for rows that are shown. The user's choice is
 * reported by [selectedFileProperty].
 *
 * The content is defined in `ProjectFileListView.fxml`, which is loaded with this node as its root.
 */
class ProjectFileList : ListView<VirtualEntry>() {
    private val viewModel: ProjectFileListViewModel

    init {
        FluentViewLoader.fxmlView(ProjectFileListView::class.java).root(this).load().apply {
            this@ProjectFileList.viewModel = viewModel
        }
    }

    /**
     * The folder whose entries are shown.
     *
     * Bind it to a folder property of the project model, for instance the selected folder of the
     * `ProjectTree`. The list is empty as long as it holds no folder. Binding another source releases
     * the former one.
     *
     * @return the property of the folder
     */
    fun folderProperty(): ObjectProperty<VirtualFolder?> = viewModel.folder

    /**
     * The source the previews are loaded from.
     *
     * Without a source the entries are shown with placeholder icons only.
     *
     * @return the property of the media source
     */
    fun mediaSourceProperty(): ObjectProperty<MediaSource?> = viewModel.mediaSource

    /**
     * The entry the user selected, or `null` while none is selected.
     *
     * @return the read-only property of the selected entry
     */
    fun selectedFileProperty(): ReadOnlyObjectProperty<VirtualEntry?> = viewModel.selectedFile
}
