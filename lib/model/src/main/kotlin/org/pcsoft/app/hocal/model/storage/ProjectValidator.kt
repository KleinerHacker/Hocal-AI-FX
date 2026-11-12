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
package org.pcsoft.app.hocal.model.storage

import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.project.ProjectFormat
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.pcsoft.app.hocal.model.project.VirtualNode

/** Checks the rules of a [ProjectFile] that hold for reading as well as for writing. */
internal object ProjectValidator {

    /** Allowed form of a reference UUID; it is used as folder name in the ZIP. */
    val NAME_PATTERN: Regex = Regex("[A-Za-z0-9-]{1,64}")

    /**
     * Validates the format version, the references, the empty name of the root folder and the links of the tree.
     *
     * @throws UnsupportedFormatVersionException if the format version is newer than known.
     * @throws InvalidProjectFileException on every other violation.
     */
    fun validate(file: ProjectFile) {
        val version = file.project.formatVersion
        if (version > ProjectFormat.FORMAT_VERSION) {
            throw UnsupportedFormatVersionException(version, ProjectFormat.FORMAT_VERSION)
        }
        if (version < 1) {
            throw InvalidProjectFileException("Invalid format version $version")
        }

        val uuids = HashSet<String>()
        for (reference in file.references) {
            if (!NAME_PATTERN.matches(reference.uuid)) {
                throw InvalidProjectFileException("Invalid reference UUID '${reference.uuid}'")
            }
            if (!uuids.add(reference.uuid)) {
                throw InvalidProjectFileException("Duplicate reference UUID '${reference.uuid}'")
            }
            val count = reference.media.size
            if (count < 1) {
                throw InvalidProjectFileException("Reference '${reference.uuid}' has no medium")
            }
            if (count > ProjectFormat.MEDIUM_MAX + 1) {
                throw InvalidProjectFileException("Reference '${reference.uuid}' has more than 1000 media")
            }
        }

        if (file.tree.name.isNotEmpty()) {
            throw InvalidProjectFileException("The root folder must have no name, found '${file.tree.name}'")
        }

        val pending = ArrayDeque<VirtualNode>()
        pending.add(file.tree)
        while (pending.isNotEmpty()) {
            when (val node = pending.removeLast()) {
                is VirtualFolder -> pending.addAll(node.children)
                is VirtualEntry -> if (node.reference !in uuids) {
                    throw InvalidProjectFileException(
                        "Entry '${node.name}' links the unknown reference '${node.reference}'",
                    )
                }
            }
        }
    }
}
