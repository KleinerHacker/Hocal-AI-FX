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
package org.pcsoft.app.hocal.model.project

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Developer tests for [Reference]. */
class ReferenceTest {

    /** A new reference has no id and no media. */
    @Test
    fun defaults() {
        val reference = Reference()

        assertEquals("", reference.uuid)
        assertTrue(reference.media.isEmpty())
    }

    /** The media are a history: the position is the medium number, the last one is current. */
    @Test
    fun mediaAreAHistoryByPosition() {
        val original = Medium(prompt = "original")
        val second = Medium(prompt = "second")
        val reference = Reference(uuid = "6f1c1f0e-0000-4000-8000-000000000002")

        reference.media.add(original)
        reference.media.add(second)

        assertEquals("6f1c1f0e-0000-4000-8000-000000000002", reference.uuid)
        assertSame(original, reference.media[0])
        assertSame(second, reference.media.last())
    }

    /** Removing a medium from the middle renumbers the following ones without any key to fix. */
    @Test
    fun removingFromTheMiddleRenumbersByPosition() {
        val first = Medium(prompt = "000")
        val middle = Medium(prompt = "001")
        val last = Medium(prompt = "002")
        val reference = Reference(media = mutableListOf(first, middle, last))

        reference.media.remove(middle)

        assertSame(last, reference.media[1])
    }

    /** Two references with equal values are equal. */
    @Test
    fun equalityByValue() {
        assertEquals(
            Reference("a", mutableListOf(Medium(prompt = "fox"))),
            Reference("a", mutableListOf(Medium(prompt = "fox"))),
        )
    }
}
