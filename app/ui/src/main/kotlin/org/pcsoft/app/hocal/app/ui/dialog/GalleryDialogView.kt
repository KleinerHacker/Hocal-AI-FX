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

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.SplitPane
import org.pcsoft.app.hocal.app.ui.component.ProjectFileList
import org.pcsoft.app.hocal.app.ui.component.ProjectTree
import java.net.URL
import java.util.ResourceBundle

/**
 * View of the gallery dialog: the project tree on the left, the file list of the selected folder on the right.
 *
 * The view wires the components to the view model: the tree shows the root folder, the list shows the
 * folder selected in the tree, and the entry selected in the list becomes the chosen entry.
 */
class GalleryDialogView : FxmlView<GalleryDialogViewModel>, Initializable {
    @FXML
    private lateinit var splRoot: SplitPane
    @FXML
    private lateinit var trvProject: ProjectTree
    @FXML
    private lateinit var lstFiles: ProjectFileList

    @InjectViewModel
    private lateinit var viewModel: GalleryDialogViewModel

    /**
     * Initializes the view after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        splRoot.setDividerPositions(DIVIDER)

        trvProject.folderProperty().bind(viewModel.rootFolder)
        lstFiles.mediaSourceProperty().bind(viewModel.mediaSource)
        lstFiles.folderProperty().bind(trvProject.selectedFolderProperty())
        viewModel.selectedEntry.bind(lstFiles.selectedFileProperty())
    }

    /** Releases the resources of the file list, namely the thread pool of the thumbnails. Called when the dialog is hidden. */
    fun close() {
        lstFiles.close()
    }

    private companion object {
        const val DIVIDER = 0.3
    }
}
