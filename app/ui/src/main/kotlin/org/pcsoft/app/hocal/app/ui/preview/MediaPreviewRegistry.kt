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
 * Registry of the preview providers; the first provider that supports the extension of a medium wins.
 *
 * @param providers the providers in order of priority, by default the [ImageMediaPreview] only
 */
class MediaPreviewRegistry(providers: List<MediaPreview> = listOf(ImageMediaPreview())) {
    private val providers: List<MediaPreview> = providers.toList()

    /**
     * Loads the preview of a medium with the first provider that supports its extension.
     *
     * Never throws: an unknown type or any failure, also of the source, results in `null`, for which
     * the caller shows the placeholder icon.
     *
     * @param source the source of the medium
     * @param reference UUID of the reference
     * @param number position of the medium in the history
     * @param size edge length of the preview in pixels
     * @return the preview image, or `null`
     */
    fun load(source: MediaSource, reference: String, number: Int, size: Int): Image? =
        try {
            val extension = source.extension(reference, number)
            providers.firstOrNull { it.supports(extension) }?.load(source, reference, number, size)
        } catch (_: Exception) {
            null
        }
}
