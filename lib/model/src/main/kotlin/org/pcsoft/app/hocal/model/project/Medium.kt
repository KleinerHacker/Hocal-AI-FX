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

/**
 * One medium of a reference together with the details of its creation.
 *
 * A medium belongs to the history of its [Reference]; its number (`000` to `999`) is its position
 * in [Reference.media]. Every detail may be empty. Explicitly used additional images are NOT
 * recorded.
 *
 * @property prompt The prompt, `null` if empty.
 * @property model Id of the model the medium was generated with, `null` if empty.
 * @property flow Id of the flow the medium was generated with, `null` if empty.
 * @property seed The seed used, `null` if empty.
 * @property parameters The values of the editable flow parameters by parameter id. A value is stored
 *   as text whatever the type of its parameter is (text, number, boolean); the parameter definition
 *   of the flow tells how it is read.
 */
data class Medium(
    var prompt: String? = null,
    var model: String? = null,
    var flow: String? = null,
    var seed: Long? = null,
    var parameters: MutableMap<String, String> = linkedMapOf(),
)
