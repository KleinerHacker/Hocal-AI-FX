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
 * Constants of the project file format: the format version, the fixed file name and the fixed
 * key names of `project.json`.
 */
object ProjectFormat {

    /** Version of the file format written by this application; a newer version in a file is rejected. */
    const val FORMAT_VERSION: Int = 1

    /** Name of the description file in the root of the project ZIP. */
    const val PROJECT_FILE_NAME: String = "project.json"

    /** Key of the project data object in `project.json`. */
    const val KEY_PROJECT: String = "project"

    /** Key of the reference list in `project.json`. */
    const val KEY_REFERENCES: String = "references"

    /** Key of the virtual directory tree in `project.json`. */
    const val KEY_TREE: String = "tree"

    /** Key of the media history details below a reference. */
    const val KEY_MEDIA: String = "media"

    /** Number of the first medium of a reference, the original. */
    const val MEDIUM_MIN: Int = 0

    /** Number of the last possible medium of a reference. */
    const val MEDIUM_MAX: Int = 999

    /** Number of digits of a medium number, as it is used in file names and keys. */
    const val MEDIUM_DIGITS: Int = 3

    /**
     * Formats a medium number as the three digit key used in `project.json` and in the file names.
     *
     * @param number the medium number, `0` to `999`
     * @return the number with leading zeros, e.g. `007`
     * @throws IllegalArgumentException if the number is outside `0` to `999`
     */
    fun mediumKey(number: Int): String {
        require(number in MEDIUM_MIN..MEDIUM_MAX) { "Medium number out of range: $number" }
        return number.toString().padStart(MEDIUM_DIGITS, '0')
    }
}
