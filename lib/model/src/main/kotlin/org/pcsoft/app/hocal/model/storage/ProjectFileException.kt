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

/**
 * Base of all errors that reading or writing a project file can report for its content.
 *
 * Plain I/O problems of the underlying file or stream are reported as [java.io.IOException] and are
 * not part of this hierarchy, except where a broken ZIP structure is detected while reading.
 */
sealed class ProjectFileException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * The content breaks a rule of the format: a missing or misplaced `project.json`, unreadable JSON,
 * a dangling entry link, a reference without medium, a mismatch between media and details, or an
 * unsafe or unknown ZIP entry name.
 */
class InvalidProjectFileException(message: String, cause: Throwable? = null) : ProjectFileException(message, cause)

/**
 * The ZIP container itself is broken (truncated, corrupt) or exceeds one of the [StorageLimits].
 */
class DamagedProjectFileException(message: String, cause: Throwable? = null) : ProjectFileException(message, cause)

/**
 * The file was written with a newer format version than this application knows.
 *
 * @property foundVersion The format version found in the file.
 * @property supportedVersion The newest format version this application can read.
 */
class UnsupportedFormatVersionException(
    val foundVersion: Int,
    val supportedVersion: Int,
) : ProjectFileException("Format version $foundVersion is newer than the supported version $supportedVersion")
