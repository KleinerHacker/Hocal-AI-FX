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

package org.pcsoft.app.hocal.fx.model.internal

import javafx.beans.InvalidationListener
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.ListProperty
import javafx.beans.property.LongProperty
import javafx.beans.property.Property
import javafx.beans.property.StringProperty
import javafx.beans.property.adapter.JavaBeanBooleanPropertyBuilder
import javafx.beans.property.adapter.JavaBeanDoublePropertyBuilder
import javafx.beans.property.adapter.JavaBeanIntegerPropertyBuilder
import javafx.beans.property.adapter.JavaBeanLongPropertyBuilder
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder
import javafx.beans.property.adapter.JavaBeanProperty
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder

/**
 * The field properties of one property model and their connection to the object behind it.
 *
 * A property model registers every field it offers here once, in the order the fields are declared,
 * and calls [rebind] whenever the object it wraps is exchanged. From then on each field property is
 * tied to the matching field of that object through a `JavaBean*Property`, so a write on either side
 * reaches the other one without the model holding a value of its own.
 *
 * The name a field is registered under is the name of the property on the wrapped object - the
 * builders of `javafx.beans.property.adapter` look up its accessors by that name. A name that does
 * not exist is only noticed when the object is bound, which is why every field needs a test.
 *
 * An adapter reads the field it is tied to when it is told to, not when the field changes: a plain
 * object reports nothing. A value that was written on the wrapped object past the model is therefore
 * taken over by [refresh] and only then. A field carrying a model object of its own is registered
 * through [model] and hands that call on into its own fields, so one [refresh] covers the whole tree
 * below this model and a nested model cannot be forgotten.
 *
 * [fireEvent] lets the model report a change of one of its fields as a change of its own, so a change
 * deep in the object tree reaches everyone listening further up. A field taking over the value of a
 * freshly bound or freshly read object is not reported that way: the model announces that itself.
 */
internal class BeanFields<B : Any>(private val fireEvent: () -> Unit) {

    // The registered fields in the order they were declared: how each one connects itself to the
    // object it is handed, and how it reads that object again.
    private val binders = mutableListOf<(B?) -> Unit>()
    private val readers = mutableListOf<() -> Unit>()

    // Guards the parent while the fields take over the values of the wrapped object, so such an
    // alignment is not reported as a change of its own.
    private var aligning = false

    // The object every binder is tied to right now, and whether [rebind] ran at all yet - `null` is a
    // legitimate wrapped object of its own (no object at all), so a flag is needed to tell that apart
    // from "never bound". Tracked by reference, not by equality: two structurally equal objects - two
    // freshly created default parts, for instance - are still a genuine exchange the fields must retie
    // themselves to, while the repeated [rebind] calls a nested field's own change triggers - every
    // field this model reports as changed reaches every listener above it, [rebind] included, since the
    // trigger for it is an `InvalidationListener` that cannot tell a real exchange from a report of a
    // field mutated in place - must stay a no-op instead of rebuilding every field's adapter again.
    private var isBound = false
    private var boundTo: B? = null

    /**
     * Ties [property] to the text field named [name] of the wrapped object.
     *
     * The property carries `null` as long as no object is bound.
     */
    fun string(property: StringProperty, name: String) =
        value(property, null, null) { bean ->
            JavaBeanStringPropertyBuilder.create().bean(bean).name(name).build()
        }

    /**
     * Ties [property] to the whole number field named [name] of the wrapped object.
     *
     * The property carries `0` as long as no object is bound.
     */
    fun integer(property: IntegerProperty, name: String) =
        value(property, 0, null) { bean ->
            JavaBeanIntegerPropertyBuilder.create().bean(bean).name(name).build()
        }

    /**
     * Ties [property] to the wide whole number field named [name] of the wrapped object, the one a
     * count that may leave the range of an `Int` is carried in.
     *
     * The property carries `0` as long as no object is bound.
     */
    fun long(property: LongProperty, name: String) =
        value(property, 0L, null) { bean ->
            JavaBeanLongPropertyBuilder.create().bean(bean).name(name).build()
        }

    /**
     * Ties [property] to the fractional number field named [name] of the wrapped object, the one a
     * measure in points or a factor is carried in.
     *
     * The property carries `0.0` as long as no object is bound.
     */
    fun double(property: DoubleProperty, name: String) =
        value(property, 0.0, null) { bean ->
            JavaBeanDoublePropertyBuilder.create().bean(bean).name(name).build()
        }

    /**
     * Ties [property] to the truth value field named [name] of the wrapped object.
     *
     * The property carries `false` as long as no object is bound.
     */
    fun boolean(property: BooleanProperty, name: String) =
        value(property, false, null) { bean ->
            JavaBeanBooleanPropertyBuilder.create().bean(bean).name(name).build()
        }

