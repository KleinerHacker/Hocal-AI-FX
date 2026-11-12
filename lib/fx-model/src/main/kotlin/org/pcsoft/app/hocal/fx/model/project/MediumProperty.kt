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

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import org.pcsoft.app.hocal.fx.model.internal.BeanFields
import org.pcsoft.app.hocal.model.project.Medium

/**
 * Property wrapping a single medium of a reference - the one the user is looking at for instance -
 * and offering every field of it as a property of its own.
 *
 * The wrapped object may be absent as long as no medium is picked, so every field property answers
 * with a neutral value and drops what is written to it until a medium sits behind this property.
 *
 * A medium is only one entry of the media list of a reference, so this property is not built by a
 * reference property itself; a caller that picked a medium out of that list builds one through [of]
 * instead. The parameters are a plain map without a property model of its own: a change inside that
 * map is only picked up by [refresh].
 */
class MediumProperty internal constructor() : SimpleObjectProperty<Medium?>() {

    private val fields = BeanFields<Medium> { fireValueChangedEvent() }

    /** Prompt the medium was generated with, as a property of its own; `null` if empty. */
    val promptProperty: StringProperty = SimpleStringProperty()

    /** Prompt the medium was generated with, `null` if empty. */
    var prompt: String?
        get() = promptProperty.get()
        set(value) {
            promptProperty.set(value)
        }

    /** Id of the model the medium was generated with, as a property of its own; `null` if empty. */
    val modelProperty: StringProperty = SimpleStringProperty()

    /** Id of the model the medium was generated with, `null` if empty. */
    var model: String?
        get() = modelProperty.get()
        set(value) {
            modelProperty.set(value)
        }

    /** Id of the flow the medium was generated with, as a property of its own; `null` if empty. */
    val flowProperty: StringProperty = SimpleStringProperty()

    /** Id of the flow the medium was generated with, `null` if empty. */
    var flow: String?
        get() = flowProperty.get()
        set(value) {
            flowProperty.set(value)
        }

    /** The seed used, as a property of its own; `null` if empty. */
    val seedProperty: ObjectProperty<Long?> = SimpleObjectProperty<Long?>()

    /** The seed used, `null` if empty. */
    var seed: Long?
        get() = seedProperty.get()
        set(value) {
            seedProperty.set(value)
        }

    /** Values of the editable flow parameters by parameter id, as a property of their own. */
    val parametersProperty: ObjectProperty<MutableMap<String, String>?> =
        SimpleObjectProperty<MutableMap<String, String>?>()

    /** Values of the editable flow parameters by parameter id, each one stored as text. */
    var parameters: MutableMap<String, String>?
        get() = parametersProperty.get()
        set(value) {
            parametersProperty.set(value)
        }

    init {
        fields.string(promptProperty, "prompt")
        fields.string(modelProperty, "model")
        fields.string(flowProperty, "flow")
        fields.reference(seedProperty, "seed")
        fields.reference(parametersProperty, "parameters")

        addListener { _, _, newValue -> fields.rebind(newValue) }
        fields.rebind(get())
    }

    /**
     * Reads the medium again.
     *
     * Use it after the medium was changed past this property - a change inside the parameters map
     * included: a plain object reports nothing, so the change is only taken over by this call.
     */
    fun refresh() = fields.refresh()

    companion object {
        /**
         * Builds a medium property already bound to the given medium.
         *
         * The medium is a plain entry of [ReferenceProperty.mediaProperty] and therefore carries no
         * property model of its own; this factory is the way a caller that picked one out of that list
         * reaches its fields as properties. The medium itself is wrapped, not copied.
         *
         * @param medium the medium to wrap
         * @return a medium property wrapping the given medium
         */
        @JvmStatic
        fun of(medium: Medium): MediumProperty {
            val property = MediumProperty()
            property.set(medium)
            return property
        }
    }

}
