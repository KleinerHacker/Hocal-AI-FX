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
 * Loads the thumbnail of a medium of a reference in the background; [loadCurrent] takes the current (last) one.
 *
 * The [cache] is asked first and receives every successful result, never a failure. The result is delivered on
 * the FX thread, and only if the load was not cancelled meanwhile. Every failure, also of the source, is reported
 * as `null`.
 *
 * Without arguments the loader uses what the whole application shares, see [ThumbnailService]: its registry, its
 * cache - which is emptied when the project changes - and its pool of load threads. Only a loader that is given
 * a number of threads has a pool of its own, which [close] ends; the shared pool is never ended by a loader.
 *
 * @param registry the registry of the preview providers
 * @param cache the cache of the loaded thumbnails
 * @param threads the number of load threads of a pool of its own, at least 1; `null` for the shared pool
 */
class ThumbnailLoader(
    private val registry: MediaPreviewRegistry = ThumbnailService.registry,
    private val cache: ThumbnailCache = ThumbnailService.cache,
    threads: Int? = null,
) {
    private class Handle : LoadHandle {
        @Volatile
        var cancelled = false

        override fun cancel() {
            cancelled = true
        }
    }

    private val counter = AtomicInteger()
    private val ownPool: ExecutorService? = threads?.let { count ->
        Executors.newFixedThreadPool(count) { task ->
            Thread(task, "hocal-chip-${counter.incrementAndGet()}").apply { isDaemon = true }
        }
    }
    private val pool: ExecutorService get() = ownPool ?: ThumbnailService.executor

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
    fun loadCurrent(source: MediaSource, reference: String, size: Int, onLoaded: (Image?) -> Unit): LoadHandle =
        submit(source, reference, size, { count -> count - 1 }, onLoaded)

    /**
     * Loads a medium of a reference, given by its number.
     *
     * @param source the source of the media
     * @param reference UUID of the reference
     * @param number position of the medium in the history, `0` is the original
     * @param size edge length of the thumbnail in pixels
     * @param onLoaded called on the FX thread with the thumbnail, or `null` if there is none - the number does
     *   not exist, for instance - or loading failed; never called after a cancel
     * @return the handle to cancel the load
     */
    fun load(source: MediaSource, reference: String, number: Int, size: Int, onLoaded: (Image?) -> Unit): LoadHandle =
        submit(source, reference, size, { number }, onLoaded)

    private fun submit(
        source: MediaSource,
        reference: String,
        size: Int,
        numberOf: (Int) -> Int,
        onLoaded: (Image?) -> Unit,
    ): LoadHandle {
        val handle = Handle()
        val deliver = { image: Image? -> Platform.runLater { if (!handle.cancelled) onLoaded(image) } }
        try {
            pool.submit {
                if (!handle.cancelled) deliver(loadImage(source, reference, size, numberOf))
            }
        } catch (_: RejectedExecutionException) {
            deliver(null)
        }
        return handle
    }

    private fun loadImage(source: MediaSource, reference: String, size: Int, numberOf: (Int) -> Int): Image? =
        try {
            val startStamp = stamp()
            val count = source.mediaCount(reference)
            val number = numberOf(count)
            if (count <= 0 || number !in 0 until count) {
                null
            } else {
                val key = ThumbnailKey(reference, number, size)
                cache.get(key, count) ?: registry.load(source, reference, number, size)?.also {
                    if (startStamp == stamp()) cache.put(key, count, it)
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

    /**
     * Drops every cached thumbnail, for instance because another media source - another project - is used.
     * A load that is still running does not store its result any more.
     */
    fun clear() {
        epoch++
        cache.clear()
    }

    // The invalidations of this loader and of the service, so that no load stores an image that went stale meanwhile.
    private fun stamp(): Pair<Int, Int> = epoch to ThumbnailService.epoch

    /** Ends the load threads of a pool of its own; loads that wait are dropped. The shared pool is not ended. */
    fun close() {
        ownPool?.shutdownNow()
    }
}
