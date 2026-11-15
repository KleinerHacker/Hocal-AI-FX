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

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Tells the user interface that the media of a reference changed.
 *
 * Whoever changes the media history of a reference - appends a medium or removes one - calls [changed] with
 * the UUID of the reference afterwards. The parts of the window that show previews of the reference load them
 * anew. The thumbnail caches recognize a changed number of media by themselves, but not a change that keeps
 * the number, such as removing one medium and appending another, so the call is needed in every case.
 *
 * Listeners may be called on any thread; each of them has to move to the FX thread itself if it needs to.
 */
object MediaChanges {
    private val listeners = CopyOnWriteArrayList<(String) -> Unit>()

    /**
     * Registers a listener for changed media.
     *
     * @param listener called with the UUID of the reference whose media changed
     * @return a handle that removes the listener again when it is closed
     */
    fun subscribe(listener: (String) -> Unit): AutoCloseable {
        listeners.add(listener)
        return AutoCloseable { listeners.remove(listener) }
    }

    /**
     * Reports that the media of a reference changed.
     *
     * @param reference UUID of the reference
     */
    fun changed(reference: String) {
        listeners.forEach { it(reference) }
    }
}
