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

package org.pcsoft.app.hocal.app.ui.preview

/**
 * The details of the creation of one medium, as far as the user interface shows them.
 *
 * Explicitly used additional images are not part of the details.
 *
 * @property prompt The prompt, `null` if empty.
 * @property model Id of the model the medium was generated with, `null` if empty.
 * @property flow Id of the flow the medium was generated with, `null` if empty.
 */
data class MediumDetails(val prompt: String? = null, val model: String? = null, val flow: String? = null)
