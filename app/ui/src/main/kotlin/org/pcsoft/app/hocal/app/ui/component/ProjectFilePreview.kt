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

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.scene.image.Image
import java.util.concurrent.Future

/**
 * The loaded previews of one entry: the original and the current medium.
 *
 * A property holds `null` as long as its image is not loaded or could not be loaded; the cell shows the
 * placeholder icon then. Both properties belong to the FX thread.
 */
class ProjectFilePreview {
    private val originalWrapper = ReadOnlyObjectWrapper<Image?>(this, "original")
    private val currentWrapper = ReadOnlyObjectWrapper<Image?>(this, "current")

    /** Whether loading was started for this entry, so it is started once only. */
    internal var started = false

    /** The running load task, to cancel it. */
    internal var future: Future<*>? = null

    /**
     * Number of the current loading. A [reset] starts a new one, so the result of a loading that was
     * started before is not taken over any more. Read by the load threads.
     */
    @Volatile
    internal var loadId = 0

    /** Forgets the loading and the images, so that the entry is loaded anew and shows the placeholder meanwhile. */
    internal fun reset() {
        future?.cancel(true)
        future = null
        started = false
        loadId++
        setOriginal(null)
        setCurrent(null)
    }

    /** The preview of the original medium, `null` until loaded. */
    val original: ReadOnlyObjectProperty<Image?> = originalWrapper.readOnlyProperty

    /** The preview of the current medium, `null` until loaded. */
    val current: ReadOnlyObjectProperty<Image?> = currentWrapper.readOnlyProperty

    internal fun setOriginal(image: Image?) = originalWrapper.set(image)

    internal fun setCurrent(image: Image?) = currentWrapper.set(image)
}
