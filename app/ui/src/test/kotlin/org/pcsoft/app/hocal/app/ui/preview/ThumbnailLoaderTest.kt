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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Developer tests of the thumbnail loader of the prompt chips.
 */
class ThumbnailLoaderTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val source = FakeMediaSource()
    private val cache = MemoryThumbnailCache()
    private val loader = ThumbnailLoader(cache = cache)

    @AfterEach
    fun closeLoader() {
        loader.close()
    }

    private fun load(from: MediaSource = source, reference: String = "r1"): CompletableFuture<Image?> {
        val future = CompletableFuture<Image?>()
        loader.loadCurrent(from, reference, 32) { future.complete(it) }
        return future
    }

    private fun result(future: CompletableFuture<Image?>): Image? = future.get(5, TimeUnit.SECONDS)

    private fun settle() {
        repeat(3) {
            Thread.sleep(50)
            WaitForAsyncUtils.waitForFxEvents()
        }
    }

    /**
     * Use case: a reference has an original and a later medium. The chip shows the current medium, so
     * the last one is loaded, scaled to the requested size, and only that one is opened.
     */
    @Test
    fun loadsTheLastMedium() {
        source.add("r1", "png" to pngBytes(100, 100, 0xFF0000), "png" to pngBytes(64, 64, 0x00FF00))

        val image = result(load())

        assertNotNull(image)
        assertEquals(32.0, image!!.width)
        assertEquals(0x00FF00, image.pixelReader.getArgb(5, 5) and 0xFFFFFF)
        assertEquals(1, source.openRequested.get())
    }

    /**
     * Use case: the same chip is loaded twice, for instance after the list was rebuilt. The second load
     * is answered from the cache and does not open the medium again.
     */
    @Test
    fun cacheHitDoesNotLoadAgain() {
        source.add("r1", "png" to pngBytes(50, 50))

        val first = result(load())
        val second = result(load())

        assertNotNull(first)
        assertEquals(first, second)
        assertEquals(1, source.openRequested.get())
    }

    /**
     * Use case: the medium is no image. The result is null and the failure is not cached, so a later
     * repair of the medium is noticed.
     */
    @Test
    fun brokenImageResultsInNullAndIsNotCached() {
        source.add("r1", "png" to brokenBytes())

        assertNull(result(load()))
        assertEquals(0, cache.size)
    }

    /**
     * Use case: the reference has no medium at all or is unknown. The result is null and nothing is
     * cached.
     */
    @Test
    fun missingMediumResultsInNull() {
        assertNull(result(load(reference = "unknown")))
        assertEquals(0, cache.size)
    }

    /**
     * Use case: the user removes the chip while its preview is still loading. The load is cancelled and
     * no result is delivered, not even null.
     */
    @Test
    fun cancelledLoadDeliversNothing() {
        source.add("r1", "png" to pngBytes(50, 50))
        source.gate = CountDownLatch(1)
        val future = CompletableFuture<Image?>()
        val handle = loader.loadCurrent(source, "r1", 32) { future.complete(it) }
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS) { source.openRequested.get() >= 1 }

        handle.cancel()
        source.gate!!.countDown()
        settle()

        assertFalse(future.isDone)
    }

    /**
     * Use case: the source throws an exception while it is asked, as a medium of the store was removed
     * meanwhile. The error does not escape; the result is null.
     */
    @Test
    fun errorOfTheSourceResultsInNull() {
        val failing = object : MediaSource {
            override fun mediaCount(reference: String): Int = throw IllegalStateException("store closed")
            override fun extension(reference: String, number: Int): String = "png"
            override fun open(reference: String, number: Int): InputStream = throw IllegalStateException("store closed")
        }

        assertNull(result(load(failing)))
    }

    /**
     * Use case: a medium was replaced and the reference is invalidated. The cache is dropped, so the next
     * load opens the medium again and shows the new content.
     */
    @Test
    fun invalidateLoadsAgain() {
        source.add("r1", "png" to pngBytes(50, 50, 0xFF0000))
        assertNotNull(result(load()))

        source.add("r1", "png" to pngBytes(50, 50, 0x0000FF))
        loader.invalidate("r1")
        val image = result(load())

        assertEquals(2, source.openRequested.get())
        assertEquals(0x0000FF, image!!.pixelReader.getArgb(5, 5) and 0xFFFFFF)
    }

    /**
     * Use case: the loader runs its work on daemon threads named `hocal-chip-N`, so the application can
     * end while a load is still running.
     */
    @Test
    fun loadsOnNamedDaemonThreads() {
        val threadInfo = CompletableFuture<Pair<String, Boolean>>()
        val spy = object : MediaSource {
            override fun mediaCount(reference: String): Int {
                threadInfo.complete(Thread.currentThread().name to Thread.currentThread().isDaemon)
                return 0
            }
            override fun extension(reference: String, number: Int): String = "png"
            override fun open(reference: String, number: Int): InputStream = throw IllegalStateException()
        }

        load(spy)
        val (name, daemon) = threadInfo.get(5, TimeUnit.SECONDS)

        assertTrue(name.startsWith("hocal-chip-"))
        assertTrue(daemon)
    }
}
