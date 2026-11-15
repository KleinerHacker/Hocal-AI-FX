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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

/**
 * Developer tests of the preview registry.
 */
class MediaPreviewRegistryTest {

    private class StubPreview(private val extensions: Set<String>, val image: Image? = WritableImage(1, 1)) : MediaPreview {
        var loads = 0
        override fun supports(extension: String) = extension in extensions
        override fun load(source: MediaSource, reference: String, number: Int, size: Int): Image? {
            loads++
            return image
        }
    }

    private fun source(extension: String) = FakeMediaSource().apply { add("ref", extension to ByteArray(0)) }

    /**
     * Use case: two providers are registered for different types. The registry asks the provider that
     * supports the extension of the medium, and no other.
     */
    @Test
    fun providerIsChosenByExtension() {
        val first = StubPreview(setOf("aaa"))
        val second = StubPreview(setOf("bbb"))
        val registry = MediaPreviewRegistry(listOf(first, second))

        assertSame(second.image, registry.load(source("bbb"), "ref", 0, 48))
        assertEquals(0, first.loads)
        assertEquals(1, second.loads)
    }

    /**
     * Use case: two providers support the same extension. The first registered one wins, the second is
     * not asked.
     */
    @Test
    fun firstMatchingProviderWins() {
        val first = StubPreview(setOf("aaa"))
        val second = StubPreview(setOf("aaa"))
        val registry = MediaPreviewRegistry(listOf(first, second))

        assertSame(first.image, registry.load(source("aaa"), "ref", 0, 48))
        assertEquals(0, second.loads)
    }

    /**
     * Use case: no provider knows the extension of the medium. The registry reports no preview.
     */
    @Test
    fun unknownTypeResultsInNull() {
        val registry = MediaPreviewRegistry(listOf(StubPreview(setOf("aaa"))))

        assertNull(registry.load(source("zzz"), "ref", 0, 48))
    }

    /**
     * Use case: a provider throws while loading. The registry never throws and reports no preview.
     */
    @Test
    fun throwingProviderResultsInNull() {
        val throwing = object : MediaPreview {
            override fun supports(extension: String) = true
            override fun load(source: MediaSource, reference: String, number: Int, size: Int): Image? = throw IllegalStateException("boom")
        }

        assertNull(MediaPreviewRegistry(listOf(throwing)).load(source("aaa"), "ref", 0, 48))
    }

    /**
     * Use case: the source cannot tell the extension, as the reference is unknown. The registry catches
     * the exception and reports no preview.
     */
    @Test
    fun failingSourceResultsInNull() {
        assertNull(MediaPreviewRegistry().load(FakeMediaSource(), "unknown", 0, 48))
    }

    /**
     * Use case: the default registry knows the image provider, so a PNG medium is loaded.
     */
    @Test
    fun defaultRegistryLoadsImages() {
        val source = FakeMediaSource().apply { add("ref", "png" to pngBytes(10, 10)) }

        assertNotNull(MediaPreviewRegistry().load(source, "ref", 0, 48))
    }
}
