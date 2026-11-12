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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Developer tests for [MediumDetails]. */
class MediumDetailsTest {

    /** A new object has no value at all; every detail may be empty. */
    @Test
    fun everythingMayBeEmpty() {
        val details = MediumDetails()

        assertNull(details.prompt)
        assertNull(details.model)
        assertNull(details.flow)
        assertNull(details.seed)
        assertTrue(details.parameters.isEmpty())
    }

    /** Every field keeps the value written through its setter, parameters by parameter id. */
    @Test
    fun settersAndGetters() {
        val details = MediumDetails()

        details.prompt = "a red fox"
        details.model = "sample-model"
        details.flow = "sample-flow"
        details.seed = 4_000_000_000L
        details.parameters = linkedMapOf("steps" to "20", "sampler" to "euler", "tiled" to "true")

        assertEquals("a red fox", details.prompt)
        assertEquals("sample-model", details.model)
        assertEquals("sample-flow", details.flow)
        assertEquals(4_000_000_000L, details.seed)
        assertEquals(mapOf("steps" to "20", "sampler" to "euler", "tiled" to "true"), details.parameters)
    }

    /** The parameter values keep the order they were put in. */
    @Test
    fun parametersKeepInsertionOrder() {
        val details = MediumDetails()

        details.parameters["b"] = "1"
        details.parameters["a"] = "2"

        assertEquals(listOf("b", "a"), details.parameters.keys.toList())
    }
}
