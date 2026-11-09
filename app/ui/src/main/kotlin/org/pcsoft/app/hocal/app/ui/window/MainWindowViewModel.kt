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

import de.saxsys.mvvmfx.ViewModel
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.pcsoft.app.hocal.app.Messages
import java.text.MessageFormat

/**
 * View model of the application main window.
 *
 * The window owns the flows the user can choose from: [flows] is handed to the flow list, and the
 * flow picked there is followed through [selectedFlow]. [selectionText] is the text the content area
 * shows for the current selection.
 *
 * The flows are a fixed example list until the application reads the flows ComfyUI offers.
 */
class MainWindowViewModel : ViewModel {

    /** The names of the flows the user can choose from, in the order they are shown. */
    val flows: ObservableList<String> = FXCollections.observableArrayList(
        Messages["window.main.flow.textToImage"],
        Messages["window.main.flow.imageToImage"],
        Messages["window.main.flow.upscale"]
    )

    /** The flow the user picked, absent while nothing is selected. */
    val selectedFlow: ObjectProperty<String?> = SimpleObjectProperty(this, "selectedFlow", null)

    /** Text of the content area: a hint while nothing is selected, the picked flow otherwise. */
    val selectionText: StringBinding = Bindings.createStringBinding(
        {
            selectedFlow.value
                ?.let { MessageFormat.format(Messages["window.main.selection.selected"], it) }
                ?: Messages["window.main.selection.none"]
        },
        selectedFlow
    )
}
