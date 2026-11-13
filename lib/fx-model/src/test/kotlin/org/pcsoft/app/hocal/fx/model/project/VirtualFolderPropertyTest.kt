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
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.pcsoft.app.hocal.model.project.VirtualNode

/**
 * Developer tests for [VirtualFolderProperty] and its factory [VirtualFolderProperty.of].
 *
 * The property wraps one folder of the virtual directory structure and offers its name and its
 * children as properties of their own. A [ChangeRecorder] hangs a binding on the property itself and
 * on every field of it, and the tests assert that a change reaches every binding that has to know
 * about it. The children are plain nodes of a list: a change inside such a node is only picked up by
 * [VirtualFolderProperty.refresh].
 */
class VirtualFolderPropertyTest {

    private lateinit var folder: VirtualFolder
    private lateinit var property: VirtualFolderProperty
    private lateinit var recorder: ChangeRecorder

    private fun entry(name: String, reference: String): VirtualEntry {
        val result = VirtualEntry()
        result.name = name
        result.reference = reference
        return result
    }

    private fun folder(name: String, vararg children: VirtualNode): VirtualFolder {
        val result = VirtualFolder()
        result.name = name
        result.children = mutableListOf(*children)
        return result
    }

    private fun newFolder() = folder("Root", entry("Cats", "ref-1"), folder("Sub"))

    private fun otherFolder() = folder("Other", entry("Dogs", "ref-2"))

