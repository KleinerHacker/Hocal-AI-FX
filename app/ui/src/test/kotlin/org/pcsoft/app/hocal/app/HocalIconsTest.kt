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

package org.pcsoft.app.hocal.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Tests of the central icon access.
 */
class HocalIconsTest {

    /**
     * Use case: the file name follows the scheme `<name>@<size>.png`.
     */
    @Test
    fun fileNameFollowsTheNamingScheme() {
        assertEquals("app@32.png", HocalIcons.fileName("app", 32))
        assertEquals("project-settings@16.png", HocalIcons.fileName("project-settings", 16))
    }

    /**
     * Use case: every shipped size of the application icon loads and is a square of exactly that size.
     */
    @Test
    fun everyAppIconSizeLoadsWithItsEdgeLength() {
        for (size in HocalIcons.APP_SIZES) {
            val image = HocalIcons.image(HocalIcons.APP, size)

            assertFalse(image.isError, "size $size")
            assertEquals(size.toDouble(), image.width, "width of $size")
            assertEquals(size.toDouble(), image.height, "height of $size")
        }
    }

    /**
     * Use case: the application icon has a transparent background, so its corner pixels are transparent.
     */
    @Test
    fun appIconHasATransparentBackground() {
        val image = HocalIcons.image(HocalIcons.APP, 256)

        assertEquals(0.0, image.pixelReader.getColor(0, 0).opacity)
        assertTrue(image.pixelReader.getColor(128, 128).opacity > 0.0)
    }

    /**
     * Use case: the application icon is offered in all shipped sizes, in ascending order.
     */
    @Test
    fun appIconsOfferAllSizes() {
        val sizes = HocalIcons.appIcons().map { it.width.toInt() }

        assertEquals(listOf(16, 24, 32, 48, 64, 128, 256), sizes)
    }

    /**
     * Use case: an icon is read only once, so asking again returns the same image.
     */
    @Test
    fun imageIsCached() {
        assertSame(HocalIcons.image(HocalIcons.APP, 48), HocalIcons.image(HocalIcons.APP, 48))
    }

    /**
     * Use case: an icon that does not exist is reported with its file name.
     */
    @Test
    fun missingIconFailsWithTheFileName() {
        val error = assertThrows<IllegalArgumentException> { HocalIcons.image("does-not-exist", 16) }

        assertTrue(error.message!!.contains("does-not-exist@16.png"))
        assertNull(error.cause)
    }
}
