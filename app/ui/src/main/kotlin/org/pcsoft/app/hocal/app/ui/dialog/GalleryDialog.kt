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

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.stage.Modality
import javafx.stage.Window
import org.pcsoft.app.hocal.app.Messages
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * The gallery dialog: the user picks one entry out of the project.
 *
 * Based on a plain [Alert] without type; the content is the FXML of [GalleryDialogView]. No icon and no
 * header. Buttons are the standard `OK` and `CANCEL`; ESC and the close button of the window act as
 * cancel. The dialog is modal to its owner window (window modal), without owner it is application modal.
 * It is resizable and grows with its content (720 x 420 by default).
 *
 * Show it with [showAndSelect]; set [folderProperty] and [mediaSourceProperty] before.
 *
 * @param ownerWindow the owner window, or `null` for none
 */
class GalleryDialog(ownerWindow: Window? = null) : Alert(AlertType.NONE) {
    private val viewModel: GalleryDialogViewModel
    private val view: GalleryDialogView

    init {
        val tuple = FluentViewLoader.fxmlView(GalleryDialogView::class.java).load()
        viewModel = tuple.viewModel
        view = tuple.codeBehind

        title = Messages["dialog.gallery.title"]
        headerText = null
        graphic = null
        dialogPane.header = null
        dialogPane.graphic = null
        dialogPane.content = tuple.view
        dialogPane.buttonTypes.setAll(ButtonType.OK, ButtonType.CANCEL)
        isResizable = true

        // The OK button exists only after the button types are set.
        dialogPane.lookupButton(ButtonType.OK).disableProperty().bind(viewModel.okDisabled)

        if (ownerWindow != null) {
            initOwner(ownerWindow)
            initModality(Modality.WINDOW_MODAL)
        } else {
            initModality(Modality.APPLICATION_MODAL)
        }

        setOnHidden { view.close() }
    }

    /**
     * The root folder shown in the tree; `null` shows an empty tree.
     *
     * @return the property of the root folder
     */
    fun folderProperty(): ObjectProperty<VirtualFolder?> = viewModel.rootFolder

    /**
     * The source the previews are loaded from; `null` shows placeholder icons only.
     *
     * @return the property of the media source
     */
    fun mediaSourceProperty(): ObjectProperty<MediaSource?> = viewModel.mediaSource

    /**
     * The entry currently chosen in the dialog.
     *
     * @return the read-only property of the chosen entry
     */
    fun selectedEntryProperty(): ReadOnlyObjectProperty<VirtualEntry?> = viewModel.selectedEntry

    /**
     * Shows the dialog and waits until it is closed.
     *
     * @return the chosen entry if the user confirmed with OK, `null` if the user cancelled, pressed ESC or closed the window
     */
    fun showAndSelect(): VirtualEntry? {
        val answer = showAndWait().orElse(null)
        // Read before anything is torn down; the answer alone cannot carry an entry.
        return if (answer == ButtonType.OK) viewModel.selectedEntry.get() else null
    }
}
