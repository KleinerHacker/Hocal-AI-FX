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

import javafx.beans.property.SimpleObjectProperty
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * A folder property that can report a change although it still holds the same folder.
 *
 * The folders are plain objects that report nothing when their children change. Whoever knows that the folder
 * was changed calls [refresh], and everything bound to this property reads the folder again.
 *
 * @param bean the object that owns the property
 * @param name the name of the property
 */
class RefreshableFolderProperty(bean: Any?, name: String) : SimpleObjectProperty<VirtualFolder?>(bean, name) {

    /** Reports a change of the folder to everything that observes this property. */
    fun refresh() {
        fireValueChangedEvent()
    }
}
