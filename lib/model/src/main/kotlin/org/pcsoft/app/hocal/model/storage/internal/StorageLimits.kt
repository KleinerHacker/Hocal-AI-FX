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
package org.pcsoft.app.hocal.model.storage.internal

import org.pcsoft.app.hocal.model.storage.DamagedProjectFileException

/**
 * Upper bounds applied while a project file is read, as protection against hostile or broken files.
 * Exceeding a bound is reported as [DamagedProjectFileException].
 *
 * @property maxProjectFileBytes Largest accepted size of `project.json` in bytes.
 * @property maxMediumBytes Largest accepted size of one medium in bytes.
 * @property maxEntries Largest accepted number of ZIP entries.
 */
data class StorageLimits(
    val maxProjectFileBytes: Int = 64 * 1024 * 1024,
    val maxMediumBytes: Long = 4L * 1024 * 1024 * 1024,
    val maxEntries: Int = 1_000_000,
) {
    init {
        require(maxProjectFileBytes > 0) { "maxProjectFileBytes must be positive" }
        require(maxMediumBytes > 0) { "maxMediumBytes must be positive" }
        require(maxEntries > 0) { "maxEntries must be positive" }
    }
}
