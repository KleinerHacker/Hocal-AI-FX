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

import de.saxsys.mvvmfx.ViewModel
import javafx.application.ColorScheme
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.image.Image
import javafx.stage.Screen
import javafx.stage.Window
import org.pcsoft.app.hocal.app.ui.HocalDialog
import org.pcsoft.app.hocal.app.ui.preview.LoadHandle
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.app.ui.preview.ThumbnailLoader
import org.pcsoft.app.hocal.app.ui.prompt.FlowOption
import org.pcsoft.app.hocal.app.ui.prompt.ModelOption
import org.pcsoft.app.hocal.app.ui.prompt.ModelProvider
import org.pcsoft.app.hocal.app.ui.prompt.PlaceholderModelProvider
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * View model of the prompt input.
 *
 * It holds the prompt, the chosen model and flow, the images attached to the prompt and the models and
 * flows to choose from. The properties are bound by the caller. The previews of the images are loaded in
 * the background; a preview is cancelled when its image is removed and loaded anew when the media source
 * changes or [invalidate] is called.
 *
 * Only the appearance of the system is observed globally, and only between [onShow] and [onHide].
 */
class PromptInputViewModel : ViewModel {
    private val darkWrapper = ReadOnlyBooleanWrapper(this, "dark", false)

    private var observing = false

    // Counts the registrations that are alive now; it is 0 or 1 and lets the tests prove the symmetry.
    internal var appearanceRegistrations: Int = 0
        private set

    // True while the models are replaced, so that the intermediate changes of the selection are ignored.
    private var reloading = false

    private val previews = LinkedHashMap<String, ObjectProperty<Image?>>()
    private val handles = HashMap<String, LoadHandle>()

    // Fields: a method reference would be a new object per use and could not be removed again.
    private val appearanceListener = InvalidationListener { onAppearanceChanged() }

    /** The text of the prompt. */
    val prompt: StringProperty = SimpleStringProperty(this, "prompt", "")

    /** The chosen model, `null` while there is none. */
    val selectedModel: ObjectProperty<ModelOption?> = SimpleObjectProperty(this, "selectedModel")

    /** The chosen flow of the chosen model, `null` while there is none. */
    val selectedFlow: ObjectProperty<FlowOption?> = SimpleObjectProperty(this, "selectedFlow")

    /** The root folder the gallery dialog shows. */
    val folder: ObjectProperty<VirtualFolder?> = SimpleObjectProperty(this, "folder")

    /** The source the previews of the images are loaded from. */
    val mediaSource: ObjectProperty<MediaSource?> = SimpleObjectProperty(this, "mediaSource")

    /** The models to choose from. */
    val models: ObservableList<ModelOption> = FXCollections.observableArrayList()

    /** The flows of the chosen model. */
    val flows: ObservableList<FlowOption> = FXCollections.observableArrayList()

    /** The images attached to the prompt, in the order they were added. */
    val images: ObservableList<PromptImage> = FXCollections.observableArrayList()

    /** `true` while the prompt is empty or blank, so there is nothing to send. */
    val sendDisabled: BooleanBinding = Bindings.createBooleanBinding({ prompt.get().isNullOrBlank() }, prompt)

    /** Whether the system shows the dark appearance, so the dark icons are used. */
    val dark: ReadOnlyBooleanProperty = darkWrapper.readOnlyProperty

    /** Loads the previews of the chips. */
    var thumbnails: ThumbnailLoader = ThumbnailLoader()

    /** Called when the user sends the prompt; `null` as long as nothing is implemented. */
    var sendHandler: (() -> Unit)? = null

    /** Opens the gallery dialog and returns the chosen entry, or `null` if the user cancelled. */
    var galleryOpener: (owner: Window?, folder: VirtualFolder?, source: MediaSource?) -> VirtualEntry? =
        { owner, folder, source -> HocalDialog.gallery(owner, folder, source) }

    /** The source of the models and flows. Setting it loads the models anew and chooses the first one. */
    var modelProvider: ModelProvider = PlaceholderModelProvider()
        set(value) {
            field = value
            reloadModels()
        }

    init {
        selectedModel.addListener { _, _, _ -> if (!reloading) reloadFlows() }
        mediaSource.addListener { _, _, _ -> reloadAllPreviews() }
        images.addListener(ListChangeListener { syncPreviews() })
        reloadModels()
    }

