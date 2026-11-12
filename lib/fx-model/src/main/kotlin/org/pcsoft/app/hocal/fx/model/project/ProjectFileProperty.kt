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

import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import org.pcsoft.app.hocal.fx.model.internal.BeanFields
import org.pcsoft.app.hocal.model.project.ProjectData
import org.pcsoft.app.hocal.model.project.ProjectFile
import org.pcsoft.app.hocal.model.project.Reference
import org.pcsoft.app.hocal.model.project.VirtualFolder

/**
 * Property holding the project file the user is working on and offering every part of that object -
 * and every field of the objects nested in those parts - as a property of its own.
 *
 * The general project data and the root folder of the virtual directory structure are reached through
 * [projectProperty] and [treeProperty], each handed out with its own type, so a user interface binds
 * to any field of them directly. The references are a plain list of objects, because the user arranges
 * them as a whole; a caller that picked one out of that list reaches its fields as properties through
 * [ReferenceProperty.of].
 *
 * A change travels through the whole object tree in both directions: a changed field is reported by
 * every property between that field and this one, and an exchanged object - a project file that was
 * loaded again for instance - is passed down to the properties of its parts, so a control bound to
 * any level shows the current value.
 *
 * As long as no project file sits behind this property every field property answers with a neutral
 * value and drops what is written to it, so a user interface may be built before a project is opened.
 *
 * @param projectFile The project file to wrap at the start, `null` for none.
 */
class ProjectFileProperty(projectFile: ProjectFile? = null) : SimpleObjectProperty<ProjectFile?>(projectFile) {

    private val fields = BeanFields<ProjectFile> { fireValueChangedEvent() }

    /** General data of the project, as a property of its own. */
    val projectProperty: ProjectDataProperty = ProjectDataProperty()

    /** General data of the project. */
    var project: ProjectData?
        get() = projectProperty.get()
        set(value) {
            projectProperty.set(value)
        }

    /** References of the project in their user defined order, as a property of their own. */
    val referencesProperty: ListProperty<Reference> =
        SimpleListProperty(FXCollections.observableArrayList())

    /** References of the project in their user defined order. */
    var references: List<Reference>
        get() = referencesProperty.get()
        set(value) {
            referencesProperty.setAll(value)
        }

    /** Root folder of the virtual directory structure, as a property of its own. */
    val treeProperty: VirtualFolderProperty = VirtualFolderProperty()

    /** Root folder of the virtual directory structure. */
    var tree: VirtualFolder?
        get() = treeProperty.get()
        set(value) {
            treeProperty.set(value)
        }

    init {
        fields.model(projectProperty, "project", projectProperty::refresh)
        fields.list(referencesProperty, "references")
        fields.model(treeProperty, "tree", treeProperty::refresh)

        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads the project file and everything nested in it again.
     *
     * Use it after the project file was changed past this property: a plain object reports nothing, so
     * the change is only taken over by this call.
     */
    fun refresh() = fields.refresh()

}
