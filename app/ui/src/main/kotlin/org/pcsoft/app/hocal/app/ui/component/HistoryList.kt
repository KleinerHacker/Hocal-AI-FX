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
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.scene.control.ListView
import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import org.pcsoft.app.hocal.model.project.VirtualEntry

/**
 * List of all media of a file, from number 0 to the last one, each row with the preview of the medium and
 * its number.
 *
 * The component holds no data of its own: [fileProperty] and [mediaSourceProperty] are bound by the caller,
 * and the list is rebuilt whenever one of them reports a change. The previews are loaded in the background,
 * only for rows that are shown. The last medium is selected when another file is chosen or the history
 * grows; the choice of the user is reported by [selectedMediumProperty].
 *
 * The content is defined in `HistoryListView.fxml`, which is loaded with this node as its root.
 */
class HistoryList : ListView<HistoryMedium>() {
    /** The view model, reachable for the tests of the module. */
    internal val viewModel: HistoryListViewModel

    init {
        FluentViewLoader.fxmlView(HistoryListView::class.java).root(this).load().apply {
            this@HistoryList.viewModel = viewModel
        }
    }

    /**
     * The file whose media are shown.
     *
     * Bind it to a refreshable property that reports a change of the project, also when the entry stays the
     * same. A file is told from another by object identity. The list is empty as long as it holds no file.
     *
     * @return the property of the file
     */
    fun fileProperty(): ObjectProperty<VirtualEntry?> = viewModel.file

    /**
     * The source the previews and the number of media are read from.
     *
     * Without a source the list is empty.
     *
     * @return the property of the media source
     */
    fun mediaSourceProperty(): ObjectProperty<MediaSource?> = viewModel.mediaSource

    /**
     * The medium that is selected, or `null` while none is selected.
     *
     * @return the read-only property of the selected medium
     */
    fun selectedMediumProperty(): ReadOnlyObjectProperty<HistoryMedium?> = viewModel.selectedMedium

    /**
     * Tells that the media of a reference changed: the previews of the reference are dropped from the cache,
     * the list is rebuilt and the previews of its rows are loaded anew.
     *
     * @param reference UUID of the reference whose media changed
     */
    fun invalidate(reference: String) {
        viewModel.invalidate(reference)
    }

    /**
     * Cancels all loading and ends the thread pool of the previews for good.
     *
     * To be called when the list is not used any more. The previews cannot be loaded afterwards.
     */
    fun close() {
        viewModel.close()
    }
}
