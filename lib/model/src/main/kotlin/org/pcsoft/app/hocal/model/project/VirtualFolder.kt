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
package org.pcsoft.app.hocal.model.project

/**
 * A folder of the virtual directory structure. It holds children and no reference; the tree is
 * recursive.
 *
 * Two folders are equal when their name and their children are equal. The text form covers the whole
 * subtree, so a change deep inside it shows up in the text form of every folder above.
 */
class VirtualFolder : VirtualNode() {

    /** The children of the folder, folders and entries. */
    var children: MutableList<VirtualNode> = mutableListOf()

    override fun equals(other: Any?): Boolean =
        this === other || (other is VirtualFolder && name == other.name && children == other.children)

    override fun hashCode(): Int = 31 * name.hashCode() + children.hashCode()

    override fun toString(): String = "VirtualFolder(name=$name, children=$children)"
}
