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
import org.pcsoft.app.hocal.model.project.VirtualEntry

/**
 * Developer tests for [VirtualEntryProperty] and its factory [VirtualEntryProperty.of].
 *
 * The property wraps one entry of the virtual directory structure and offers its name and the linked
 * reference as properties of their own. A [ChangeRecorder] hangs a binding on the property itself and
 * on every field of it, and the tests assert that a change reaches every binding that has to know
 * about it. Both fields are driven through one table, so no field can be forgotten.
 */
@Suppress("UNCHECKED_CAST")
class VirtualEntryPropertyTest {

    /** One field: how to reach its property, how to read and write the POJO, three values. */
    private class FieldCase(
        val path: String,
        val access: (VirtualEntryProperty) -> Property<Any?>,
        val read: (VirtualEntry) -> Any?,
        val write: (VirtualEntry, Any?) -> Unit,
        val initial: Any?,
        val changed: Any?,
        val neutral: Any?,
    )

    private fun fieldCase(
        path: String,
        access: (VirtualEntryProperty) -> Property<*>,
        read: (VirtualEntry) -> Any?,
        write: (VirtualEntry, Any?) -> Unit,
        initial: Any?,
        changed: Any?,
        neutral: Any?,
    ) = FieldCase(path, { target: VirtualEntryProperty -> access(target) as Property<Any?> }, read, write, initial, changed, neutral)

    private val cases: List<FieldCase> = listOf(
        fieldCase("entry.name", { it.nameProperty }, { it.name }, { e, v -> e.name = v as String }, "Cats", "Dogs", null),
        fieldCase("entry.reference", { it.referenceProperty }, { it.reference }, { e, v -> e.reference = v as String }, "ref-1", "ref-2", null),
    )

    private lateinit var entry: VirtualEntry
    private lateinit var property: VirtualEntryProperty
    private lateinit var recorder: ChangeRecorder

    /** Builds an entry carrying the initial value of every field. */
    private fun newEntry(): VirtualEntry {
        val result = VirtualEntry()
        cases.forEach { it.write(result, it.initial) }
        return result
    }

    /** Builds an entry carrying another value in every field. */
    private fun otherEntry(): VirtualEntry {
        val result = VirtualEntry()
        cases.forEach { it.write(result, it.changed) }
        return result
    }

    @BeforeEach
    fun setUp() {
        entry = newEntry()
        property = VirtualEntryProperty()
        property.set(entry)

        recorder = ChangeRecorder()
        recorder.watch("entry", property)
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
     * Use case: an entry is selected in the project tree, so every field property delivers the value
     * that already sits in the entry.
     */
    @Test
    fun readsInitialValuesFromPojo() {
        cases.forEach { assertEquals(it.initial, it.access(property).value, it.path) }
    }

    /**
     * Use case: the user renames the entry or links another reference, so the value lands in the entry
     * and the field property as well as the property wrapping the entry report the change.
     */
    @Test
    fun writesThroughPropertyToPojoAndFiresUpToRoot() {
        cases.forEach {
            recorder.reset()

            it.access(property).value = it.changed

            assertEquals(it.changed, it.read(entry), it.path)
            assertFiredUpToRoot(it.path)
        }
    }

    /**
     * Use case: a field is bound to the control the user types into, so every value that control
     * produces reaches the entry and every property up to the root reports it.
     */
    @Test
    fun writesThroughBindingToPojoAndFiresUpToRoot() {
        cases.forEach {
            val source = SimpleObjectProperty<Any?>(it.initial)
            val target = it.access(property)
            target.bind(source)
            recorder.reset()

            source.set(it.changed)

            assertEquals(it.changed, it.read(entry), it.path)
            assertFiredUpToRoot(it.path)
            target.unbind()
        }
    }

    /**
     * Use case: the entry is changed by application code past the property, so the property is told to
     * read it again and every field property delivers the current value afterwards.
     */
    @Test
    fun readsChangesMadePastPropertyAfterRefresh() {
        cases.forEach { it.write(entry, it.changed) }
        recorder.reset()

        property.refresh()

        cases.forEach { assertEquals(it.changed, it.access(property).value, it.path) }
        recorder.assertAllFired("refresh")
    }

    /**
     * Use case: another entry is selected, so every field property belongs to the other entry
     * afterwards, delivers its values and reports the change up to the root.
     */
    @Test
    fun exchangeUpdatesAllFieldsAndFiresUpToRoot() {
        val other = otherEntry()
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
     * Use case: another entry carrying the same values is selected, so no field property reports a
     * change, while a later write reaches the new entry only and leaves the one left behind untouched.
     */
    @Test
    fun exchangeAgainstEqualValuesKeepsFieldsQuiet() {
        val fresh = newEntry()
        recorder.reset()

        property.value = fresh

        recorder.assertNoneFired("exchange against equal values")
        cases.forEach { it.access(property).value = it.changed }
        cases.forEach {
            assertEquals(it.changed, it.read(fresh), it.path)
            assertEquals(it.initial, it.read(entry), it.path)
        }
    }

    /**
     * Use case: no entry is selected, so every field property answers with a neutral value and what is
     * written to it is dropped instead of creating an entry nobody asked for.
     */
    @Test
    fun readsNeutralValuesAndDropsWritesWhenEntryIsAbsent() {
        property.value = null

        cases.forEach { assertEquals(it.neutral, it.access(property).value, it.path) }

        cases.forEach { it.access(property).value = it.changed }

        assertEquals(newEntry(), entry)
    }

    /**
     * Use case: the accessors of the property are used instead of the field properties, so reading
     * and writing through them reaches the entry as well.
     */
    @Test
    fun accessorsDelegateToFieldProperties() {
        property.name = "Birds"
        property.reference = "ref-3"

        assertEquals("Birds", entry.name)
        assertEquals("ref-3", entry.reference)
        assertEquals("Birds", property.name)
        assertEquals("ref-3", property.reference)
    }

    /**
     * Use case: an entry is picked out of the children of a folder, so the property built for it
     * already carries that very entry - not a copy of it - and a write through the property lands on
     * that object.
     */
    @Test
    fun factoryWrapsTheGivenEntryInstance() {
        val picked = VirtualEntry().also {
            it.name = "Cats"
            it.reference = "ref-1"
        }

        val built = VirtualEntryProperty.of(picked)

        assertSame(picked, built.get())
        assertEquals("Cats", built.name)
        assertEquals("ref-1", built.reference)
        built.name = "Dogs"
        assertEquals("Dogs", picked.name)
    }

    /**
     * Use case: two entries are picked one after another, so each call to [VirtualEntryProperty.of]
     * builds a property of its own instead of two callers sharing the same one.
     */
    @Test
    fun factoryBuildsAnIndependentPropertyPerCall() {
        val first = VirtualEntryProperty.of(VirtualEntry().also { it.name = "first" })
        val second = VirtualEntryProperty.of(VirtualEntry().also { it.name = "second" })

        assertNotSame(first, second)
        assertEquals("first", first.name)
        assertEquals("second", second.name)
    }
}
