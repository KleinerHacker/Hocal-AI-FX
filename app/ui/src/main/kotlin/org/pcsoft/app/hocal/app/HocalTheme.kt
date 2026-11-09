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
import javafx.application.ColorScheme
import javafx.application.Platform
import javafx.scene.Scene

/**
 * Central access point for the visual appearance of the application.
 *
 * The theme is split into three layers, which are applied to a scene in exactly this order:
 *
 * 1. the colour scheme ([HocalColorScheme]), the only stylesheet carrying colour literals,
 * 2. [BASE_PATH], the type face and the window surface,
 * 3. [COMPONENT_PATHS], one stylesheet per styled control.
 *
 * Every resource path lives here exactly once, so no other place in the code base names a
 * stylesheet.
 *
 * [install] prepares the platform default and selects the colour scheme, every scene of the
 * application is then decorated through [apply].
 */
object HocalTheme {

    /** Resource path of the base stylesheet, which is independent of the colour scheme. */
    const val BASE_PATH: String = "/styles/base.css"

    /**
     * Resource paths of the component stylesheets, one per styled control.
     *
     * Styling another control means adding a stylesheet next to the existing ones and listing it
     * here.
     */
    val COMPONENT_PATHS: List<String> = listOf(
        "/styles/component/menu-bar.css",
        "/styles/component/context-menu.css",
        "/styles/component/list-view.css",
        "/styles/component/scroll-bar.css"
    )

    /**
     * Colour scheme the theme is dressed in.
     *
     * Resolved by [install] from the colour scheme of the operating system, read exactly once at
     * start up. Scenes decorated before a change keep the scheme they were decorated with.
     */
    var colorScheme: HocalColorScheme = HocalColorScheme.LIGHT

    /** External forms of the stylesheets of the theme, in the order they have to be applied. */
    val stylesheets: List<String>
        get() = (listOf(colorScheme.path) + BASE_PATH + COMPONENT_PATHS).map(::externalForm)

    /**
     * Installs the base look the application theme builds upon.
     *
     * Modena is set explicitly so that the appearance does not change with the platform default; the
     * theme itself only overrides what it needs on top of it. The colour scheme follows the
     * operating system.
     */
    fun install() {
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA)
        colorScheme = systemColorScheme()
    }

    /**
     * Adds the stylesheets of the theme to the given scene.
     *
     * Calling this more than once for the same scene is safe - a stylesheet is added only if it is
     * not present yet.
     *
     * @param scene the scene to decorate
     */
    fun apply(scene: Scene) {
        stylesheets.forEach { stylesheet ->
            if (stylesheet !in scene.stylesheets) {
                scene.stylesheets += stylesheet
            }
        }
    }

    /**
     * Reads the colour scheme of the operating system, falling back to [HocalColorScheme.LIGHT].
     *
     * [Platform.getPreferences] needs a running toolkit and is therefore guarded: outside a JavaFX
     * application the call fails, which is a missing answer here, not an error.
     */
    private fun systemColorScheme(): HocalColorScheme =
        runCatching { Platform.getPreferences().colorScheme }
            .getOrNull()
            .let { if (it == ColorScheme.DARK) HocalColorScheme.DARK else HocalColorScheme.LIGHT }

    /**
     * Resolves a resource path into the external form JavaFX expects in `Scene.getStylesheets()`.
     *
     * @param path resource path of a stylesheet of the theme
     * @return external form of the resource
     * @throws IllegalArgumentException if the stylesheet is not shipped with the application
     */
    private fun externalForm(path: String): String {
        val url = requireNotNull(HocalTheme::class.java.getResource(path)) {
            "Stylesheet resource not found: $path"
        }
        return url.toExternalForm()
    }
}
