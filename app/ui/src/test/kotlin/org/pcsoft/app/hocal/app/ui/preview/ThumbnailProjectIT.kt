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

import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.pcsoft.app.hocal.fx.model.manager.ProjectManager
import org.pcsoft.app.hocal.model.project.Medium
import org.pcsoft.app.hocal.thumbnail.ImageThumbnails
import org.pcsoft.app.hocal.thumbnail.ThumbnailKey
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Integration tests of the whole thumbnail system on a real project: the `ProjectManager` with real media, the
 * `ProjectMediaSource`, the `ThumbnailLoader`, the shared `ThumbnailService` and the thumbnail file of the project -
 * for a plain and for an encrypted project, and for the switch between both.
 */
class ThumbnailProjectIT {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }

        private const val GRAY = 0x5A5A5A
        private const val GREEN = 0x00FF00
    }

    @TempDir
    lateinit var directory: Path

    private val work: Path get() = directory.resolve("work")

    private val opened = AtomicInteger()

    private val source = object : MediaSource {
        private val inner = ProjectMediaSource()
        override fun mediaCount(reference: String): Int = inner.mediaCount(reference)
        override fun extension(reference: String, number: Int): String = inner.extension(reference, number)
        override fun open(reference: String, number: Int): InputStream {
            opened.incrementAndGet()
            return inner.open(reference, number)
        }
    }

    private val loader = ThumbnailLoader()

    @BeforeEach
    fun prepare() {
        Files.createDirectories(work)
        ProjectManager.workRoot = work
    }

    @AfterEach
    fun cleanUp() {
        loader.close()
        fx { ProjectManager.close(discardChanges = true) }
        ProjectManager.workRoot = null
    }

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    private fun password(encrypted: Boolean): CharArray? = if (encrypted) "secret".toCharArray() else null

    private val file: Path get() = directory.resolve("project.hocal")

    /** A saved project with one reference holding one gray medium; returns the UUID of the reference. */
    private fun project(encrypted: Boolean): String = fx {
        ProjectManager.createNew("Sample")
        val uuid = ProjectManager.addReference(Medium(prompt = "a"), "png", pngBytes(64, 64, GRAY).inputStream())
        ProjectManager.saveAs(file, password(encrypted))
        uuid
    }

    private fun load(reference: String, number: Int? = null): Image {
        val future = CompletableFuture<Image?>()
        if (number == null) {
            loader.loadCurrent(source, reference, 48) { future.complete(it) }
        } else {
            loader.load(source, reference, number, 48) { future.complete(it) }
        }
        return checkNotNull(future.get(10, TimeUnit.SECONDS)) { "no thumbnail" }
    }

    // More small thumbnails than the memory holds push every other one out of it; the file of the project keeps them.
    private fun evictMemory() {
        repeat(ThumbnailService.THUMBNAIL_ENTRIES + 1) {
            ThumbnailService.cache.put(ThumbnailKey("filler$it", 0, 48), 1, WritableImage(1, 1))
        }
    }

    private fun rgb(image: Image) = image.pixelReader.getArgb(5, 5) and 0xFFFFFF

    private fun thumbnailFile(): Path =
        Files.walk(work).use { stream -> stream.filter { it.fileName.toString().startsWith("thumbnails-") }.toList() }.single()

    private fun fileHoldsPixelsInTheClear(): Boolean {
        val pattern = ByteArray(32) { if (it % 4 == 3) 0xFF.toByte() else 0x5A }
        val content = Files.readAllBytes(thumbnailFile())
        return (0..content.size - pattern.size).any { start -> pattern.indices.all { content[start + it] == pattern[it] } }
    }

    /**
     * Use case: a thumbnail was loaded and the memory lost it again. It is answered from the file of the project, with
     * the same pixels and without opening the medium again - in a plain and in an encrypted project alike.
     */
    @ParameterizedTest(name = "encrypted = {0}")
    @ValueSource(booleans = [false, true])
    fun thumbnailIsServedFromTheFileOfTheProject(encrypted: Boolean) {
        val reference = project(encrypted)
        val first = load(reference)
        assertEquals(1, opened.get())
        evictMemory()

        val second = load(reference)

        assertEquals(1, opened.get())
        assertEquals(ImageThumbnails.toPixels(first), ImageThumbnails.toPixels(second))
        assertEquals(GRAY, rgb(second))
    }

    /**
     * Use case: the file of the project holds the thumbnail pixels in the clear in a plain project and does not in an
     * encrypted one.
     */
    @ParameterizedTest(name = "encrypted = {0}")
    @ValueSource(booleans = [false, true])
    fun fileIsEncryptedExactlyForAnEncryptedProject(encrypted: Boolean) {
        load(project(encrypted))

        assertEquals(!encrypted, fileHoldsPixelsInTheClear())
    }

    /**
     * Use case: a project is closed and opened again. The new session starts without thumbnails, so the medium is
     * opened again and the right thumbnail comes out.
     */
    @ParameterizedTest(name = "encrypted = {0}")
    @ValueSource(booleans = [false, true])
    fun reopenedProjectStartsWithAnEmptyStore(encrypted: Boolean) {
        val reference = project(encrypted)
        load(reference)
        fx { ProjectManager.close() }
        assertEquals(0L, Files.list(work).use { it.count() })

        fx { ProjectManager.open(file, password(encrypted)) }
        val image = load(reference)

        assertEquals(2, opened.get())
        assertEquals(GRAY, rgb(image))
        assertEquals(!encrypted, fileHoldsPixelsInTheClear())
    }

    /**
     * Use case: a medium is appended to the reference after its thumbnail was stored in the file. The stored thumbnail
     * is stale and is not used; the medium is read again, and the current medium is the new one.
     */
    @ParameterizedTest(name = "encrypted = {0}")
    @ValueSource(booleans = [false, true])
    fun appendedMediumMakesTheStoredThumbnailStale(encrypted: Boolean) {
        val reference = project(encrypted)
        load(reference, 0)
        evictMemory()
        fx { ProjectManager.appendMedium(reference, Medium(prompt = "b"), "png", pngBytes(64, 64, GREEN).inputStream()) }

        val original = load(reference, 0)
        val current = load(reference)

        assertEquals(3, opened.get())
        assertEquals(GRAY, rgb(original))
        assertEquals(GREEN, rgb(current))
    }

    /**
     * Use case: a plain project is saved with a password, or an encrypted one without. The thumbnail file is renewed
     * in the other form: the medium is read again once, and from then on the thumbnails come from the new file, which
     * has the form of the project.
     */
    @ParameterizedTest(name = "toEncrypted = {0}")
    @ValueSource(booleans = [false, true])
    fun switchingTheFormRenewsTheThumbnailFile(toEncrypted: Boolean) {
        val reference = project(!toEncrypted)
        load(reference)
        assertEquals(toEncrypted, fileHoldsPixelsInTheClear())
        evictMemory()

        fx { ProjectManager.saveAs(directory.resolve("other.hocal"), password(toEncrypted)) }
        val renewed = load(reference)
        assertEquals(2, opened.get())
        evictMemory()
        val again = load(reference)

        assertEquals(2, opened.get())
        assertEquals(ImageThumbnails.toPixels(renewed), ImageThumbnails.toPixels(again))
        assertEquals(!toEncrypted, fileHoldsPixelsInTheClear())
        assertEquals(1L, Files.walk(work).use { stream -> stream.filter { it.fileName.toString().startsWith("thumbnails-") }.count() })
    }

    /**
     * Use case: the project is closed. Nothing of its thumbnails stays in the temporary folder, and the next project
     * does not see the thumbnails of the former one.
     */
    @ParameterizedTest(name = "encrypted = {0}")
    @ValueSource(booleans = [false, true])
    fun closingLeavesNothingBehind(encrypted: Boolean) {
        val reference = project(encrypted)
        load(reference)
        assertTrue(Files.exists(thumbnailFile()))

        fx { ProjectManager.close() }

        assertEquals(0L, Files.list(work).use { it.count() })
        assertFalse(ProjectManager.isLoaded)
        assertNotNull(ProjectManager.thumbnailCache)
        assertEquals(null, ProjectManager.thumbnailCache.get(ThumbnailKey(reference, 0, 48), 1))
    }

    /**
     * Use case: the project is saved as a copy under the same secret - no password both times, or the same password
     * twice. The thumbnail file stays, so the thumbnails still come from it without opening the medium again.
     */
    @ParameterizedTest(name = "encrypted = {0}")
    @ValueSource(booleans = [false, true])
    fun saveAsWithTheSameSecretKeepsTheThumbnails(encrypted: Boolean) {
        val reference = project(encrypted)
        val first = load(reference)
        evictMemory()

        fx { ProjectManager.saveAs(directory.resolve("copy.hocal"), password(encrypted)) }
        val second = load(reference)

        assertEquals(1, opened.get())
        assertEquals(ImageThumbnails.toPixels(first), ImageThumbnails.toPixels(second))
        assertEquals(!encrypted, fileHoldsPixelsInTheClear())
    }

    /**
     * Use case: an encrypted project is saved as a copy with another password. The thumbnail file is renewed with the
     * new password: the medium is read again once, then the thumbnails come from the new file, which is still encrypted.
     */
    @org.junit.jupiter.api.Test
    fun saveAsWithAnotherPasswordRenewsTheEncryptedFile() {
        val reference = project(true)
        load(reference)
        evictMemory()

        fx { ProjectManager.saveAs(directory.resolve("other.hocal"), "other".toCharArray()) }
        load(reference)
        assertEquals(2, opened.get())
        evictMemory()
        load(reference)

        assertEquals(2, opened.get())
        assertFalse(fileHoldsPixelsInTheClear())
        assertEquals(1, Files.walk(work).use { stream -> stream.filter { it.fileName.toString().startsWith("thumbnails-") }.count() })
    }

    /**
     * Use case: a project is saved as a copy in every combination of plain and encrypted, the copy is closed and opened
     * with its own password. Its thumbnails are those of a fresh session, in the form of the copy.
     */
    @ParameterizedTest(name = "from encrypted = {0}, to encrypted = {1}")
    @CsvSource("false,false", "false,true", "true,false", "true,true")
    fun savedCopyOpensWithThumbnailsInItsOwnForm(fromEncrypted: Boolean, toEncrypted: Boolean) {
        val reference = project(fromEncrypted)
        load(reference)
        val copy = directory.resolve("copy.hocal")
        fx { ProjectManager.saveAs(copy, password(toEncrypted)) }
        fx { ProjectManager.close() }
        assertEquals(0L, Files.list(work).use { it.count() })

        fx { ProjectManager.open(copy, password(toEncrypted)) }
        val image = load(reference)

        assertEquals(2, opened.get())
        assertEquals(GRAY, rgb(image))
        assertEquals(!toEncrypted, fileHoldsPixelsInTheClear())
    }

    /**
     * Use case: saving as a copy fails, because the target cannot be written. Nothing of the project changes: its
     * file, its form and its thumbnail file stay, and saving to the old file still works with the old password.
     */
    @ParameterizedTest(name = "encrypted = {0}")
    @ValueSource(booleans = [false, true])
    fun failedSaveAsKeepsTheThumbnailFile(encrypted: Boolean) {
        val reference = project(encrypted)
        load(reference)
        evictMemory()
        val blocked = directory.resolve("blocked")
        Files.createDirectories(blocked)
        Files.write(blocked.resolve("content"), ByteArray(1))

        val error = assertThrows<ExecutionException> {
            fx { ProjectManager.saveAs(blocked, password(!encrypted)) }
        }

        assertTrue(error.cause is IOException)
        assertEquals(file, ProjectManager.pathProperty.get())
        assertEquals(encrypted, ProjectManager.encryptedProperty.get())
        load(reference)
        assertEquals(1, opened.get())
        assertEquals(!encrypted, fileHoldsPixelsInTheClear())
        fx { ProjectManager.save() }
    }
}
