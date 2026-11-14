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

import de.saxsys.mvvmfx.FxmlView
import de.saxsys.mvvmfx.InjectViewModel
import javafx.beans.binding.BooleanBinding
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.pcsoft.app.hocal.app.ui.showingBinding
import java.net.URL
import java.util.ResourceBundle

/**
 * View of the generation details, living in the root pane itself (`fx:root`).
 *
 * The two labels show one line each. A text longer than the width of the panel is cut with `...` by the label
 * itself, so the length follows the width of the panel. The observation of the medium is tied to the screen state.
 */
class GenerationDetailsView : FxmlView<GenerationDetailsViewModel>, Initializable {
    @FXML
    private lateinit var pnlRoot: VBox

    @FXML
    private lateinit var lblModelFlow: Label

    @FXML
    private lateinit var lblPrompt: Label

    @InjectViewModel
    private lateinit var viewModel: GenerationDetailsViewModel

    // Kept as a field: a garbage collected binding stops notifying, and the registration would stay.
    private lateinit var showing: BooleanBinding

    /**
     * Initializes the view after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        lblModelFlow.textProperty().bind(viewModel.modelFlow)
        lblPrompt.textProperty().bind(viewModel.prompt)

        showing = pnlRoot.showingBinding()
        showing.addListener { _, _, onScreen -> if (onScreen) viewModel.onShow() else viewModel.onHide() }
    }
}
