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
package org.pcsoft.app.hocal.thumbnail

import javafx.scene.image.Image
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage

/**
 * Converts a thumbnail image to its pixels and back, colour exact. Meant for small images only: the large
 * images of the image view are not stored in a file.
 */
object ImageThumbnails {

    /**
     * Reads the pixels of an image.
     *
     * @param image the image
     * @return the pixels, or `null` if the image is not completely loaded, failed or is empty
     */
    fun toPixels(image: Image): ThumbnailPixels? {
        if (image.isError || image.progress < 1.0) return null
        val width = image.width.toInt()
        val height = image.height.toInt()
        if (width <= 0 || height <= 0) return null
        val reader = image.pixelReader ?: return null
        val bytes = ByteArray(width * height * ThumbnailPixels.BYTES_PER_PIXEL)
        reader.getPixels(0, 0, width, height, PixelFormat.getByteBgraPreInstance(), bytes, 0, width * ThumbnailPixels.BYTES_PER_PIXEL)
        return ThumbnailPixels(width, height, bytes)
    }

    /**
     * Makes an image of the pixels.
     *
     * @param pixels the pixels
     * @return the image, a new one on every call
     */
    fun toImage(pixels: ThumbnailPixels): Image {
        val image = WritableImage(pixels.width, pixels.height)
        image.pixelWriter.setPixels(
            0, 0, pixels.width, pixels.height, PixelFormat.getByteBgraPreInstance(),
            pixels.bytes, 0, pixels.width * ThumbnailPixels.BYTES_PER_PIXEL,
        )
        return image
    }
}
