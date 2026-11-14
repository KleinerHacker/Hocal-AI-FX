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

import javafx.beans.value.ChangeListener
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.Tooltip
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.stage.Screen
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.Messages
import org.pcsoft.app.hocal.model.project.VirtualEntry

/**
 * Cell of the project file list: the preview of the original medium, the preview of the current medium
 * and the name of the entry.
 *
 * As long as a preview is not loaded, or cannot be, the placeholder icon is shown. A cell is reused for
 * other entries, so on every change of the entry the images are set anew from the preview of the new
 * entry, and nothing of the former one stays visible.
 *
 * @param dark tells whether the dark appearance is active, asked every time the cell is updated
 * @param previewOf gives the previews of an entry, and starts loading them if that has not happened yet
 */
class ProjectFileListCell(
    private val dark: () -> Boolean,
    private val previewOf: (VirtualEntry) -> ProjectFilePreview,
) : ListCell<VirtualEntry>() {
    private val originalView = newImageView(Messages["component.projectFileList.original"])
    private val currentView = newImageView(Messages["component.projectFileList.current"])
    private val lblName = Label().apply { styleClass.add("file-name") }
    private val content = HBox(4.0, originalView, currentView, lblName).apply { alignment = Pos.CENTER_LEFT }

    private var preview: ProjectFilePreview? = null

    // A field, so that it can be removed again from the preview of the former entry.
    private val imageListener = ChangeListener<Image?> { _, _, _ -> applyImages() }

    /**
     * Updates the cell for the given entry.
     *
     * @param item the entry of the cell, or `null`
     * @param empty `true` if the cell holds no item
     */
    override fun updateItem(item: VirtualEntry?, empty: Boolean) {
        super.updateItem(item, empty)
        detach()
        text = null
        if (empty || item == null) {
            graphic = null
            return
        }

        lblName.text = item.name
        preview = previewOf(item).also {
            it.original.addListener(imageListener)
            it.current.addListener(imageListener)
        }
        applyImages()
        graphic = content
    }

    private fun detach() {
        preview?.let {
            it.original.removeListener(imageListener)
            it.current.removeListener(imageListener)
        }
        preview = null
    }

    private fun applyImages() {
        val placeholder = HocalIcons.image(HocalIcons.MEDIA_PLACEHOLDER, fileSize(), dark())
        originalView.image = preview?.original?.get() ?: placeholder
        currentView.image = preview?.current?.get() ?: placeholder
    }

    // The larger file keeps the preview sharp on a HiDPI screen, where it is scaled down to the display size.
    private fun fileSize(): Int = if (Screen.getPrimary().outputScaleX > 1.0) HocalIcons.PREVIEW_SIZES.last() else HocalIcons.PREVIEW_SIZES.first()

    private fun newImageView(tooltip: String) = ImageView().apply {
        fitWidth = PREVIEW_SIZE
        fitHeight = PREVIEW_SIZE
        isPreserveRatio = true
        isSmooth = true
        Tooltip.install(this, Tooltip(tooltip))
    }

    private companion object {
        const val PREVIEW_SIZE = 48.0
    }
}
