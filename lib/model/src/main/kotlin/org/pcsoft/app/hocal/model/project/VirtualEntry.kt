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
 * An entry of the virtual directory structure. It links exactly one reference and has no children.
 * One reference can be linked by several entries.
 *
 * Two entries are equal when their name and their reference are equal.
 */
class VirtualEntry : VirtualNode() {

    /** UUID of the linked reference. */
    var reference: String = ""

    override fun equals(other: Any?): Boolean =
        this === other || (other is VirtualEntry && name == other.name && reference == other.reference)

    override fun hashCode(): Int = 31 * name.hashCode() + reference.hashCode()

    override fun toString(): String = "VirtualEntry(name=$name, reference=$reference)"
}
