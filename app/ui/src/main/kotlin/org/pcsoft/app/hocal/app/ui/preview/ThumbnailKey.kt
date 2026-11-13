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
 * Identifies one thumbnail: a medium of a reference at a pixel size.
 *
 * The key does not name a virtual entry on purpose: several entries may link the same reference and
 * share its thumbnails, and the thumbnails stay valid when the user switches the folder.
 *
 * @property reference UUID of the reference the medium belongs to
 * @property number position of the medium in the history of the reference, `0` is the original
 * @property size edge length of the thumbnail in pixels
 */
data class ThumbnailKey(val reference: String, val number: Int, val size: Int)
