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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Developer tests for [VirtualNode] and [VirtualNodeType]. */
class VirtualNodeTest {

    /** A new node is an empty folder without a reference. */
    @Test
    fun defaults() {
        val node = VirtualNode()

        assertEquals("", node.name)
        assertEquals(VirtualNodeType.FOLDER, node.type)
        assertNull(node.reference)
        assertTrue(node.children.isEmpty())
    }

    /** An entry carries exactly one reference UUID. */
    @Test
    fun entryLinksReference() {
        val entry = VirtualNode()

        entry.name = "fox.png"
        entry.type = VirtualNodeType.ENTRY
        entry.reference = "6f1c1f0e-0000-4000-8000-000000000003"

        assertEquals("fox.png", entry.name)
        assertEquals(VirtualNodeType.ENTRY, entry.type)
        assertEquals("6f1c1f0e-0000-4000-8000-000000000003", entry.reference)
    }

    /** The tree is recursive: folders hold folders and entries on several levels. */
    @Test
    fun recursiveTree() {
        val root = VirtualNode()
        val folder = VirtualNode().apply { name = "animals" }
        val entry = VirtualNode().apply {
            name = "fox"
            type = VirtualNodeType.ENTRY
            reference = "6f1c1f0e-0000-4000-8000-000000000004"
        }

        folder.children.add(entry)
        root.children.add(folder)

        assertSame(entry, root.children[0].children[0])
    }

    /** One reference can be linked by several entries. */
    @Test
    fun oneReferenceByManyEntries() {
        val uuid = "6f1c1f0e-0000-4000-8000-000000000005"
        val first = VirtualNode().apply { type = VirtualNodeType.ENTRY; reference = uuid }
        val second = VirtualNode().apply { type = VirtualNodeType.ENTRY; reference = uuid }

        assertEquals(first.reference, second.reference)
    }

    /** Both kinds of node exist. */
    @Test
    fun nodeTypes() {
        assertEquals(listOf(VirtualNodeType.FOLDER, VirtualNodeType.ENTRY), VirtualNodeType.entries)
    }
}
