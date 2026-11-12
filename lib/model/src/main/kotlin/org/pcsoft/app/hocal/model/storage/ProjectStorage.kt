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

import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.project.ProjectFormat
import tools.jackson.core.JacksonException
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Reads and writes the project ZIP: `project.json` as first entry and the media as
 * `<reference uuid>/<NNN>.<extension>`. All entries are written uncompressed ([ZipEntry.STORED]).
 *
 * The object holds no state. The stream variants never close the streams they are given, so a later
 * layer (e.g. encryption) can wrap them. The media are streamed through [MediaSink] and [MediaSource].
 */
object ProjectStorage {

    private const val BUFFER_SIZE = 64 * 1024

    private val mapper: JsonMapper = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build()

    private val mediaEntry = Regex("([A-Za-z0-9-]{1,64})/(\\d{3})\\.([A-Za-z0-9]{1,16})")

    private val extensionPattern = Regex("[A-Za-z0-9]{1,16}")

    /**
     * Reads a project from a stream. The stream is not closed.
     *
     * @param input The ZIP stream.
     * @param sink Receives every medium while the stream is read.
     * @param limits Upper bounds against hostile files.
     * @return the validated project description.
     * @throws InvalidProjectFileException if the content breaks a format rule.
     * @throws DamagedProjectFileException if the ZIP is broken, a limit is exceeded or the stream or the
     *   sink fail with an I/O error while reading.
     * @throws UnsupportedFormatVersionException if the file is newer than this application knows.
     */
    @JvmOverloads
    fun read(input: InputStream, sink: MediaSink, limits: StorageLimits = StorageLimits()): ProjectFile {
        try {
            return ZipInputStream(NonClosingInputStream(input)).use { zip -> readEntries(zip, sink, limits) }
        } catch (e: IOException) {
            throw DamagedProjectFileException("The project file cannot be read: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw DamagedProjectFileException("The project file has a malformed entry: ${e.message}", e)
        }
    }

    /**
     * Reads a project from a file.
     *
     * @throws java.nio.file.NoSuchFileException if the file does not exist.
     * @see read
     */
    @JvmOverloads
    fun read(file: Path, sink: MediaSink, limits: StorageLimits = StorageLimits()): ProjectFile =
        Files.newInputStream(file).use { read(it.buffered(BUFFER_SIZE), sink, limits) }

    /**
     * Writes a project to a stream. The stream is not closed.
     *
     * The project is validated first, so nothing is written for an invalid project.
     *
     * @param output The target stream.
     * @param project The project; the media of each reference are taken by list index.
     * @param source Delivers extension and content of every medium.
     * @throws InvalidProjectFileException if the project breaks a format rule.
     * @throws IOException if reading a medium or writing fails.
     */
    fun write(output: OutputStream, project: ProjectFile, source: MediaSource) {
        ProjectValidator.validate(project)
        val json = try {
            mapper.writeValueAsBytes(project)
        } catch (e: JacksonException) {
            throw InvalidProjectFileException("The project cannot be written as JSON: ${e.message}", e)
        }
        val parts = measure(project, source)

        ZipOutputStream(NonClosingOutputStream(output)).use { zip ->
            zip.putNextEntry(storedEntry(ProjectFormat.PROJECT_FILE_NAME, json.size.toLong(), crcOf(json)))
            zip.write(json)
            zip.closeEntry()
            for (part in parts) {
                zip.putNextEntry(storedEntry(part.entryName, part.size, part.crc))
                val written = source.open(part.reference, part.number).use { copy(it, zip) }
                if (written != part.size) {
                    throw IOException("Medium '${part.entryName}' changed while it was saved")
                }
                zip.closeEntry()
            }
        }
    }

    /**
     * Writes a project to a file atomically: the data goes to a temporary file in the same directory,
     * which then replaces the target. On any failure the temporary file is removed and an existing
     * target stays untouched.
     *
     * @see write
     */
    fun write(file: Path, project: ProjectFile, source: MediaSource) {
        ProjectValidator.validate(project)
        val directory = file.toAbsolutePath().parent
        val temporary = Files.createTempFile(directory, ".hocal-", ".tmp")
        try {
            Files.newOutputStream(temporary).buffered(BUFFER_SIZE).use { write(it, project, source) }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE)
            } catch (e: AtomicMoveNotSupportedException) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: Throwable) {
            Files.deleteIfExists(temporary)
            throw e
        }
    }

    private fun readEntries(zip: ZipInputStream, sink: MediaSink, limits: StorageLimits): ProjectFile {
        val first = zip.nextEntry
            ?: throw InvalidProjectFileException("The file has no entries, ${ProjectFormat.PROJECT_FILE_NAME} is missing")
        if (first.name != ProjectFormat.PROJECT_FILE_NAME) {
            throw InvalidProjectFileException(
                "${ProjectFormat.PROJECT_FILE_NAME} must be the first entry, found '${first.name}'",
            )
        }
        val bytes = zip.readNBytes(limits.maxProjectFileBytes + 1)
        if (bytes.size > limits.maxProjectFileBytes) {
            throw DamagedProjectFileException(
                "${ProjectFormat.PROJECT_FILE_NAME} exceeds the limit of ${limits.maxProjectFileBytes} bytes",
            )
        }
        val file = parse(bytes)
        ProjectValidator.validate(file)

        val expected = file.references.associate { it.uuid to it.media.size }
        val seen = HashMap<String, MutableSet<Int>>()
        var entries = 1
        while (true) {
            val entry = zip.nextEntry ?: break
            entries++
            if (entries > limits.maxEntries) {
                throw DamagedProjectFileException("The file has more than ${limits.maxEntries} entries")
            }
            requireSafeName(entry.name)
            if (entry.isDirectory) continue

            val match = mediaEntry.matchEntire(entry.name)
                ?: throw InvalidProjectFileException("Unknown entry '${entry.name}'")
            val (uuid, digits, extension) = match.destructured
            val number = digits.toInt()
            val count = expected[uuid]
                ?: throw InvalidProjectFileException("Entry '${entry.name}' belongs to no reference")
            if (number >= count) {
                throw InvalidProjectFileException("Entry '${entry.name}' has no details in project.json")
            }
            if (!seen.getOrPut(uuid) { HashSet() }.add(number)) {
                throw InvalidProjectFileException("Duplicate entry '${entry.name}'")
            }
            if (entry.size > limits.maxMediumBytes) {
                throw DamagedProjectFileException("Entry '${entry.name}' exceeds the limit of ${limits.maxMediumBytes} bytes")
            }
            sink.accept(uuid, number, extension, BoundedInputStream(zip, limits.maxMediumBytes, entry.name))
        }

        for ((uuid, count) in expected) {
            val found = seen[uuid]?.size ?: 0
            if (found != count) {
                throw InvalidProjectFileException("Reference '$uuid' has $count details but $found media")
            }
        }
        return file
    }

    private fun parse(bytes: ByteArray): ProjectFile {
        try {
            val probe = mapper.readValue(bytes, FormatProbe::class.java)
            val version = probe.project?.formatVersion ?: ProjectFormat.FORMAT_VERSION
            if (version > ProjectFormat.FORMAT_VERSION) {
                throw UnsupportedFormatVersionException(version, ProjectFormat.FORMAT_VERSION)
            }
            return mapper.readValue(bytes, ProjectFile::class.java)
        } catch (e: JacksonException) {
            throw InvalidProjectFileException("${ProjectFormat.PROJECT_FILE_NAME} is not valid: ${e.message}", e)
        }
    }

    private fun requireSafeName(name: String) {
        if (name.contains('\\') || name.startsWith("/") || name.split('/').any { it == ".." }) {
            throw InvalidProjectFileException("Unsafe entry name '$name'")
        }
    }

    private class MediaPart(val reference: String, val number: Int, val entryName: String, val size: Long, val crc: Long)

    /** First pass: size and checksum of every medium, needed before an uncompressed entry can start. */
    private fun measure(project: ProjectFile, source: MediaSource): List<MediaPart> {
        val parts = ArrayList<MediaPart>()
        for (reference in project.references) {
            for (number in reference.media.indices) {
                val extension = source.extension(reference.uuid, number)
                if (!extensionPattern.matches(extension)) {
                    throw InvalidProjectFileException("Invalid extension '$extension' of medium $number of '${reference.uuid}'")
                }
                val crc = CRC32()
                val size = source.open(reference.uuid, number).use { stream ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        val read = stream.read(buffer)
                        if (read < 0) break
                        crc.update(buffer, 0, read)
                        total += read
                    }
                    total
                }
                val name = "${reference.uuid}/${ProjectFormat.mediumKey(number)}.$extension"
                parts.add(MediaPart(reference.uuid, number, name, size, crc.value))
            }
        }
        return parts
    }

    private fun storedEntry(name: String, size: Long, crc: Long): ZipEntry =
        ZipEntry(name).apply {
            method = ZipEntry.STORED
            this.size = size
            compressedSize = size
            this.crc = crc
        }

    private fun crcOf(bytes: ByteArray): Long = CRC32().also { it.update(bytes) }.value

    private fun copy(input: InputStream, output: OutputStream): Long {
        val buffer = ByteArray(BUFFER_SIZE)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) return total
            output.write(buffer, 0, read)
            total += read
        }
    }
}
