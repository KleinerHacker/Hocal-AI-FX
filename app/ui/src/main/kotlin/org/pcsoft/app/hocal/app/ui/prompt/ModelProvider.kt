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
 * Source of the models and flows the prompt input offers.
 */
interface ModelProvider {
    /**
     * Returns the models to choose from.
     *
     * @return the models in the order they are shown
     */
    fun models(): List<ModelOption>

    /**
     * Returns the flows of a model.
     *
     * @param model a model of [models]
     * @return the flows in the order they are shown
     */
    fun flows(model: ModelOption): List<FlowOption>
}
