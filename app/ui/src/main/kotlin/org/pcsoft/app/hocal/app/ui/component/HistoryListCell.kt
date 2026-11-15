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

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.stage.Screen
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.ui.preview.LoadHandle

/**
 * Cell of the history list: the preview of the medium and its number.
 *
 * As long as the preview is not loaded, or cannot be, the placeholder icon is shown. A cell is reused for
 * other media, so on every update the running load is cancelled and the placeholder is shown at once;
 * nothing of the former medium stays visible.
 *
 * @param dark tells whether the dark appearance is active, asked every time the cell is updated
 * @param loadOf starts loading the preview of a medium and returns the handle to cancel it, or `null`
 */
class HistoryListCell(
    private val dark: () -> Boolean,
    private val loadOf: (HistoryMedium, (Image?) -> Unit) -> LoadHandle?,
) : ListCell<HistoryMedium>() {
    private val previewView = ImageView().apply {
        fitWidth = PREVIEW_SIZE
        fitHeight = PREVIEW_SIZE
        isPreserveRatio = true
        isSmooth = true
    }
    private val lblNumber = Label().apply { styleClass.add("history-number") }
    private val content = HBox(8.0, previewView, lblNumber).apply { alignment = Pos.CENTER_LEFT }

    private var handle: LoadHandle? = null

    // Tells apart the loads of this cell, so that the result of a former medium is never shown.
    private var loadToken = 0

    /**
     * Updates the cell for the given medium.
     *
     * @param item the medium of the cell, or `null`
     * @param empty `true` if the cell holds no item
     */
    override fun updateItem(item: HistoryMedium?, empty: Boolean) {
        super.updateItem(item, empty)
        handle?.cancel()
        handle = null
        val token = ++loadToken
        text = null
        if (empty || item == null) {
            graphic = null
            previewView.image = null
            return
        }

        lblNumber.text = item.label
        previewView.image = placeholder()
        graphic = content
        handle = loadOf(item) { image ->
            if (token == loadToken) previewView.image = image ?: placeholder()
        }
    }

    private fun placeholder(): Image = HocalIcons.image(HocalIcons.MEDIA_PLACEHOLDER, fileSize(), dark())

    // The larger file keeps the preview sharp on a HiDPI screen, where it is scaled down to the display size.
    private fun fileSize(): Int =
        if (Screen.getPrimary().outputScaleX > 1.0) HocalIcons.PREVIEW_SIZES.last() else HocalIcons.PREVIEW_SIZES.first()

    private companion object {
        const val PREVIEW_SIZE = 48.0
    }
}
