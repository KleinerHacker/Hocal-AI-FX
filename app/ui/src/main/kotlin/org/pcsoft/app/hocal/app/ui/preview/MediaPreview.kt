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

import javafx.scene.image.Image

/**
 * Creates the preview image of one kind of medium.
 *
 * Providers are registered in the [MediaPreviewRegistry]. They may run outside the FX thread.
 */
interface MediaPreview {
    /**
     * Tells whether this provider can create previews for the given extension.
     *
     * @param extension the file extension without the leading dot
     * @return `true` if the extension is supported
     */
    fun supports(extension: String): Boolean

    /**
     * Loads the preview of a medium.
     *
     * @param source the source of the medium
     * @param reference UUID of the reference
     * @param number position of the medium in the history
     * @param size edge length of the preview in pixels
     * @return the preview image, or `null` if the medium cannot be loaded
     */
    fun load(source: MediaSource, reference: String, number: Int, size: Int): Image?
}
