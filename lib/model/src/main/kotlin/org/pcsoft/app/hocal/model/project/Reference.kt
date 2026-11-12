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
 * A reference of the project: the id of a medium history and the history details of its media.
 */
class Reference {

    /** Unique id of the reference, as text; it names the folder of the reference in the ZIP. */
    var uuid: String = ""

    /**
     * The history details of the media of this reference, keyed by the three digit medium number
     * `000` to `999` (see [ProjectFormat.mediumKey]).
     */
    var media: MutableMap<String, MediumDetails> = linkedMapOf()
}
