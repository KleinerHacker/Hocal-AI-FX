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
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.fx.model.ChangeRecorder
import org.pcsoft.app.hocal.model.project.ProjectData
import java.time.Instant

/**
 * Developer tests for [ProjectDataProperty].
 *
 * The property wraps the general data of a project and offers every field of it as a property of its
 * own. Every test checks the object tree the way the user interface uses it: a [ChangeRecorder] hangs
 * a binding on the property itself and on every single field of it, and the tests assert that a change
 * reaches every binding that has to know about it - upwards to the property itself as well as into the
 * field properties of an exchanged object. The scalar fields are driven through one table, so no field
 * can be forgotten; the tag list has tests of its own.
 */
@Suppress("UNCHECKED_CAST")
class ProjectDataPropertyTest {

    /** One scalar field: how to reach its property, how to read and write the POJO, three values. */
    private class FieldCase(
        val path: String,
        val access: (ProjectDataProperty) -> Property<Any?>,
        val read: (ProjectData) -> Any?,
        val write: (ProjectData, Any?) -> Unit,
        val initial: Any?,
        val changed: Any?,
        val neutral: Any?,
    )

    private fun fieldCase(
        path: String,
        access: (ProjectDataProperty) -> Property<*>,
        read: (ProjectData) -> Any?,
        write: (ProjectData, Any?) -> Unit,
        initial: Any?,
        changed: Any?,
        neutral: Any?,
    ) = FieldCase(path, { target: ProjectDataProperty -> access(target) as Property<Any?> }, read, write, initial, changed, neutral)

