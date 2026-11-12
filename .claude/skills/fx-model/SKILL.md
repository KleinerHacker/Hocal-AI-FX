---
name: fx-model
description: Mirroring rule between the plain model POJOs in lib/model (hocal-model) and their Java FX property models in lib/fx-model (hocal-fx-model) - package mirroring, nested change propagation, mandatory tests and the usage in app/ui. Load before a POJO under lib/model is added, renamed, removed or changed in its fields, before an FX property model is created or changed, and whenever a POJO in lib/model has no counterpart in lib/fx-model yet.
---

# FX Model

* Neither `lib/model` nor `lib/fx-model` exists yet; both are created with the first POJO, and
  from then on these rules apply

## Mirroring Rule

* EVERY POJO in module `hocal-model` (`lib/model`) MUST have a counterpart in module
  `hocal-fx-model` (`lib/fx-model`)
  * The counterpart is built on Java FX properties and wraps the POJO - it MUST NOT copy its data
  * The POJO stays the single source of truth, the FX model is only the property view on it
* Enums, value objects without fields and pure marker types need NO counterpart
* Adding, renaming, removing or re-typing a field of a POJO REQUIRES the same change in its FX model
  in the SAME change set

## Package And Naming

* The package sub structure of `lib/model` MUST be mirrored under
  `org.pcsoft.app.hocal.fx.model`
  * `org.pcsoft.app.hocal.model.pref.RecentOpened`
    -> `org.pcsoft.app.hocal.fx.model.pref.RecentOpenedProperty`
* Naming scheme: `<PojoName>Property`
* A storage of `lib/model` gets NO counterpart in `lib/fx-model`
  * A storage only reads and writes; it keeps nothing, so there is no state to offer as a property
  * Whoever holds the loaded object holds it as a property model of `lib/fx-model` - the settings in
    `IoController`, the open project in `ProjectManager` (`lib/fx-model`), which is bound by the view
    models

## Structure Of An FX Model

* EVERY FX model - root and nested alike - extends `SimpleObjectProperty<Pojo?>`
  * A project part extends `ProjectPartProperty<Pojo>`, which is such a property
  * A constructor parameter is FORBIDDEN, except the initial object of a root model
* NO custom property base classes - a field is a plain `Simple*Property` from JavaFX
* The connection to the POJO is made by `BeanFields` from
  `org.pcsoft.app.hocal.fx.model.internal`
  * `BeanFields` is `internal` and MUST NOT leave the module
* EVERY field of the POJO gets an own property and is registered in `BeanFields` exactly once
  * `string`, `integer`, `boolean` for the matching field types
  * `list` for every list field
  * `model` for every field whose value is a POJO with an FX model of its own - the third argument
    is that model's `::refresh`
  * `reference` ONLY for an object field without an own FX model, an enum constant for instance
  * Using `reference` where `model` belongs compiles and binds correctly, but a change INSIDE the
    nested object is then never picked up by `refresh()`
* Registration and the listener that rebinds MUST stand in `init`, in field declaration order
  * `addListener { _, _, newValue -> fields.rebind(newValue) }` followed by `fields.rebind(get())`
* For every field expose
  * `val <field>Property` - the property itself, typed as the JavaFX interface (`StringProperty`,
    `ListProperty<T>`, ...) or as the nested FX model
  * `var <field>` - delegating getter and setter onto the property
* A change of a field MUST fire the change event of its parent property, up to the root
  * `BeanFields` does this through an `InvalidationListener` - a `ChangeListener` is FORBIDDEN here,
    it swallows the report of a nested model whose own value did not change

## Reflection And Field Names

* `BeanFields` resolves a field through the builders of `javafx.beans.property.adapter`, which look
  up `get<Name>()` and `set<Name>()` on the POJO by the registered name
  * The name registered MUST be the POJO property name, spelled exactly
  * A wrong name compiles and only fails at runtime when the object is bound
* No `opens` directive is needed: the POJOs are public types in exported packages, and a public
  method is invoked reflectively without opening the package
* A field declared `val` on the POJO (a `version` for instance) has NO setter and MUST NOT be
  registered

## Reading The POJO Again

* An adapter reads its field when it is told to, NOT when the field changes
* A write made on the POJO past the FX model is therefore only picked up by `refresh()`
* `refresh()` reports the reading ONCE on the model itself, so a view bound to the whole object
  follows as well - the single field properties stay quiet towards the parent while reading
* EVERY FX model MUST offer `refresh()`
  * Its body is EXACTLY `fields.refresh()` - nothing else
  * The descent into a nested model is NOT written here; it comes from registering that model with
    `fields.model(...)`, so a nested model can never be forgotten
  * `ProjectPartProperty.refresh()` is `open` and MUST be overridden by a part with own fields
  * `ProjectProperty.refresh()` additionally reads the attached plugin parts, which are entries of a
    map and not fields of the project

## Visibility

* An FX model used by `app/ui` MUST be public AND its package MUST be exported in
  `lib/fx-model/src/main/java/module-info.java`
  * Without the export the module system rejects the type, although Kotlin resolves it
* An FX model nobody outside the module reaches stays `internal`
* A public FX model MUST NOT expose a member of an `internal` type
  * Such a member stays `internal`, and what the user interface needs is offered as a short
    accessor of a public type next to it

## Testing

* Load the `testing` skill before writing the tests
* For EVERY FX model a test MUST prove for ALL fields
  * The value is read from the wrapped POJO
  * A write through the property reaches the POJO
  * A write through a binding reaches the POJO
  * The change fires on ALL object properties of the tree up to the root
  * A change on the POJO itself is visible through the properties
  * Exchanging the wrapped object updates every field property and fires up to the root
  * Exchanging the wrapped object against one with equal values keeps the field properties quiet
* A nested tree MUST be tested through the root as well, not only per level
