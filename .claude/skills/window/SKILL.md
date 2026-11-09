---
Name: window
Description: Basic architecture for a Java FX window component; required to create or edit Java FX windows.
---

# Java FX Windows

## Template

* The template is located here: `app/ui/src/main/kotlin/org/pcsoft/app/hocal/app/ui/window`
  * ONLY the files `MainWindow*.kt` are relevant
  * Their design and architecture MUST be adopted (Component, View, ViewModel)

## Structure

* The FXML always contains the window content
  * The content is fully defined there
* The window is assembled through the Stage class
  * Integration always happens via the `scene`
* You MUST ask the user which properties apply to the window:
  * Window title
  * Modal?
  * Window size
  * Window position (relative to a parent?)
  * Fix the window size?
  * Is an icon required, and if so, which one or should a new one be generated?
* ALL properties are relevant

## Location

* Window classes MUST be placed under the root package in `ui.window`
  * Every required class MUST be placed in its own file
* Window FXML MUST be placed in the same directory within the resource directory