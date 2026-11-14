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

package org.pcsoft.app.hocal.app.ui.dialog

import org.pcsoft.app.hocal.app.ui.preview.MediaSource
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO

/**
 * Media source of the demo: every reference has exactly one medium, a small PNG generated on demand.
 *
 * The color of the picture depends on the reference, so the entries can be told apart.
 */
class DemoMediaSource : MediaSource {

    /**
     * Returns the number of media of a reference.
     *
     * @param reference UUID of the reference
     * @return always 1
     */
    override fun mediaCount(reference: String): Int = 1

    /**
     * Returns the file extension of a medium.
     *
     * @param reference UUID of the reference
     * @param number position of the medium in the history
     * @return always `png`
     */
    override fun extension(reference: String, number: Int): String = "png"

    /**
     * Opens the generated picture of a reference.
     *
     * @param reference UUID of the reference
     * @param number position of the medium in the history
     * @return a new stream over the PNG bytes
     */
    override fun open(reference: String, number: Int): InputStream {
        val width = 160
        val height = 120
        val hue = (reference.hashCode() and 0x7FFFFFFF) % 360 / 360f
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        try {
            graphics.color = Color.getHSBColor(hue, 0.55f, 0.85f)
            graphics.fillRect(0, 0, width, height)
            graphics.color = Color.getHSBColor(hue, 0.7f, 0.45f)
            graphics.fillOval(width / 4, height / 4, width / 2, height / 2)
        } finally {
            graphics.dispose()
        }
        val bytes = ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray()
        return ByteArrayInputStream(bytes)
    }
}