    private val cases: List<FieldCase> = listOf(
        fieldCase("data.name", { it.nameProperty }, { it.name }, { d, v -> d.name = v as String }, "Sunset", "Sunrise", null),
        fieldCase("data.description", { it.descriptionProperty }, { it.description }, { d, v -> d.description = v as String }, "Warm light", "Cold light", null),
        fieldCase("data.uuid", { it.uuidProperty }, { it.uuid }, { d, v -> d.uuid = v as String }, "uuid-1", "uuid-2", null),
        fieldCase("data.author", { it.authorProperty }, { it.author }, { d, v -> d.author = v as String }, "Jane Doe", "John Roe", null),
        fieldCase("data.created", { it.createdProperty }, { it.created }, { d, v -> d.created = v as Instant? }, Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-02-02T11:00:00Z"), null),
        fieldCase("data.modified", { it.modifiedProperty }, { it.modified }, { d, v -> d.modified = v as Instant? }, Instant.parse("2026-01-05T10:00:00Z"), Instant.parse("2026-02-06T11:00:00Z"), null),
        fieldCase("data.formatVersion", { it.formatVersionProperty }, { it.formatVersion }, { d, v -> d.formatVersion = v as Int }, 1, 2, 0),
        fieldCase("data.applicationVersion", { it.applicationVersionProperty }, { it.applicationVersion }, { d, v -> d.applicationVersion = v as String }, "1.0.0", "1.1.0", null),
    )

    private lateinit var data: ProjectData
    private lateinit var property: ProjectDataProperty
    private lateinit var recorder: ChangeRecorder

    /** Builds project data carrying the initial value of every field. */
    private fun newData(): ProjectData {
        val result = ProjectData()
        cases.forEach { it.write(result, it.initial) }
        result.tags = mutableListOf("alpha", "beta")
        return result
    }

    /** Builds project data carrying another value in every field. */
    private fun otherData(): ProjectData {
        val result = ProjectData()
        cases.forEach { it.write(result, it.changed) }
        result.tags = mutableListOf("gamma")
        return result
    }

    @BeforeEach
    fun setUp() {
        data = newData()
        property = ProjectDataProperty()
        property.set(data)

        recorder = ChangeRecorder()
        recorder.watch("data", property)
        cases.forEach { recorder.watch(it.path, it.access(property)) }
        recorder.watch("data.tags", property.tagsProperty)
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
     * Use case: the project is read from its file before the user interface is built, so every field
     * property delivers the value that already sits in the project data.
     */
    @Test
    fun readsInitialValuesFromPojo() {
        cases.forEach { assertEquals(it.initial, it.access(property).value, it.path) }
        assertEquals(listOf("alpha", "beta"), property.tagsProperty.get())
    }

    /**
     * Use case: the user edits a field in the project settings, so the value lands in the project data
     * and the field property as well as the property wrapping the data report the change.
     */
    @Test
    fun writesThroughPropertyToPojoAndFiresUpToRoot() {
        cases.forEach {
            recorder.reset()

            it.access(property).value = it.changed

            assertEquals(it.changed, it.read(data), it.path)
            assertFiredUpToRoot(it.path)
        }
    }

    /**
     * Use case: a field is bound to the control the user types into, so every value that control
     * produces reaches the project data and every property up to the root reports it.
     */
    @Test
    fun writesThroughBindingToPojoAndFiresUpToRoot() {
        cases.forEach {
            val source = SimpleObjectProperty<Any?>(it.initial)
            val target = it.access(property)
            target.bind(source)
            recorder.reset()

            source.set(it.changed)

            assertEquals(it.changed, it.read(data), it.path)
            assertFiredUpToRoot(it.path)
            target.unbind()
        }
    }

    /**
     * Use case: the tags are bound to a list the tag editor owns, so a list produced by that editor
     * reaches the project data and every property up to the root reports it.
     */
    @Test
    fun writesBoundTagsToPojoAndFiresUpToRoot() {
        val source = SimpleListProperty(FXCollections.observableArrayList("omega"))
        property.tagsProperty.bind(source)
        recorder.reset()

        source.set(FXCollections.observableArrayList("sigma", "tau"))

        assertEquals(listOf("sigma", "tau"), data.tags)
        assertFiredUpToRoot("data.tags")
        property.tagsProperty.unbind()
    }

    /**
     * Use case: the user adds, removes and replaces tags, so every operation lands in the project
     * data and is reported up to the root.
     */
    @Test
    fun writesTagOperationsToPojoAndFiresUpToRoot() {
        recorder.reset()
        property.tagsProperty.add("gamma")
        assertEquals(listOf("alpha", "beta", "gamma"), data.tags)
        assertFiredUpToRoot("data.tags")

        recorder.reset()
        property.tagsProperty.removeAt(0)
        assertEquals(listOf("beta", "gamma"), data.tags)
        assertFiredUpToRoot("data.tags")

        recorder.reset()
        property.tagsProperty[0] = "delta"
        assertEquals(listOf("delta", "gamma"), data.tags)
        assertFiredUpToRoot("data.tags")
    }

    /**
     * Use case: the project data is changed by application code past the property, so the property is
     * told to read it again and every field property delivers the current value afterwards.
     */
    @Test
    fun readsChangesMadePastPropertyAfterRefresh() {
        cases.forEach { it.write(data, it.changed) }
        data.tags = mutableListOf("delta")
        recorder.reset()

        property.refresh()

        cases.forEach { assertEquals(it.changed, it.access(property).value, it.path) }
        assertEquals(listOf("delta"), property.tagsProperty.get())
        recorder.assertAllFired("refresh")
    }

    /**
     * Use case: another project is opened, so every field property belongs to the other data
     * afterwards, delivers its values and reports the change up to the root.
     */
    @Test
    fun exchangeUpdatesAllFieldsAndFiresUpToRoot() {
        val other = otherData()
        recorder.reset()

        property.value = other

        cases.forEach { assertEquals(it.changed, it.access(property).value, it.path) }
        assertEquals(listOf("gamma"), property.tagsProperty.get())
        recorder.assertAllFired("exchange against other values")
        cases.forEach {
            it.access(property).value = it.initial
            assertEquals(it.initial, it.read(other), it.path)
        }
    }

    /**
     * Use case: the project is loaded again and carries the same values as before, so no field
     * property reports a change, while a later write reaches the new object only and leaves the one
     * left behind untouched.
     */
    @Test
    fun exchangeAgainstEqualValuesKeepsFieldsQuiet() {
        val fresh = newData()
        recorder.reset()

        property.value = fresh

        recorder.assertNoneFired("exchange against equal values")
        cases.forEach { it.access(property).value = it.changed }
        cases.forEach {
            assertEquals(it.changed, it.read(fresh), it.path)
            assertEquals(it.initial, it.read(data), it.path)
        }
    }

    /**
     * Use case: no project is open, so every field property answers with a neutral value and what is
     * written to it is dropped instead of creating data nobody asked for.
     */
    @Test
    fun readsNeutralValuesAndDropsWritesWhenDataIsAbsent() {
        property.value = null

        cases.forEach { assertEquals(it.neutral, it.access(property).value, it.path) }
        assertTrue(property.tagsProperty.isEmpty())

        cases.forEach { it.access(property).value = it.changed }
        property.tagsProperty.add("lost")

        assertEquals(newData(), data)
    }

    /**
     * Use case: the accessors of the property are used instead of the field properties, so reading
     * and writing through them reaches the project data as well.
     */
    @Test
    fun accessorsDelegateToFieldProperties() {
        val moment = Instant.parse("2026-03-03T12:00:00Z")

        property.name = "Dawn"
        property.description = "Pale light"
        property.uuid = "uuid-3"
        property.author = "Max Mustermann"
        property.created = moment
        property.modified = moment
        property.tags = listOf("x", "y")
        property.formatVersion = 3
        property.applicationVersion = "2.0.0"

        val expected = ProjectData(
            name = "Dawn",
            description = "Pale light",
            uuid = "uuid-3",
            author = "Max Mustermann",
            created = moment,
            modified = moment,
            tags = mutableListOf("x", "y"),
            formatVersion = 3,
            applicationVersion = "2.0.0",
        )
        assertEquals(expected, data)
        assertEquals("Dawn", property.name)
        assertEquals("Pale light", property.description)
        assertEquals("uuid-3", property.uuid)
        assertEquals("Max Mustermann", property.author)
        assertEquals(moment, property.created)
        assertEquals(moment, property.modified)
        assertEquals(listOf("x", "y"), property.tags)
        assertEquals(3, property.formatVersion)
        assertEquals("2.0.0", property.applicationVersion)
    }
}
