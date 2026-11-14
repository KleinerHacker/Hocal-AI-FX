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

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * View model of the gallery dialog.
 *
 * It holds the two inputs of the dialog (root folder and media source) and the entry the user chose.
 * The view binds [selectedEntry] to the selection of the file list, so a change of the folder, which
 * clears the selection of the list, clears it here as well.
 */
class GalleryDialogViewModel : ViewModel {

    /** The root folder shown in the tree, or `null` for an empty tree. */
    val rootFolder: ObjectProperty<VirtualFolder?> = SimpleObjectProperty(this, "rootFolder")

    /** The source the previews are loaded from, or `null` for placeholder icons only. */
    val mediaSource: ObjectProperty<MediaSource?> = SimpleObjectProperty(this, "mediaSource")

    /** The entry the user chose, or `null` while none is chosen. */
    val selectedEntry: ObjectProperty<VirtualEntry?> = SimpleObjectProperty(this, "selectedEntry")

    /** `true` while no entry is chosen, so the OK button of the dialog is disabled. */
    val okDisabled: BooleanBinding = selectedEntry.isNull
}
