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
 * The history details of one medium: what it was generated with.
 *
 * Every value may be empty. Explicitly used additional images are NOT recorded.
 */
class MediumDetails {

    /** The prompt, `null` if empty. */
    var prompt: String? = null

    /** Id of the model the medium was generated with, `null` if empty. */
    var model: String? = null

    /** Id of the flow the medium was generated with, `null` if empty. */
    var flow: String? = null

    /** The seed used, `null` if empty. */
    var seed: Long? = null

    /**
     * The values of the editable flow parameters by parameter id.
     *
     * A value is stored as text whatever the type of its parameter is (text, number, boolean); the
     * parameter definition of the flow tells how it is read.
     */
    var parameters: MutableMap<String, String> = linkedMapOf()
}
