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
import org.pcsoft.app.hocal.model.storage.InvalidProjectFileException
import org.pcsoft.app.hocal.model.storage.ManipulatedProjectFileException
import org.pcsoft.app.hocal.model.storage.PasswordRequiredException
import org.pcsoft.app.hocal.model.storage.UnsupportedEncryptionVersionException
import org.pcsoft.app.hocal.model.storage.WrongPasswordException

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.storage.internal.StorageFixtures.FIRST
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream

/** Developer tests for the encrypted form of the project file. */
class ProjectStorageEncryptionTest {

    @TempDir
    lateinit var directory: Path

    private val password = "correct horse"

    private val block = EncryptionHeader.DEFAULT_BLOCK_SIZE + EncryptionHeader.TAG_BYTES

    private val data = EncryptionHeader.SIZE

    /** A project with a medium of 200,000 bytes, so the encrypted file holds several blocks. */
    private fun bigProject(): Pair<ProjectFile, MemoryMedia> {
        val project = StorageFixtures.project()
        val media = StorageFixtures.media(project)
        media.put(FIRST, 0, "png", ByteArray(200_000) { (it * 31).toByte() })
        return project to media
    }

    private fun encrypted(): ByteArray {
        val (project, media) = bigProject()
        return StorageFixtures.saveEncrypted(project, media, password)
    }

    private fun read(bytes: ByteArray, pw: String? = password): ProjectFile =
        ProjectStorage.read(ByteArrayInputStream(bytes), MemoryMedia(), StorageLimits(), pw?.toCharArray())

    private fun flipped(bytes: ByteArray, index: Int): ByteArray =
        bytes.copyOf().also { it[index] = (it[index].toInt() xor 0x01).toByte() }

    /** An encrypted project is read back with the password, project and media content are equal. */
    @Test
    fun roundTrip() {
        val (project, media) = bigProject()
        val bytes = StorageFixtures.saveEncrypted(project, media, password)

        val sink = MemoryMedia()
        val read = ProjectStorage.read(ByteArrayInputStream(bytes), sink, StorageLimits(), password.toCharArray())

        assertEquals(project, read)
        assertArrayEquals(media.contentOf(FIRST, 0), sink.contentOf(FIRST, 0))
        StorageFixtures.assertMatching(read, sink.also { it.put(FIRST, 0, "png", StorageFixtures.content("a")) })
    }

    /** The file starts with the magic, is no ZIP any more and spans several blocks. */
    @Test
    fun fileIsEncryptedInBlocks() {
        val bytes = encrypted()

        assertEquals("HOCALENC", String(bytes, 0, 8))
        assertNull(ZipInputStream(ByteArrayInputStream(bytes)).nextEntry)
        assertTrue(bytes.size > data + 3 * block)
    }

    /** Every save uses a fresh salt and IV, so two saves of the same project differ. */
    @Test
    fun everySaveIsDifferent() {
        val (project, media) = bigProject()

        val first = StorageFixtures.saveEncrypted(project, media, password)
        val second = StorageFixtures.saveEncrypted(project, media, password)

        assertNotEquals(first.toList(), second.toList())
        assertNotEquals(first.copyOfRange(14, 42).toList(), second.copyOfRange(14, 42).toList())
    }

    /** A wrong password is reported before any medium is read. */
    @Test
    fun wrongPassword() {
        val sink = MemoryMedia()

        assertThrows(WrongPasswordException::class.java) {
            ProjectStorage.read(ByteArrayInputStream(encrypted()), sink, StorageLimits(), "wrong".toCharArray())
        }
        assertTrue(sink.contents.isEmpty())
    }

    /** An encrypted file without a password asks for one. */
    @Test
    fun passwordRequired() {
        assertThrows(PasswordRequiredException::class.java) { read(encrypted(), null) }
    }

    /** An unencrypted file is read normally, also when a password is passed. */
    @Test
    fun plainFileIgnoresPassword() {
        val project = StorageFixtures.project()
        val bytes = StorageFixtures.save(project, StorageFixtures.media(project))

        assertEquals(project, read(bytes))
        assertEquals(project, read(bytes, null))
    }

    /** A flipped byte in the data or in the tag of a block is detected as manipulation. */
    @Test
    fun flippedDataIsManipulated() {
        val bytes = encrypted()

        assertThrows(ManipulatedProjectFileException::class.java) { read(flipped(bytes, data + 5)) }
        assertThrows(ManipulatedProjectFileException::class.java) { read(flipped(bytes, data + block + 100)) }
        assertThrows(ManipulatedProjectFileException::class.java) { read(flipped(bytes, bytes.size - 1)) }
    }

    /** A flipped byte of the IV or the block size in the header is detected, the header is authenticated. */
    @Test
    fun flippedHeaderIsManipulated() {
        val bytes = encrypted()

        assertThrows(ManipulatedProjectFileException::class.java) { read(flipped(bytes, 30)) }
        assertThrows(ManipulatedProjectFileException::class.java) { read(flipped(bytes, 44)) }
    }

    /** A flipped byte of the salt or of the check value makes the password fail. */
    @Test
    fun flippedSaltOrCheckIsWrongPassword() {
        val bytes = encrypted()

        assertThrows(WrongPasswordException::class.java) { read(flipped(bytes, 14)) }
        assertThrows(WrongPasswordException::class.java) { read(flipped(bytes, 46)) }
    }

