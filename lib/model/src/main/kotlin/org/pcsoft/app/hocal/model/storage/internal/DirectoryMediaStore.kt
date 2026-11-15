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

import org.pcsoft.app.hocal.crypto.BlockStreamCipher
import org.pcsoft.app.hocal.crypto.CryptoException
import org.pcsoft.app.hocal.model.project.ProjectFormat
import java.io.BufferedOutputStream
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.SecureRandom

/**
 * A [MediaStore] in a directory: the medium is the file `<root>/<reference uuid>/<NNN>.<extension>`.
 *
 * Reference UUID, number and extension are checked, so no path outside the root can be reached. A
 * medium is written to a temporary file first and then moved into place, so no half medium is visible.
 *
 * The directory is usually a temporary one that holds the media of an open project. If the project is
 * encrypted, the store is [encrypted]: every medium is then kept as `<NNN>.<extension>.enc`, encrypted block
 * by block with a random key that exists in memory only and is overwritten by [close]. The key has nothing to
 * do with the password of the project, so changing that password needs no work here. Whether a medium is
 * encrypted can be told by its file name, so a plain medium is never taken for an encrypted one.
 *
 * Switching on the encryption later with [enableEncryption] encrypts the media already there; switching it off
 * is not possible. A store is not thread-safe, it is used by one thread at a time.
 *
 * @param root The directory of the media.
 * @param encrypted Whether the media are kept encrypted from the start.
 */
