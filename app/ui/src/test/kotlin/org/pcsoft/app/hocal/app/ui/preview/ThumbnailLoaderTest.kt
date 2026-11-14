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
import org.junit.jupiter.api.Assertions.assertSame
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
     * Use case: a medium of the history is loaded by its number, not the last one. The original and a later
     * medium are two thumbnails of their own, each opened once and then answered from the cache.
     */
    @Test
    fun loadsAMediumByItsNumber() {
        source.add("r1", "png" to pngBytes(50, 50, 0xFF0000), "png" to pngBytes(50, 50, 0x00FF00))
        fun byNumber(number: Int): Image? {
            val future = CompletableFuture<Image?>()
            loader.load(source, "r1", number, 32) { future.complete(it) }
            return result(future)
        }

        val original = byNumber(0)
        val current = byNumber(1)
        byNumber(0)
        byNumber(1)

        assertEquals(0xFF0000, original!!.pixelReader.getArgb(5, 5) and 0xFFFFFF)
        assertEquals(0x00FF00, current!!.pixelReader.getArgb(5, 5) and 0xFFFFFF)
        assertEquals(2, source.openRequested.get())
    }

    /**
     * Use case: a number that the history does not have - the medium was deleted meanwhile - is asked for.
     * The answer is null, and the source is not asked for a medium that does not exist.
     */
    @Test
    fun numberOutsideTheHistoryResultsInNull() {
        source.add("r1", "png" to pngBytes(50, 50))
        val future = CompletableFuture<Image?>()

        loader.load(source, "r1", 3, 32) { future.complete(it) }

        assertNull(result(future))
        assertEquals(0, source.openRequested.get())
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
     * Use case: another media source is used, as another project was opened, which has a reference with
     * the same UUID and the same number of media. After the cache was cleared, the thumbnail comes from
     * the new source and not from the cache.
     */
    @Test
    fun clearDropsTheThumbnailsOfTheFormerSource() {
        source.add("r1", "png" to pngBytes(50, 50, 0xFF0000))
        val other = FakeMediaSource().apply { add("r1", "png" to pngBytes(50, 50, 0x0000FF)) }
        val first = result(load())
        assertEquals(0xFF0000, first!!.pixelReader.getArgb(5, 5) and 0xFFFFFF)

        loader.clear()
        val second = result(load(other))

        assertEquals(1, cache.size)
        assertEquals(0x0000FF, second!!.pixelReader.getArgb(5, 5) and 0xFFFFFF)
        assertEquals(1, other.openRequested.get())
    }

    /**
     * Use case: a loader is made without arguments, as the components do. It stores what it loads in the cache
     * that the whole application shares, so another component finds it there.
     */
    @Test
    fun loaderWithoutArgumentsUsesTheSharedCache() {
        source.add("r1", "png" to pngBytes(50, 50, 0xFF0000))
        val shared = ThumbnailLoader()
        val future = CompletableFuture<Image?>()

        shared.load(source, "r1", 0, 32) { future.complete(it) }

        val image = result(future)
        assertNotNull(image)
        assertSame(image, ThumbnailService.cache.get(ThumbnailKey("r1", 0, 32), 1))
    }

    /**
     * Use case: a component that used the shared loader goes away and closes it. The pool that all components
     * share stays in use, so the other components keep loading.
     */
    @Test
    fun closingALoaderWithoutOwnPoolKeepsTheSharedPool() {
        source.add("r1", "png" to pngBytes(50, 50))
        val shared = ThumbnailLoader()

        shared.close()

        assertFalse(ThumbnailService.executor.isShutdown)
        val future = CompletableFuture<Image?>()
        ThumbnailLoader().load(source, "r1", 0, 32) { future.complete(it) }
        assertNotNull(result(future))
    }

    /**
     * Use case: a loader is given a number of threads, so it has a pool of its own. Closing it ends that pool:
     * a load that is asked for afterwards delivers nothing, and the shared pool is not touched.
     */
    @Test
    fun closingALoaderWithOwnPoolEndsItsPool() {
        source.add("r1", "png" to pngBytes(50, 50))
        val own = ThumbnailLoader(cache = MemoryThumbnailCache(), threads = 1)

        own.close()
        val future = CompletableFuture<Image?>()
        own.load(source, "r1", 0, 32) { future.complete(it) }

        assertNull(result(future))
        assertFalse(ThumbnailService.executor.isShutdown)
    }

    /**
     * Use case: the project is closed - the shared cache is cleared - while a load is still running. The load
     * ends and delivers its image, but does not store it: no image of the former project stays in the cache.
     */
    @Test
    fun clearDuringALoadKeepsTheResultOutOfTheSharedCache() {
        source.add("r1", "png" to pngBytes(50, 50, 0xFF0000))
        val gate = CountDownLatch(1)
        source.gate = gate
        val shared = ThumbnailLoader()
        val future = CompletableFuture<Image?>()
        shared.load(source, "r1", 0, 32) { future.complete(it) }
        while (source.openRequested.get() == 0) Thread.sleep(10)

        ThumbnailService.clear()
        gate.countDown()

        assertNotNull(result(future))
        assertNull(ThumbnailService.cache.get(ThumbnailKey("r1", 0, 32), 1))
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
