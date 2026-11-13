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

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.fx.model.ChangeRecorder
import org.pcsoft.app.hocal.model.project.Medium
import org.pcsoft.app.hocal.model.project.Reference

/**
 * Developer tests for [ReferenceProperty] and its factory [ReferenceProperty.of].
 *
 * The property wraps one reference of a project and offers its id and its media list as properties of
 * their own. A [ChangeRecorder] hangs a binding on the property itself and on every field of it, and
 * the tests assert that a change reaches every binding that has to know about it. The media are plain
 * entries of a list: a change inside such an entry is only picked up by [ReferenceProperty.refresh].
 */
class ReferencePropertyTest {

    private lateinit var reference: Reference
    private lateinit var property: ReferenceProperty
    private lateinit var recorder: ChangeRecorder

    private fun newReference() = Reference(
        uuid = "ref-1",
        media = mutableListOf(Medium(prompt = "first"), Medium(prompt = "second")),
    )

    private fun otherReference() = Reference(
        uuid = "ref-2",
        media = mutableListOf(Medium(prompt = "third")),
    )

    @BeforeEach
    fun setUp() {
        reference = newReference()
        property = ReferenceProperty()
        property.set(reference)

        recorder = ChangeRecorder()
        recorder.watch("reference", property)
        recorder.watch("reference.uuid", property.uuidProperty)
        recorder.watch("reference.media", property.mediaProperty)
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
     * Use case: a reference is picked in the project, so the id and the media list deliver the values
     * that already sit in the reference.
     */
    @Test
    fun readsInitialValuesFromPojo() {
        assertEquals("ref-1", property.uuidProperty.get())
        assertEquals(newReference().media, property.mediaProperty.get())
    }

    /**
     * Use case: the id of the reference is changed, so the value lands in the reference and the field
     * property as well as the property wrapping the reference report the change.
     */
    @Test
    fun writesUuidThroughPropertyToPojoAndFiresUpToRoot() {
        property.uuidProperty.set("ref-9")

        assertEquals("ref-9", reference.uuid)
        assertFiredUpToRoot("reference.uuid")
    }

    /**
     * Use case: the id is bound to a control, so every value that control produces reaches the
     * reference and every property up to the root reports it.
     */
    @Test
    fun writesUuidThroughBindingToPojoAndFiresUpToRoot() {
        val source = SimpleObjectProperty("ref-0")
        property.uuidProperty.bind(source)
        recorder.reset()

        source.set("ref-9")

        assertEquals("ref-9", reference.uuid)
        assertFiredUpToRoot("reference.uuid")
        property.uuidProperty.unbind()
    }

    /**
     * Use case: the media list is bound to a list another component owns, so a list produced by that
     * component reaches the reference and every property up to the root reports it.
     */
    @Test
    fun writesBoundMediaThroughBindingToPojoAndFiresUpToRoot() {
        val source = SimpleListProperty(FXCollections.observableArrayList(Medium(prompt = "bound")))
        property.mediaProperty.bind(source)
        recorder.reset()

        source.set(FXCollections.observableArrayList(Medium(prompt = "x"), Medium(prompt = "y")))

        assertEquals(listOf(Medium(prompt = "x"), Medium(prompt = "y")), reference.media)
        assertFiredUpToRoot("reference.media")
        property.mediaProperty.unbind()
    }

    /**
     * Use case: a medium is added to the history, another one removed and a third one replaced, so
     * every operation lands in the reference and is reported up to the root.
     */
    @Test
    fun writesMediaOperationsToPojoAndFiresUpToRoot() {
        recorder.reset()
        property.mediaProperty.add(Medium(prompt = "third"))
        assertEquals(listOf("first", "second", "third"), reference.media.map { it.prompt })
        assertFiredUpToRoot("reference.media")

        recorder.reset()
        property.mediaProperty.removeAt(0)
        assertEquals(listOf("second", "third"), reference.media.map { it.prompt })
        assertFiredUpToRoot("reference.media")

        recorder.reset()
        property.mediaProperty[0] = Medium(prompt = "replaced")
        assertEquals(listOf("replaced", "third"), reference.media.map { it.prompt })
        assertFiredUpToRoot("reference.media")
    }

    /**
     * Use case: the reference is changed by application code past the property - the list exchanged
     * and an entry changed in place - so nothing is noticed before the property is told to read the
     * reference again, and everything is delivered afterwards.
     */
    @Test
    fun readsChangesMadePastPropertyOnlyAfterRefresh() {
        reference.uuid = "ref-9"
        reference.media = mutableListOf(Medium(prompt = "other"))
        recorder.assertNoneFired("a write past the property")

        property.refresh()

        assertEquals("ref-9", property.uuid)
        assertEquals(listOf("other"), property.media.map { it.prompt })
        recorder.assertAllFired("refresh")
    }

    /**
     * Use case: a medium inside the list is changed in place, which a plain object does not report, so
     * the change shows up only when the property is told to read the reference again.
     */
    @Test
    fun noticesChangeInsideAnEntryOnlyAfterRefresh() {
        reference.media[0].prompt = "changed inside"
        recorder.assertNoneFired("a change inside an entry")

        property.refresh()

        assertEquals("changed inside", property.media[0].prompt)
        assertTrue(recorder.countOf("reference.media") > 0) { "the media list stayed quiet" }
        assertTrue(recorder.countOf("reference") > 0) { "the reference stayed quiet" }
    }

    /**
     * Use case: another reference is picked, so the id and the media list belong to the other object
     * afterwards, deliver its values and report the change up to the root.
     */
    @Test
    fun exchangeUpdatesAllFieldsAndFiresUpToRoot() {
        val other = otherReference()
        recorder.reset()

        property.value = other

        assertEquals("ref-2", property.uuid)
        assertEquals(other.media, property.media)
        recorder.assertAllFired("exchange against other values")
        property.uuidProperty.set("ref-3")
        assertEquals("ref-3", other.uuid)
    }

    /**
     * Use case: another reference carrying the same values is picked, so no field property reports a
     * change, while a later write reaches the new reference only and leaves the old one untouched.
     */
    @Test
    fun exchangeAgainstEqualValuesKeepsFieldsQuiet() {
        val fresh = newReference()
        recorder.reset()

        property.value = fresh

        recorder.assertNoneFired("exchange against equal values")
        property.uuidProperty.set("ref-9")
        property.mediaProperty.add(Medium(prompt = "added"))
        assertEquals("ref-9", fresh.uuid)
        assertEquals(3, fresh.media.size)
        assertEquals(newReference(), reference)
    }

    /**
     * Use case: no reference is picked, so the id answers with a neutral value, the list is empty and
     * what is written is dropped instead of creating a reference nobody asked for.
     */
    @Test
    fun readsNeutralValuesAndDropsWritesWhenReferenceIsAbsent() {
        property.value = null

        assertEquals(null, property.uuidProperty.get())
        assertTrue(property.mediaProperty.isEmpty())

        property.uuidProperty.set("lost")
        property.mediaProperty.add(Medium(prompt = "lost"))

        assertEquals(newReference(), reference)
    }

    /**
     * Use case: the accessors of the property are used instead of the field properties, so reading
     * and writing through them reaches the reference as well.
     */
    @Test
    fun accessorsDelegateToFieldProperties() {
        property.uuid = "ref-7"
        property.media = listOf(Medium(prompt = "only"))

        assertEquals(Reference(uuid = "ref-7", media = mutableListOf(Medium(prompt = "only"))), reference)
        assertEquals("ref-7", property.uuid)
        assertEquals(listOf(Medium(prompt = "only")), property.media)
    }

    /**
     * Use case: a reference is picked out of the reference list of the project, so the property built
     * for it already carries that very reference - not a copy of it - and a write through the property
     * lands on that object.
     */
    @Test
    fun factoryWrapsTheGivenReferenceInstance() {
        val picked = Reference(uuid = "ref-5")

        val built = ReferenceProperty.of(picked)

        assertSame(picked, built.get())
        assertEquals("ref-5", built.uuid)
        built.uuid = "ref-6"
        assertEquals("ref-6", picked.uuid)
    }

    /**
     * Use case: two references are picked one after another, so each call to [ReferenceProperty.of]
     * builds a property of its own instead of two callers sharing the same one.
     */
    @Test
    fun factoryBuildsAnIndependentPropertyPerCall() {
        val first = ReferenceProperty.of(Reference(uuid = "ref-a"))
        val second = ReferenceProperty.of(Reference(uuid = "ref-b"))

        assertNotSame(first, second)
        assertEquals("ref-a", first.uuid)
        assertEquals("ref-b", second.uuid)
    }
}
