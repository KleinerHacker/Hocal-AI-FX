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

package org.pcsoft.app.hocal.fx.model.project

import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.fx.model.ChangeRecorder
import org.pcsoft.app.hocal.model.project.Medium

/**
 * Developer tests for [MediumProperty] and its factory [MediumProperty.of].
 *
 * The property wraps one medium of a reference and offers every field of it as a property of its own.
 * A [ChangeRecorder] hangs a binding on the property itself and on every single field of it, and the
 * tests assert that a change reaches every binding that has to know about it. All fields are driven
 * through one table, so no field can be forgotten.
 */
@Suppress("UNCHECKED_CAST")
class MediumPropertyTest {

    /** One field: how to reach its property, how to read and write the POJO, three values. */
    private class FieldCase(
        val path: String,
        val access: (MediumProperty) -> Property<Any?>,
        val read: (Medium) -> Any?,
        val write: (Medium, Any?) -> Unit,
        val initial: Any?,
        val changed: Any?,
        val neutral: Any?,
    )

    private fun fieldCase(
        path: String,
        access: (MediumProperty) -> Property<*>,
        read: (Medium) -> Any?,
        write: (Medium, Any?) -> Unit,
        initial: Any?,
        changed: Any?,
        neutral: Any?,
    ) = FieldCase(path, { target: MediumProperty -> access(target) as Property<Any?> }, read, write, initial, changed, neutral)

    private val cases: List<FieldCase> = listOf(
        fieldCase("medium.prompt", { it.promptProperty }, { it.prompt }, { m, v -> m.prompt = v as String? }, "a red fox", "a blue whale", null),
        fieldCase("medium.model", { it.modelProperty }, { it.model }, { m, v -> m.model = v as String? }, "model-a", "model-b", null),
        fieldCase("medium.flow", { it.flowProperty }, { it.flow }, { m, v -> m.flow = v as String? }, "flow-a", "flow-b", null),
        fieldCase("medium.seed", { it.seedProperty }, { it.seed }, { m, v -> m.seed = v as Long? }, 11L, 22L, null),
        fieldCase(
            "medium.parameters",
            { it.parametersProperty },
            { it.parameters },
            { m, v -> m.parameters = LinkedHashMap(v as Map<String, String>) },
            linkedMapOf("steps" to "20"),
            linkedMapOf("steps" to "30", "cfg" to "7"),
            null,
        ),
    )

    private lateinit var medium: Medium
    private lateinit var property: MediumProperty
    private lateinit var recorder: ChangeRecorder

    /** Builds a medium carrying the initial value of every field. */
    private fun newMedium(): Medium {
        val result = Medium()
        cases.forEach { it.write(result, it.initial) }
        return result
    }

    /** Builds a medium carrying another value in every field. */
    private fun otherMedium(): Medium {
        val result = Medium()
        cases.forEach { it.write(result, it.changed) }
        return result
    }

    @BeforeEach
    fun setUp() {
        medium = newMedium()
        property = MediumProperty()
        property.set(medium)

        recorder = ChangeRecorder()
        recorder.watch("medium", property)
        cases.forEach { recorder.watch(it.path, it.access(property)) }
    }

    /** Asserts that the property at [path] and every property above it up to the root fired. */
    private fun assertFiredUpToRoot(path: String) {
        var current = path
        while (true) {
            assertTrue(recorder.countOf(current) > 0) { "$current stayed quiet" }
            val dot = current.lastIndexOf('.')
            if (dot < 0) break
            current = current.substring(0, dot)
        }
    }

    /**
     * Use case: a medium is picked out of the history of a reference, so every field property
     * delivers the value that already sits in the medium.
     */
    @Test
    fun readsInitialValuesFromPojo() {
        cases.forEach { assertEquals(it.initial, it.access(property).value, it.path) }
    }

    /**
     * Use case: the details of a medium are edited, so every value lands in the medium and the field
     * property as well as the property wrapping the medium report the change.
     */
    @Test
    fun writesThroughPropertyToPojoAndFiresUpToRoot() {
        cases.forEach {
            recorder.reset()

            it.access(property).value = it.changed

            assertEquals(it.changed, it.read(medium), it.path)
            assertFiredUpToRoot(it.path)
        }
    }

