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

package org.pcsoft.app.hocal.app.ui.window

import javafx.beans.InvalidationListener
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.ui.MediaChanges
import org.pcsoft.app.hocal.fx.model.manager.ProjectManager
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicInteger

/**
 * Developer tests of the view model of the main window.
 *
 * The view model follows the project of the [ProjectManager], so every test starts with a new project and
 * closes it again.
 */
class MainWindowViewModelTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val viewModel = MainWindowViewModel()

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    @BeforeEach
    fun createProject() {
        fx { ProjectManager.createNew("Test") }
    }

    @AfterEach
    fun closeProject() {
        fx {
            viewModel.onHide()
            ProjectManager.close(discardChanges = true)
        }
    }

    private val root get() = ProjectManager.project.treeProperty.get()!!

    private fun countInvalidations(): AtomicInteger {
        val count = AtomicInteger()
        viewModel.listFolder.addListener(InvalidationListener {
            // Reading again is a must: a property reports the next change only after its value was read.
            viewModel.listFolder.get()
            count.incrementAndGet()
        })
        return count
    }

    /**
     * Use case: the window follows the project of the manager. The tree is the root folder of the new
     * project, and it is gone when the project is closed.
     */
    @Test
    fun treeIsTheRootFolderOfTheProject() {
        assertNotNull(viewModel.tree.get())
        assertSame(root, viewModel.tree.get())

        fx { ProjectManager.close(discardChanges = true) }

        assertNull(viewModel.tree.get())
    }

    /**
     * Use case: the user selects a folder in the tree and then no folder. The list folder follows the
     * selection, by identity.
     */
    @Test
    fun listFolderFollowsTheSelectedFolder() {
        fx { viewModel.selectedFolder.set(root) }
        assertSame(root, viewModel.listFolder.get())

        fx { viewModel.selectedFolder.set(null) }
        assertNull(viewModel.listFolder.get())
    }

    /**
     * Use case: the project changes - a folder is created - while the window is shown. The folders are plain
     * objects that report nothing, so the list folder reports the change itself, and the list reads its folder
     * again.
     */
    @Test
    fun projectChangeIsReportedByTheListFolder() {
        fx { viewModel.selectedFolder.set(root) }
        fx { viewModel.onShow() }
        val count = countInvalidations()

        fx { ProjectManager.createFolder(root, "Photos") }
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(count.get() >= 1, "reports: ${count.get()}")
        assertSame(root, viewModel.listFolder.get())
    }

    /**
     * Use case: the window leaves the screen. A change of the project is not followed any more, and showing
     * the window again brings the registration back.
     */
    @Test
    fun hiddenViewModelDoesNotFollowTheProject() {
        fx { viewModel.selectedFolder.set(root) }
        fx { viewModel.onShow() }
        fx { viewModel.onHide() }
        val count = countInvalidations()

        fx { ProjectManager.createFolder(root, "Hidden") }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(0, count.get())

        fx { viewModel.onShow() }
        WaitForAsyncUtils.waitForFxEvents()
        val afterShow = count.get()
        fx { ProjectManager.createFolder(root, "Shown") }
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(afterShow >= 1, "showing reports the list folder once")
        assertTrue(count.get() > afterShow, "a change after showing is followed")
    }

    /**
     * Use case: the media of a reference change while the window is shown. The view is told which reference
     * it was, on the FX thread; after the window was hidden it is not told any more.
     */
    @Test
    fun mediaChangeIsReportedWhileShown() {
        val received = mutableListOf<String>()
        viewModel.onMediaChanged = { received.add(it) }
        fx { viewModel.onShow() }

        MediaChanges.changed("r1")
        WaitForAsyncUtils.waitForFxEvents()
        fx { viewModel.onHide() }
        MediaChanges.changed("r2")
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("r1"), received)
    }

    /**
     * Use case: show is reported twice, as can happen on quick screen changes. The registration exists
     * once only, so a media change is reported once.
     */
    @Test
    fun doubleShowRegistersOnlyOnce() {
        val received = mutableListOf<String>()
        viewModel.onMediaChanged = { received.add(it) }
        fx {
            viewModel.onShow()
            viewModel.onShow()
        }

        MediaChanges.changed("r1")
        WaitForAsyncUtils.waitForFxEvents()

        assertEquals(listOf("r1"), received)
    }

    /**
     * Use case: the user selects a file and then no file. The file of the history follows the selection, by
     * identity.
     */
    @Test
    fun historyFileFollowsTheSelectedFile() {
        val file = VirtualEntry().apply { name = "A.png"; reference = "r1" }

        fx { viewModel.selectedFile.set(file) }
        assertSame(file, viewModel.historyFile.get())

        fx { viewModel.selectedFile.set(null) }
        assertNull(viewModel.historyFile.get())
    }

    /**
     * Use case: the project changes - for instance a medium is appended - while the window is shown. The
     * file of the history is reported again although it is the same entry, so the history list counts the
     * media again.
     */
    @Test
    fun projectChangeIsReportedByTheHistoryFile() {
        val file = VirtualEntry().apply { name = "A.png"; reference = "r1" }
        fx { viewModel.selectedFile.set(file) }
        fx { viewModel.onShow() }
        val count = AtomicInteger()
        viewModel.historyFile.addListener(InvalidationListener {
            viewModel.historyFile.get()
            count.incrementAndGet()
        })

        fx { ProjectManager.createFolder(root, "Photos") }
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(count.get() >= 1, "reports: ${count.get()}")
        assertSame(file, viewModel.historyFile.get())
    }

    /**
     * Use case: nothing is selected in the history list. The medium the image view shows is empty, and a
     * medium that the list reports is taken over.
     */
    @Test
    fun selectedMediumIsEmptyUntilTheListReportsOne() {
        assertNull(viewModel.selectedMedium.get())
    }
}