    /** A file cut inside a block or exactly between two blocks is detected, also in the last block. */
    @Test
    fun truncatedFileIsManipulated() {
        val bytes = encrypted()

        assertThrows(ManipulatedProjectFileException::class.java) { read(bytes.copyOf(bytes.size - 10)) }
        assertThrows(ManipulatedProjectFileException::class.java) { read(bytes.copyOf(data + block)) }
        assertThrows(ManipulatedProjectFileException::class.java) { read(bytes.copyOf(data + block + 5)) }
    }

    /** Two blocks swapped in place fail, the block counter is part of the IV. */
    @Test
    fun swappedBlocksAreManipulated() {
        val bytes = encrypted()
        val swapped = bytes.copyOf()
        System.arraycopy(bytes, data, swapped, data + block, block)
        System.arraycopy(bytes, data + block, swapped, data, block)

        assertThrows(ManipulatedProjectFileException::class.java) { read(swapped) }
    }

    /** A header cut off is damaged. */
    @Test
    fun truncatedHeaderIsDamaged() {
        assertThrows(DamagedProjectFileException::class.java) { read(encrypted().copyOf(40)) }
    }

    /** An unknown encryption version or key derivation is reported as unsupported. */
    @Test
    fun unknownVersionOrKdfIsUnsupported() {
        val bytes = encrypted()

        val version = assertThrows(UnsupportedEncryptionVersionException::class.java) {
            read(bytes.copyOf().also { it[8] = 2 })
        }
        assertEquals(2, version.foundVersion)
        assertEquals(1, version.supportedVersion)
        assertThrows(UnsupportedEncryptionVersionException::class.java) { read(bytes.copyOf().also { it[9] = 9 }) }
    }

    /** An absurd iteration count or block size in the header is rejected before any key is derived. */
    @Test
    fun outOfRangeParametersAreInvalid() {
        val bytes = encrypted()

        assertThrows(InvalidProjectFileException::class.java) {
            read(bytes.copyOf().also { it[10] = 0x7f; it[11] = 0; it[12] = 0; it[13] = 0 })
        }
        assertThrows(InvalidProjectFileException::class.java) {
            read(bytes.copyOf().also { it[10] = 0; it[11] = 0; it[12] = 0; it[13] = 0 })
        }
        assertThrows(InvalidProjectFileException::class.java) {
            read(bytes.copyOf().also { it[42] = 0; it[43] = 0; it[44] = 0; it[45] = 1 })
        }
    }

    /** A project can be switched from encrypted to unencrypted and back when saved. */
    @Test
    fun switchBetweenBothForms() {
        val (project, media) = bigProject()
        val sink = MemoryMedia()
        val first = ProjectStorage.read(
            ByteArrayInputStream(StorageFixtures.saveEncrypted(project, media, password)), sink, StorageLimits(), password.toCharArray(),
        )

        val plain = StorageFixtures.save(first, sink)
        assertNotNull(ZipInputStream(ByteArrayInputStream(plain)).nextEntry)
        val sink2 = MemoryMedia()
        val second = ProjectStorage.read(ByteArrayInputStream(plain), sink2)
        val again = StorageFixtures.saveEncrypted(second, sink2, "other")

        assertEquals("HOCALENC", String(again, 0, 8))
        assertEquals(project, read(again, "other"))
        assertThrows(WrongPasswordException::class.java) { read(again, password) }
    }

    private fun assertNotNull(value: Any?) = assertFalse(value == null)

    /** The password array of the caller is not changed by writing or reading. */
    @Test
    fun callerPasswordStaysUntouched() {
        val (project, media) = bigProject()
        val secret = password.toCharArray()

        val out = ByteArrayOutputStream()
        ProjectStorage.writeWithIterations(out, project, media, secret, 1000)
        ProjectStorage.read(ByteArrayInputStream(out.toByteArray()), MemoryMedia(), StorageLimits(), secret)

        assertEquals(password, String(secret))
    }

    /** Neither writing nor reading the encrypted form closes the given stream. */
    @Test
    fun streamsStayOpen() {
        val (project, media) = bigProject()
        var outputClosed = false
        val output = object : ByteArrayOutputStream() {
            override fun close() {
                outputClosed = true
            }
        }
        ProjectStorage.writeWithIterations(output, project, media, password.toCharArray(), 1000)
        var inputClosed = false
        val input = object : ByteArrayInputStream(output.toByteArray()) {
            override fun close() {
                inputClosed = true
            }
        }
        ProjectStorage.read(input, MemoryMedia(), StorageLimits(), password.toCharArray())

        assertFalse(outputClosed)
        assertFalse(inputClosed)
    }

    /** The file variants save and read the encrypted form, and an aborted save keeps the old file. */
    @Test
    fun fileVariantsAreAtomic() {
        val (project, media) = bigProject()
        val target = directory.resolve("secret.hocal")
        ProjectStorage.writeFileWithIterations(target, project, media, password.toCharArray(), 1000)
        val before = Files.readAllBytes(target)

        assertEquals(project, ProjectStorage.read(target, MemoryMedia(), StorageLimits(), password.toCharArray()))

        val failing = object : MediaSource {
            override fun extension(reference: String, number: Int) = media.extension(reference, number)
            override fun open(reference: String, number: Int): InputStream {
                if (reference == FIRST && number == 1) throw IOException("disk gone")
                return media.open(reference, number)
            }
        }
        assertThrows(IOException::class.java) {
            ProjectStorage.writeFileWithIterations(target, project, failing, password.toCharArray(), 1000)
        }

        assertArrayEquals(before, Files.readAllBytes(target))
        assertEquals(1L, Files.list(directory).use { it.count() })
    }
}
