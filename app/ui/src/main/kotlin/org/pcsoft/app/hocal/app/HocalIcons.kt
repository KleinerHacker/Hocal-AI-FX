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

import javafx.scene.image.Image
import java.util.concurrent.ConcurrentHashMap

/**
 * Central access to all icons of the user interface.
 *
 * Icons are stored flat in the resource folder `icons`, one PNG per name and size, named
 * `<name>@<size>.png`. The file name is built here and nowhere else.
 */
object HocalIcons {

    /** Resource folder holding every icon file. */
    const val ICON_FOLDER: String = "/icons/"

    /** Name of the application icon. */
    const val APP: String = "app"

    /** Name of the icon of a closed (collapsed) folder; shipped in light and dark mode, in [FOLDER_SIZES]. */
    const val FOLDER_CLOSED: String = "folder-closed"

    /** Name of the icon of an open (expanded) folder; shipped in light and dark mode, in [FOLDER_SIZES]. */
    const val FOLDER_OPEN: String = "folder-open"

    /** Name of the neutral placeholder shown where a medium preview is not available; shipped in light and dark mode, in [PREVIEW_SIZES]. */
    const val MEDIA_PLACEHOLDER: String = "media-placeholder"

    /** Edge lengths in pixels in which the preview placeholder is shipped: the display size and the HiDPI size. */
    val PREVIEW_SIZES: List<Int> = listOf(48, 96)

    /** Name of the icon of the add button (a plus); shipped in light and dark mode, in [BUTTON_SIZES]. */
    const val ADD: String = "add"

    /** Name of the icon of the remove button (a cross); shipped in light and dark mode, in [BUTTON_SIZES]. */
    const val REMOVE: String = "remove"

    /** Name of the icon of the send button; shipped in light and dark mode, in [BUTTON_SIZES]. */
    const val SEND: String = "send"

    /** Edge lengths in pixels in which the button icons are shipped: the display size and the HiDPI size. */
    val BUTTON_SIZES: List<Int> = listOf(16, 32)

    /** Edge lengths in pixels in which the application icon is shipped. */
    val APP_SIZES: List<Int> = listOf(16, 24, 32, 48, 64, 128, 256)

    /** Edge lengths in pixels in which the folder icons are shipped: the display size and the HiDPI size. */
    val FOLDER_SIZES: List<Int> = listOf(16, 32)

    private const val DARK_SUFFIX = "-dark"

    private val cache = ConcurrentHashMap<String, Image>()

    /**
     * Builds the file name of an icon.
     *
     * @param name purpose of the icon in lower kebab-case, e.g. `app`
     * @param size edge length of the square image in pixels
     * @return the file name `<name>@<size>.png`
     */
    fun fileName(name: String, size: Int): String = "$name@$size.png"

    /**
     * Loads an icon; every icon is read only once.
     *
     * @param name purpose of the icon in lower kebab-case, e.g. `app`
     * @param size edge length of the square image in pixels
     * @return the icon image
     * @throws IllegalArgumentException if no icon file exists for the name and size
     */
    fun image(name: String, size: Int): Image = cache.computeIfAbsent(fileName(name, size)) { file ->
        val stream = HocalIcons::class.java.getResourceAsStream(ICON_FOLDER + file)
            ?: throw IllegalArgumentException("Icon not found: $ICON_FOLDER$file")
        stream.use { Image(it) }
    }

    /**
     * Loads an icon for the light or the dark appearance; every icon is read only once.
     *
     * The dark variant is stored as `<name>-dark@<size>.png`.
     *
     * @param name purpose of the icon in lower kebab-case, e.g. `folder-open`
     * @param size edge length of the square image in pixels
     * @param dark `true` for the variant for dark backgrounds, `false` for the one for light backgrounds
     * @return the icon image
     * @throws IllegalArgumentException if no icon file exists for the name, variant and size
     */
    fun image(name: String, size: Int, dark: Boolean): Image = image(if (dark) name + DARK_SUFFIX else name, size)

    /**
     * The application icon in all shipped sizes, so the window system picks the best fit.
     *
     * @return one image per entry of [APP_SIZES]
     */
    fun appIcons(): List<Image> = APP_SIZES.map { image(APP, it) }
}
