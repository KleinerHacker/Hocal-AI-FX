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

package org.pcsoft.app.hocal.app.ui.preview

import javafx.application.Platform
import org.pcsoft.app.hocal.fx.model.manager.ProjectManager
import java.io.InputStream
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

/**
 * The [MediaSource] of the project that the [ProjectManager] holds.
 *
 * The manager belongs to the FX thread, but the thumbnails are loaded on background threads. Every access
 * therefore moves to the FX thread and waits for the answer; reading the content of a stream that was opened
 * happens afterwards on the calling thread.
 */
class ProjectMediaSource : MediaSource {

    /**
     * The number of media of a reference.
     *
     * @param reference UUID of the reference
     * @return the number of media, `0` if there is no project or no such reference
     */
    override fun mediaCount(reference: String): Int = onFxThread {
        ProjectManager.project.get()?.references?.firstOrNull { it.uuid == reference }?.media?.size ?: 0
    }

    /**
     * The file extension of a medium, without dot.
     *
     * @param reference UUID of the reference
     * @param number number of the medium, `0` is the original
     * @return the extension
     * @throws IllegalStateException if no project is loaded
     * @throws java.io.IOException if the medium does not exist
     */
    override fun extension(reference: String, number: Int): String = onFxThread {
        ProjectManager.mediumExtension(reference, number)
    }

    /**
     * Opens the content of a medium; the caller closes the stream.
     *
     * @param reference UUID of the reference
     * @param number number of the medium, `0` is the original
     * @return the content
     * @throws IllegalStateException if no project is loaded
     * @throws java.io.IOException if the medium does not exist
     */
    override fun open(reference: String, number: Int): InputStream = onFxThread {
        ProjectManager.openMedium(reference, number)
    }

    private fun <T> onFxThread(action: () -> T): T {
        if (Platform.isFxApplicationThread()) return action()

        val task = FutureTask(action)
        Platform.runLater(task)
        try {
            return task.get()
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        }
    }
}
