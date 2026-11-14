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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Developer tests for [TieredThumbnailCache]; the second tier stores the length of the text. */
class TieredThumbnailCacheTest {

    private val memory = MemoryThumbnailCache<String>()
    private val disk = MemoryThumbnailCache<Int>()
    private val key = ThumbnailKey("r1", 0, 48)

    private fun cache(toDisk: (String) -> Int? = { it.length }, fromDisk: (Int) -> String? = { "x".repeat(it) }) =
        TieredThumbnailCache(memory, disk, toDisk, fromDisk)

    /** Use case: a stored thumbnail lands in both tiers. */
    @Test
    fun putStoresInBothTiers() {
        cache().put(key, 1, "abc")
        assertEquals("abc", memory.get(key, 1))
        assertEquals(3, disk.get(key, 1))
    }

    /** Use case: the memory tier is asked first, its very value is returned. */
    @Test
    fun memoryAnswersFirst() {
        memory.put(key, 1, "memory")
        disk.put(key, 1, 99)
        assertEquals("memory", cache().get(key, 1))
    }

    /** Use case: the memory has nothing, the second tier knows it: the value is converted and moved into the memory. */
    @Test
    fun diskHitFillsTheMemory() {
        disk.put(key, 1, 2)
        assertEquals("xx", cache().get(key, 1))
        assertEquals("xx", memory.get(key, 1))
    }

    /** Use case: neither tier knows the thumbnail. */
    @Test
    fun missIsNull() {
        assertNull(cache().get(key, 1))
    }

    /** Use case: the media count changed. Both tiers treat the thumbnail as stale. */
    @Test
    fun staleThumbnailIsAMissInBothTiers() {
        val tiered = cache()
        tiered.put(key, 1, "abc")
        assertNull(tiered.get(key, 2))
        assertNull(memory.get(key, 1))
        assertNull(disk.get(key, 1))
    }

    /** Use case: a thumbnail the second tier cannot hold stays in the memory only. */
    @Test
    fun nothingConvertibleStaysInTheMemory() {
        val tiered = cache(toDisk = { null })
        tiered.put(key, 1, "abc")
        assertEquals("abc", tiered.get(key, 1))
        assertNull(disk.get(key, 1))
    }

    /** Use case: an entry of the second tier cannot be converted back. It counts as a miss. */
    @Test
    fun unconvertibleEntryIsAMiss() {
        disk.put(key, 1, 2)
        assertNull(cache(fromDisk = { null }).get(key, 1))
        assertNull(memory.get(key, 1))
    }

    /** Use case: invalidating a reference and clearing reach both tiers. */
    @Test
    fun invalidateAndClearReachBothTiers() {
        val tiered = cache()
        tiered.put(key, 1, "abc")
        tiered.put(ThumbnailKey("r2", 0, 48), 1, "def")

        tiered.invalidate("r1")
        assertNull(memory.get(key, 1))
        assertNull(disk.get(key, 1))
        assertEquals("def", memory.get(ThumbnailKey("r2", 0, 48), 1))

        tiered.clear()
        assertEquals(0, memory.size)
        assertEquals(0, disk.size)
    }
}
