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
import org.pcsoft.app.hocal.fx.model.manager.ProjectManager
import org.pcsoft.app.hocal.thumbnail.ImageThumbnails
import org.pcsoft.app.hocal.thumbnail.MemoryThumbnailCache
import org.pcsoft.app.hocal.thumbnail.SizeClassCache
import org.pcsoft.app.hocal.thumbnail.ThumbnailCache
import org.pcsoft.app.hocal.thumbnail.TieredThumbnailCache
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * The one place that holds what all thumbnails of the application share: the cache, the registry of the preview
 * providers and the pool of the load threads.
 *
 * The cache holds images of the project. It is emptied whenever a project is opened, created or closed, so images of a
 * closed project - an encrypted one, for instance - do not stay in memory. The cache keeps small and large images apart,
 * see [SizeClassCache]: a small thumbnail lives in memory and in the file of the open project, see
 * [ProjectManager.thumbnailCache] - so it survives the memory cache within a session - a large image of the image view
 * lives in memory only.
 *
 * A loader that is made without its own cache and pool uses these. Everything is thread-safe; [threads] and the
 * clearing belong to whoever configures the application.
 */
object ThumbnailService {
    /** The edge length in pixels from which an image counts as large: the images of the image view. */
    const val LARGE_FROM: Int = 256

    /** The number of small thumbnails the cache holds. */
    const val THUMBNAIL_ENTRIES: Int = 500

    /** The number of large images the cache holds. */
    const val LARGE_ENTRIES: Int = 8

    /** The number of load threads when no other number is set. */
    const val DEFAULT_THREADS: Int = 5

    /** The registry of the preview providers. */
    val registry: MediaPreviewRegistry = MediaPreviewRegistry()

    /** The cache of all loaded thumbnails and images. */
    val cache: ThumbnailCache<Image> = SizeClassCache(
        small = TieredThumbnailCache(
            memory = MemoryThumbnailCache<Image>(THUMBNAIL_ENTRIES),
            disk = ProjectManager.thumbnailCache,
            toDisk = ImageThumbnails::toPixels,
            fromDisk = ImageThumbnails::toImage,
        ),
        large = MemoryThumbnailCache<Image>(LARGE_ENTRIES),
        largeFrom = LARGE_FROM,
    )

    private val lock = Any()
    private val counter = AtomicInteger()
    private var pool: ExecutorService? = null

    /**
     * Counts the times the cache was emptied or invalidated, so that a load that was running meanwhile does not
     * store a stale image.
     */
    @Volatile
    var epoch: Int = 0
        private set

    /**
     * The number of threads that load thumbnails at the same time, at least 1.
     *
     * Meant to be set from the settings of the application later. A new value is used by the loads that start
     * afterwards; loads that are running finish on the threads they started on.
     *
     * @throws IllegalArgumentException if the value is less than 1
     */
    var threads: Int = DEFAULT_THREADS
        set(value) {
            require(value >= 1) { "threads must be at least 1, was $value" }
            synchronized(lock) {
                if (field == value) return
                field = value
                pool?.shutdown()
                pool = null
            }
        }
        get() = synchronized(lock) { field }

    /** The pool of the load threads, created on demand. */
    val executor: ExecutorService
        get() = synchronized(lock) {
            pool ?: Executors.newFixedThreadPool(threads) { task ->
                Thread(task, "hocal-thumbnail-${counter.incrementAndGet()}").apply { isDaemon = true }
            }.also { pool = it }
        }

    init {
        // The service lives as long as the application, so this registration is never released on purpose.
        ProjectManager.loadedProperty.addListener { _, _, _ -> clear() }
    }

    /** Empties the cache; loads that were running do not store their result any more. */
    fun clear() {
        epoch++
        cache.clear()
    }

    /**
     * Drops the cached images of a reference, so the next load reads the media again.
     *
     * @param reference UUID of the reference
     */
    fun invalidate(reference: String) {
        epoch++
        cache.invalidate(reference)
    }
}
