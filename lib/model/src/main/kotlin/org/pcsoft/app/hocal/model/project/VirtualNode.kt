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
 * A node of the virtual directory structure: a folder or an entry.
 *
 * The tree is recursive; a folder holds its children, an entry links one reference by its UUID. One
 * reference can be linked by several entries.
 */
class VirtualNode {

    /** Name of the node. */
    var name: String = ""

    /** Whether the node is a folder or an entry. */
    var type: VirtualNodeType = VirtualNodeType.FOLDER

    /** UUID of the linked reference; only used by an entry, `null` for a folder. */
    var reference: String? = null

    /** The children; only used by a folder, empty for an entry. */
    var children: MutableList<VirtualNode> = mutableListOf()
}
