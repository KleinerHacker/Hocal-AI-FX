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
import org.pcsoft.app.hocal.model.project.VirtualFolder
import org.pcsoft.app.hocal.model.project.VirtualNode

/**
 * Property wrapping a folder of the virtual directory structure - the root folder of a project or
 * the one the user opened - and offering every field of it as a property of its own.
 *
 * The wrapped object may be absent as long as no folder is picked, so every field property answers
 * with a neutral value and drops what is written to it until a folder sits behind this property.
 *
 * The children are a plain list of nodes, because the user arranges them as a whole. A subfolder is
 * only one entry of that list, so this property is not built for it by the folder itself; a caller
 * that picked a folder out of the list builds one through [of] instead, an entry through
 * [VirtualEntryProperty.of]. A change inside a node of the list is only picked up by [refresh].
 *
 * The root folder is built by [ProjectFileProperty] alone and therefore carries an internal
 * constructor.
 */
class VirtualFolderProperty internal constructor() : SimpleObjectProperty<VirtualFolder?>() {

    private val fields = BeanFields<VirtualFolder> { fireValueChangedEvent() }

    /** Name of the folder, as a property of its own. */
    val nameProperty: StringProperty = SimpleStringProperty()

    // A property carries a name of its own, so the accessors of the wrapped field are given another
    // name on the JVM side - otherwise they would silently replace the one of the base class.
    /** Name of the folder. */
    var name: String?
        @JvmName("getFolderName") get() = nameProperty.get()
        @JvmName("setFolderName") set(value) {
            nameProperty.set(value)
        }

    /** Children of the folder, folders and entries, as a property of their own. */
    val childrenProperty: ListProperty<VirtualNode> =
        SimpleListProperty(FXCollections.observableArrayList())

    /** Children of the folder, folders and entries. */
    var children: List<VirtualNode>
        get() = childrenProperty.get()
        set(value) {
            childrenProperty.setAll(value)
        }

    init {
        fields.string(nameProperty, "name")
        fields.list(childrenProperty, "children")

        // An invalidation listener and not a change listener: a change listener stays silent when the new object
        // equals the old one, and the fields would then stay tied to the object that was left behind.
        addListener { fields.rebind(get()) }
        fields.rebind(get())
    }

    /**
     * Reads the folder again.
     *
     * Use it after the folder - or a node inside it - was changed past this property: a plain object
     * reports nothing, so the change is only taken over by this call.
     */
    fun refresh() = fields.refresh()

    companion object {
        /**
         * Builds a folder property already bound to the given folder.
         *
         * The folder is a plain entry of [childrenProperty] of its parent folder and therefore carries
         * no property model of its own; this factory is the way a caller that picked one out of that
         * list reaches its fields as properties. The folder itself is wrapped, not copied.
         *
         * @param folder the folder to wrap
         * @return a folder property wrapping the given folder
         */
        @JvmStatic
        fun of(folder: VirtualFolder): VirtualFolderProperty {
            val property = VirtualFolderProperty()
            property.set(folder)
            return property
        }
    }

}
