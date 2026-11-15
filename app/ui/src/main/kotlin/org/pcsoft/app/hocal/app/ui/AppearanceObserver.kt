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
import javafx.beans.InvalidationListener
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper

/**
 * Follows the light or dark appearance of the system, so that icons can be chosen to match it.
 *
 * The appearance of the system is global, therefore a component calls [start] when it comes on screen and
 * [stop] when it leaves it, see the `fx-component-lifecycle` skill. Between the two, [dark] follows the
 * system; before [start] and after [stop] it keeps its last value. Everything belongs to the FX thread.
 */
class AppearanceObserver {
    private val darkWrapper = ReadOnlyBooleanWrapper(this, "dark", false)
    private var observing = false

    // A field: a method reference would be a new object per use and could not be removed again.
    private val listener = InvalidationListener { onAppearanceChanged() }

    /** Whether the system shows the dark appearance. */
    val dark: ReadOnlyBooleanProperty = darkWrapper.readOnlyProperty

    /** The number of registrations at the system appearance, which is 1 while observing and 0 else. */
    internal var registrations = 0
        private set

    /**
     * Starts following the appearance and reads it now.
     *
     * Calling it twice registers only once.
     */
    fun start() {
        if (!observing) {
            observing = true
            Platform.getPreferences().colorSchemeProperty().addListener(listener)
            registrations++
        }
        darkWrapper.set(isDarkAppearance())
    }

    /**
     * Stops following the appearance; [dark] keeps its last value.
     *
     * Calling it twice is harmless.
     */
    fun stop() {
        if (observing) {
            observing = false
            Platform.getPreferences().colorSchemeProperty().removeListener(listener)
            registrations--
        }
    }

    private fun isDarkAppearance(): Boolean = Platform.getPreferences().colorScheme == ColorScheme.DARK

    private fun onAppearanceChanged() {
        // Reading again is a must: a property reports the next change only after its value was read.
        Platform.getPreferences().colorScheme
        if (Platform.isFxApplicationThread()) {
            if (observing) darkWrapper.set(isDarkAppearance())
        } else {
            Platform.runLater { if (observing) darkWrapper.set(isDarkAppearance()) }
        }
    }
}
