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

import org.pcsoft.app.hocal.model.project.ProjectFormat

/** Base of the errors of the media history operations. */
sealed class MediaHistoryException(message: String) : Exception(message)

/** The history of a reference already holds the maximum of 1000 media. */
class MediaHistoryFullException(val reference: String) :
    MediaHistoryException("Reference $reference already holds the maximum of ${ProjectFormat.MEDIUM_MAX + 1} media")

/** The original medium `000` cannot be removed; consequently a reference never ends up without a medium. */
class OriginalMediumException(val reference: String) :
    MediaHistoryException("The original medium of reference $reference cannot be removed")

/** The reference has no medium with this number. */
class MediumNotFoundException(val reference: String, val number: Int) :
    MediaHistoryException("Reference $reference has no medium $number")

/** The history details and the media in the store do not belong together. */
class InconsistentHistoryException(message: String) : MediaHistoryException(message)
