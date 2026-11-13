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

/**
 * The place where the media of a project live while it is open: a [MediaSource] and [MediaSink]
 * that can also remove and renumber media. The medium number is its position in the history of
 * its reference; the extension belongs to the medium and travels with it.
 */
interface MediaStore : MediaSource, MediaSink {

    /**
     * Removes one medium.
     *
     * @throws java.nio.file.NoSuchFileException if the medium does not exist.
     */
    fun delete(reference: String, number: Int)

    /**
     * Gives a medium another number, keeping its content and extension.
     *
     * @throws java.nio.file.NoSuchFileException if the medium does not exist.
     * @throws java.nio.file.FileAlreadyExistsException if the target number is taken.
     */
    fun move(reference: String, from: Int, to: Int)

    /** The numbers of all media of a reference, ascending; empty if the reference has none. */
    fun numbers(reference: String): List<Int>

    /** Removes all media of a reference; a reference without media is no error. */
    fun deleteReference(reference: String) {
        for (number in numbers(reference)) delete(reference, number)
    }
}
