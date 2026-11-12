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

import java.io.InputStream

/**
 * Receives the media of a project file while it is read, one after another.
 *
 * Implementations store the content where they need it (a cache directory, a database), so no
 * medium has to be held completely in memory.
 */
fun interface MediaSink {

    /**
     * Takes one medium.
     *
     * @param reference UUID of the reference the medium belongs to.
     * @param number Medium number, equal to the index in the media list of the reference.
     * @param extension File extension without dot, e.g. `png`.
     * @param content The content; it MUST be consumed inside this call and MUST NOT be closed.
     */
    fun accept(reference: String, number: Int, extension: String, content: InputStream)
}

/**
 * Delivers the media of a project while it is written.
 *
 * [open] is called twice per medium: once to measure size and checksum, once to write the content.
 */
interface MediaSource {

    /**
     * Gives the file extension of a medium.
     *
     * @param reference UUID of the reference.
     * @param number Medium number, equal to the index in the media list of the reference.
     * @return the extension without dot, 1 to 16 letters or digits.
     */
    fun extension(reference: String, number: Int): String

    /**
     * Opens the content of a medium; the caller closes the stream.
     *
     * @param reference UUID of the reference.
     * @param number Medium number, equal to the index in the media list of the reference.
     */
    fun open(reference: String, number: Int): InputStream
}
