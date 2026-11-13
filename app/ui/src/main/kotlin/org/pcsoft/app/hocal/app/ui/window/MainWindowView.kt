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

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.beans.binding.BooleanBinding
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import org.pcsoft.app.hocal.app.ui.component.ProjectFileList
import org.pcsoft.app.hocal.app.ui.component.ProjectTree
import org.pcsoft.app.hocal.app.ui.component.PromptInput
import org.pcsoft.app.hocal.app.ui.showingBinding
import java.net.URL
import java.util.ResourceBundle

/**
 * View of the application main window, holding the menu bar and the splitter layout.
 *
 * The layout is made of three nested split panes:
 *
 * * [splMain] divides the project side (left, 30 %) from the content area (right, 70 %)
 * * [splProject] divides the project side into the folder tree (top) and the file list (bottom), 50 % each
 * * [splContent] divides the content area into the generation details (20 %), the media with the
 *   prompt input (60 %) and the history list (20 %)
 *
 * The folder tree, the file list and the prompt input are bound to the project the view model follows: the tree
 * shows its folders, the file list the files of the selected folder, and the prompt input offers its folders in
 * the gallery. The other parts are placeholders until their components are built. Texts come from the global
 * resource bundle via the `%key` syntax of the FXML file.
 */
class MainWindowView : FxmlView<MainWindowViewModel>, Initializable {
    @FXML
    private lateinit var pnlRoot: BorderPane
    @FXML
    private lateinit var splMain: SplitPane
    @FXML
    private lateinit var splProject: SplitPane
    @FXML
    private lateinit var splContent: SplitPane
    @FXML
    private lateinit var trvProject: ProjectTree
    @FXML
    private lateinit var lstFiles: ProjectFileList
    @FXML
    private lateinit var promptInput: PromptInput

    @InjectViewModel
    private lateinit var viewModel: MainWindowViewModel

    // Kept as a field: a garbage collected binding stops notifying, and the registration would stay.
    private lateinit var showing: BooleanBinding

    /**
     * Initializes the view after the FXML file has been loaded.
     *
     * Sets the divider positions of the three split panes as soon as each of them has a size, binds the folder
     * tree, the file list and the prompt input to the project, and registers the view model at the project
     * only while the window is on screen.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        applyWhenSized(splMain, MAIN_DIVIDERS)
        applyWhenSized(splProject, PROJECT_DIVIDERS)
        applyWhenSized(splContent, CONTENT_DIVIDERS)

        trvProject.folderProperty().bind(viewModel.tree)
        viewModel.selectedFolder.bind(trvProject.selectedFolderProperty())

        lstFiles.folderProperty().bind(viewModel.listFolder)
        lstFiles.mediaSourceProperty().set(viewModel.mediaSource)
        viewModel.selectedFile.bind(lstFiles.selectedFileProperty())

        promptInput.folderProperty().bind(viewModel.tree)
        promptInput.mediaSourceProperty().set(viewModel.mediaSource)

        viewModel.onMediaChanged = { reference ->
            lstFiles.invalidate(reference)
            promptInput.invalidate(reference)
        }

        showing = pnlRoot.showingBinding()
        showing.addListener { _, _, onScreen -> if (onScreen) viewModel.onShow() else viewModel.onHide() }
    }

    /**
     * Sets the divider positions once the split pane has a size along its orientation.
     *
     * Positions are fractions of that size, so they only make sense after the first layout pass.
     */
    private fun applyWhenSized(pane: SplitPane, positions: DoubleArray) {
        val size = if (pane.orientation == Orientation.VERTICAL) pane.heightProperty() else pane.widthProperty()
        if (size.get() > 0.0) {
            pane.setDividerPositions(*positions)
            return
        }

        size.addListener(object : ChangeListener<Number> {
            override fun changed(observable: ObservableValue<out Number>, oldValue: Number, newValue: Number) {
                if (newValue.toDouble() > 0.0) {
                    observable.removeListener(this)
                    pane.setDividerPositions(*positions)
                }
            }
        })
    }

    /**
     * Exits the application by hiding the main window.
     *
     * This action is triggered by the Exit menu item. The window is hidden rather than closed
     * to allow for proper cleanup and potential "are you sure" dialogs in the future.
     */
    @FXML
    private fun actionExit() {
        pnlRoot.scene.window.hide()
    }

    private companion object {
        val MAIN_DIVIDERS = doubleArrayOf(0.3)
        val PROJECT_DIVIDERS = doubleArrayOf(0.5)
        val CONTENT_DIVIDERS = doubleArrayOf(0.2, 0.8)
    }
}
