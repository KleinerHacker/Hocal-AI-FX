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

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * A node of the virtual directory structure: a [VirtualFolder] or a [VirtualEntry].
 *
 * The kind of a node is written to JSON as the field `type` (`folder` or `entry`).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = ProjectFormat.KEY_TYPE)
@JsonSubTypes(
    JsonSubTypes.Type(value = VirtualFolder::class, name = ProjectFormat.TYPE_FOLDER),
    JsonSubTypes.Type(value = VirtualEntry::class, name = ProjectFormat.TYPE_ENTRY),
)
sealed class VirtualNode {

    /** Name of the node. */
    var name: String = ""
}
