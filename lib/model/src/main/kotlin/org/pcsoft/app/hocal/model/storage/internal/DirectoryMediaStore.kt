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

import org.pcsoft.app.hocal.model.project.ProjectFormat
import java.io.InputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * A [MediaStore] in a directory: the medium is the file `<root>/<reference uuid>/<NNN>.<extension>`.
 *
 * Reference UUID, number and extension are checked, so no path outside the root can be reached. A
 * medium is written to a temporary file first and then moved into place, so no half medium is visible.
 */
class DirectoryMediaStore(private val root: Path) : MediaStore {

    private val extensionPattern = Regex("[A-Za-z0-9]{1,16}")

    private val filePattern = Regex("(\\d{3})\\.([A-Za-z0-9]{1,16})")

    override fun extension(reference: String, number: Int): String {
        val file = find(reference, number) ?: throw NoSuchFileException(describe(reference, number))
        return file.fileName.toString().substringAfter('.')
    }

    override fun open(reference: String, number: Int): InputStream {
        val file = find(reference, number) ?: throw NoSuchFileException(describe(reference, number))
        return Files.newInputStream(file).buffered()
    }

    override fun accept(reference: String, number: Int, extension: String, content: InputStream) {
        require(extensionPattern.matches(extension)) { "Invalid extension '$extension'" }
        val folder = folder(reference)
        key(number)
        Files.createDirectories(folder)
        val temporary = Files.createTempFile(folder, ".medium-", ".tmp")
        try {
            Files.copy(content, temporary, StandardCopyOption.REPLACE_EXISTING)
            find(reference, number)?.let { Files.delete(it) }
            val target = folder.resolve("${key(number)}.$extension")
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE)
            } catch (e: AtomicMoveNotSupportedException) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: Throwable) {
            Files.deleteIfExists(temporary)
            throw e
        }
    }

    override fun delete(reference: String, number: Int) {
        val file = find(reference, number) ?: throw NoSuchFileException(describe(reference, number))
        Files.delete(file)
    }

    override fun move(reference: String, from: Int, to: Int) {
        val source = find(reference, from) ?: throw NoSuchFileException(describe(reference, from))
        if (find(reference, to) != null) throw FileAlreadyExistsException(describe(reference, to))
        val extension = source.fileName.toString().substringAfter('.')
        Files.move(source, folder(reference).resolve("${key(to)}.$extension"))
    }

    override fun deleteReference(reference: String) {
        super.deleteReference(reference)
        try {
            Files.deleteIfExists(folder(reference))
        } catch (e: DirectoryNotEmptyException) {
            // foreign files stay, the folder stays with them
        }
    }

    override fun numbers(reference: String): List<Int> {
        val folder = folder(reference)
        if (!Files.isDirectory(folder)) return emptyList()
        return Files.list(folder).use { files ->
            files.map { filePattern.matchEntire(it.fileName.toString()) }
                .filter { it != null }
                .map { it!!.groupValues[1].toInt() }
                .sorted()
                .toList()
        }
    }

    private fun folder(reference: String): Path {
        require(ProjectValidator.NAME_PATTERN.matches(reference)) { "Invalid reference UUID '$reference'" }
        return root.resolve(reference)
    }

    private fun key(number: Int): String = ProjectFormat.mediumKey(number)

    private fun find(reference: String, number: Int): Path? {
        val folder = folder(reference)
        val prefix = "${key(number)}."
        if (!Files.isDirectory(folder)) return null
        return Files.list(folder).use { files ->
            files.filter { it.fileName.toString().let { name -> name.startsWith(prefix) && filePattern.matches(name) } }
                .findFirst().orElse(null)
        }
    }

    private fun describe(reference: String, number: Int) = "$reference/${key(number)}"
}
