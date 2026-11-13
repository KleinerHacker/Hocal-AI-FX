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
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Developer tests for [Medium]. */
class MediumTest {

    /** A new medium has no value at all; every detail may be empty. */
    @Test
    fun everythingMayBeEmpty() {
        val medium = Medium()

        assertNull(medium.prompt)
        assertNull(medium.model)
        assertNull(medium.flow)
        assertNull(medium.seed)
        assertTrue(medium.parameters.isEmpty())
    }

    /** Every field keeps the value written through its setter, parameters by parameter id. */
    @Test
    fun settersAndGetters() {
        val medium = Medium()

        medium.prompt = "a red fox"
        medium.model = "sample-model"
        medium.flow = "sample-flow"
        medium.seed = 4_000_000_000L
        medium.parameters = linkedMapOf("steps" to "20", "sampler" to "euler", "tiled" to "true")

        assertEquals("a red fox", medium.prompt)
        assertEquals("sample-model", medium.model)
        assertEquals("sample-flow", medium.flow)
        assertEquals(4_000_000_000L, medium.seed)
        assertEquals(mapOf("steps" to "20", "sampler" to "euler", "tiled" to "true"), medium.parameters)
    }

    /** The parameter values keep the order they were put in. */
    @Test
    fun parametersKeepInsertionOrder() {
        val medium = Medium()

        medium.parameters["b"] = "1"
        medium.parameters["a"] = "2"

        assertEquals(listOf("b", "a"), medium.parameters.keys.toList())
    }

    /** Two media with equal values are equal, a different value makes them differ. */
    @Test
    fun equalityByValue() {
        assertEquals(Medium(prompt = "fox", seed = 1L), Medium(prompt = "fox", seed = 1L))
        assertNotEquals(Medium(prompt = "fox"), Medium(prompt = "wolf"))
    }
}
