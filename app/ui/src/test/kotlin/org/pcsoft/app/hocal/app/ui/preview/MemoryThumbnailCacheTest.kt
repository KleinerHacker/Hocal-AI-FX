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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testfx.api.FxToolkit
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Developer tests of the in-memory thumbnail cache.
 */
class MemoryThumbnailCacheTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private fun image(): Image = WritableImage(1, 1)

    private fun key(reference: String, number: Int = 0, size: Int = 48) = ThumbnailKey(reference, number, size)

    /**
     * Use case: a stored thumbnail is found again by the same key and the same number of media, as the
     * very same image object.
     */
    @Test
    fun storedThumbnailIsFound() {
        val cache = MemoryThumbnailCache()
        val stored = image()

        cache.put(key("r1"), 3, stored)

        assertSame(stored, cache.get(key("r1"), 3))
    }

    /**
     * Use case: a key that was never stored has no thumbnail, and the keys differ in reference, medium
     * number and size.
     */
    @Test
    fun unknownKeyHasNoThumbnail() {
        val cache = MemoryThumbnailCache()
        cache.put(key("r1", 0, 48), 1, image())

        assertNull(cache.get(key("r2", 0, 48), 1))
        assertNull(cache.get(key("r1", 1, 48), 1))
        assertNull(cache.get(key("r1", 0, 96), 1))
    }

    /**
     * Use case: the reference got another number of media since the thumbnail was stored. The thumbnail is
     * stale, so it is not returned and is dropped.
     */
    @Test
    fun staleThumbnailIsDropped() {
        val cache = MemoryThumbnailCache()
        cache.put(key("r1"), 1, image())

        assertNull(cache.get(key("r1"), 2))
        assertEquals(0, cache.size)
        assertNull(cache.get(key("r1"), 1))
    }

    /**
     * Use case: the cache is full. The thumbnail used least recently is dropped; a thumbnail that was
     * read in between counts as used and stays.
     */
    @Test
    fun leastRecentlyUsedThumbnailIsEvicted() {
        val cache = MemoryThumbnailCache(maxEntries = 2)
        val first = image()
        val third = image()
        cache.put(key("r1"), 1, first)
        cache.put(key("r2"), 1, image())
        cache.get(key("r1"), 1)

        cache.put(key("r3"), 1, third)

        assertEquals(2, cache.size)
        assertSame(first, cache.get(key("r1"), 1))
        assertNull(cache.get(key("r2"), 1))
        assertSame(third, cache.get(key("r3"), 1))
    }

    /**
     * Use case: a reference is invalidated. All its thumbnails go, whatever medium number and size, and
     * the thumbnails of other references stay.
     */
    @Test
    fun invalidateDropsAllThumbnailsOfOneReference() {
        val cache = MemoryThumbnailCache()
        val other = image()
        cache.put(key("r1", 0, 48), 2, image())
        cache.put(key("r1", 1, 48), 2, image())
        cache.put(key("r1", 0, 96), 2, image())
        cache.put(key("r2", 0, 48), 1, other)

        cache.invalidate("r1")

        assertEquals(1, cache.size)
        assertSame(other, cache.get(key("r2", 0, 48), 1))
    }

    /**
     * Use case: the cache is cleared, for instance because another project was opened. Nothing is left.
     */
    @Test
    fun clearDropsEverything() {
        val cache = MemoryThumbnailCache()
        cache.put(key("r1"), 1, image())
        cache.put(key("r2"), 1, image())

        cache.clear()

        assertEquals(0, cache.size)
        assertNull(cache.get(key("r1"), 1))
    }

    /**
     * Use case: a cache that could hold nothing is a programming error and is rejected at once.
     */
    @Test
    fun maxEntriesBelowOneIsRejected() {
        assertThrows<IllegalArgumentException> { MemoryThumbnailCache(maxEntries = 0) }
    }

    /**
     * Use case: several load threads and the FX thread use the cache at once. No access fails, and the
     * cache never grows beyond its limit.
     */
    @Test
    fun concurrentAccessKeepsTheLimit() {
        val cache = MemoryThumbnailCache(maxEntries = 50)
        val pool = Executors.newFixedThreadPool(8)
        try {
            val tasks = (0 until 8).map { worker ->
                pool.submit(Callable {
                    repeat(500) { i ->
                        val k = key("r${(worker * 31 + i) % 120}", i % 3)
                        cache.put(k, 3, image())
                        cache.get(k, 3)
                        if (i % 97 == 0) cache.invalidate("r${i % 120}")
                    }
                })
            }
            tasks.forEach { it.get(30, TimeUnit.SECONDS) }
        } finally {
            pool.shutdownNow()
        }

        assertTrue(cache.size <= 50, "size ${cache.size}")
    }
}
