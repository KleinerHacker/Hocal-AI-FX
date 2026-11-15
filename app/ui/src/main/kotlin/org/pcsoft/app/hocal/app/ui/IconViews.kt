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

package org.pcsoft.app.hocal.app.ui

import javafx.scene.image.ImageView
import javafx.stage.Screen
import org.pcsoft.app.hocal.app.HocalIcons

/**
 * Creates an image view of a button icon.
 *
 * The larger icon file is used on a HiDPI screen and scaled down to [displaySize], so the icon stays
 * sharp. The view keeps the aspect ratio and is smoothed.
 *
 * @param name name of the icon, see [HocalIcons]
 * @param dark `true` for the variant for dark backgrounds
 * @param displaySize edge length of the shown icon in device independent pixels
 * @return a new image view showing the icon
 * @throws IllegalArgumentException if the icon file does not exist
 */
fun iconView(name: String, dark: Boolean, displaySize: Double = 16.0): ImageView {
    val size = if (Screen.getPrimary().outputScaleX > 1.0) HocalIcons.BUTTON_SIZES.last() else HocalIcons.BUTTON_SIZES.first()
    return ImageView(HocalIcons.image(name, size, dark)).apply {
        fitWidth = displaySize
        fitHeight = displaySize
        isPreserveRatio = true
        isSmooth = true
    }
}
