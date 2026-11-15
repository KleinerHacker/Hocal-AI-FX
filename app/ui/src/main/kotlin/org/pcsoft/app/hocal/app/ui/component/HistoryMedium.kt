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

package org.pcsoft.app.hocal.app.ui.component

/**
 * One medium of the history of a reference.
 *
 * @property reference UUID of the reference the medium belongs to
 * @property number position of the medium in the history, `0` is the original and the last number is the
 *   current medium
 */
data class HistoryMedium(val reference: String, val number: Int) {

    /** The number as shown to the user, with three digits: `000`, `001`, ... */
    val label: String get() = "%03d".format(number)
}
