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
package org.pcsoft.app.hocal.thumbnail

/**
 * The pixels of a thumbnail as plain bytes, free of any toolkit class, so a store can keep them in a file.
 *
 * The bytes are BGRA, four per pixel, with premultiplied alpha, row by row.
 *
 * @property width the width in pixels
 * @property height the height in pixels
 * @property bytes the pixel bytes, `width * height * 4` of them; the array is not copied
 * @throws IllegalArgumentException if a size is not positive or the byte count does not match
 */
class ThumbnailPixels(val width: Int, val height: Int, val bytes: ByteArray) {

    init {
        require(width > 0 && height > 0) { "The size must be positive, was ${width}x$height" }
        require(bytes.size.toLong() == width.toLong() * height * BYTES_PER_PIXEL) {
            "The byte count ${bytes.size} does not fit ${width}x$height"
        }
    }

    override fun equals(other: Any?): Boolean =
        other is ThumbnailPixels && width == other.width && height == other.height && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = 31 * (31 * width + height) + bytes.contentHashCode()

    /** The number of bytes per pixel. */
    companion object {
        /** Four bytes: blue, green, red, alpha. */
        const val BYTES_PER_PIXEL: Int = 4
    }
}
