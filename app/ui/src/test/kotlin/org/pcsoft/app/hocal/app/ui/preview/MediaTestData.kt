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

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import javax.imageio.ImageIO

/** Creates a small PNG of the given size and color as bytes. */
internal fun pngBytes(width: Int, height: Int, rgb: Int = 0xFF0000): ByteArray {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until width) for (y in 0 until height) image.setRGB(x, y, rgb)
    return ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray()
}

/** Bytes that are no image in any format. */
internal fun brokenBytes(): ByteArray = "this is not an image".toByteArray()

/**
 * In-memory media source for the tests: reference to the list of media, each with extension and content.
 *
 * An optional [gate] holds back every [open] until it is released; the counters tell how far the loading came.
 */
internal class FakeMediaSource : MediaSource {
    private val media = mutableMapOf<String, List<Pair<String, ByteArray>>>()

    /** If set, [open] waits for it before it returns the stream. */
    @Volatile
    var gate: CountDownLatch? = null

    /** Number of [open] calls started. */
    val openRequested = AtomicInteger()

    /** Number of streams closed again, i.e. loads that came to an end. */
    val closed = AtomicInteger()

    /** Registers the media of a reference, in the order of its history. */
    fun add(reference: String, vararg items: Pair<String, ByteArray>) {
        media[reference] = items.toList()
    }

    override fun mediaCount(reference: String): Int = media[reference]?.size ?: 0

    override fun extension(reference: String, number: Int): String = media.getValue(reference)[number].first

    override fun open(reference: String, number: Int): InputStream {
        openRequested.incrementAndGet()
        gate?.await()
        val bytes = media.getValue(reference)[number].second
        return object : FilterInputStream(ByteArrayInputStream(bytes)) {
            override fun close() {
                super.close()
                closed.incrementAndGet()
            }
        }
    }
}
