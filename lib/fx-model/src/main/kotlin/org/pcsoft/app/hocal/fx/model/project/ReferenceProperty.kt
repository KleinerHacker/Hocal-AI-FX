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
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import org.pcsoft.app.hocal.fx.model.internal.BeanFields
import org.pcsoft.app.hocal.model.project.Medium
import org.pcsoft.app.hocal.model.project.Reference

/**
 * Property wrapping a single reference of a project - the one the user is working on for instance -
 * and offering every field of it as a property of its own.
 *
 * The wrapped object may be absent as long as no reference is picked, so every field property answers
 * with a neutral value and drops what is written to it until a reference sits behind this property.
 *
 * A reference is only one entry of a plain list, so this property is not built by the project file
 * itself; a caller that picked a reference out of that list builds one through [of] instead.
 */
class ReferenceProperty internal constructor() : SimpleObjectProperty<Reference?>() {

    private val fields = BeanFields<Reference> { fireValueChangedEvent() }

    /** Unique id of the reference as text, as a property of its own. */
    val uuidProperty: StringProperty = SimpleStringProperty()

    /** Unique id of the reference as text. */
    var uuid: String?
        get() = uuidProperty.get()
        set(value) {
            uuidProperty.set(value)
        }

    /** Media of the reference as a history, as a property of their own. */
    val mediaProperty: ListProperty<Medium> =
        SimpleListProperty(FXCollections.observableArrayList())

    /** Media of the reference as a history; the position in the list is the medium number. */
    var media: List<Medium>
        get() = mediaProperty.get()
        set(value) {
            mediaProperty.setAll(value)
        }

    init {
        fields.string(uuidProperty, "uuid")
        fields.list(mediaProperty, "media")

        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads the reference again.
     *
     * Use it after the reference was changed past this property: a plain object reports nothing, so
     * the change is only taken over by this call.
     */
    fun refresh() = fields.refresh()

    companion object {
        /**
         * Builds a reference property already bound to the given reference.
         *
         * The reference is a plain entry of [ProjectFileProperty.referencesProperty] and therefore
         * carries no property model of its own; this factory is the way a caller that picked one out
         * of that list reaches its fields as properties. The reference itself is wrapped, not copied.
         *
         * @param reference the reference to wrap
         * @return a reference property wrapping the given reference
         */
        @JvmStatic
        fun of(reference: Reference): ReferenceProperty {
            val property = ReferenceProperty()
            property.set(reference)
            return property
        }
    }

}
