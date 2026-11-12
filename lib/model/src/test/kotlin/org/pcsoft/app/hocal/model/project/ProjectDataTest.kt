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

import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Developer tests for [ProjectData]. */
class ProjectDataTest {

    /** A new object is empty and carries the current format version. */
    @Test
    fun defaults() {
        val data = ProjectData()

        assertEquals("", data.name)
        assertEquals("", data.description)
        assertEquals("", data.uuid)
        assertEquals("", data.author)
        assertNull(data.created)
        assertNull(data.modified)
        assertTrue(data.tags.isEmpty())
        assertEquals(ProjectFormat.FORMAT_VERSION, data.formatVersion)
        assertEquals("", data.applicationVersion)
    }

    /** Every field keeps the value written through its setter. */
    @Test
    fun settersAndGetters() {
        val created = Instant.parse("2026-01-02T03:04:05Z")
        val modified = Instant.parse("2026-02-03T04:05:06Z")
        val data = ProjectData()

        data.name = "Sample"
        data.description = "A sample project"
        data.uuid = "6f1c1f0e-0000-4000-8000-000000000001"
        data.author = "Jane Doe"
        data.created = created
        data.modified = modified
        data.tags = mutableListOf("one", "two")
        data.formatVersion = 3
        data.applicationVersion = "1.2.3"

        assertEquals("Sample", data.name)
        assertEquals("A sample project", data.description)
        assertEquals("6f1c1f0e-0000-4000-8000-000000000001", data.uuid)
        assertEquals("Jane Doe", data.author)
        assertEquals(created, data.created)
        assertEquals(modified, data.modified)
        assertEquals(listOf("one", "two"), data.tags)
        assertEquals(3, data.formatVersion)
        assertEquals("1.2.3", data.applicationVersion)
    }

    /** Two objects with equal values are equal, and a copy changes only what is named. */
    @Test
    fun equalityAndCopy() {
        val data = ProjectData(name = "Sample", author = "Jane Doe")

        assertEquals(ProjectData(name = "Sample", author = "Jane Doe"), data)
        assertEquals("Sample", data.copy(author = "John Doe").name)
        assertEquals("John Doe", data.copy(author = "John Doe").author)
    }
}
