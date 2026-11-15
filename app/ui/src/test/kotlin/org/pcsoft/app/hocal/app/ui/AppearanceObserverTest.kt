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

package org.pcsoft.app.hocal.app.ui

import javafx.application.ColorScheme
import javafx.application.Platform
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.Callable

/**
 * Developer tests of the observer of the system appearance.
 *
 * The appearance of the system cannot be changed in a test, so the tests prove the registration and the
 * value read at the start.
 */
class AppearanceObserverTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun startToolkit() {
            FxToolkit.registerPrimaryStage()
        }
    }

    private val observer = AppearanceObserver()

    private fun <T> fx(block: () -> T): T = WaitForAsyncUtils.asyncFx(Callable { block() }).get()

    /**
     * Use case: a component comes on screen. The observer registers once and reads the appearance of the
     * system.
     */
    @Test
    fun startRegistersAndReadsTheAppearance() {
        fx { observer.start() }

        assertEquals(1, observer.registrations)
        assertEquals(fx { Platform.getPreferences().colorScheme == ColorScheme.DARK }, observer.dark.get())

        fx { observer.stop() }
    }

    /**
     * Use case: show is reported twice, as can happen on quick screen changes. The registration exists once
     * only, so one stop is enough.
     */
    @Test
    fun doubleStartRegistersOnlyOnce() {
        fx {
            observer.start()
            observer.start()
        }
        assertEquals(1, observer.registrations)

        fx { observer.stop() }
        assertEquals(0, observer.registrations)
    }

    /**
     * Use case: a component leaves the screen and comes back. Stopping twice is harmless, and starting again
     * registers again.
     */
    @Test
    fun stopReleasesAndStartRegistersAgain() {
        fx {
            observer.start()
            observer.stop()
            observer.stop()
        }
        assertEquals(0, observer.registrations)

        fx { observer.start() }
        assertEquals(1, observer.registrations)

        fx { observer.stop() }
    }
}
