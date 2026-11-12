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

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.pcsoft.app.hocal.fx.model.internal.BeanFields
import org.pcsoft.app.hocal.model.project.VirtualEntry

/**
 * Property wrapping an entry of the virtual directory structure - the one the user selected in the
 * project tree for instance - and offering every field of it as a property of its own.
 *
 * The wrapped object may be absent as long as no entry is picked, so every field property answers
 * with a neutral value and drops what is written to it until an entry sits behind this property.
 *
 * An entry is only one node in the children list of a folder, so this property is not built by a
 * folder property itself; a caller that picked an entry out of that list builds one through [of]
 * instead.
 */
class VirtualEntryProperty internal constructor() : SimpleObjectProperty<VirtualEntry?>() {

    private val fields = BeanFields<VirtualEntry> { fireValueChangedEvent() }

    /** Name of the entry, as a property of its own. */
    val nameProperty: StringProperty = SimpleStringProperty()

    // A property carries a name of its own, so the accessors of the wrapped field are given another
    // name on the JVM side - otherwise they would silently replace the one of the base class.
    /** Name of the entry. */
    var name: String?
        @JvmName("getEntryName") get() = nameProperty.get()
        @JvmName("setEntryName") set(value) {
            nameProperty.set(value)
        }

    /** UUID of the linked reference, as a property of its own. */
    val referenceProperty: StringProperty = SimpleStringProperty()

    /** UUID of the linked reference. */
    var reference: String?
        get() = referenceProperty.get()
        set(value) {
            referenceProperty.set(value)
        }

    init {
        fields.string(nameProperty, "name")
        fields.string(referenceProperty, "reference")

        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads the entry again.
     *
     * Use it after the entry was changed past this property: a plain object reports nothing, so the
     * change is only taken over by this call.
     */
    fun refresh() = fields.refresh()

    companion object {
        /**
         * Builds an entry property already bound to the given entry.
         *
         * The entry is a plain node of [VirtualFolderProperty.childrenProperty] and therefore carries
         * no property model of its own; this factory is the way a caller that picked one out of that
         * list reaches its fields as properties. The entry itself is wrapped, not copied.
         *
         * @param entry the entry to wrap
         * @return an entry property wrapping the given entry
         */
        @JvmStatic
        fun of(entry: VirtualEntry): VirtualEntryProperty {
            val property = VirtualEntryProperty()
            property.set(entry)
            return property
        }
    }

}
