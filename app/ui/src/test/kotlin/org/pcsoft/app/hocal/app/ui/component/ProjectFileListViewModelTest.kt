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

package org.pcsoft.app.hocal.app.ui.component

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.pcsoft.app.hocal.app.ui.preview.FakeMediaSource
import org.pcsoft.app.hocal.app.ui.preview.brokenBytes
import org.pcsoft.app.hocal.app.ui.preview.pngBytes
import org.pcsoft.app.hocal.fx.model.project.VirtualFolderProperty
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Developer tests of the project file list view model, driven without a view.
 *
 * The view model dispatches to the FX thread, so every action runs there.
 */
class ProjectFileListViewModelTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val viewModel = ProjectFileListViewModel()
    private val source = FakeMediaSource()

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    private fun refEntry(name: String, reference: String): VirtualEntry = entry(name).apply { this.reference = reference }

    private fun show(root: VirtualFolder?, withSource: Boolean = true): VirtualFolderProperty? {
        val property = root?.let { VirtualFolderProperty.of(it) }
        fx {
            if (property != null) viewModel.folder.bind(property)
            if (withSource) viewModel.mediaSource.set(source)
            viewModel.onShow()
        }
        return property
    }

    private fun awaitTrue(condition: () -> Boolean) {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { condition() })
    }

    // Lets a task that was released run to its end, including the dispatch of its result to the FX thread.
    private fun settle() {
        repeat(3) {
            Thread.sleep(50)
            WaitForAsyncUtils.waitForFxEvents()
        }
    }

    /**
     * Use case: a view model with no folder and no source offers an empty list without any error,
     * also when it is shown and hidden.
     */
    @Test
    fun emptyWithoutFolderAndSource() {
        fx {
            viewModel.onShow()
            viewModel.onHide()
        }

        assertTrue(viewModel.entries.isEmpty())
        assertNull(viewModel.selectedFile.get())
        assertEquals(-1, viewModel.indexToSelect)
    }

    /**
     * Use case: a folder holds entries and subfolders. Only the entries are listed, in the order of the
     * model, as the very objects of the model; folders never appear.
     */
    @Test
    fun onlyEntriesInModelOrder() {
        val first = entry("B.png")
        val second = entry("A.png")
        show(folder("Project", folder("Photos"), first, folder("Videos"), second))

        assertEquals(2, viewModel.entries.size)
        assertSame(first, viewModel.entries[0])
        assertSame(second, viewModel.entries[1])
    }

    /**
     * Use case: another folder is bound. The list shows the entries of the new folder only.
     */
    @Test
    fun folderChangeShowsTheNewEntries() {
        val a = entry("A.png")
        val b = entry("B.png")
        show(folder("One", a))
        val revision = viewModel.revision.get()

        fx { viewModel.folder.unbind(); viewModel.folder.set(folder("Two", b)) }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(1, viewModel.entries.size)
        assertSame(b, viewModel.entries[0])
        assertTrue(viewModel.revision.get() > revision)
    }

    /**
     * Use case: the model changes inside the same folder - an entry is added. The list is rebuilt with it.
     */
    @Test
    fun modelChangeRebuildsTheList() {
        val root = folder("Project", entry("A.png"))
        val property = show(root)!!

        fx {
            root.children.add(entry("B.png"))
            property.refresh()
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("A.png", "B.png"), viewModel.entries.map { it.name })
    }

    /**
     * Use case: a reference with a single medium. Original and current are the same medium, so it is
     * loaded exactly once and both images are the same object.
     */
    @Test
    fun singleMediumIsLoadedOnceForBothImages() {
        source.add("r1", "png" to pngBytes(200, 100))
        val e = refEntry("A.png", "r1")
        show(folder("Project", e))

        val preview = fx { viewModel.preview(e) }
        awaitTrue { fx { preview.current.get() != null } }

        assertSame(preview.original.get(), preview.current.get())
        assertEquals(1, source.openRequested.get())
    }

    /**
     * Use case: a reference with three media. The original is the first, the current the last one, so
     * the two images differ.
     */
    @Test
    fun severalMediaShowOriginalAndCurrent() {
        source.add("r1", "png" to pngBytes(100, 100, 0xFF0000), "png" to pngBytes(50, 100, 0x00FF00), "png" to pngBytes(100, 50, 0x0000FF))
        val e = refEntry("A.png", "r1")
        show(folder("Project", e))

        val preview = fx { viewModel.preview(e) }
        awaitTrue { fx { preview.original.get() != null && preview.current.get() != null } }

        assertNotSame(preview.original.get(), preview.current.get())
        assertEquals(24.0, preview.current.get()!!.height)
        assertEquals(2, source.openRequested.get(), "the medium in between is not loaded")
    }

    /**
     * Use case: without a source nothing can be loaded. The entries are listed and their previews stay
     * empty, which the cell shows as placeholders.
     */
    @Test
    fun withoutSourceEntriesStayWithoutImages() {
        val e = refEntry("A.png", "r1")
        show(folder("Project", e), withSource = false)

        val preview = fx { viewModel.preview(e) }
        settle()

        assertEquals(1, viewModel.entries.size)
        assertNull(preview.original.get())
        assertNull(preview.current.get())
    }

    /**
     * Use case: a reference the source does not know (no medium). The previews stay empty, nothing fails.
     */
    @Test
    fun referenceWithoutMediaStaysWithoutImages() {
        val e = refEntry("A.png", "unknown")
        show(folder("Project", e))

        val preview = fx { viewModel.preview(e) }
        settle()

        assertNull(preview.original.get())
        assertEquals(0, source.openRequested.get())
    }

    /**
     * Use case: two sibling entries of equal name and reference are selected one after another and the
     * list is rebuilt. The selection stays on the very entry the user chose, not on its equal sibling;
     * when that entry is removed the selection is cleared although the equal sibling remains.
     */
    @Test
    fun selectionSurvivesRebuildByIdentity() {
        val first = refEntry("Same.png", "r1")
        val second = refEntry("Same.png", "r1")
        val root = folder("Project", first, second)
        val property = show(root)!!
        fx { viewModel.selectFile(second) }

        fx { root.children.add(entry("Other.png")); property.refresh() }
        WaitForAsyncUtils.waitForFxEvents()
        assertSame(second, viewModel.selectedFile.get())
        assertEquals(1, viewModel.indexToSelect)

        fx { root.children = mutableListOf(first, root.children[2]); property.refresh() }
        WaitForAsyncUtils.waitForFxEvents()
        assertNull(viewModel.selectedFile.get())
        assertEquals(-1, viewModel.indexToSelect)
    }

    /**
     * Use case: the folder is exchanged while a load task is still running. The late result belongs to
     * the former folder and is discarded; the entry of the new folder is not touched by it.
     */
    @Test
    fun folderChangeDiscardsRunningLoad() {
        source.add("r1", "png" to pngBytes(10, 10))
        val old = refEntry("Old.png", "r1")
        source.gate = CountDownLatch(1)
        show(folder("One", old))
        val oldPreview = fx { viewModel.preview(old) }
        awaitTrue { source.openRequested.get() == 1 }

        fx { viewModel.folder.unbind(); viewModel.folder.set(folder("Two", refEntry("New.png", "r1"))) }
        WaitForAsyncUtils.waitForFxEvents()
        source.gate!!.countDown()
        settle()

        assertNull(oldPreview.original.get())
        assertNull(oldPreview.current.get())
    }

    /**
     * Use case: the component leaves the screen while a load task is running. The result arriving later
     * is not taken over; coming back builds the previews anew.
     */
    @Test
    fun hideDiscardsRunningLoad() {
        source.add("r1", "png" to pngBytes(10, 10))
        val e = refEntry("A.png", "r1")
        source.gate = CountDownLatch(1)
        show(folder("Project", e))
        val preview = fx { viewModel.preview(e) }
        awaitTrue { source.openRequested.get() == 1 }

        fx { viewModel.onHide() }
        source.gate!!.countDown()
        settle()
        assertNull(preview.original.get(), "a result after onHide must be discarded")

        fx { viewModel.onShow() }
        val again = fx { viewModel.preview(e) }
        awaitTrue { fx { again.current.get() != null } }
        assertNotNull(again.original.get())
    }

    /**
     * Use case: the media source is exchanged. Earlier results are dropped and the previews are loaded
     * again from the new source.
     */
    @Test
    fun sourceChangeReloadsFromTheNewSource() {
        source.add("r1", "png" to pngBytes(10, 10))
        val e = refEntry("A.png", "r1")
        show(folder("Project", e))
        val first = fx { viewModel.preview(e) }
        awaitTrue { fx { first.current.get() != null } }

        val other = FakeMediaSource().apply { add("r1", "png" to pngBytes(20, 20)) }
        fx { viewModel.mediaSource.set(other) }
        WaitForAsyncUtils.waitForFxEvents()

        val second = fx { viewModel.preview(e) }
        assertNotSame(first, second)
        awaitTrue { fx { second.current.get() != null } }
        assertEquals(1, other.openRequested.get())
    }

    /**
     * Use case: onShow is called twice, as a view may report the screen state twice. The view model
     * registers only once: after a single onHide a model change is not noticed any more.
     */
    @Test
    fun doubleShowRegistersOnce() {
        val root = folder("Project", entry("A.png"))
        val property = show(root)!!
        fx { viewModel.onShow() }

        fx { viewModel.onHide() }
        fx { root.children.add(entry("B.png")); property.refresh() }
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(1, viewModel.entries.size)
    }

    /**
     * Use case: the view model is closed. The pool is ended; asking for a preview afterwards does not
     * fail, the images simply stay empty.
     */
    @Test
    fun closeEndsThePool() {
        source.add("r1", "png" to pngBytes(10, 10))
        val e = refEntry("A.png", "r1")
        show(folder("Project", e))
        fx { viewModel.preview(e) }

        fx { viewModel.close(); viewModel.onShow() }
        val preview = fx { viewModel.preview(e) }
        settle()

        assertNull(preview.original.get())
    }

    // ---- thumbnail cache --------------------------------------------------------------------------------------

    private fun loaded(entry: VirtualEntry) {
        val preview = fx { viewModel.preview(entry) }
        awaitTrue { fx { preview.current.get() != null } }
    }

    /**
     * Use case: the user switches to another folder and back. The thumbnails of the first folder come from
     * the cache, so no medium is opened a second time.
     */
    @Test
    fun switchingTheFolderBackDoesNotLoadAgain() {
        source.add("r1", "png" to pngBytes(60, 40))
        source.add("r2", "png" to pngBytes(60, 40, 0x00FF00))
        val first = refEntry("A.png", "r1")
        val second = refEntry("B.png", "r2")
        val rootOne = folder("One", first)
        val rootTwo = folder("Two", second)
        show(rootOne)
        loaded(first)

        fx { viewModel.folder.unbind(); viewModel.folder.set(rootTwo) }
        WaitForAsyncUtils.waitForFxEvents()
        loaded(second)
        fx { viewModel.folder.set(rootOne) }
        WaitForAsyncUtils.waitForFxEvents()
        loaded(first)

        assertEquals(2, source.openRequested.get())
    }

    /**
     * Use case: two entries link the same reference. They share its thumbnails, so the medium is loaded
     * once only.
     */
    @Test
    fun entriesOfTheSameReferenceShareTheThumbnails() {
        source.add("r1", "png" to pngBytes(60, 40))
        val first = refEntry("A.png", "r1")
        val second = refEntry("B.png", "r1")
        show(folder("Project", first, second))

        loaded(first)
        loaded(second)

        assertEquals(1, source.openRequested.get())
    }

    /**
     * Use case: the component leaves the screen and comes back. The thumbnails stay in the cache, so
     * showing the list again loads nothing.
     */
    @Test
    fun hidingAndShowingKeepsTheThumbnails() {
        source.add("r1", "png" to pngBytes(60, 40))
        val e = refEntry("A.png", "r1")
        show(folder("Project", e))
        loaded(e)

        fx { viewModel.onHide() }
        fx { viewModel.onShow() }
        loaded(e)

        assertEquals(1, source.openRequested.get())
    }

    /**
     * Use case: another media source is set, for instance because another project was opened. The
     * thumbnails of the former source are dropped, so the new source is asked.
     */
    @Test
    fun anotherSourceClearsTheThumbnails() {
        source.add("r1", "png" to pngBytes(60, 40))
        val e = refEntry("A.png", "r1")
        show(folder("Project", e))
        loaded(e)
        val other = FakeMediaSource()
        other.add("r1", "png" to pngBytes(60, 40, 0x0000FF))

        fx { viewModel.mediaSource.set(other) }
        WaitForAsyncUtils.waitForFxEvents()
        loaded(e)

        assertEquals(1, other.openRequested.get())
    }

    /**
     * Use case: a medium is appended to the reference. Its number of media changed, so the stored thumbnails
     * are stale and are loaded anew.
     */
    @Test
    fun changedMediaCountLoadsAgain() {
        source.add("r1", "png" to pngBytes(60, 40))
        val e = refEntry("A.png", "r1")
        show(folder("Project", e))
        loaded(e)
        source.add("r1", "png" to pngBytes(60, 40), "png" to pngBytes(60, 40, 0x00FF00))

        fx { viewModel.onHide() }
        fx { viewModel.onShow() }
        loaded(e)

        assertEquals(3, source.openRequested.get())
    }

    /**
     * Use case: a medium cannot be decoded. The failure is not stored, so the next look at the list tries
     * again, for instance after the file was repaired.
     */
    @Test
    fun brokenMediumIsNotCached() {
        source.add("r1", "png" to brokenBytes())
        val e = refEntry("A.png", "r1")
        show(folder("Project", e))
        fx { viewModel.preview(e) }
        awaitTrue { source.closed.get() == 1 }
        settle()

        fx { viewModel.onHide() }
        fx { viewModel.onShow() }
        fx { viewModel.preview(e) }
        awaitTrue { source.closed.get() == 2 }

        assertEquals(2, source.openRequested.get())
    }

    // ---- load threads -----------------------------------------------------------------------------------------

    /**
     * Use case: nothing is set, so thumbnails are loaded by five threads at the same time.
     */
    @Test
    fun fiveLoadThreadsByDefault() {
        assertEquals(5, viewModel.loadThreads)
        assertEquals(ProjectFileListViewModel.DEFAULT_LOAD_THREADS, viewModel.loadThreads)
    }

    /**
     * Use case: a number of threads below one cannot load anything and is rejected at once.
     */
    @Test
    fun loadThreadsBelowOneAreRejected() {
        assertThrows<IllegalArgumentException> { viewModel.loadThreads = 0 }
        assertEquals(5, viewModel.loadThreads)
    }

    /**
     * Use case: six entries ask for a preview while every load is held back. Exactly five loads run at the
     * same time, the sixth waits for a free thread; when the loads are released, all six finish.
     */
    @Test
    fun loadsRunOnFiveThreadsAtTheSameTime() {
        val gate = CountDownLatch(1)
        source.gate = gate
        val entries = (1..6).map { refEntry("E$it.png", "r$it") }
        entries.forEach { source.add(it.reference, "png" to pngBytes(10, 10)) }
        show(folder("Project", *entries.toTypedArray()))

        entries.forEach { fx { viewModel.preview(it) } }
        awaitTrue { source.openRequested.get() == 5 }
        settle()
        assertEquals(5, source.openRequested.get())

        gate.countDown()
        awaitTrue { source.closed.get() == 6 }
    }

    /**
     * Use case: the number of threads is changed. The loads that start afterwards use the new number: with a
     * single thread, only one load runs at a time.
     */
    @Test
    fun changedLoadThreadsApplyToLaterLoads() {
        viewModel.loadThreads = 1
        val gate = CountDownLatch(1)
        source.gate = gate
        val entries = (1..3).map { refEntry("E$it.png", "r$it") }
        entries.forEach { source.add(it.reference, "png" to pngBytes(10, 10)) }
        show(folder("Project", *entries.toTypedArray()))

        entries.forEach { fx { viewModel.preview(it) } }
        awaitTrue { source.openRequested.get() == 1 }
        settle()
        assertEquals(1, source.openRequested.get())

        gate.countDown()
        awaitTrue { source.closed.get() == 3 }
    }
}