    /**
     * Returns the preview property of the chip of an image; it holds `null` until the preview is loaded.
     *
     * @param reference UUID of the reference of the image
     * @return the property, valid as long as the image is attached
     */
    fun previewOf(reference: String): ReadOnlyObjectProperty<Image?> =
        previews.getOrPut(reference) { SimpleObjectProperty(null) }

    /**
     * Opens the gallery dialog and attaches the chosen entry as image, unless its reference is attached
     * already. A cancelled dialog changes nothing.
     *
     * @param owner the window that owns the dialog, or `null`
     */
    fun addImage(owner: Window?) {
        val entry = galleryOpener(owner, folder.get(), mediaSource.get()) ?: return
        if (images.none { it.reference == entry.reference }) images.add(PromptImage(entry.reference, entry.name))
    }

    /**
     * Removes an image and cancels its running preview load.
     *
     * @param image the image to remove
     */
    fun removeImage(image: PromptImage) {
        images.removeIf { it.reference == image.reference }
    }

    /** Calls the send handler if the prompt is not blank; does nothing without a handler. */
    fun send() {
        if (!sendDisabled.get()) sendHandler?.invoke()
    }

    /**
     * Tells that the media of a reference changed: the cache is dropped and the chips of the reference
     * are loaded anew. May be called from any thread.
     *
     * @param reference UUID of the reference whose media changed
     */
    fun invalidate(reference: String) {
        thumbnails.invalidate(reference)
        if (Platform.isFxApplicationThread()) reload(reference) else Platform.runLater { reload(reference) }
    }

    /**
     * Starts observing the appearance of the system. Calling it twice registers only once.
     */
    fun onShow() {
        if (!observing) {
            observing = true
            Platform.getPreferences().colorSchemeProperty().addListener(appearanceListener)
            appearanceRegistrations++
        }
        darkWrapper.set(isDarkAppearance())
    }

    /** Stops observing the appearance. Calling it twice is harmless. */
    fun onHide() {
        if (observing) {
            observing = false
            Platform.getPreferences().colorSchemeProperty().removeListener(appearanceListener)
            appearanceRegistrations--
        }
    }

    private fun isDarkAppearance(): Boolean = Platform.getPreferences().colorScheme == ColorScheme.DARK

    private fun onAppearanceChanged() {
        // Reading again is a must: a property reports the next change only after its value was read.
        Platform.getPreferences().colorScheme
        val apply = { if (observing) darkWrapper.set(isDarkAppearance()) }
        if (Platform.isFxApplicationThread()) apply() else Platform.runLater(apply)
    }

    private fun reloadModels() {
        reloading = true
        try {
            models.setAll(modelProvider.models())
            if (!selectedModel.isBound) selectedModel.set(models.firstOrNull())
        } finally {
            reloading = false
        }
        reloadFlows()
    }

    private fun reloadFlows() {
        val model = selectedModel.get()
        flows.setAll(if (model == null) emptyList() else modelProvider.flows(model))
        if (selectedFlow.isBound) return
        val kept = flows.firstOrNull { it.id == selectedFlow.get()?.id }
        selectedFlow.set(kept ?: flows.firstOrNull())
    }

    // The larger size keeps the preview sharp on a HiDPI screen.
    private fun loadSize(): Int = if (Screen.getPrimary().outputScaleX > 1.0) CHIP_SIZE * 2 else CHIP_SIZE

    private fun syncPreviews() {
        val current = images.map { it.reference }.toSet()
        previews.keys.filter { it !in current }.forEach { reference ->
            handles.remove(reference)?.cancel()
            previews.remove(reference)
        }
        current.filter { it !in handles }.forEach { startLoading(it) }
    }

    private fun reloadAllPreviews() {
        // Another source means another project: its thumbnails must not be taken for those of the former one.
        thumbnails.clear()
        images.map { it.reference }.toSet().forEach { startLoading(it) }
    }

    private fun reload(reference: String) {
        if (images.any { it.reference == reference }) startLoading(reference)
    }

    private fun startLoading(reference: String) {
        handles.remove(reference)?.cancel()
        val property = previews.getOrPut(reference) { SimpleObjectProperty(null) }
        property.set(null)
        val source = mediaSource.get() ?: return
        handles[reference] = thumbnails.loadCurrent(source, reference, loadSize()) { property.set(it) }
    }

    companion object {
        /** The display edge length of the preview of a chip in pixels. */
        const val CHIP_SIZE: Int = 32
    }
}
