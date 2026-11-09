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

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension

/**
 * Developer tests for [HocalTheme] and [HocalColorScheme].
 */
@ExtendWith(ApplicationExtension::class)
class HocalThemeTest {

    private val originalColorScheme = HocalTheme.colorScheme

    /** Restores the colour scheme, so a test selecting one does not influence the following ones. */
    @AfterEach
    fun restoreColorScheme() {
        HocalTheme.colorScheme = originalColorScheme
    }

    private fun read(path: String): String =
        HocalTheme::class.java.getResourceAsStream(path)!!.use { it.reader().readText() }

    private fun variablesOf(css: String): Set<String> =
        Regex("(-hocal-[a-z-]+)\\s*:").findAll(css).map { it.groupValues[1] }.toSet()

    /**
     * Use case: every stylesheet the theme names is shipped with the application, so applying the
     * theme never fails for a missing resource.
     */
    @Test
    fun everyStylesheetIsShipped() {
        (HocalColorScheme.entries.map { it.path } + HocalTheme.BASE_PATH + HocalTheme.COMPONENT_PATHS).forEach {
            assertNotNull(HocalTheme::class.java.getResource(it), "missing stylesheet $it")
        }
    }

    /**
     * Use case: switching between the light and the dark scheme never leaves a colour undefined,
     * because both schemes define the same palette variables.
     */
    @Test
    fun bothColorSchemesDefineTheSamePalette() {
        val light = variablesOf(read(HocalColorScheme.LIGHT.path))
        val dark = variablesOf(read(HocalColorScheme.DARK.path))

        assertTrue(light.isNotEmpty(), "the light scheme must define palette variables")
        assertEquals(light, dark)
    }

    /**
     * Use case: a scene receives the stylesheets in layer order - colour scheme first, then the base,
     * then the components - so the later layers can use what the earlier ones define.
     */
    @Test
    fun stylesheetsFollowTheLayerOrder() {
        HocalTheme.colorScheme = HocalColorScheme.DARK

        val expected = (listOf(HocalColorScheme.DARK.path, HocalTheme.BASE_PATH) + HocalTheme.COMPONENT_PATHS)
            .map { HocalTheme::class.java.getResource(it)!!.toExternalForm() }
        assertEquals(expected, HocalTheme.stylesheets)
    }

    /**
     * Use case: a scene decorated twice keeps every stylesheet exactly once.
     */
    @Test
    fun applyingTwiceAddsEveryStylesheetOnce() {
        val scene = Scene(StackPane())

        HocalTheme.apply(scene)
        HocalTheme.apply(scene)

        assertEquals(HocalTheme.stylesheets, scene.stylesheets.toList())
    }

    /**
     * Use case: installing the theme pins Modena as the base look, so the appearance does not change
     * with the platform default.
     */
    @Test
    fun installSelectsModena() {
        HocalTheme.install()

        assertEquals(Application.STYLESHEET_MODENA, Application.getUserAgentStylesheet())
    }
}
