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
import javafx.scene.image.WritableImage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.thumbnail.ImageThumbnails
import org.pcsoft.app.hocal.thumbnail.ThumbnailPixels
import org.testfx.api.FxToolkit

/** Developer tests of the conversion between thumbnail images and their pixels. */
class ImageThumbnailsTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private fun coloured(): WritableImage {
        val image = WritableImage(3, 2)
        var color = 0xFF102030.toInt()
        for (y in 0 until 2) for (x in 0 until 3) {
            image.pixelWriter.setArgb(x, y, color)
            color += 0x00050709
        }
        return image
    }

    /** Use case: an opaque image is converted to pixels and back. Every pixel keeps its exact colour. */
    @Test
    fun opaqueImageKeepsEveryColour() {
        val original = coloured()

        val back = ImageThumbnails.toImage(ImageThumbnails.toPixels(original)!!)

        assertNotSame(original, back)
        assertEquals(3.0, back.width)
        assertEquals(2.0, back.height)
        for (y in 0 until 2) for (x in 0 until 3) {
            assertEquals(original.pixelReader.getArgb(x, y), back.pixelReader.getArgb(x, y), "pixel $x/$y")
        }
    }

    /** Use case: pixels with transparency are converted to an image and back. The bytes stay the same. */
    @Test
    fun translucentPixelsStayTheSame() {
        val bytes = ByteArray(2 * 2 * 4) { (it * 13 % 64).toByte() }
        for (pixel in 0 until 4) bytes[pixel * 4 + 3] = 0x80.toByte()
        val pixels = ThumbnailPixels(2, 2, bytes)

        assertEquals(pixels, ImageThumbnails.toPixels(ImageThumbnails.toImage(pixels)))
    }

    /** Use case: the pixels have the size of the image and four bytes per pixel. */
    @Test
    fun pixelsHaveTheSizeOfTheImage() {
        val pixels = ImageThumbnails.toPixels(coloured())

        assertNotNull(pixels)
        assertEquals(3, pixels!!.width)
        assertEquals(2, pixels.height)
        assertEquals(3 * 2 * 4, pixels.bytes.size)
    }

    /** Use case: an image that failed to load has no pixels, so it never reaches the file. */
    @Test
    fun failedImageHasNoPixels() {
        val failed: Image = Image("file:/does/not/exist.png")

        assertNull(ImageThumbnails.toPixels(failed))
    }
}
