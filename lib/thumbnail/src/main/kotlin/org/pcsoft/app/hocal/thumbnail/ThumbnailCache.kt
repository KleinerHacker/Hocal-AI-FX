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
 * A store for loaded thumbnails of the value type [V], so a thumbnail is loaded once and not on every look at its folder.
 *
 * This is the extension point for later caches: [MemoryThumbnailCache] lives for the run of
 * the application only, [ZipThumbnailStore] keeps the thumbnails in a file, [TieredThumbnailCache] combines two
 * caches and [SizeClassCache] splits by size. All implement this interface and replace each other freely.
 *
 * A thumbnail is stored together with the number of media its reference had when it was loaded. Removing
 * or appending a medium changes that number, so a stored thumbnail is stale as soon as the number the
 * caller asks with differs.
 *
 * All members are called from several threads, so an implementation has to be thread-safe.
 */
interface ThumbnailCache<V : Any> {

    /**
     * Returns the stored thumbnail, if there is one that is still valid.
     *
     * A thumbnail stored for another number of media is stale: it is dropped and `null` is returned.
     *
     * @param key the thumbnail to look for
     * @param mediaCount the number of media the reference has now
     * @return the thumbnail, or `null` if there is none or it is stale
     */
    fun get(key: ThumbnailKey, mediaCount: Int): V?

    /**
     * Stores a successfully loaded thumbnail. Never store a placeholder or a failure here.
     *
     * @param key the thumbnail
     * @param mediaCount the number of media the reference has now
     * @param value the loaded thumbnail
     */
    fun put(key: ThumbnailKey, mediaCount: Int, value: V)

    /**
     * Drops every thumbnail of a reference, whatever its medium number and size.
     *
     * @param reference UUID of the reference
     */
    fun invalidate(reference: String)

    /** Drops every thumbnail. */
    fun clear()
}
