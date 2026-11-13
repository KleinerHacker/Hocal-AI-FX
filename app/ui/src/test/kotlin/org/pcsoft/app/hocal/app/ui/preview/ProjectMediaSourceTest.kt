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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.pcsoft.app.hocal.fx.model.manager.ProjectManager
import org.pcsoft.app.hocal.model.project.Medium
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.io.ByteArrayInputStream
import java.util.concurrent.Callable

/**
 * Developer tests of the media source of the project.
 *
 * The project manager accepts the FX thread only, and the source is asked from the test thread, as the load
 * threads of the previews do.
 */
class ProjectMediaSourceTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val source = ProjectMediaSource()

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    @BeforeEach
    fun createProject() {
        fx { ProjectManager.createNew("Test") }
    }

    @AfterEach
    fun closeProject() {
        fx { ProjectManager.close(discardChanges = true) }
    }

    private fun addReference(content: ByteArray, extension: String = "png"): String =
        fx { ProjectManager.addReference(Medium(), extension, ByteArrayInputStream(content)) }

    /**
     * Use case: a reference with one medium. Asked from a background thread, the source answers the number
     * of media, the extension and the content of the medium.
     */
    @Test
    fun answersCountExtensionAndContentFromAnotherThread() {
        val content = pngBytes(20, 20)
        val reference = addReference(content)

        assertEquals(1, source.mediaCount(reference))
        assertEquals("png", source.extension(reference, 0))
        assertArrayEquals(content, source.open(reference, 0).use { it.readBytes() })
    }

    /**
     * Use case: the same questions are asked on the FX thread itself. The answers are the same, and nothing
     * waits for itself.
     */
    @Test
    fun answersOnTheFxThread() {
        val content = pngBytes(20, 20)
        val reference = addReference(content)

        assertEquals(1, fx { source.mediaCount(reference) })
        assertEquals("png", fx { source.extension(reference, 0) })
        assertArrayEquals(content, fx { source.open(reference, 0).use { it.readBytes() } })
    }

    /**
     * Use case: a medium is appended to the history. The source counts it, and the new current medium is
     * the one with the last number.
     */
    @Test
    fun appendedMediumIsCounted() {
        val reference = addReference(pngBytes(20, 20, 0xFF0000))
        val appended = pngBytes(20, 20, 0x00FF00)

        fx { ProjectManager.appendMedium(reference, Medium(), "png", ByteArrayInputStream(appended)) }

        assertEquals(2, source.mediaCount(reference))
        assertArrayEquals(appended, source.open(reference, 1).use { it.readBytes() })
    }

    /**
     * Use case: a reference that does not exist, or no project at all. The number of media is zero.
     */
    @Test
    fun unknownReferenceAndMissingProjectHaveNoMedia() {
        assertEquals(0, source.mediaCount("unknown"))

        fx { ProjectManager.close(discardChanges = true) }

        assertEquals(0, source.mediaCount("unknown"))
    }

    /**
     * Use case: a medium that does not exist is asked for. The failure of the manager reaches the caller
     * as it is, and does not end up as a failure of the waiting.
     */
    @Test
    fun missingMediumFailsWithTheErrorOfTheManager() {
        val reference = addReference(pngBytes(20, 20))

        assertThrows<Exception> { source.extension(reference, 5) }
        assertThrows<Exception> { source.open(reference, 5) }
    }
}
