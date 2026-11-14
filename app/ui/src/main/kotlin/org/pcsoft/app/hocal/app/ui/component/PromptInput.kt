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

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyListProperty
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.StringProperty
import javafx.scene.layout.VBox
import javafx.stage.Window
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.app.ui.prompt.FlowOption
import org.pcsoft.app.hocal.app.ui.prompt.ModelOption
import org.pcsoft.app.hocal.app.ui.prompt.ModelProvider
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * The input part of the application: the prompt, the choice of model and flow, and the images attached
 * to the prompt as chips.
 *
 * The component holds no project data of its own: the properties are bound by the caller. The images
 * are not stored as generation detail. Sending is not implemented yet; it calls the [sendHandler].
 *
 * The content is defined in `PromptInputView.fxml`, which is loaded with this node as its root.
 */
class PromptInput : VBox() {
    private val viewModel: PromptInputViewModel

    init {
        FluentViewLoader.fxmlView(PromptInputView::class.java).root(this).load().apply {
            this@PromptInput.viewModel = viewModel
        }
    }

    /**
     * The text of the prompt.
     *
     * @return the property of the prompt
     */
    fun promptProperty(): StringProperty = viewModel.prompt

    /**
     * The chosen model; bind it bidirectionally, the component also sets it itself.
     *
     * @return the property of the chosen model
     */
    fun selectedModelProperty(): ObjectProperty<ModelOption?> = viewModel.selectedModel

    /**
     * The chosen flow; bind it bidirectionally, the component also sets it itself.
     *
     * @return the property of the chosen flow
     */
    fun selectedFlowProperty(): ObjectProperty<FlowOption?> = viewModel.selectedFlow

    /**
     * The root folder the gallery dialog shows.
     *
     * @return the property of the folder
     */
    fun folderProperty(): ObjectProperty<VirtualFolder?> = viewModel.folder

    /**
     * The source the previews of the chips are loaded from.
     *
     * @return the property of the media source
     */
    fun mediaSourceProperty(): ObjectProperty<MediaSource?> = viewModel.mediaSource

    private val imagesProperty: ReadOnlyListProperty<PromptImage> by lazy {
        ReadOnlyListWrapper(this, "images", viewModel.images).readOnlyProperty
    }

    /**
     * The images attached to the prompt, in the order they were added.
     *
     * @return the read-only property of the images
     */
    fun imagesProperty(): ReadOnlyListProperty<PromptImage> = imagesProperty

    /** The source of the models and flows; setting it loads the models anew and chooses the first one. */
    var modelProvider: ModelProvider
        get() = viewModel.modelProvider
        set(value) {
            viewModel.modelProvider = value
        }

    /** Called when the user sends the prompt; `null` as long as nothing is implemented. */
    var sendHandler: (() -> Unit)?
        get() = viewModel.sendHandler
        set(value) {
            viewModel.sendHandler = value
        }

    /** Opens the gallery dialog and returns the chosen entry, or `null` if the user cancelled. */
    var galleryOpener: (owner: Window?, folder: VirtualFolder?, source: MediaSource?) -> VirtualEntry?
        get() = viewModel.galleryOpener
        set(value) {
            viewModel.galleryOpener = value
        }

    /**
     * Tells that the media of a reference changed: the chips of the reference are loaded anew.
     *
     * @param reference UUID of the reference whose media changed
     */
    fun invalidate(reference: String) {
        viewModel.invalidate(reference)
    }
}
