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
package org.pcsoft.app.hocal.model.tree

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Developer tests for the name rules of the virtual tree. */
class TreeNamesTest {

    /** Ordinary names, also with umlauts, inner spaces and dots, are valid. */
    @Test
    fun validNames() {
        for (name in listOf("fox", "Füchse", "a b", "x.png", "..x", "a-b_c", "日本")) {
            assertTrue(TreeNames.isValid(name), name)
        }
    }

    /** An empty name, a space at the start or the end, and a name above 255 characters are invalid. */
    @Test
    fun emptyBlankAndLongNamesAreInvalid() {
        for (name in listOf("", " ", " x", "x ", "x".repeat(256))) {
            assertFalse(TreeNames.isValid(name), name)
        }
        assertTrue(TreeNames.isValid("x".repeat(255)))
    }

    /** Separators, characters that are forbidden in file names, and control characters are invalid. */
    @Test
    fun forbiddenCharactersAreInvalid() {
        for (name in listOf("a/b", "a\\b", "a:b", "a*b", "a?b", "a\"b", "a<b", "a>b", "a|b", "a\u0007b", "a\tb")) {
            assertFalse(TreeNames.isValid(name), name)
        }
    }

    /** The names `.` and `..` are reserved. */
    @Test
    fun dotNamesAreInvalid() {
        assertFalse(TreeNames.isValid("."))
        assertFalse(TreeNames.isValid(".."))
    }

    /** A rejected name is reported with the name itself. */
    @Test
    fun validateReportsTheName() {
        val error = assertThrows(InvalidNameException::class.java) { TreeNames.validate("a/b") }

        assertEquals("a/b", error.name)
    }

    /** Upper and lower case are not told apart when names are compared. */
    @Test
    fun sameNameIgnoresCase() {
        assertTrue(TreeNames.sameName("Fox", "fox"))
        assertTrue(TreeNames.sameName("Füchse", "FÜCHSE"))
        assertFalse(TreeNames.sameName("fox", "wolf"))
    }
}
