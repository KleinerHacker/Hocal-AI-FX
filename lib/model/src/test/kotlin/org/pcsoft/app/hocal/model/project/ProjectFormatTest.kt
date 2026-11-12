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
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

/** Developer tests for [ProjectFormat]. */
class ProjectFormatTest {

    /** The key names are fixed by the format and must not change by accident. */
    @Test
    fun fixedKeyNames() {
        assertEquals("project.json", ProjectFormat.PROJECT_FILE_NAME)
        assertEquals("project", ProjectFormat.KEY_PROJECT)
        assertEquals("references", ProjectFormat.KEY_REFERENCES)
        assertEquals("tree", ProjectFormat.KEY_TREE)
        assertEquals("media", ProjectFormat.KEY_MEDIA)
    }

    /** The format starts at version 1. */
    @Test
    fun formatVersion() {
        assertEquals(1, ProjectFormat.FORMAT_VERSION)
    }

    /** A medium number is written with three digits, the borders included. */
    @Test
    fun mediumKeyPadsToThreeDigits() {
        assertEquals("000", ProjectFormat.mediumKey(0))
        assertEquals("007", ProjectFormat.mediumKey(7))
        assertEquals("042", ProjectFormat.mediumKey(42))
        assertEquals("999", ProjectFormat.mediumKey(999))
    }

    /** A medium number outside `0` to `999` is rejected. */
    @Test
    fun mediumKeyRejectsOutOfRange() {
        assertThrows(IllegalArgumentException::class.java) { ProjectFormat.mediumKey(-1) }
        assertThrows(IllegalArgumentException::class.java) { ProjectFormat.mediumKey(1000) }
    }
}
