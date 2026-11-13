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

import de.saxsys.mvvmfx.MvvmFX
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.pcsoft.app.hocal.app.Messages
import org.pcsoft.app.hocal.app.ui.HocalDialog
import org.pcsoft.app.hocal.model.project.VirtualEntry
import java.util.ResourceBundle

/**
 * Small window with one button per dialog variant and a label for each result.
 *
 * The texts are English and hard coded on purpose: the demo is no part of the application.
 */
class DialogDemoApplication : Application() {

    /**
     * Builds and shows the demo window.
     *
     * @param primaryStage the stage of the demo window
     */
    override fun start(primaryStage: Stage) {
        MvvmFX.setGlobalResourceBundle(ResourceBundle.getBundle(Messages.BUNDLE_NAME))

        val sampleResult = Label("Gallery with sample project: -")
        val sampleButton = Button("Gallery with sample project")
        sampleButton.setOnAction {
            val entry = HocalDialog.gallery(primaryStage, DemoProject.create(), DemoMediaSource())
            sampleResult.text = "Gallery with sample project: ${describe(entry)}"
        }

        val emptyResult = Label("Empty gallery: -")
        val emptyButton = Button("Empty gallery (no folder, no source)")
        emptyButton.setOnAction {
            val entry = HocalDialog.gallery(primaryStage, null, null)
            emptyResult.text = "Empty gallery: ${describe(entry)}"
        }

        val root = VBox(10.0, sampleButton, sampleResult, emptyButton, emptyResult)
        root.padding = Insets(16.0)
        primaryStage.title = "Hocal dialog demo"
        primaryStage.scene = Scene(root, 420.0, 220.0)
        primaryStage.show()
    }

    private fun describe(entry: VirtualEntry?): String = if (entry == null) "cancelled" else "selected '${entry.name}'"
}
