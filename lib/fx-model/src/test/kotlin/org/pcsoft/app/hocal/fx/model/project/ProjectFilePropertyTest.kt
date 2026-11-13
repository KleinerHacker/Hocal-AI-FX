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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.pcsoft.app.hocal.fx.model.ChangeRecorder
import org.pcsoft.app.hocal.model.project.ProjectData
import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.project.Reference
import org.pcsoft.app.hocal.model.project.VirtualEntry
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.pcsoft.app.hocal.model.project.VirtualNode
import java.time.Instant

/**
 * Developer tests for [ProjectFileProperty], the root of the property tree of a project.
 *
 * The tests prove the tree through the root, not only per level: a [ChangeRecorder] hangs a binding on
 * the root, on the nested property of every part and on every single field below them, named after the
 * path from the root (`file`, `file.project`, `file.project.name`, `file.tree`, `file.tree.name`, ...).
 * A change of any field must be reported by every property on its path up to the root, an exchanged
 * object must reach every field below it, and an exchange against equal values must leave the whole
 * tree quiet. The scalar fields of every level are driven through one table, so no field can be
 * forgotten; the lists have tests of their own.
 */
@Suppress("UNCHECKED_CAST")
class ProjectFilePropertyTest {

    /** One scalar field somewhere in the tree: how to reach its property and its value in the POJO. */
    private class FieldCase(
        val path: String,
        val access: (ProjectFileProperty) -> Property<Any?>,
        val read: (ProjectFile) -> Any?,
        val write: (ProjectFile, Any?) -> Unit,
        val initial: Any?,
        val changed: Any?,
        val neutral: Any?,
    )

    private fun fieldCase(
        path: String,
        access: (ProjectFileProperty) -> Property<*>,
        read: (ProjectFile) -> Any?,
        write: (ProjectFile, Any?) -> Unit,
        initial: Any?,
        changed: Any?,
        neutral: Any?,
    ) = FieldCase(path, { target: ProjectFileProperty -> access(target) as Property<Any?> }, read, write, initial, changed, neutral)

