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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests of the image preview provider.
 */
class ImageMediaPreviewTest {
    private val provider = ImageMediaPreview()

    /**
     * Use case: a valid PNG of 200 x 100 pixels is loaded for a preview of 48 pixels. The image is scaled
     * to fit into the square and keeps its aspect ratio, so it becomes 48 x 24 pixels.
     */
    @Test
    fun validPngIsLoadedScaledWithAspectRatio() {
        val source = FakeMediaSource().apply { add("ref", "png" to pngBytes(200, 100)) }

        val image = provider.load(source, "ref", 0, 48)

        assertNotNull(image)
        assertEquals(48.0, image!!.width)
        assertEquals(24.0, image.height)
        assertEquals(1, source.closed.get(), "the stream must be closed")
    }

    /**
     * Use case: the bytes of a medium are not an image. The provider reports no preview instead of
     * failing, so the caller shows the placeholder.
     */
    @Test
    fun brokenImageResultsInNull() {
        val source = FakeMediaSource().apply { add("ref", "png" to brokenBytes()) }

        assertNull(provider.load(source, "ref", 0, 48))
    }

    /**
     * Use case: only the raster formats of the Java FX loader are supported; every other extension, such
     * as a video or a raw photo format, is not.
     */
    @Test
    fun onlyRasterExtensionsAreSupported() {
        listOf("png", "jpg", "jpeg", "gif", "bmp").forEach { assertTrue(provider.supports(it), it) }
        listOf("mp4", "raw", "webp", "txt", "").forEach { assertFalse(provider.supports(it), it) }
    }

    /**
     * Use case: the extension of a stored medium may be written in any letter case, such as `PNG` or `Jpg`.
     */
    @Test
    fun extensionsAreCaseInsensitive() {
        listOf("PNG", "Jpg", "JPEG", "GiF", "bMp").forEach { assertTrue(provider.supports(it), it) }
    }

    /**
     * Use case: the requested medium does not exist, so the source throws while the provider opens it.
     * The provider reports no preview and does not propagate the exception.
     */
    @Test
    fun missingMediumResultsInNull() {
        val source = FakeMediaSource()

        assertNull(provider.load(source, "unknown", 0, 48))
    }

    /**
     * Use case: the source fails with an exception of its own while opening. The provider catches it.
     */
    @Test
    fun failingSourceResultsInNull() {
        val source = object : MediaSource {
            override fun mediaCount(reference: String) = 1
            override fun extension(reference: String, number: Int) = "png"
            override fun open(reference: String, number: Int) = throw IllegalStateException("disk gone")
        }

        assertNull(provider.load(source, "ref", 0, 48))
    }
}
