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

package org.pcsoft.app.hocal.app.ui

import javafx.stage.Window
import org.pcsoft.app.hocal.app.ui.dialog.GalleryDialog
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * Registry of all dialogs of the application. Dialogs that a plain `Alert` covers are built here as well.
 */
object HocalDialog {

    /**
     * Shows the gallery dialog and waits until it is closed.
     *
     * @param owner the owner window, or `null` for none
     * @param folder the root folder shown in the tree, or `null` for an empty tree
     * @param source the source of the previews, or `null` for placeholder icons only
     * @return the entry the user chose with OK, or `null` if the dialog was cancelled
     */
    fun gallery(owner: Window?, folder: VirtualFolder?, source: MediaSource?): VirtualEntry? {
        val dialog = GalleryDialog(owner)
        dialog.folderProperty().set(folder)
        dialog.mediaSourceProperty().set(source)
        return dialog.showAndSelect()
    }
}