    private val cases: List<FieldCase> = listOf(
        fieldCase("file.project.name", { it.projectProperty.nameProperty }, { it.project.name }, { f, v -> f.project.name = v as String }, "Sunset", "Sunrise", null),
        fieldCase("file.project.description", { it.projectProperty.descriptionProperty }, { it.project.description }, { f, v -> f.project.description = v as String }, "Warm light", "Cold light", null),
        fieldCase("file.project.uuid", { it.projectProperty.uuidProperty }, { it.project.uuid }, { f, v -> f.project.uuid = v as String }, "uuid-1", "uuid-2", null),
        fieldCase("file.project.author", { it.projectProperty.authorProperty }, { it.project.author }, { f, v -> f.project.author = v as String }, "Jane Doe", "John Roe", null),
        fieldCase("file.project.created", { it.projectProperty.createdProperty }, { it.project.created }, { f, v -> f.project.created = v as Instant? }, Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-02-02T11:00:00Z"), null),
        fieldCase("file.project.modified", { it.projectProperty.modifiedProperty }, { it.project.modified }, { f, v -> f.project.modified = v as Instant? }, Instant.parse("2026-01-05T10:00:00Z"), Instant.parse("2026-02-06T11:00:00Z"), null),
        fieldCase("file.project.formatVersion", { it.projectProperty.formatVersionProperty }, { it.project.formatVersion }, { f, v -> f.project.formatVersion = v as Int }, 1, 2, 0),
        fieldCase("file.project.applicationVersion", { it.projectProperty.applicationVersionProperty }, { it.project.applicationVersion }, { f, v -> f.project.applicationVersion = v as String }, "1.0.0", "1.1.0", null),
        fieldCase("file.tree.name", { it.treeProperty.nameProperty }, { it.tree.name }, { f, v -> f.tree.name = v as String }, "Root", "Top", null),
    )

    private lateinit var file: ProjectFile
    private lateinit var property: ProjectFileProperty
    private lateinit var recorder: ChangeRecorder

    private fun entry(name: String, reference: String): VirtualEntry {
        val result = VirtualEntry()
        result.name = name
        result.reference = reference
        return result
    }

    /** Builds a project file carrying the initial value of every field of the tree. */
    private fun newFile(): ProjectFile {
        val result = ProjectFile()
        cases.forEach { it.write(result, it.initial) }
        result.project.tags = mutableListOf("alpha", "beta")
        result.references = mutableListOf(Reference(uuid = "ref-1"))
        result.tree.children = mutableListOf<VirtualNode>(entry("Cats", "ref-1"))
        return result
    }

    /** Builds a project file carrying another value in every field of the tree. */
    private fun otherFile(): ProjectFile {
        val result = ProjectFile()
        cases.forEach { it.write(result, it.changed) }
        result.project.tags = mutableListOf("gamma")
        result.references = mutableListOf(Reference(uuid = "ref-2"), Reference(uuid = "ref-3"))
        result.tree.children = mutableListOf<VirtualNode>(entry("Dogs", "ref-2"))
        return result
    }

    @BeforeEach
    fun setUp() {
        file = newFile()
        property = ProjectFileProperty(file)

        recorder = ChangeRecorder()
        recorder.watch("file", property)
        recorder.watch("file.project", property.projectProperty)
        cases.forEach { recorder.watch(it.path, it.access(property)) }
        recorder.watch("file.project.tags", property.projectProperty.tagsProperty)
        recorder.watch("file.references", property.referencesProperty)
        recorder.watch("file.tree", property.treeProperty)
        recorder.watch("file.tree.children", property.treeProperty.childrenProperty)
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

    /** Asserts that every watched path matching [selector] reported at least one change. */
    private fun assertFired(selector: (String) -> Boolean) {
        recorder.watchedNames().filter(selector).forEach {
            assertTrue(recorder.countOf(it) > 0) { "$it stayed quiet" }
        }
    }

    /** Asserts that every watched path matching [selector] stayed quiet. */
    private fun assertQuiet(selector: (String) -> Boolean) {
        recorder.watchedNames().filter(selector).forEach {
            assertEquals(0, recorder.countOf(it), it)
        }
    }

    /**
     * Use case: no project is open yet, so the property is built without a project file, wraps nothing
     * and every level of the tree is empty - and a project file handed over at construction time is
     * wrapped right away.
     */
    @Test
    fun wrapsTheProjectFileGivenAtConstructionOrNone() {
        val empty = ProjectFileProperty()

        assertNull(empty.get())
        assertNull(empty.projectProperty.get())
        assertNull(empty.treeProperty.get())
        assertTrue(empty.referencesProperty.isEmpty())
        assertSame(file, property.get())
    }

    /**
     * Use case: the project is read from its file before the user interface is built, so every level
     * of the tree delivers the value that already sits in the project file - and the nested properties
     * wrap the very objects of the file, not copies of them.
     */
    @Test
    fun readsEveryLevelOfTheTreeRightAway() {
        cases.forEach { assertEquals(it.initial, it.access(property).value, it.path) }
        assertEquals(listOf("alpha", "beta"), property.projectProperty.tagsProperty.get())
        assertEquals(newFile().references, property.referencesProperty.get())
        assertEquals(newFile().tree.children, property.treeProperty.childrenProperty.get())
        assertSame(file.project, property.projectProperty.get())
        assertSame(file.tree, property.treeProperty.get())
    }

    /**
     * Use case: the user edits a field on any level of the tree, so the value lands in the project
     * file and every property on the path from that field up to the root reports the change.
     */
    @Test
    fun writesThroughPropertyToPojoAndFiresUpToRoot() {
        cases.forEach {
            recorder.reset()

            it.access(property).value = it.changed

            assertEquals(it.changed, it.read(file), it.path)
            assertFiredUpToRoot(it.path)
        }
    }

    /**
     * Use case: a field on any level of the tree is bound to the control the user types into, so every
     * value that control produces reaches the project file and every property up to the root reports
     * it.
     */
    @Test
    fun writesThroughBindingToPojoAndFiresUpToRoot() {
        cases.forEach {
            val source = SimpleObjectProperty<Any?>(it.initial)
            val target = it.access(property)
            target.bind(source)
            recorder.reset()

            source.set(it.changed)

            assertEquals(it.changed, it.read(file), it.path)
            assertFiredUpToRoot(it.path)
            target.unbind()
        }
    }

    /**
     * Use case: the user adds an entry to a list on any level of the tree - a tag, a reference, a
     * node of the root folder - so the entry lands in the project file and every property up to the
     * root reports the change.
     */
    @Test
    fun writesListOperationsToPojoAndFiresUpToRoot() {
        recorder.reset()
        property.projectProperty.tagsProperty.add("delta")
        assertEquals(listOf("alpha", "beta", "delta"), file.project.tags)
        assertFiredUpToRoot("file.project.tags")

        recorder.reset()
        property.referencesProperty.add(Reference(uuid = "ref-9"))
        assertEquals(listOf("ref-1", "ref-9"), file.references.map { it.uuid })
        assertFiredUpToRoot("file.references")

        recorder.reset()
        property.treeProperty.childrenProperty.add(entry("Birds", "ref-9"))
        assertEquals(listOf("Cats", "Birds"), file.tree.children.map { it.name })
        assertFiredUpToRoot("file.tree.children")
    }

    /**
     * Use case: the lists on every level of the tree are bound to lists other components own, so a
     * list produced by such a component reaches the project file and every property up to the root
     * reports it.
     */
    @Test
    fun writesBoundListsToPojoAndFiresUpToRoot() {
        val tags = SimpleListProperty(FXCollections.observableArrayList("omega"))
        val references = SimpleListProperty(FXCollections.observableArrayList(Reference(uuid = "ref-0")))
        val children = SimpleListProperty(FXCollections.observableArrayList<VirtualNode>(entry("Bound", "ref-0")))
        property.projectProperty.tagsProperty.bind(tags)
        property.referencesProperty.bind(references)
        property.treeProperty.childrenProperty.bind(children)

        recorder.reset()
        tags.set(FXCollections.observableArrayList("sigma"))
        assertEquals(listOf("sigma"), file.project.tags)
        assertFiredUpToRoot("file.project.tags")

        recorder.reset()
        references.set(FXCollections.observableArrayList(Reference(uuid = "ref-8")))
        assertEquals(listOf(Reference(uuid = "ref-8")), file.references)
        assertFiredUpToRoot("file.references")

        recorder.reset()
        children.set(FXCollections.observableArrayList<VirtualNode>(entry("Bound too", "ref-8")))
        assertEquals(listOf<VirtualNode>(entry("Bound too", "ref-8")), file.tree.children)
        assertFiredUpToRoot("file.tree.children")

        property.projectProperty.tagsProperty.unbind()
        property.referencesProperty.unbind()
        property.treeProperty.childrenProperty.unbind()
    }

    /**
     * Use case: the project file is changed by application code past the property - a field on every
     * level and every list - so the property is told to read it again and every level delivers the
     * current value afterwards, reports it and reaches the root.
     */
    @Test
    fun readsChangesMadePastPropertyAfterRefresh() {
        cases.forEach { it.write(file, it.changed) }
        file.project.tags = mutableListOf("delta")
        file.references = mutableListOf(Reference(uuid = "ref-9"))
        file.tree.children = mutableListOf<VirtualNode>(entry("Birds", "ref-9"))
        recorder.assertNoneFired("a write past the property")

        property.refresh()

        cases.forEach { assertEquals(it.changed, it.access(property).value, it.path) }
        assertEquals(listOf("delta"), property.projectProperty.tagsProperty.get())
        assertEquals(listOf("ref-9"), property.references.map { it.uuid })
        assertEquals(listOf("Birds"), property.treeProperty.children.map { it.name })
        recorder.assertAllFired("refresh")
    }

    /**
     * Use case: the nested objects of the project file are replaced by application code past the
     * property, so after the property is told to read the file again the nested properties wrap the
     * new objects and every level below them delivers their values.
     */
    @Test
    fun readsNestedObjectsReplacedPastPropertyAfterRefresh() {
        val other = otherFile()
        file.project = other.project
        file.tree = other.tree
        recorder.assertNoneFired("a write past the property")

        property.refresh()

        assertSame(other.project, property.projectProperty.get())
        assertSame(other.tree, property.treeProperty.get())
        cases.forEach { assertEquals(it.changed, it.access(property).value, it.path) }
        assertFired { it == "file" || it.startsWith("file.project") || it.startsWith("file.tree") }
        assertQuiet { it.startsWith("file.references") }
    }

    /**
     * Use case: another project file is loaded, so every level of the tree belongs to the other file
     * afterwards, delivers its values, and every property up to the root reports the change.
     */
    @Test
    fun exchangeUpdatesEveryLevelAndFiresUpToRoot() {
        val other = otherFile()
        recorder.reset()

        property.value = other

        cases.forEach { assertEquals(it.changed, it.access(property).value, it.path) }
        assertEquals(listOf("gamma"), property.projectProperty.tagsProperty.get())
        assertEquals(other.references, property.referencesProperty.get())
        assertEquals(other.tree.children, property.treeProperty.childrenProperty.get())
        assertSame(other.project, property.projectProperty.get())
        assertSame(other.tree, property.treeProperty.get())
        recorder.assertAllFired("exchange against other values")
        cases.forEach {
            it.access(property).value = it.initial
            assertEquals(it.initial, it.read(other), it.path)
        }
    }

    /**
     * Use case: the project file is loaded again and carries the same values as before, so no property
     * of the whole tree reports a change, while a later write reaches the new file only and leaves
     * the one left behind untouched.
     */
    @Test
    fun exchangeAgainstEqualValuesKeepsTheWholeTreeQuiet() {
        val fresh = newFile()
        recorder.reset()

        property.value = fresh

        recorder.assertNoneFired("exchange against equal values")
        cases.forEach { it.access(property).value = it.changed }
        cases.forEach {
            assertEquals(it.changed, it.read(fresh), it.path)
            assertEquals(it.initial, it.read(file), it.path)
        }
    }

    /**
     * Use case: the project part of the tree is exchanged for another project while the rest is
     * watched, so the project and everything below it reports the change up to the root, while the
     * references and the folder tree stay quiet and a later write reaches the new project only.
     */
    @Test
    fun observedExchangeOfTheProjectAgainstAnotherOneFiresOnlyItsPath() {
        val otherProject = otherFile().project
        recorder.reset()

        property.projectProperty.value = otherProject

        assertSame(otherProject, file.project)
        assertFired { it == "file" || it.startsWith("file.project") }
        assertQuiet { it.startsWith("file.references") || it.startsWith("file.tree") }
        property.projectProperty.nameProperty.set("Renamed")
        assertEquals("Renamed", otherProject.name)
        assertSame(otherProject, file.project)
    }

    /**
     * Use case: the project part of the tree is exchanged for another project carrying the same
     * values, so no property of the whole tree reports a change, while a later write reaches the new
     * project.
     */
    @Test
    fun observedExchangeOfTheProjectAgainstEqualOneKeepsTheWholeTreeQuiet() {
        val equalProject = newFile().project
        recorder.reset()

        property.projectProperty.value = equalProject

        recorder.assertNoneFired("observed exchange against an equal project")
        property.projectProperty.nameProperty.set("Renamed")
        assertEquals("Renamed", equalProject.name)
    }

    /**
     * Use case: no project file is open, so every field of every level answers with a neutral value,
     * the nested properties wrap nothing and what is written is dropped instead of creating a file
     * nobody asked for.
     */
    @Test
    fun readsNeutralValuesAndDropsWritesWhenFileIsAbsent() {
        property.value = null

        cases.forEach { assertEquals(it.neutral, it.access(property).value, it.path) }
        assertNull(property.projectProperty.get())
        assertNull(property.treeProperty.get())
        assertTrue(property.projectProperty.tagsProperty.isEmpty())
        assertTrue(property.referencesProperty.isEmpty())
        assertTrue(property.treeProperty.childrenProperty.isEmpty())

        cases.forEach { it.access(property).value = it.changed }
        property.projectProperty.tagsProperty.add("lost")
        property.referencesProperty.add(Reference(uuid = "lost"))
        property.treeProperty.childrenProperty.add(entry("lost", "lost"))

        assertEquals(newFile(), file)
    }

    /**
     * Use case: a reference picked out of the reference list is changed through the property built by
     * its factory, which writes into the very object of the list - a plain list entry reports nothing,
     * so the change is only noticed once the property is told to read the file again.
     */
    @Test
    fun noticesChangeInsideAListEntryOnlyAfterRefresh() {
        val picked = property.references[0]

        ReferenceProperty.of(picked).uuid = "ref-9"

        assertEquals("ref-9", file.references[0].uuid)
        recorder.assertNoneFired("a change inside a list entry")

        property.refresh()

        assertTrue(recorder.countOf("file.references") > 0) { "file.references stayed quiet" }
        assertTrue(recorder.countOf("file") > 0) { "file stayed quiet" }
    }

    /**
     * Use case: the accessors of the property are used instead of the nested properties, so reading
     * and writing through them reaches the project file as well.
     */
    @Test
    fun accessorsDelegateToNestedProperties() {
        val newProject = ProjectData(name = "Via accessor")
        val newTree = VirtualFolder()
        newTree.name = "Via accessor"

        property.project = newProject
        property.references = listOf(Reference(uuid = "ref-7"))
        property.tree = newTree

        assertSame(newProject, file.project)
        assertEquals(listOf(Reference(uuid = "ref-7")), file.references)
        assertSame(newTree, file.tree)
        assertSame(newProject, property.project)
        assertEquals(listOf(Reference(uuid = "ref-7")), property.references)
        assertSame(newTree, property.tree)
    }
}
