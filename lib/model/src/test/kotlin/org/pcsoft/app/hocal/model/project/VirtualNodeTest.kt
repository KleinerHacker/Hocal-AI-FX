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
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper

/** Developer tests for [VirtualNode], [VirtualFolder] and [VirtualEntry]. */
class VirtualNodeTest {

    private val mapper = JsonMapper.builder().build()

    /** A new folder is empty and has no name. */
    @Test
    fun folderDefaults() {
        val folder = VirtualFolder()

        assertEquals("", folder.name)
        assertTrue(folder.children.isEmpty())
    }

    /** An entry carries exactly one reference UUID. */
    @Test
    fun entryLinksReference() {
        val entry = VirtualEntry()

        entry.name = "fox.png"
        entry.reference = "6f1c1f0e-0000-4000-8000-000000000003"

        assertEquals("fox.png", entry.name)
        assertEquals("6f1c1f0e-0000-4000-8000-000000000003", entry.reference)
    }

    /** The tree is recursive: folders hold folders and entries on several levels. */
    @Test
    fun recursiveTree() {
        val root = VirtualFolder()
        val folder = VirtualFolder().apply { name = "animals" }
        val entry = VirtualEntry().apply {
            name = "fox"
            reference = "6f1c1f0e-0000-4000-8000-000000000004"
        }

        folder.children.add(entry)
        root.children.add(folder)

        assertSame(entry, (root.children[0] as VirtualFolder).children[0])
    }

    /** One reference can be linked by several entries. */
    @Test
    fun oneReferenceByManyEntries() {
        val uuid = "6f1c1f0e-0000-4000-8000-000000000005"
        val first = VirtualEntry().apply { reference = uuid }
        val second = VirtualEntry().apply { reference = uuid }

        assertEquals(first.reference, second.reference)
    }

    /** A folder is written with the JSON type `folder`, an entry with the type `entry`. */
    @Test
    fun jsonCarriesTheType() {
        val folder = VirtualFolder().apply { name = "animals" }
        val entry = VirtualEntry().apply { name = "fox"; reference = "ref" }

        assertTrue(mapper.writeValueAsString(folder).contains("\"type\":\"folder\""))
        assertTrue(mapper.writeValueAsString(entry).contains("\"type\":\"entry\""))
    }

    /** The JSON type decides which class a node is read as, on every level of the tree. */
    @Test
    fun jsonRoundTripKeepsTheHierarchy() {
        val root = VirtualFolder().apply {
            name = "root"
            children.add(VirtualFolder().apply {
                name = "animals"
                children.add(VirtualEntry().apply { name = "fox"; reference = "ref-1" })
            })
        }

        val read = mapper.readValue(mapper.writeValueAsString(root), VirtualNode::class.java)

        val folder = assertInstanceOf(VirtualFolder::class.java, read)
        val animals = assertInstanceOf(VirtualFolder::class.java, folder.children[0])
        val fox = assertInstanceOf(VirtualEntry::class.java, animals.children[0])
        assertEquals("root", folder.name)
        assertEquals("animals", animals.name)
        assertEquals("fox", fox.name)
        assertEquals("ref-1", fox.reference)
    }
}
