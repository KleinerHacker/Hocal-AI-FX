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

    /** Edge lengths in pixels in which the application icon is shipped. */
    val APP_SIZES: List<Int> = listOf(16, 24, 32, 48, 64, 128, 256)

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
     * The application icon in all shipped sizes, so the window system picks the best fit.
     *
     * @return one image per entry of [APP_SIZES]
     */
    fun appIcons(): List<Image> = APP_SIZES.map { image(APP, it) }
}
