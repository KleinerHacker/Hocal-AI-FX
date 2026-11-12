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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Developer tests for [ProjectFile]. */
class ProjectFileTest {

    /** A new root holds empty project data, no reference and an empty root folder. */
    @Test
    fun defaults() {
        val file = ProjectFile()

        assertNotNull(file.project)
        assertTrue(file.references.isEmpty())
        assertEquals(VirtualNodeType.FOLDER, file.tree.type)
        assertTrue(file.tree.children.isEmpty())
    }

    /** All three parts of the file can be exchanged and are returned as set. */
    @Test
    fun partsCanBeSet() {
        val file = ProjectFile()
        val data = ProjectData().apply { name = "Sample" }
        val reference = Reference().apply { uuid = "6f1c1f0e-0000-4000-8000-000000000006" }
        val tree = VirtualNode().apply { name = "root" }

        file.project = data
        file.references = mutableListOf(reference)
        file.tree = tree

        assertSame(data, file.project)
        assertSame(reference, file.references[0])
        assertSame(tree, file.tree)
    }
}
