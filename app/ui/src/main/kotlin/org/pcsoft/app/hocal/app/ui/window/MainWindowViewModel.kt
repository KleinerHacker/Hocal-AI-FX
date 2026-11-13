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
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.property.ObjectProperty
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.hocal.app.ui.MediaChanges
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.app.ui.preview.ProjectMediaSource
import org.pcsoft.app.hocal.fx.model.manager.ProjectManager
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * View model of the application main window.
 *
 * The window follows the project the [ProjectManager] holds; it never creates or changes the project itself.
 * This view model offers what the parts of the window are bound to:
 *
 * * [tree], the root folder of the project, for the folder tree and the gallery of the prompt input
 * * [selectedFolder], the folder the user selected in the tree, and [listFolder], the folder the file list shows.
 *   The folders are plain objects that report nothing when they change, so [listFolder] additionally reports
 *   every change of the project, and the list reads its folder again
 * * [selectedFile], the file the user selected in the list
 * * [mediaSource], through which the previews are loaded from the project
 *
 * The project and the media changes are global, so the view model observes them only between [onShow] and
 * [onHide]; the view calls the two according to the screen state of the window.
 */
class MainWindowViewModel : ViewModel {
    private var observing = false
    private var mediaSubscription: AutoCloseable? = null

    // A field: a method reference would be a new object per use and could not be removed again.
    private val projectListener = InvalidationListener { onProjectInvalidated() }

    /** The root folder of the loaded project, `null` while no project is loaded. */
    val tree: ReadOnlyObjectProperty<VirtualFolder?> = ProjectManager.project.treeProperty

    /** The folder the user selected in the tree, `null` while none is selected. */
    val selectedFolder: ObjectProperty<VirtualFolder?> = SimpleObjectProperty(this, "selectedFolder")

    /** The folder the file list shows: the selected folder, reported again whenever the project changes. */
    val listFolder: RefreshableFolderProperty = RefreshableFolderProperty(this, "listFolder")

    /** The file the user selected in the list, `null` while none is selected. */
    val selectedFile: ObjectProperty<VirtualEntry?> = SimpleObjectProperty(this, "selectedFile")

    /** The source the previews are loaded from: the media of the project. */
    val mediaSource: MediaSource = ProjectMediaSource()

    /**
     * Called, on the FX thread, with the UUID of a reference whose media changed while the window is on screen.
     * The view sets it to reload the previews of that reference.
     */
    var onMediaChanged: ((String) -> Unit)? = null

    init {
        selectedFolder.addListener { _, _, folder -> listFolder.set(folder) }
    }

    /**
     * Starts observing the project and the media changes.
     *
     * Called when the window comes on screen. Calling it twice registers only once.
     */
    fun onShow() {
        if (!observing) {
            observing = true
            ProjectManager.project.addListener(projectListener)
            mediaSubscription = MediaChanges.subscribe { reference -> onFx { onMediaChanged?.invoke(reference) } }
        }
        // Reading validates the property, so that its next change is reported.
        ProjectManager.project.get()
        listFolder.refresh()
    }

    /**
     * Stops observing the project and the media changes.
     *
     * Called when the window leaves the screen. Calling it twice is harmless.
     */
    fun onHide() {
        if (observing) {
            observing = false
            ProjectManager.project.removeListener(projectListener)
            mediaSubscription?.close()
            mediaSubscription = null
        }
    }

    private fun onProjectInvalidated() {
        // Reading again is a must: a property reports the next change only after its value was read.
        ProjectManager.project.get()
        onFx { listFolder.refresh() }
    }

    private fun onFx(action: () -> Unit) {
        if (Platform.isFxApplicationThread()) {
            if (observing) action()
        } else {
            Platform.runLater { if (observing) action() }
        }
    }
}
