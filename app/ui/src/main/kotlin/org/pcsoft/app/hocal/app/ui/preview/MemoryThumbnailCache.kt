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
 * A [ThumbnailCache] that keeps the thumbnails in memory for the run of the application.
 *
 * It holds at most [maxEntries] thumbnails. When it is full, the thumbnail that was used least recently
 * is dropped. All members are synchronized, so the load threads and the FX thread may use it at once.
 *
 * @param maxEntries the largest number of thumbnails to hold, at least 1
 * @throws IllegalArgumentException if [maxEntries] is less than 1
 */
class MemoryThumbnailCache(private val maxEntries: Int = DEFAULT_MAX_ENTRIES) : ThumbnailCache {

    private class Stored(val mediaCount: Int, val image: Image)

    private val lock = Any()

    // Ordered by access, so the eldest entry is the one used least recently.
    private val entries = object : LinkedHashMap<ThumbnailKey, Stored>(INITIAL_CAPACITY, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<ThumbnailKey, Stored>?): Boolean =
            size > maxEntries
    }

    init {
        require(maxEntries >= 1) { "maxEntries must be at least 1, was $maxEntries" }
    }

    /** The number of thumbnails held now. */
    val size: Int
        get() = synchronized(lock) { entries.size }

    override fun get(key: ThumbnailKey, mediaCount: Int): Image? = synchronized(lock) {
        val stored = entries[key] ?: return null
        if (stored.mediaCount != mediaCount) {
            entries.remove(key)
            return null
        }
        stored.image
    }

    override fun put(key: ThumbnailKey, mediaCount: Int, image: Image) {
        synchronized(lock) { entries[key] = Stored(mediaCount, image) }
    }

    override fun invalidate(reference: String) {
        synchronized(lock) { entries.keys.removeIf { it.reference == reference } }
    }

    override fun clear() {
        synchronized(lock) { entries.clear() }
    }

    companion object {
        /** The number of thumbnails held when no limit is given. */
        const val DEFAULT_MAX_ENTRIES: Int = 500

        private const val INITIAL_CAPACITY = 16
        private const val LOAD_FACTOR = 0.75f
    }
}
