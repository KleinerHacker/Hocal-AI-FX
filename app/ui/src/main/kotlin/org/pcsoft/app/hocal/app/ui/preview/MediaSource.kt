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

import java.io.InputStream

/**
 * Read access to the media of a reference, without any knowledge about files or model classes.
 *
 * The media of a reference form its history. The number of a medium is its position in that history:
 * 0 is the original, `mediaCount - 1` is the current medium.
 */
interface MediaSource {
    /**
     * Returns how many media the given reference has.
     *
     * @param reference UUID of the reference
     * @return the number of media, 0 if the reference has none or is unknown
     */
    fun mediaCount(reference: String): Int

    /**
     * Returns the file extension of a medium.
     *
     * @param reference UUID of the reference
     * @param number position of the medium in the history
     * @return the extension without the leading dot
     */
    fun extension(reference: String, number: Int): String

    /**
     * Opens the content of a medium. The caller closes the stream.
     *
     * @param reference UUID of the reference
     * @param number position of the medium in the history
     * @return a new stream over the content
     */
    fun open(reference: String, number: Int): InputStream
}
