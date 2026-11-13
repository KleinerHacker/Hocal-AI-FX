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

import org.pcsoft.app.hocal.model.storage.InconsistentHistoryException
import org.pcsoft.app.hocal.model.storage.MediaHistoryFullException
import org.pcsoft.app.hocal.model.storage.MediumNotFoundException
import org.pcsoft.app.hocal.model.storage.OriginalMediumException

import org.pcsoft.app.hocal.model.project.Medium
import org.pcsoft.app.hocal.model.project.ProjectFormat
import org.pcsoft.app.hocal.model.project.Reference
import java.io.InputStream

/*
 * The operations on the media history of a [Reference]: the last medium is the current one, a new
 * medium is appended, any medium but the original can be removed and the following ones move down.
 *
 * The details of a medium are part of the [Medium] in the list, so they follow it automatically; the
 * content in the [MediaStore] is moved in step. If the store fails midway, the list and the store may
 * differ; call [verifyMedia] then, or reload the project.
 */

/** The current medium: the last one of the history. */
val Reference.currentMedium: Medium
    get() = media.lastOrNull() ?: throw InconsistentHistoryException("Reference $uuid has no medium")

/** The number of the current medium. */
val Reference.currentNumber: Int
    get() {
        currentMedium
        return media.size - 1
    }

/**
 * Appends a medium as the new current one: highest number plus one.
 *
 * The content goes into the store first, the list is extended afterwards, so a failing store leaves
 * the list untouched.
 *
 * @param store The store that holds the content of the media.
 * @param medium The details of the new medium.
 * @param extension The file extension of the new medium, without dot.
 * @param content The content of the medium; it is not closed.
 * @return the number of the new medium.
 * @throws MediaHistoryFullException if the history already holds 1000 media.
 */
fun Reference.appendMedium(store: MediaStore, medium: Medium, extension: String, content: InputStream): Int {
    val number = media.size
    if (number > ProjectFormat.MEDIUM_MAX) throw MediaHistoryFullException(uuid)
    store.accept(uuid, number, extension, content)
    media.add(medium)
    return number
}

/**
 * Removes a medium and renumbers the following ones without gap. When the current medium is
 * removed, its predecessor becomes the current one.
 *
 * @param store The store that holds the content of the media.
 * @param number The number of the medium to remove.
 * @throws MediumNotFoundException if the number does not exist.
 * @throws OriginalMediumException for the number `000`.
 */
fun Reference.removeMedium(store: MediaStore, number: Int) {
    if (number !in media.indices) throw MediumNotFoundException(uuid, number)
    if (number == ProjectFormat.MEDIUM_MIN) throw OriginalMediumException(uuid)
    store.delete(uuid, number)
    for (next in number + 1 until media.size) {
        store.move(uuid, next, next - 1)
    }
    media.removeAt(number)
}

/**
 * Checks that the media in the store are exactly the numbers `000` to the last one of the history.
 *
 * @param store The store that holds the content of the media.
 * @throws InconsistentHistoryException on a missing medium, a gap or an extra medium.
 */
fun Reference.verifyMedia(store: MediaStore) {
    if (media.isEmpty()) throw InconsistentHistoryException("Reference $uuid has no medium")
    val expected = media.indices.toList()
    val actual = store.numbers(uuid)
    if (actual != expected) {
        throw InconsistentHistoryException("Media of reference $uuid are $actual but the history needs $expected")
    }
}
