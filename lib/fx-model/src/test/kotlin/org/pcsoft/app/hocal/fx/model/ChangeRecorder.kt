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

package org.pcsoft.app.hocal.fx.model

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.value.ObservableValue
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Counts the change events of a whole property tree, one counter per watched property.
 *
 * A view of the user interface never listens on the property itself, it hangs a binding on it and
 * reacts to the change of that binding. The recorder does the same: every watched property gets a
 * binding on the text form of its value, and a listener on that binding counts how often the value
 * behind it really changed. That is the event a bound control would receive, so a property that
 * stays quiet here would leave a stale value on screen.
 *
 * The text form is the `toString` of the value. All model objects are data classes, so their text
 * form covers the nested objects as well - a change deep inside the tree therefore also shows up on
 * every property above it, which is exactly what the propagation to the root has to achieve.
 */
internal class ChangeRecorder {

    private val counts = linkedMapOf<String, Int>()

    // The observable keeps only a weak reference to the binding hanging on it, so the bindings are
    // held here for as long as the recorder lives - otherwise they could be collected and stop counting.
    private val bindings = mutableListOf<StringBinding>()

    /**
     * Puts [observable] under observation as [name].
     *
     * The counter starts at zero, so only what happens after this call is counted.
     */
    fun watch(name: String, observable: ObservableValue<*>) {
        val binding = Bindings.createStringBinding({ observable.value?.toString() ?: "null" }, observable)
        binding.addListener { _, _, _ -> counts[name] = (counts[name] ?: 0) + 1 }
        bindings += binding
        counts[name] = 0
    }

    /** Sets every counter back to zero, so a test counts only the operation it is about. */
    fun reset() = counts.keys.forEach { counts[it] = 0 }

    /** The number of change events counted for the property watched as [name]. */
    fun countOf(name: String): Int = counts.getValue(name)

    /** The names of all watched properties, in the order they were put under observation. */
    fun watchedNames(): Set<String> = counts.keys.toSet()

    /**
     * Fails unless every watched property reported at least one change, naming those that stayed
     * quiet.
     */
    fun assertAllFired(operation: String) {
        val silent = counts.filterValues { it == 0 }.keys
        assertTrue(silent.isEmpty()) {
            "$operation fired no change event on ${silent.size} of ${counts.size} properties: " +
                    silent.joinToString()
        }
    }

    /**
     * Fails as soon as any watched property reported a change, naming those that did.
     */
    fun assertNoneFired(operation: String) {
        val noisy = counts.filterValues { it > 0 }.keys
        assertTrue(noisy.isEmpty()) {
            "$operation fired an unexpected change event on: " + noisy.joinToString()
        }
    }
}
