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

package org.pcsoft.app.hocal.app

import de.saxsys.mvvmfx.MvvmFX
import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import org.apache.commons.lang3.SystemUtils
import org.pcsoft.app.hocal.fx.model.manager.ProjectManager
import org.pcsoft.app.hocal.app.ui.window.MainWindow
import org.pcsoft.app.hocal.app.util.logger

/**
 * JavaFX application entry point of Hocal AI.
 *
 * There is no startup area yet (see the `startup` skill), so the main window is shown directly. Before it,
 * the [ProjectManager] is set up and a new, empty project is created: the window follows the project the
 * manager holds and never creates one itself.
 */
class HocalApplication : Application() {
    override fun start(stage: Stage) {
        ProjectManager.threadCheck = { Platform.isFxApplicationThread() }
        ProjectManager.applicationVersion = HocalApplication::class.java.`package`?.implementationVersion ?: "dev"
        ProjectManager.createNew(Messages["project.new.name"])

        MainWindow().show()
    }

    /** Closes the project and removes its temporary folder when the application ends. */
    override fun stop() {
        ProjectManager.close(discardChanges = true)
    }
}

/**
 * Starts the application after registering the global resource bundle used for I18N.
 */
fun main() {
    logger<HocalApplication>().apply {
        info("====================")
        info("Starting application")
        info(
            "System: {}, Java: [Version: {}, Vendor: {}]",
            SystemUtils.OS_NAME,
            SystemUtils.JAVA_VERSION,
            SystemUtils.JAVA_VENDOR
        )
        info("====================")
    }

    MvvmFX.setGlobalResourceBundle(Messages.bundle)
    Application.launch(HocalApplication::class.java)
}
