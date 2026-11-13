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
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import java.net.URL
import java.util.ResourceBundle

/**
 * View of the application main window, holding the menu bar and the splitter layout.
 *
 * The layout is made of three nested split panes:
 *
 * * [splMain] divides the project side (left, 30 %) from the content area (right, 70 %)
 * * [splProject] divides the project side into the tree (top) and the file list (bottom), 50 % each
 * * [splContent] divides the content area into the generation details (20 %), the media with the
 *   prompt input (60 %) and the history list (20 %)
 *
 * The parts are placeholders until their components are built. Texts come from the global resource
 * bundle via the `%key` syntax of the FXML file.
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

    @InjectViewModel
    private lateinit var viewModel: MainWindowViewModel

    /**
     * Initializes the view after the FXML file has been loaded.
     *
     * Sets the divider positions of the three split panes as soon as each of them has a size.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        applyWhenSized(splMain, MAIN_DIVIDERS)
        applyWhenSized(splProject, PROJECT_DIVIDERS)
        applyWhenSized(splContent, CONTENT_DIVIDERS)
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
