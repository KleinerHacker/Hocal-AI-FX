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
import org.pcsoft.app.hocal.fx.model.manager.ProjectManager
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
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
}
