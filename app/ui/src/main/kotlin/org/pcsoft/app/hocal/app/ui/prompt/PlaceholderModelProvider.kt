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

/**
 * A [ModelProvider] with fixed placeholder data, used until the real data comes from the catalog.
 *
 * It offers three models with three, two and one flow.
 */
class PlaceholderModelProvider : ModelProvider {
    private val textToImage = FlowOption("text-to-image", "Text to Image")
    private val imageToImage = FlowOption("image-to-image", "Image to Image")
    private val upscale = FlowOption("upscale", "Upscale")

    private val flowsByModel: Map<ModelOption, List<FlowOption>> = linkedMapOf(
        ModelOption("placeholder-1", "Placeholder model 1") to listOf(textToImage, imageToImage, upscale),
        ModelOption("placeholder-2", "Placeholder model 2") to listOf(textToImage, imageToImage),
        ModelOption("placeholder-3", "Placeholder model 3") to listOf(textToImage),
    )

    override fun models(): List<ModelOption> = flowsByModel.keys.toList()

    override fun flows(model: ModelOption): List<FlowOption> = flowsByModel[model] ?: emptyList()
}
