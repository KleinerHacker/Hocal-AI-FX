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

import javafx.application.Platform
import javafx.scene.image.Image
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Loads the thumbnail of the current (last) medium of a reference in the background.
 *
 * The loading runs on a pool of daemon threads named `hocal-chip-N`. The [cache] is asked first and
 * receives every successful result, never a failure. The result is delivered on the FX thread, and only
 * if the load was not cancelled meanwhile. Every failure, also of the source, is reported as `null`.
 *
 * @param registry the registry of the preview providers
 * @param cache the cache of the loaded thumbnails
 * @param threads the number of load threads, at least 1
 */
class ThumbnailLoader(
    private val registry: MediaPreviewRegistry = MediaPreviewRegistry(),
    private val cache: ThumbnailCache = MemoryThumbnailCache(),
    threads: Int = 2,
) {
    private class Handle : LoadHandle {
        @Volatile
        var cancelled = false

        override fun cancel() {
            cancelled = true
        }
    }

    private val counter = AtomicInteger()
    private val pool: ExecutorService = Executors.newFixedThreadPool(threads) { task ->
        Thread(task, "hocal-chip-${counter.incrementAndGet()}").apply { isDaemon = true }
    }

    // Counts the invalidations, so that a load that was running meanwhile does not store a stale image.
    @Volatile
    private var epoch = 0

    /**
     * Loads the last medium of a reference.
     *
     * @param source the source of the media
     * @param reference UUID of the reference
     * @param size edge length of the thumbnail in pixels
     * @param onLoaded called on the FX thread with the thumbnail, or `null` if there is none or loading
     *   failed; never called after a cancel
     * @return the handle to cancel the load
     */
    fun loadCurrent(source: MediaSource, reference: String, size: Int, onLoaded: (Image?) -> Unit): LoadHandle {
        val handle = Handle()
        val deliver = { image: Image? -> Platform.runLater { if (!handle.cancelled) onLoaded(image) } }
        try {
            pool.submit {
                if (!handle.cancelled) deliver(load(source, reference, size))
            }
        } catch (_: RejectedExecutionException) {
            deliver(null)
        }
        return handle
    }

    private fun load(source: MediaSource, reference: String, size: Int): Image? =
        try {
            val startEpoch = epoch
            val count = source.mediaCount(reference)
            if (count <= 0) {
                null
            } else {
                val key = ThumbnailKey(reference, count - 1, size)
                cache.get(key, count) ?: registry.load(source, reference, count - 1, size)?.also {
                    if (startEpoch == epoch) cache.put(key, count, it)
                }
            }
        } catch (_: Exception) {
            null
        }

    /**
     * Drops the cached thumbnails of a reference, so the next load reads the medium again.
     *
     * @param reference UUID of the reference
     */
    fun invalidate(reference: String) {
        epoch++
        cache.invalidate(reference)
    }

    /** Ends the load threads; loads that wait are dropped. */
    fun close() {
        pool.shutdownNow()
    }
}
