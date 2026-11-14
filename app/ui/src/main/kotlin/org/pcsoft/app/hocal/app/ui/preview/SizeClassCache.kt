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
 * A [ThumbnailCache] that keeps small and large images apart, so that a few large images cannot fill the
 * memory that is meant for many small ones.
 *
 * An image of the edge length [largeFrom] or more goes into the [large] cache, which holds few images; every
 * other image goes into the [small] cache. Invalidating and clearing reach both.
 *
 * @param small the cache of the small thumbnails
 * @param large the cache of the large images, the ones of the image view
 * @param largeFrom the edge length in pixels from which an image counts as large
 */
class SizeClassCache(
    private val small: ThumbnailCache,
    private val large: ThumbnailCache,
    private val largeFrom: Int,
) : ThumbnailCache {

    private fun cacheOf(key: ThumbnailKey): ThumbnailCache = if (key.size >= largeFrom) large else small

    override fun get(key: ThumbnailKey, mediaCount: Int): Image? = cacheOf(key).get(key, mediaCount)

    override fun put(key: ThumbnailKey, mediaCount: Int, image: Image) = cacheOf(key).put(key, mediaCount, image)

    override fun invalidate(reference: String) {
        small.invalidate(reference)
        large.invalidate(reference)
    }

    override fun clear() {
        small.clear()
        large.clear()
    }
}
