---
Name: component
Description: Basic architecture for a Java FX component (NOT a window); required to create or edit Java FX components.
---

# Java FX Components

## Template

* The template is located here: `app/ui/src/main/kotlin/org/pcsoft/app/hocal/app/ui/component`
  * ONLY the files `ProjectTree*.kt` are relevant
  * Their design and architecture MUST be adopted (Component, View, ViewModel)

## Structure

* The FXML always contains the component content
  * The content is fully defined there
  * The `fx:root` notation MUST be used
* The component is assembled through any FX Node class
  * Integration always happens via `root`

## Model Binding

* A component editing project data MUST be bound to the FX property model of `lib/fx-model`
  * `ProjectTree` shows the folders of the project: its `folderProperty()` is bound to the tree
    property of the project model by the caller, and it observes that property while it is on screen
  * The component MUST NOT copy the data and MUST NOT own a state of its own
  * The property model is bound from outside to a property of the component, its single interface
* The binding MUST be released when the bound model object is exchanged, and built up anew for the
  new object
* Both directions MUST be proven by tests on the real controls, as described in the `testing` skill
* A `VirtualFolder` is equal to another by VALUE (name and children); everything that has to find
  a folder again - selection, expanded state - MUST work on object identity

## Location

* Component classes MUST be placed under the root package in `ui.component`
  * Every required class MUST be placed in its own file
* Component FXML MUST be placed in the same directory within the resource directory