class DirectoryMediaStore @JvmOverloads constructor(
    private val root: Path,
    encrypted: Boolean = false,
) : MediaStore, AutoCloseable {

    private val filePattern = Regex("(\\d{3})\\.([A-Za-z0-9]{1,16})(\\.enc)?")

    private val extensionPattern = Regex("[A-Za-z0-9]{1,16}")

    private var key: CharArray? = if (encrypted) newKey() else null

    /** Whether the media are kept encrypted. */
    val isEncrypted: Boolean get() = key != null

    /**
     * Switches the encryption on and encrypts the media that are not encrypted yet. It can be called again
     * after a failure: it takes up where it stopped. Without effect on the media if it is on already, apart
     * from encrypting what a failed call left behind.
     *
     * @throws java.io.IOException if a medium cannot be encrypted; every medium is then either plain or encrypted,
     *   never half, and the store stays readable.
     */
    fun enableEncryption() {
        if (key == null) key = newKey()
        if (!Files.isDirectory(root)) return
        val folders = Files.list(root).use { stream -> stream.filter { Files.isDirectory(it) }.toList() }
        for (folder in folders) {
            val plain = Files.list(folder).use { files ->
                files.filter { name(it).let { n -> filePattern.matchEntire(n)?.groupValues?.get(3) == "" } }.toList()
            }
            for (file in plain) {
                val target = folder.resolve(name(file) + ENCRYPTED_SUFFIX)
                if (!Files.exists(target)) {
                    val temporary = Files.createTempFile(folder, ".medium-", ".tmp")
                    try {
                        Files.newInputStream(file).use { writeEncrypted(it, temporary) }
                        moveInto(temporary, target)
                    } catch (e: Throwable) {
                        Files.deleteIfExists(temporary)
                        throw e
                    }
                }
                Files.delete(file)
            }
        }
    }

    /** Overwrites the key in memory; the encrypted media cannot be read afterwards. */
    override fun close() {
        key?.fill('\u0000')
        key = null
    }

    override fun extension(reference: String, number: Int): String {
        val file = find(reference, number) ?: throw NoSuchFileException(describe(reference, number))
        return extensionOf(file)
    }

    override fun open(reference: String, number: Int): InputStream {
        val file = find(reference, number) ?: throw NoSuchFileException(describe(reference, number))
        val raw = Files.newInputStream(file).buffered()
        if (!name(file).endsWith(ENCRYPTED_SUFFIX)) return raw
        try {
            val sessionKey = checkNotNull(key) { "The key of the encrypted media is gone" }
            return DecryptingStream(raw, BlockStreamCipher.openInput(raw, sessionKey))
        } catch (e: CryptoException) {
            raw.close()
            throw IOException("The temporary medium '${describe(reference, number)}' cannot be decrypted", e)
        } catch (e: Throwable) {
            raw.close()
            throw e
        }
    }

    override fun accept(reference: String, number: Int, extension: String, content: InputStream) {
        require(extensionPattern.matches(extension)) { "Invalid extension '$extension'" }
        val folder = folder(reference)
        key(number)
        Files.createDirectories(folder)
        val temporary = Files.createTempFile(folder, ".medium-", ".tmp")
        try {
            if (key != null) writeEncrypted(content, temporary) else Files.copy(content, temporary, StandardCopyOption.REPLACE_EXISTING)
            findAll(reference, number).forEach { Files.delete(it) }
            val suffix = if (key != null) ENCRYPTED_SUFFIX else ""
            moveInto(temporary, folder.resolve("${key(number)}.$extension$suffix"))
        } catch (e: Throwable) {
            Files.deleteIfExists(temporary)
            throw e
        }
    }

    override fun delete(reference: String, number: Int) {
        val files = findAll(reference, number)
        if (files.isEmpty()) throw NoSuchFileException(describe(reference, number))
        files.forEach { Files.delete(it) }
    }

    override fun move(reference: String, from: Int, to: Int) {
        val source = find(reference, from) ?: throw NoSuchFileException(describe(reference, from))
        if (findAll(reference, to).isNotEmpty()) throw FileAlreadyExistsException(describe(reference, to))
        val suffix = if (name(source).endsWith(ENCRYPTED_SUFFIX)) ENCRYPTED_SUFFIX else ""
        Files.move(source, folder(reference).resolve("${key(to)}.${extensionOf(source)}$suffix"))
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
            files.map { filePattern.matchEntire(name(it)) }
                .filter { it != null }
                .map { it!!.groupValues[1].toInt() }
                .sorted()
                .distinct()
                .toList()
        }
    }

    private fun folder(reference: String): Path {
        require(ProjectValidator.NAME_PATTERN.matches(reference)) { "Invalid reference UUID '$reference'" }
        return root.resolve(reference)
    }

    private fun key(number: Int): String = ProjectFormat.mediumKey(number)

    private fun name(file: Path): String = file.fileName.toString()

    private fun extensionOf(file: Path): String = filePattern.matchEntire(name(file))!!.groupValues[2]

    /** All files of a medium: one, or two while an interrupted encryption left the plain and the encrypted one. */
    private fun findAll(reference: String, number: Int): List<Path> {
        val folder = folder(reference)
        val prefix = "${key(number)}."
        if (!Files.isDirectory(folder)) return emptyList()
        return Files.list(folder).use { files ->
            files.filter { name(it).let { n -> n.startsWith(prefix) && filePattern.matches(n) } }.toList()
        }
    }

    /** The file of a medium; the encrypted one if an interrupted encryption left both, as it is complete. */
    private fun find(reference: String, number: Int): Path? =
        findAll(reference, number).let { files -> files.firstOrNull { name(it).endsWith(ENCRYPTED_SUFFIX) } ?: files.firstOrNull() }

    private fun writeEncrypted(content: InputStream, target: Path) {
        val sessionKey = checkNotNull(key) { "The encryption is off" }
        Files.newOutputStream(target).buffered().use { raw ->
            BlockStreamCipher.openOutput(raw, sessionKey, ITERATIONS).use { content.copyTo(it) }
        }
    }

    private fun moveInto(temporary: Path, target: Path) {
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun describe(reference: String, number: Int) = "$reference/${key(number)}"

    /** Reads decrypted data and reports a failed check as [IOException]; closing it closes the file too. */
    private class DecryptingStream(private val file: InputStream, decrypted: InputStream) : FilterInputStream(decrypted) {

        override fun read(): Int = guarded { super.read() }

        override fun read(target: ByteArray, offset: Int, length: Int): Int = guarded { super.read(target, offset, length) }

        override fun close() {
            try {
                super.close()
            } finally {
                file.close()
            }
        }

        private fun <T> guarded(block: () -> T): T = try {
            block()
        } catch (e: CryptoException) {
            throw IOException("The temporary medium is damaged or manipulated", e)
        }
    }

    private companion object {
        const val ENCRYPTED_SUFFIX = ".enc"

        // The key is random and strong, so stretching it would only cost time for every single medium.
        const val ITERATIONS = 1

        /** 32 random characters with 8 bits each, made without a String, so nothing immutable is left behind. */
        fun newKey(): CharArray {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            val chars = CharArray(bytes.size) { (0x100 + (bytes[it].toInt() and 0xff)).toChar() }
            bytes.fill(0)
            return chars
        }
    }
}
