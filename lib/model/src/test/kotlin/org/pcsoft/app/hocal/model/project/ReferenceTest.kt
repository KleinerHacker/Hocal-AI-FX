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

    /** A new reference has no id and no history details. */
    @Test
    fun defaults() {
        val reference = Reference()

        assertEquals("", reference.uuid)
        assertTrue(reference.media.isEmpty())
    }

    /** The history details are held by their three digit medium number. */
    @Test
    fun mediaByMediumNumber() {
        val reference = Reference()
        val original = MediumDetails()
        val second = MediumDetails()

        reference.uuid = "6f1c1f0e-0000-4000-8000-000000000002"
        reference.media[ProjectFormat.mediumKey(0)] = original
        reference.media[ProjectFormat.mediumKey(1)] = second

        assertEquals("6f1c1f0e-0000-4000-8000-000000000002", reference.uuid)
        assertSame(original, reference.media["000"])
        assertSame(second, reference.media["001"])
    }
}
