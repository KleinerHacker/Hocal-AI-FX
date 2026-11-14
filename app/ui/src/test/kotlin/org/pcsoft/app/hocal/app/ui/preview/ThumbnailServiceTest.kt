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

import org.pcsoft.app.hocal.thumbnail.ThumbnailKey
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.hocal.fx.model.manager.ProjectManager
import org.pcsoft.app.hocal.thumbnail.ImageThumbnails
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.nio.file.Path
import java.util.concurrent.Callable

/**
 * Developer tests of what all thumbnails share: the cache that is emptied with the project, and the pool.
 */
class ThumbnailServiceTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    @TempDir
    lateinit var directory: Path

    private val key = ThumbnailKey("r1", 0, 48)

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    private fun image(): Image = WritableImage(1, 1)

    @AfterEach
    fun closeProject() {
        fx { ProjectManager.close(discardChanges = true) }
        ThumbnailService.threads = ThumbnailService.DEFAULT_THREADS
    }

    /**
     * Use case: the project is closed. The images of it do not stay in memory: the shared cache is empty.
     */
    @Test
    fun closingTheProjectEmptiesTheCache() {
        fx { ProjectManager.createNew("Test") }
        ThumbnailService.cache.put(key, 1, image())
        assertNotNull(ThumbnailService.cache.get(key, 1))

        fx { ProjectManager.close(discardChanges = true) }

        assertNull(ThumbnailService.cache.get(key, 1))
    }

    /**
     * Use case: a new project is created while images of the former one are cached. The cache is empty, so no
     * image of the former project is taken for one of the new project.
     */
    @Test
    fun creatingAProjectEmptiesTheCache() {
        ThumbnailService.cache.put(key, 1, image())

        fx { ProjectManager.createNew("Test") }

        assertNull(ThumbnailService.cache.get(key, 1))
    }

    /**
     * Use case: the cache is cleared. It is empty, and the epoch counts it, so a load that was running meanwhile
     * does not store its result afterwards.
     */
    @Test
    fun clearEmptiesTheCacheAndCountsTheEpoch() {
        ThumbnailService.cache.put(key, 1, image())
        val before = ThumbnailService.epoch

        ThumbnailService.clear()

        assertNull(ThumbnailService.cache.get(key, 1))
        assertTrue(ThumbnailService.epoch > before)
    }

    /**
     * Use case: the media of a reference changed. Its images go, the images of other references stay.
     */
    @Test
    fun invalidateDropsOnlyTheImagesOfOneReference() {
        val other = ThumbnailKey("r2", 0, 48)
        val kept = image()
        ThumbnailService.cache.put(key, 1, image())
        ThumbnailService.cache.put(other, 1, kept)

        ThumbnailService.invalidate("r1")

        assertNull(ThumbnailService.cache.get(key, 1))
        assertSame(kept, ThumbnailService.cache.get(other, 1))
    }

    /**
     * Use case: a number of threads below one cannot load anything and is rejected at once.
     */
    @Test
    fun threadsBelowOneAreRejected() {
        assertThrows<IllegalArgumentException> { ThumbnailService.threads = 0 }
        assertEquals(ThumbnailService.DEFAULT_THREADS, ThumbnailService.threads)
    }

    /**
     * Use case: the number of threads is changed, as the settings will do. The loads that start afterwards use
     * a pool of the new size, and the same pool is used as long as the number stays.
     */
    @Test
    fun changingTheThreadsGivesANewPool() {
        val before = ThumbnailService.executor
        assertSame(before, ThumbnailService.executor)

        ThumbnailService.threads = 2

        assertNotSame(before, ThumbnailService.executor)
        assertEquals(2, ThumbnailService.threads)
    }

    /**
     * Use case: more small thumbnails are loaded than the memory holds. The first one is dropped from the memory,
     * but the file of the open project still has it: it is found again, with the same pixels.
     */
    @Test
    fun smallThumbnailSurvivesTheMemoryCacheInTheFileOfTheProject() {
        fx { ProjectManager.createNew("Test") }
        val first = ThumbnailKey("first", 0, 48)
        val original = image()
        ThumbnailService.cache.put(first, 1, original)
        repeat(ThumbnailService.THUMBNAIL_ENTRIES) { ThumbnailService.cache.put(ThumbnailKey("r$it", 0, 48), 1, image()) }

        val found = ThumbnailService.cache.get(first, 1)

        assertNotNull(found)
        assertNotSame(original, found)
        assertEquals(ImageThumbnails.toPixels(original), ImageThumbnails.toPixels(found!!))
    }

    /**
     * Use case: more large images are loaded than the memory holds. The large images of the image view are not
     * written to the file, so the first one is gone.
     */
    @Test
    fun largeImagesStayInMemoryOnly() {
        fx { ProjectManager.createNew("Test") }
        val first = ThumbnailKey("first", 0, ThumbnailService.LARGE_FROM)
        ThumbnailService.cache.put(first, 1, image())
        repeat(ThumbnailService.LARGE_ENTRIES) {
            ThumbnailService.cache.put(ThumbnailKey("r$it", 0, ThumbnailService.LARGE_FROM), 1, image())
        }

        assertNull(ThumbnailService.cache.get(first, 1))
    }

    /**
     * Use case: the media of a reference changed while its small thumbnails are in the file. They are dropped from
     * the file as well, so the next load reads the media again.
     */
    @Test
    fun invalidateReachesTheFileOfTheProject() {
        fx { ProjectManager.createNew("Test") }
        ThumbnailService.cache.put(key, 1, image())

        ThumbnailService.invalidate("r1")

        assertNull(ProjectManager.thumbnailCache.get(key, 1))
    }

    /**
     * Use case: the project is encrypted. A small thumbnail that left the memory is found again in the encrypted
     * file of the project, with the same pixels.
     */
    @Test
    fun smallThumbnailSurvivesTheMemoryCacheInTheEncryptedFile() {
        fx {
            ProjectManager.createNew("Test")
            ProjectManager.saveAs(directory.resolve("secret.hocal"), "secret".toCharArray())
        }
        val first = ThumbnailKey("first", 0, 48)
        val original = image()
        ThumbnailService.cache.put(first, 1, original)
        repeat(ThumbnailService.THUMBNAIL_ENTRIES) { ThumbnailService.cache.put(ThumbnailKey("r$it", 0, 48), 1, image()) }

        val found = ThumbnailService.cache.get(first, 1)

        assertNotNull(found)
        assertNotSame(original, found)
        assertEquals(ImageThumbnails.toPixels(original), ImageThumbnails.toPixels(found!!))
    }
}
