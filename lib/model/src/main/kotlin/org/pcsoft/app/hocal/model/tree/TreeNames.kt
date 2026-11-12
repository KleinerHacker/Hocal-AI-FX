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

/**
 * The name rules of folders and entries of the virtual tree.
 *
 * A name
 * * is not empty and has no space at its start or end,
 * * has at most [MAX_LENGTH] characters,
 * * has no control character and none of `/ \ : * ? " < > |`, so it stays usable as a file name on
 *   every system, and
 * * is neither `.` nor `..`.
 *
 * Names are unique among the siblings of a folder, folders and entries sharing one name space, and
 * upper and lower case are not told apart. The root folder has the empty name and is exempt.
 */
object TreeNames {

    /** The longest allowed name. */
    const val MAX_LENGTH: Int = 255

    private val forbidden = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

    /**
     * Checks a name.
     *
     * @throws InvalidNameException if the name breaks a rule.
     */
    fun validate(name: String) {
        val problem = when {
            name.isEmpty() -> "the name is empty"
            name != name.trim() -> "the name starts or ends with a space"
            name.length > MAX_LENGTH -> "the name has more than $MAX_LENGTH characters"
            name == "." || name == ".." -> "the name is reserved"
            name.any { it.isISOControl() } -> "the name has a control character"
            name.any { it in forbidden } -> "the name has a character of / \\ : * ? \" < > |"
            else -> null
        }
        if (problem != null) throw InvalidNameException(name, problem)
    }

    /** Tells whether a name follows the rules. */
    fun isValid(name: String): Boolean =
        try {
            validate(name)
            true
        } catch (e: InvalidNameException) {
            false
        }

    /** Tells whether two names count as the same name among siblings: upper and lower case are ignored. */
    fun sameName(first: String, second: String): Boolean = first.equals(second, ignoreCase = true)
}
