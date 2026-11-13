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

package org.pcsoft.app.hocal.app.ui.dialog

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.ui.component.ProjectFileListViewModel
import org.pcsoft.app.hocal.app.ui.component.entry
import org.pcsoft.app.hocal.app.ui.component.folder
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * Developer tests of the gallery dialog view model, driven without a window.
 *
 * The selection of the file list is a real [ProjectFileListViewModel], bound the way the view binds it.
 */
class GalleryDialogViewModelTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val viewModel = GalleryDialogViewModel()
    private val listViewModel = ProjectFileListViewModel()

    @AfterEach
    fun releaseList() {
        fx { listViewModel.close() }
    }

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    /**
     * Use case: the dialog was just opened and nothing is chosen, so no entry is held and the OK button is
     * disabled.
     */
    @Test
    fun okIsDisabledWithoutSelection() {
        assertNull(viewModel.selectedEntry.get())
        assertTrue(viewModel.okDisabled.get())
    }

    /**
     * Use case: the user chooses an entry, so the OK button becomes active; when the choice is gone again,
     * the button is disabled again.
     */
    @Test
    fun okIsEnabledWithSelection() {
        val chosen = entry("Chosen.png")

        viewModel.selectedEntry.set(chosen)
        assertFalse(viewModel.okDisabled.get())
        assertSame(chosen, viewModel.selectedEntry.get())

        viewModel.selectedEntry.set(null)
        assertTrue(viewModel.okDisabled.get())
    }

    /**
     * Use case: the user chose an entry in one folder and then switches to another folder. The file list
     * drops the selection, so the chosen entry is cleared and the OK button is disabled again.
     */
    @Test
    fun folderChangeClearsTheSelection() {
        val chosen = entry("First.png")
        val firstFolder = folder("First folder", chosen, entry("Second.png"))
        val otherFolder = folder("Other folder", entry("Third.png"))

        fx {
            viewModel.selectedEntry.bind(listViewModel.selectedFile)
            listViewModel.folder.set(firstFolder)
            listViewModel.onShow()
        }
        WaitForAsyncUtils.waitForFxEvents()
        fx { listViewModel.selectFile(chosen) }

        assertSame(chosen, viewModel.selectedEntry.get())
        assertFalse(viewModel.okDisabled.get())

        fx { listViewModel.folder.set(otherFolder) }
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, Callable { viewModel.selectedEntry.get() == null })

        assertNull(viewModel.selectedEntry.get())
        assertTrue(viewModel.okDisabled.get())
    }
}
