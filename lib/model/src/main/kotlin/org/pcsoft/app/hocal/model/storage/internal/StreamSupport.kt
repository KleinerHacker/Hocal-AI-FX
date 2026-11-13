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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.FilterInputStream
import java.io.FilterOutputStream
import java.io.InputStream
import java.io.OutputStream

/** Only reads the format version, ignoring everything else, so files of a newer format can be recognised. */
@JsonIgnoreProperties(ignoreUnknown = true)
internal class FormatProbe(var project: ProjectProbe? = null)

/** The `project` part of [FormatProbe]. */
@JsonIgnoreProperties(ignoreUnknown = true)
internal class ProjectProbe(var formatVersion: Int = 1)

/** Passes everything on but never closes the stream it wraps. */
internal class NonClosingInputStream(input: InputStream) : FilterInputStream(input) {
    override fun close() {
        // the owner of the wrapped stream closes it
    }
}

/** Passes everything on but never closes the stream it wraps. */
internal class NonClosingOutputStream(output: OutputStream) : FilterOutputStream(output) {
    override fun write(buffer: ByteArray, offset: Int, length: Int) {
        out.write(buffer, offset, length)
    }

    override fun close() {
        flush()
    }
}

/** Limits the number of bytes read from one ZIP entry and never closes the underlying stream. */
internal class BoundedInputStream(
    private val delegate: InputStream,
    private val limit: Long,
    private val entryName: String,
) : InputStream() {

    private var count = 0L

    override fun read(): Int {
        val value = delegate.read()
        if (value >= 0) advance(1)
        return value
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val read = delegate.read(buffer, offset, length)
        if (read > 0) advance(read.toLong())
        return read
    }

    private fun advance(bytes: Long) {
        count += bytes
        if (count > limit) {
            throw DamagedProjectFileException("Entry '$entryName' exceeds the limit of $limit bytes")
        }
    }

    override fun close() {
        // the ZIP stream stays open for the next entry
    }
}
