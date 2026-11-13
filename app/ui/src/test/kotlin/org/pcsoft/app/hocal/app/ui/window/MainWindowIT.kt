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

import de.saxsys.mvvmfx.MvvmFX
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.MenuBar
import javafx.scene.control.SplitPane
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.Messages
import org.testfx.framework.junit5.ApplicationTest
import java.util.Locale
import java.util.ResourceBundle

/**
 * Integration tests starting the complete main window.
 */
class MainWindowIT : ApplicationTest() {

    private lateinit var window: MainWindow

    override fun start(stage: Stage) {
        MvvmFX.setGlobalResourceBundle(ResourceBundle.getBundle(Messages.BUNDLE_NAME, Locale.GERMAN))

        window = MainWindow()
        window.show()
    }

    @AfterEach
    fun releaseWindow() {
        interact { window.hide() }
    }

    private val menuBar: MenuBar
        get() = window.scene.root.lookup(".menu-bar") as MenuBar

    private val splMain: SplitPane
        get() = window.scene.root.lookup("#splMain") as SplitPane

    private val splProject: SplitPane
        get() = window.scene.root.lookup("#splProject") as SplitPane

    private val splContent: SplitPane
        get() = window.scene.root.lookup("#splContent") as SplitPane

    private fun part(id: String): Node = window.scene.root.lookup("#$id")

    /**
     * Use case: starting the application shows a window titled after the product.
     */
    @Test
    fun windowStartsWithTheProductTitle() {
        assertEquals("Hocal AI", window.title)
        assertTrue(window.isShowing)
    }

    /**
     * Use case: the running window carries the application icon in all shipped sizes.
     */
    @Test
    fun windowCarriesTheApplicationIcon() {
        assertEquals(HocalIcons.APP_SIZES, window.icons.map { it.width.toInt() })
    }

    /**
     * Use case: the running window shows the menu bar with its German texts.
     */
    @Test
    fun windowShowsTheGermanMenuBar() {
        val fileMenu = menuBar.menus.single()

        assertEquals("_Datei", fileMenu.text)
        assertEquals(listOf("_Beenden"), fileMenu.items.map { it.text })
    }

    /**
     * Use case: the window opens with a size of 1280 x 800 pixels.
     */
    @Test
    fun windowOpensWithTheDefaultSize() {
        assertEquals(1280.0, window.scene.width, 1.0)
        assertEquals(800.0, window.scene.height, 1.0)
    }

    /**
     * Use case: the window shows the tree above the file list on the left, and the generation details,
     * the media with the prompt input and the history list side by side on the right.
     */
    @Test
    fun windowShowsAllPartsOfTheLayout() {
        listOf("pnlTree", "pnlFiles", "pnlDetails", "pnlMedia", "pnlHistory").forEach {
            assertNotNull(part(it), "part $it is missing")
        }

        assertEquals(Orientation.HORIZONTAL, splMain.orientation)
        assertEquals(Orientation.VERTICAL, splProject.orientation)
        assertEquals(Orientation.HORIZONTAL, splContent.orientation)
        assertEquals(listOf(part("pnlTree"), part("pnlFiles")), splProject.items)
        assertEquals(listOf(part("pnlDetails"), part("pnlMedia"), part("pnlHistory")), splContent.items)
        assertEquals(listOf<Node>(splProject, splContent), splMain.items)
    }

    /**
     * Use case: the window opens with the dividers at 30 % (project side), 50 % (tree and file list)
     * and 20 % / 80 % (content area).
     */
    @Test
    fun dividersStartAtTheDefaultPositions() {
        assertEquals(listOf(0.3), splMain.dividerPositions.toList().map { it.rounded() })
        assertEquals(listOf(0.5), splProject.dividerPositions.toList().map { it.rounded() })
        assertEquals(listOf(0.2, 0.8), splContent.dividerPositions.toList().map { it.rounded() })
    }

    /**
     * Use case: the user enlarges the window, so the project side, the generation details and the
     * history list keep their width and only the media with the prompt input takes the difference.
     *
     * The window is shrunk first and then grown back to its initial size, because the headless
     * screen of the test cannot show a window larger than the initial one.
     */
    @Test
    fun sideAreasKeepTheirWidthWhenTheWindowGrows() {
        interact { window.width -= 400.0 }

        val projectWidth = splProject.width
        val detailsWidth = part("pnlDetails").layoutBounds.width
        val historyWidth = part("pnlHistory").layoutBounds.width
        val mediaWidth = part("pnlMedia").layoutBounds.width

        interact { window.width += 400.0 }

        assertEquals(projectWidth, splProject.width, 1.0)
        assertEquals(detailsWidth, part("pnlDetails").layoutBounds.width, 1.0)
        assertEquals(historyWidth, part("pnlHistory").layoutBounds.width, 1.0)
        assertEquals(mediaWidth + 400.0, part("pnlMedia").layoutBounds.width, 1.0)
    }

    /**
     * Use case: the user shrinks the window, so the side areas keep their width and the media with
     * the prompt input gives up the difference.
     */
    @Test
    fun sideAreasKeepTheirWidthWhenTheWindowShrinks() {
        val projectWidth = splProject.width
        val detailsWidth = part("pnlDetails").layoutBounds.width
        val historyWidth = part("pnlHistory").layoutBounds.width

        interact { window.width -= 200.0 }

        assertEquals(projectWidth, splProject.width, 1.0)
        assertEquals(detailsWidth, part("pnlDetails").layoutBounds.width, 1.0)
        assertEquals(historyWidth, part("pnlHistory").layoutBounds.width, 1.0)
    }

    /**
     * Use case: the user chooses Exit in the File menu, so the main window closes.
     */
    @Test
    fun exitClosesTheWindow() {
        interact { menuBar.menus.single().items.single().fire() }

        assertFalse(window.isShowing)
    }

    private fun Double.rounded(): Double = Math.round(this * 100.0) / 100.0
}
