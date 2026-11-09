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

import de.saxsys.mvvmfx.FluentViewLoader
import javafx.scene.Scene
import javafx.stage.Stage
import org.pcsoft.app.hocal.app.Messages

/**
 * The application main window.
 *
 * Not modal, sized by its FXML content (1280 x 800), freely resizable. It carries no icon yet - the
 * application icon is added with `HocalIcons`, see the `icons` skill.
 */
class MainWindow : Stage() {
    private val viewModel: MainWindowViewModel

    init {
        FluentViewLoader.fxmlView(MainWindowView::class.java).load().apply {
            scene = Scene(view)
            this@MainWindow.viewModel = viewModel
        }

        title = Messages["window.main.title"]
    }
}
