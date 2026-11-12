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

import javafx.beans.property.IntegerProperty
import javafx.beans.property.ListProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.pcsoft.app.hocal.fx.model.internal.BeanFields
import org.pcsoft.app.hocal.model.project.ProjectData
import java.time.Instant

/**
 * Property wrapping the general data of a project and offering every field of it as a property of its
 * own.
 *
 * The wrapped object may be absent as long as no project is open, so every field property answers
 * with a neutral value and drops what is written to it until a project sits behind this property.
 *
 * The data is built by [ProjectFileProperty] alone and therefore carries an internal constructor.
 */
class ProjectDataProperty internal constructor() : SimpleObjectProperty<ProjectData?>() {

    private val fields = BeanFields<ProjectData> { fireValueChangedEvent() }

    /** Name of the project, as a property of its own. */
    val nameProperty: StringProperty = SimpleStringProperty()

    // A property carries a name of its own, so the accessors of the wrapped field are given another
    // name on the JVM side - otherwise they would silently replace the one of the base class.
    /** Name of the project. */
    var name: String?
        @JvmName("getProjectName") get() = nameProperty.get()
        @JvmName("setProjectName") set(value) {
            nameProperty.set(value)
        }

    /** Free description of the project, as a property of its own. */
    val descriptionProperty: StringProperty = SimpleStringProperty()

    /** Free description of the project. */
    var description: String?
        get() = descriptionProperty.get()
        set(value) {
            descriptionProperty.set(value)
        }

    /** Unique id of the project as text, as a property of its own. */
    val uuidProperty: StringProperty = SimpleStringProperty()

    /** Unique id of the project as text. */
    var uuid: String?
        get() = uuidProperty.get()
        set(value) {
            uuidProperty.set(value)
        }

    /** Author of the project, as a property of its own. */
    val authorProperty: StringProperty = SimpleStringProperty()

    /** Author of the project. */
    var author: String?
        get() = authorProperty.get()
        set(value) {
            authorProperty.set(value)
        }

    /** Time the project was created, as a property of its own; `null` while unknown. */
    val createdProperty: ObjectProperty<Instant?> = SimpleObjectProperty<Instant?>()

    /** Time the project was created, `null` while unknown. */
    var created: Instant?
        get() = createdProperty.get()
        set(value) {
            createdProperty.set(value)
        }

    /** Time of the last change of the project, as a property of its own; `null` while unknown. */
    val modifiedProperty: ObjectProperty<Instant?> = SimpleObjectProperty<Instant?>()

    /** Time of the last change of the project, `null` while unknown. */
    var modified: Instant?
        get() = modifiedProperty.get()
        set(value) {
            modifiedProperty.set(value)
        }

    /** Tags of the project, as a property of their own. */
    val tagsProperty: ListProperty<String> =
        SimpleListProperty(FXCollections.observableArrayList())

    /** Tags of the project. */
    var tags: List<String>
        get() = tagsProperty.get()
        set(value) {
            tagsProperty.setAll(value)
        }

    /** Version of the file format the project was written with, as a property of its own. */
    val formatVersionProperty: IntegerProperty = SimpleIntegerProperty()

    /** Version of the file format the project was written with. */
    var formatVersion: Int
        get() = formatVersionProperty.get()
        set(value) {
            formatVersionProperty.set(value)
        }

    /** Version of the application that saved the file last, as a property of its own. */
    val applicationVersionProperty: StringProperty = SimpleStringProperty()

    /** Version of the application that saved the file last. */
    var applicationVersion: String?
        get() = applicationVersionProperty.get()
        set(value) {
            applicationVersionProperty.set(value)
        }

    init {
        fields.string(nameProperty, "name")
        fields.string(descriptionProperty, "description")
        fields.string(uuidProperty, "uuid")
        fields.string(authorProperty, "author")
        fields.reference(createdProperty, "created")
        fields.reference(modifiedProperty, "modified")
        fields.list(tagsProperty, "tags")
        fields.integer(formatVersionProperty, "formatVersion")
        fields.string(applicationVersionProperty, "applicationVersion")

        // An invalidation listener and not a change listener: a change listener stays silent when the new object
        // equals the old one, and the fields would then stay tied to the object that was left behind.
        addListener { fields.rebind(get()) }
        fields.rebind(get())
    }

    /**
     * Reads the project data again.
     *
     * Use it after the data was changed past this property: a plain object reports nothing, so the
     * change is only taken over by this call.
     */
    fun refresh() = fields.refresh()

}
