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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Developer tests for [ThumbnailPixels]. */
class ThumbnailPixelsTest {

    /** Use case: pixels with a fitting byte count are accepted, and equal content makes equal pixels. */
    @Test
    fun equalContentIsEqual() {
        val first = ThumbnailPixels(2, 1, byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8))
        val second = ThumbnailPixels(2, 1, byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8))
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertNotEquals(first, ThumbnailPixels(2, 1, ByteArray(8)))
        assertNotEquals(first, ThumbnailPixels(1, 2, byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)))
    }

    /** Use case: a byte count that does not fit the size, or a size that is not positive, is a programming error. */
    @Test
    fun wrongSizesAreRejected() {
        assertThrows<IllegalArgumentException> { ThumbnailPixels(2, 1, ByteArray(7)) }
        assertThrows<IllegalArgumentException> { ThumbnailPixels(0, 1, ByteArray(0)) }
        assertThrows<IllegalArgumentException> { ThumbnailPixels(1, -1, ByteArray(4)) }
    }
}
