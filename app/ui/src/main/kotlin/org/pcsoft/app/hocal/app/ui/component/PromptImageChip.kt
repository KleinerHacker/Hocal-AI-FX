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

import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.Messages
import org.pcsoft.app.hocal.app.ui.iconView

/**
 * Chip of an image attached to the prompt: the preview, the name and a button to remove the image.
 *
 * The preview stays empty until the image is loaded.
 *
 * @property image the image the chip shows
 * @param preview the preview of the image, `null` until loaded
 * @param dark whether the dark icons are used
 * @param onRemove called when the user clicks the remove button
 */
class PromptImageChip(
    val image: PromptImage,
    preview: ObservableValue<Image?>,
    dark: Boolean,
    onRemove: (PromptImage) -> Unit,
) : HBox(4.0) {
    /** The preview of the image. */
    val previewView: ImageView = ImageView().apply {
        styleClass.add("chip-preview")
        fitWidth = PromptInputViewModel.CHIP_SIZE.toDouble()
        fitHeight = PromptInputViewModel.CHIP_SIZE.toDouble()
        isPreserveRatio = true
        isSmooth = true
        imageProperty().bind(preview)
    }

    /** The name of the image. */
    val nameLabel: Label = Label(image.name).apply { styleClass.add("chip-name") }

    /** The button that removes the image. */
    val removeButton: Button = Button().apply {
        id = "btnRemove"
        styleClass.add("chip-remove")
        tooltip = Tooltip(Messages["component.promptInput.remove.tooltip"])
        accessibleText = Messages["component.promptInput.remove.tooltip"]
        setOnAction { onRemove(image) }
    }

    init {
        styleClass.add("prompt-chip")
        alignment = Pos.CENTER_LEFT
        children.addAll(previewView, nameLabel, removeButton)
        applyDark(dark)
    }

    /**
     * Exchanges the icon of the remove button for the given appearance.
     *
     * @param dark whether the dark icons are used
     */
    fun applyDark(dark: Boolean) {
        removeButton.graphic = iconView(HocalIcons.REMOVE, dark)
    }
}