    /**
     * Ties [property] to the object field named [name] of the wrapped object, for a field nothing
     * further is known about - an enum constant for instance.
     *
     * A field whose value is a model object of its own is registered through [model] instead.
     *
     * The property carries `null` as long as no object is bound.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> reference(property: Property<T>, name: String) =
        value(property, null as T, null) { bean -> buildObject<T>(bean, name) }

    /**
     * Ties [property] - the property model of a nested object - to the field named [name] of the
     * wrapped object, and lets [refresh] of this class reach into that model through [refreshNested].
     *
     * Reading the field again only tells whether another object sits in it now. Whether something
     * inside that object changed is a question only the model of that object can answer, which is why
     * it is handed its own way of reading here.
     *
     * @param property The property model of the nested object.
     * @param name The name of the field on the wrapped object.
     * @param refreshNested Reads the nested object again, usually `<property>::refresh`.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> model(property: Property<T>, name: String, refreshNested: () -> Unit) =
        value(property, null as T, refreshNested) { bean -> buildObject<T>(bean, name) }

    /**
     * Ties [property] to the list field named [name] of the wrapped object.
     *
     * The property carries no entry at all as long as no object is bound.
     */
    fun <T> list(property: ListProperty<T>, name: String) {
        var bean: Property<List<T>?>? = null
        var syncing = false

        fun write() {
            val target = bean ?: return

            syncing = true
            try {
                target.value = ArrayList(property)
            } finally {
                syncing = false
            }
        }

        fun read() {
            val target = bean ?: return

            syncing = true
            try {
                property.setAll(target.value ?: emptyList())
            } finally {
                syncing = false
            }
        }

        register(
            property = property,
            write = ::write,
            isSyncing = { syncing },
            read = ::read,
            bind = { wrapped ->
                bean = null
                if (wrapped == null) {
                    syncing = true
                    try {
                        property.clear()
                    } finally {
                        syncing = false
                    }
                } else {
                    bean = buildObject<List<T>?>(wrapped, name)
                    read()
                }
            }
        )
    }

    /**
     * Hands [bean] to every registered field, so each one connects itself to the matching field of
     * that object. A field of an object that is not there at all falls back to its neutral value.
     *
     * @param bean The object the model wraps now, or `null` when it wraps none.
     */
    fun rebind(bean: B?) {
        if (isBound && boundTo === bean) return
        isBound = true
        boundTo = bean

        aligned {
            for (binder in binders) {
                binder(bean)
            }
        }
    }

    /**
     * Reads every field of the wrapped object again - and, through the models registered with [model],
     * every field of the objects nested in it - and hands what changed to the field properties.
     *
     * This is what a caller uses after writing on the wrapped object past this model: a plain object
     * reports nothing, so nobody would notice such a write otherwise. Everyone listening to a field
     * property is told about it; the model itself does not report it as its own change, because the
     * caller is the one who knows about it already.
     */
    fun refresh() {
        aligned {
            for (reader in readers) {
                reader()
            }
        }

        // The fields stayed quiet while reading, so the model reports the whole reading exactly once
        // here. Without it a view bound to the wrapped object as a whole - and the parent above it -
        // would keep showing what that object carried before it was read again.
        fireEvent()
    }

    /**
     * Registers a field whose value is a single object, which is every field but a list.
     *
     * @param property The property the field is offered as.
     * @param neutral The value the property carries while no object is bound.
     * @param refreshNested Reads the object nested in this field again, `null` when there is no model
     *   for it.
     * @param build Builds the adapter of the field for the object that is bound.
     */
    private fun <T> value(
        property: Property<T>,
        neutral: T,
        refreshNested: (() -> Unit)?,
        build: (Any) -> Property<T>
    ) {
        var bean: Property<T>? = null
        var syncing = false

        fun write() {
            val target = bean ?: return
            val current = property.value
            if (target.value == current) return

            syncing = true
            try {
                target.value = current
            } finally {
                syncing = false
            }
        }

        fun take(newValue: T) {
            syncing = true
            try {
                property.value = newValue
            } finally {
                syncing = false
            }
        }

        fun read() {
            val target = bean ?: return

            // The adapter answers from what it read last, so it is told to read the field again first.
            (target as? JavaBeanProperty<*>)?.fireValueChangedEvent()
            take(target.value)
            refreshNested?.invoke()
        }

        register(
            property = property,
            write = ::write,
            isSyncing = { syncing },
            read = ::read,
            bind = { wrapped ->
                bean = null
                if (wrapped == null) {
                    take(neutral)
                } else {
                    val target = build(wrapped)
                    bean = target
                    // The wrapped object wins over what the property carried before, so a freshly bound
                    // object is shown instead of being overwritten.
                    take(target.value)
                }
            }
        )
    }

    /**
     * Registers one field: a change of [property] is written into the wrapped object and only then
     * reported as a change of the model.
     *
     * The two sides are kept in step by hand instead of by a bidirectional binding. The order is the
     * reason: a binding writes the value from a `ChangeListener`, and Java FX runs every invalidation
     * listener BEFORE the first change listener - so the model would report the change while the
     * wrapped object still carried the previous value, and everyone reading the model through a
     * binding would take over exactly that stale value and never be told again.
     *
     * @param property The property the field is offered as.
     * @param write Writes the value of [property] into the wrapped object.
     * @param isSyncing Whether the property is taking over a value right now, which is not written back.
     * @param read Reads the field of the wrapped object again.
     * @param bind Connects the field to the object it is handed.
     */
    private fun register(
        property: Property<*>,
        write: () -> Unit,
        isSyncing: () -> Boolean,
        read: () -> Unit,
        bind: (B?) -> Unit
    ) {
        // An invalidation listener and not a change listener: a nested model reports a change of one
        // of its own fields without its value differing, which a change listener would swallow.
        property.addListener(
            InvalidationListener {
                // A property reports an invalidation only while it is valid and becomes valid again by
                // being read, so without this read the next change would be reported to nobody.
                property.value

                if (isSyncing()) return@InvalidationListener

                write()

                if (!aligning) {
                    fireEvent()
                }
            }
        )

        binders += bind
        readers += read
    }

    /** Runs [block] without reporting what the field properties take over as a change of the model. */
    private inline fun aligned(block: () -> Unit) {
        aligning = true
        try {
            block()
        } finally {
            aligning = false
        }
    }

    /**
     * Builds the adapter of an object field, the one every field that is not of a primitive type is
     * reached through.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> buildObject(bean: Any, name: String): Property<T> =
        JavaBeanObjectPropertyBuilder.create()
            .bean(bean)
            .name(name)
            .build() as Property<T>

}
