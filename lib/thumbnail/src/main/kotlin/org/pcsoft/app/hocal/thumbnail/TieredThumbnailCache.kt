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

/**
 * Two caches in a row: a fast one in memory and a second one, that keeps its entries in another form - on disk, for
 * instance.
 *
 * A look first asks the [memory]; if it has nothing, the [disk] is asked and what it knows is converted back and
 * moved into the memory. A new thumbnail goes into both. Invalidating and clearing reach both.
 *
 * @param V the type of the thumbnails in the memory
 * @param D the type of the entries of the second cache
 * @param memory the first cache
 * @param disk the second cache
 * @param toDisk converts a thumbnail for the second cache; `null` keeps it out of the second cache
 * @param fromDisk converts an entry of the second cache back; `null` counts as no hit
 */
class TieredThumbnailCache<V : Any, D : Any>(
    private val memory: ThumbnailCache<V>,
    private val disk: ThumbnailCache<D>,
    private val toDisk: (V) -> D?,
    private val fromDisk: (D) -> V?,
) : ThumbnailCache<V> {

    override fun get(key: ThumbnailKey, mediaCount: Int): V? {
        memory.get(key, mediaCount)?.let { return it }
        val stored = disk.get(key, mediaCount) ?: return null
        return fromDisk(stored)?.also { memory.put(key, mediaCount, it) }
    }

    override fun put(key: ThumbnailKey, mediaCount: Int, value: V) {
        memory.put(key, mediaCount, value)
        toDisk(value)?.let { disk.put(key, mediaCount, it) }
    }

    override fun invalidate(reference: String) {
        memory.invalidate(reference)
        disk.invalidate(reference)
    }

    override fun clear() {
        memory.clear()
        disk.clear()
    }
}
