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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit

/**
 * Developer tests of the cache that keeps small and large images apart.
 */
class SizeClassCacheTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val small = MemoryThumbnailCache(maxEntries = 10)
    private val large = MemoryThumbnailCache(maxEntries = 2)
    private val cache = SizeClassCache(small, large, largeFrom = 256)

    private fun image(): Image = WritableImage(1, 1)

    /**
     * Use case: a thumbnail of the list and an image of the image view are stored. Each lands in the cache of
     * its class and is found again from there.
     */
    @Test
    fun eachImageGoesToTheCacheOfItsClass() {
        val thumbnail = image()
        val view = image()

        cache.put(ThumbnailKey("r1", 0, 48), 1, thumbnail)
        cache.put(ThumbnailKey("r1", 0, 1024), 1, view)

        assertEquals(1, small.size)
        assertEquals(1, large.size)
        assertSame(thumbnail, cache.get(ThumbnailKey("r1", 0, 48), 1))
        assertSame(view, cache.get(ThumbnailKey("r1", 0, 1024), 1))
    }

    /**
     * Use case: an image has exactly the edge length from which images count as large. It is a large one.
     */
    @Test
    fun theLimitBelongsToTheLargeImages() {
        cache.put(ThumbnailKey("r1", 0, 255), 1, image())
        cache.put(ThumbnailKey("r1", 0, 256), 1, image())

        assertEquals(1, small.size)
        assertEquals(1, large.size)
    }

    /**
     * Use case: many large images are stored. Only the few that the large cache holds stay, and the small
     * thumbnails are not pushed out by them.
     */
    @Test
    fun largeImagesDoNotPushOutSmallOnes() {
        repeat(5) { cache.put(ThumbnailKey("r$it", 0, 48), 1, image()) }

        repeat(6) { cache.put(ThumbnailKey("big$it", 0, 1024), 1, image()) }

        assertEquals(5, small.size)
        assertEquals(2, large.size)
    }

    /**
     * Use case: a reference is invalidated. Its images go from both caches, the others stay.
     */
    @Test
    fun invalidateReachesBothCaches() {
        cache.put(ThumbnailKey("r1", 0, 48), 1, image())
        cache.put(ThumbnailKey("r1", 0, 1024), 1, image())
        cache.put(ThumbnailKey("r2", 0, 48), 1, image())

        cache.invalidate("r1")

        assertNull(cache.get(ThumbnailKey("r1", 0, 48), 1))
        assertNull(cache.get(ThumbnailKey("r1", 0, 1024), 1))
        assertEquals(1, small.size)
        assertEquals(0, large.size)
    }

    /**
     * Use case: the cache is cleared. Both caches are empty afterwards.
     */
    @Test
    fun clearReachesBothCaches() {
        cache.put(ThumbnailKey("r1", 0, 48), 1, image())
        cache.put(ThumbnailKey("r1", 0, 1024), 1, image())

        cache.clear()

        assertEquals(0, small.size)
        assertEquals(0, large.size)
    }
}