    /**
     * Use case: a field is bound to the control the user types into, so every value that control
     * produces reaches the medium and every property up to the root reports it.
     */
    @Test
    fun writesThroughBindingToPojoAndFiresUpToRoot() {
        cases.forEach {
            val source = SimpleObjectProperty<Any?>(it.initial)
            val target = it.access(property)
            target.bind(source)
            recorder.reset()

            source.set(it.changed)

            assertEquals(it.changed, it.read(medium), it.path)
            assertFiredUpToRoot(it.path)
            target.unbind()
        }
    }

    /**
     * Use case: the medium is changed by application code past the property, so the property is told
     * to read it again and every field property delivers the current value afterwards.
     */
    @Test
    fun readsChangesMadePastPropertyAfterRefresh() {
        cases.forEach { it.write(medium, it.changed) }
        recorder.reset()

        property.refresh()

        cases.forEach { assertEquals(it.changed, it.access(property).value, it.path) }
        recorder.assertAllFired("refresh")
    }

    /**
     * Use case: another medium is picked, so every field property belongs to the other medium
     * afterwards, delivers its values and reports the change up to the root.
     */
    @Test
    fun exchangeUpdatesAllFieldsAndFiresUpToRoot() {
        val other = otherMedium()
        recorder.reset()

        property.value = other

        cases.forEach { assertEquals(it.changed, it.access(property).value, it.path) }
        recorder.assertAllFired("exchange against other values")
        cases.forEach {
            it.access(property).value = it.initial
            assertEquals(it.initial, it.read(other), it.path)
        }
    }

    /**
     * Use case: another medium carrying the same values is picked, so no field property reports a
     * change, while a later write reaches the new medium only and leaves the one left behind untouched.
     */
    @Test
    fun exchangeAgainstEqualValuesKeepsFieldsQuiet() {
        val fresh = newMedium()
        recorder.reset()

        property.value = fresh

        recorder.assertNoneFired("exchange against equal values")
        cases.forEach { it.access(property).value = it.changed }
        cases.forEach {
            assertEquals(it.changed, it.read(fresh), it.path)
            assertEquals(it.initial, it.read(medium), it.path)
        }
    }

    /**
     * Use case: no medium is picked, so every field property answers with a neutral value and what is
     * written to it is dropped instead of creating a medium nobody asked for.
     */
    @Test
    fun readsNeutralValuesAndDropsWritesWhenMediumIsAbsent() {
        property.value = null

        cases.forEach { assertEquals(it.neutral, it.access(property).value, it.path) }

        cases.forEach { it.access(property).value = it.changed }

        assertEquals(newMedium(), medium)
    }

    /**
     * Use case: the accessors of the property are used instead of the field properties, so reading
     * and writing through them reaches the medium as well.
     */
    @Test
    fun accessorsDelegateToFieldProperties() {
        property.prompt = "a green owl"
        property.model = "model-c"
        property.flow = "flow-c"
        property.seed = 33L
        property.parameters = linkedMapOf("steps" to "40")

        val expected = Medium(
            prompt = "a green owl",
            model = "model-c",
            flow = "flow-c",
            seed = 33L,
            parameters = linkedMapOf("steps" to "40"),
        )
        assertEquals(expected, medium)
        assertEquals("a green owl", property.prompt)
        assertEquals("model-c", property.model)
        assertEquals("flow-c", property.flow)
        assertEquals(33L, property.seed)
        assertEquals(mapOf("steps" to "40"), property.parameters)
    }

    /**
     * Use case: a medium is picked out of the media list of a reference, so the property built for it
     * already carries that very medium - not a copy of it - and a write through the property lands on
     * that object.
     */
    @Test
    fun factoryWrapsTheGivenMediumInstance() {
        val picked = Medium(prompt = "a red fox", seed = 5L)

        val built = MediumProperty.of(picked)

        assertSame(picked, built.get())
        assertEquals("a red fox", built.prompt)
        assertEquals(5L, built.seed)
        built.prompt = "a blue whale"
        assertEquals("a blue whale", picked.prompt)
    }

    /**
     * Use case: two media are picked one after another, so each call to [MediumProperty.of] builds a
     * property of its own instead of two callers sharing the same one.
     */
    @Test
    fun factoryBuildsAnIndependentPropertyPerCall() {
        val first = MediumProperty.of(Medium(prompt = "first"))
        val second = MediumProperty.of(Medium(prompt = "second"))

        assertNotSame(first, second)
        assertEquals("first", first.prompt)
        assertEquals("second", second.prompt)
    }
}