    @BeforeEach
    fun setUp() {
        folder = newFolder()
        property = VirtualFolderProperty()
        property.set(folder)

        recorder = ChangeRecorder()
        recorder.watch("folder", property)
        recorder.watch("folder.name", property.nameProperty)
        recorder.watch("folder.children", property.childrenProperty)
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
     * Use case: a folder is opened in the project tree, so the name and the children deliver the
     * values that already sit in the folder.
     */
    @Test
    fun readsInitialValuesFromPojo() {
        assertEquals("Root", property.nameProperty.get())
        assertEquals(newFolder().children, property.childrenProperty.get())
    }

    /**
     * Use case: the user renames the folder, so the name lands in the folder and the field property
     * as well as the property wrapping the folder report the change.
     */
    @Test
    fun writesNameThroughPropertyToPojoAndFiresUpToRoot() {
        property.nameProperty.set("Renamed")

        assertEquals("Renamed", folder.name)
        assertFiredUpToRoot("folder.name")
    }

    /**
     * Use case: the name is bound to the text field of the rename dialog, so every text that field
     * produces reaches the folder and every property up to the root reports it.
     */
    @Test
    fun writesNameThroughBindingToPojoAndFiresUpToRoot() {
        val source = SimpleObjectProperty("Draft")
        property.nameProperty.bind(source)
        recorder.reset()

        source.set("Renamed")

        assertEquals("Renamed", folder.name)
        assertFiredUpToRoot("folder.name")
        property.nameProperty.unbind()
    }

    /**
     * Use case: the children are bound to a list another component owns, so a list produced by that
     * component reaches the folder and every property up to the root reports it.
     */
    @Test
    fun writesBoundChildrenThroughBindingToPojoAndFiresUpToRoot() {
        val source = SimpleListProperty(FXCollections.observableArrayList<VirtualNode>(entry("Bound", "ref-0")))
        property.childrenProperty.bind(source)
        recorder.reset()

        source.set(FXCollections.observableArrayList<VirtualNode>(entry("x", "ref-8"), folder("y")))

        assertEquals(listOf(entry("x", "ref-8"), folder("y")), folder.children)
        assertFiredUpToRoot("folder.children")
        property.childrenProperty.unbind()
    }

    /**
     * Use case: the user adds a node to the folder, removes one and replaces a third one, so every
     * operation lands in the folder and is reported up to the root.
     */
    @Test
    fun writesChildOperationsToPojoAndFiresUpToRoot() {
        recorder.reset()
        property.childrenProperty.add(entry("Birds", "ref-3"))
        assertEquals(listOf("Cats", "Sub", "Birds"), folder.children.map { it.name })
        assertFiredUpToRoot("folder.children")

        recorder.reset()
        property.childrenProperty.removeAt(0)
        assertEquals(listOf("Sub", "Birds"), folder.children.map { it.name })
        assertFiredUpToRoot("folder.children")

        recorder.reset()
        property.childrenProperty[0] = entry("Replaced", "ref-4")
        assertEquals(listOf("Replaced", "Birds"), folder.children.map { it.name })
        assertFiredUpToRoot("folder.children")
    }

    /**
     * Use case: the folder is changed by application code past the property - name and list
     * exchanged - so nothing is noticed before the property is told to read the folder again, and
     * everything is delivered afterwards.
     */
    @Test
    fun readsChangesMadePastPropertyOnlyAfterRefresh() {
        folder.name = "Changed"
        folder.children = mutableListOf(entry("Other", "ref-5"))
        recorder.assertNoneFired("a write past the property")

        property.refresh()

        assertEquals("Changed", property.name)
        assertEquals(listOf("Other"), property.children.map { it.name })
        recorder.assertAllFired("refresh")
    }

    /**
     * Use case: a node inside the children is changed in place, which a plain object does not report,
     * so the change shows up only when the property is told to read the folder again.
     */
    @Test
    fun noticesChangeInsideAChildOnlyAfterRefresh() {
        (folder.children[0] as VirtualEntry).name = "Changed inside"
        recorder.assertNoneFired("a change inside a child")

        property.refresh()

        assertEquals("Changed inside", property.children[0].name)
        assertTrue(recorder.countOf("folder.children") > 0) { "the children stayed quiet" }
        assertTrue(recorder.countOf("folder") > 0) { "the folder stayed quiet" }
    }

    /**
     * Use case: another folder is opened, so the name and the children belong to the other object
     * afterwards, deliver its values and report the change up to the root.
     */
    @Test
    fun exchangeUpdatesAllFieldsAndFiresUpToRoot() {
        val other = otherFolder()
        recorder.reset()

        property.value = other

        assertEquals("Other", property.name)
        assertEquals(other.children, property.children)
        recorder.assertAllFired("exchange against other values")
        property.nameProperty.set("Third")
        assertEquals("Third", other.name)
    }

    /**
     * Use case: another folder carrying the same values is opened, so no field property reports a
     * change, while a later write reaches the new folder only and leaves the old one untouched.
     */
    @Test
    fun exchangeAgainstEqualValuesKeepsFieldsQuiet() {
        val fresh = newFolder()
        recorder.reset()

        property.value = fresh

        recorder.assertNoneFired("exchange against equal values")
        property.nameProperty.set("Renamed")
        property.childrenProperty.add(entry("Added", "ref-6"))
        assertEquals("Renamed", fresh.name)
        assertEquals(3, fresh.children.size)
        assertEquals(newFolder(), folder)
    }

    /**
     * Use case: no folder is opened, so the name answers with a neutral value, the children are empty
     * and what is written is dropped instead of creating a folder nobody asked for.
     */
    @Test
    fun readsNeutralValuesAndDropsWritesWhenFolderIsAbsent() {
        property.value = null

        assertEquals(null, property.nameProperty.get())
        assertTrue(property.childrenProperty.isEmpty())

        property.nameProperty.set("lost")
        property.childrenProperty.add(entry("lost", "ref-7"))

        assertEquals(newFolder(), folder)
    }

    /**
     * Use case: the accessors of the property are used instead of the field properties, so reading
     * and writing through them reaches the folder as well.
     */
    @Test
    fun accessorsDelegateToFieldProperties() {
        property.name = "Via accessor"
        property.children = listOf(entry("Only", "ref-8"))

        assertEquals(folder("Via accessor", entry("Only", "ref-8")), folder)
        assertEquals("Via accessor", property.name)
        assertEquals(listOf<VirtualNode>(entry("Only", "ref-8")), property.children)
    }

    /**
     * Use case: a subfolder is picked out of the children of a folder, so the property built for it
     * already carries that very folder - not a copy of it - and a write through the property lands on
     * that object.
     */
    @Test
    fun factoryWrapsTheGivenFolderInstance() {
        val picked = folder("Picked", entry("Cats", "ref-1"))

        val built = VirtualFolderProperty.of(picked)

        assertSame(picked, built.get())
        assertEquals("Picked", built.name)
        built.name = "Renamed"
        assertEquals("Renamed", picked.name)
    }

    /**
     * Use case: two folders are picked one after another, so each call to [VirtualFolderProperty.of]
     * builds a property of its own instead of two callers sharing the same one.
     */
    @Test
    fun factoryBuildsAnIndependentPropertyPerCall() {
        val first = VirtualFolderProperty.of(folder("first"))
        val second = VirtualFolderProperty.of(folder("second"))

        assertNotSame(first, second)
        assertEquals("first", first.name)
        assertEquals("second", second.name)
    }
}
