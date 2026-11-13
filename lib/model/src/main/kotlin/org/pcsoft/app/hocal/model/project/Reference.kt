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
 * A reference of the project: the id of a medium history and its media with their details.
 *
 * @property uuid Unique id of the reference, as text; it names the folder of the reference in the ZIP.
 * @property media The media of the reference as a history. The position in the list is the medium
 *   number: index `0` is the original `000`, the last one is the current medium. The numbering is
 *   continuous by construction, so the medium number is never stored on its own.
 */
data class Reference(
    var uuid: String = "",
    var media: MutableList<Medium> = mutableListOf(),
)
