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

package org.pcsoft.app.hocal.app.ui.prompt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Developer tests of the placeholder model provider.
 */
class PlaceholderModelProviderTest {
    private val provider = PlaceholderModelProvider()

    /**
     * Use case: the prompt input asks for the models before the catalog exists. Three placeholder models
     * come back in a fixed order, with the names "Placeholder model 1" to "Placeholder model 3".
     */
    @Test
    fun offersThreePlaceholderModels() {
        assertEquals(
            listOf("Placeholder model 1", "Placeholder model 2", "Placeholder model 3"),
            provider.models().map { it.name },
        )
    }

    /**
     * Use case: the flows of the three models are asked for. The first model offers three flows, the
     * second two and the third one, each named like the real flows later on.
     */
    @Test
    fun modelsOfferThreeTwoAndOneFlows() {
        val models = provider.models()

        assertEquals(listOf("Text to Image", "Image to Image", "Upscale"), provider.flows(models[0]).map { it.name })
        assertEquals(listOf("Text to Image", "Image to Image"), provider.flows(models[1]).map { it.name })
        assertEquals(listOf("Text to Image"), provider.flows(models[2]).map { it.name })
    }

    /**
     * Use case: a flow shared by several models keeps the same identifier, so a chosen flow can be kept
     * when the model changes; an unknown model has no flows.
     */
    @Test
    fun sharedFlowKeepsItsIdAndUnknownModelHasNoFlows() {
        val models = provider.models()

        assertEquals(provider.flows(models[0]).first().id, provider.flows(models[2]).first().id)
        assertTrue(provider.flows(ModelOption("unknown", "Unknown")).isEmpty())
    }
}
