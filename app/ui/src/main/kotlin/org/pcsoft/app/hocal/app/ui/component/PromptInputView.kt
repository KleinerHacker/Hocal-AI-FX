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
import javafx.application.Platform
import javafx.beans.binding.BooleanBinding
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.ToggleButton
import javafx.scene.control.Tooltip
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import org.controlsfx.control.SegmentedButton
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.Messages
import org.pcsoft.app.hocal.app.ui.iconView
import org.pcsoft.app.hocal.app.ui.prompt.FlowOption
import org.pcsoft.app.hocal.app.ui.prompt.ModelOption
import org.pcsoft.app.hocal.app.ui.showingBinding
import java.net.URL
import java.util.ResourceBundle

/**
 * View of the prompt input, living in the root box itself (`fx:root`).
 *
 * It connects the controls with the view model, keeps the model box and the flow bar in sync with the
 * chosen model and flow, shows a chip per image and ties the observation of the appearance to the
 * screen state of the component.
 */
class PromptInputView : FxmlView<PromptInputViewModel>, Initializable {
    @FXML
    private lateinit var pnlRoot: VBox

    @FXML
    private lateinit var btnAdd: Button

    @FXML
    private lateinit var pnlChips: FlowPane

    @FXML
    private lateinit var cmbModel: ComboBox<ModelOption>

    @FXML
    private lateinit var segFlows: SegmentedButton

    @FXML
    private lateinit var pnlParameters: Region

    @FXML
    private lateinit var txtPrompt: TextArea

    @FXML
    private lateinit var btnSend: Button

    @InjectViewModel
    private lateinit var viewModel: PromptInputViewModel

    // Kept as a field: a garbage collected binding stops notifying, and the registration would stay.
    private lateinit var showing: BooleanBinding

    // True while the view itself changes the flow bar, so that it is not reported back as a user choice.
    private var syncing = false

    /**
     * Initializes the view after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        btnAdd.tooltip = Tooltip(Messages["component.promptInput.add.tooltip"])
        btnAdd.accessibleText = Messages["component.promptInput.add.tooltip"]
        btnSend.tooltip = Tooltip(Messages["component.promptInput.send.tooltip"])
        btnSend.accessibleText = Messages["component.promptInput.send.tooltip"]
        txtPrompt.promptText = Messages["component.promptInput.prompt.hint"]
        cmbModel.promptText = Messages["component.promptInput.model.prompt"]

        btnAdd.setOnAction { viewModel.addImage(pnlRoot.scene?.window) }
        btnSend.setOnAction { viewModel.send() }
        btnSend.disableProperty().bind(viewModel.sendDisabled)
        txtPrompt.textProperty().bindBidirectional(viewModel.prompt)

        cmbModel.converter = object : StringConverter<ModelOption>() {
            override fun toString(model: ModelOption?): String = model?.name ?: ""
            override fun fromString(string: String?): ModelOption? = null
        }
        cmbModel.items = viewModel.models
        cmbModel.valueProperty().bindBidirectional(viewModel.selectedModel)

        viewModel.flows.addListener(ListChangeListener { rebuildFlowButtons() })
        viewModel.selectedFlow.addListener { _, _, _ -> applyFlowSelection() }
        rebuildFlowButtons()

        viewModel.images.addListener(ListChangeListener { rebuildChips() })
        rebuildChips()

        applyIcons()
        viewModel.dark.addListener { _, _, _ -> applyIcons() }

        showing = pnlRoot.showingBinding()
        showing.addListener { _, _, onScreen -> if (onScreen) viewModel.onShow() else viewModel.onHide() }
    }

    private fun applyIcons() {
        val dark = viewModel.dark.get()
        btnAdd.graphic = iconView(HocalIcons.ADD, dark)
        btnSend.graphic = iconView(HocalIcons.SEND, dark)
        pnlChips.children.filterIsInstance<PromptImageChip>().forEach { it.applyDark(dark) }
    }

    private fun rebuildChips() {
        pnlChips.children.setAll(viewModel.images.map { image ->
            PromptImageChip(image, viewModel.previewOf(image.reference), viewModel.dark.get()) { viewModel.removeImage(it) }
        })
    }

    private fun rebuildFlowButtons() {
        syncing = true
        try {
            segFlows.buttons.setAll(viewModel.flows.map { flow ->
                ToggleButton(flow.name).apply {
                    id = "flow-${flow.id}"
                    userData = flow
                    selectedProperty().addListener { _, _, selected -> onToggled(flow, selected) }
                }
            })
        } finally {
            syncing = false
        }
        applyFlowSelection()
    }

    private fun onToggled(flow: FlowOption, selected: Boolean) {
        if (syncing) return
        if (selected) {
            viewModel.selectedFlow.set(flow)
        } else {
            // The bar always shows the chosen flow: a click on the selected button must not clear it.
            Platform.runLater { applyFlowSelection() }
        }
    }

    private fun applyFlowSelection() {
        val wanted = viewModel.selectedFlow.get()?.id
        syncing = true
        try {
            segFlows.buttons.forEach { it.isSelected = (it.userData as FlowOption).id == wanted }
        } finally {
            syncing = false
        }
    }
}
