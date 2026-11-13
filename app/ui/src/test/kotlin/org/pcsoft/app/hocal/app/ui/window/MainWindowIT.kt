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
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.MenuBar
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.app.HocalIcons
import org.pcsoft.app.hocal.app.Messages
import org.testfx.framework.junit5.ApplicationTest
import java.text.MessageFormat
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

    @Suppress("UNCHECKED_CAST")
    private val flowListView: ListView<String>
        get() = window.scene.root.lookup(".list-view") as ListView<String>

    private val selectionLabel: Label
        get() = window.scene.root.lookup(".selection-hint") as Label

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
     * Use case: the window offers its flows in the flow list, and the content area asks for a pick
     * while nothing is selected.
     */
    @Test
    fun windowShowsTheFlowsAndTheHint() {
        assertEquals(3, flowListView.items.size)
        assertEquals(Messages["window.main.selection.none"], selectionLabel.text)
    }

    /**
     * Use case: the user clicks a flow in the list, so the content area names the picked flow.
     */
    @Test
    fun pickingAFlowShowsItInTheContentArea() {
        val flow = Messages["window.main.flow.upscale"]

        clickOn(flow)

        assertEquals(MessageFormat.format(Messages["window.main.selection.selected"], flow), selectionLabel.text)
    }

    /**
     * Use case: the user chooses Exit in the File menu, so the main window closes.
     */
    @Test
    fun exitClosesTheWindow() {
        interact { menuBar.menus.single().items.single().fire() }

        assertFalse(window.isShowing)
    }
}
