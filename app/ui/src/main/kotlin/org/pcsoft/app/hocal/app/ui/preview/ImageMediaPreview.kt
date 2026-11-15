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
 * Preview provider for the raster formats the Java FX image loader decodes itself: png, jpg, jpeg,
 * gif and bmp.
 *
 * The image is scaled to fit into a square of the requested edge length; its aspect ratio is kept.
 */
class ImageMediaPreview : MediaPreview {
    /**
     * Tells whether the extension is one of png, jpg, jpeg, gif or bmp, in any letter case.
     *
     * @param extension the file extension without the leading dot
     * @return `true` for a supported extension
     */
    override fun supports(extension: String): Boolean = extension.lowercase() in EXTENSIONS

    /**
     * Decodes the medium with the Java FX image loader.
     *
     * @param source the source of the medium
     * @param reference UUID of the reference
     * @param number position of the medium in the history
     * @param size edge length of the preview in pixels
     * @return the scaled image, or `null` if the medium is missing or cannot be decoded
     */
    override fun load(source: MediaSource, reference: String, number: Int, size: Int): Image? =
        try {
            source.open(reference, number).use { stream ->
                Image(stream, size.toDouble(), size.toDouble(), true, true).takeUnless { it.isError }
            }
        } catch (_: Exception) {
            null
        }

    private companion object {
        val EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "bmp")
    }
}
