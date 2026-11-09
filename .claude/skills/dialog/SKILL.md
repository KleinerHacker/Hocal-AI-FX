---
Name: dialog
Description: Basic architecture of a Java FX alert (dialog) component; required to create or edit Java FX alerts (dialogs).
---

# Java FX Dialogs

## Template

* There is no dialog yet; the first one is built after the window template
  (`app/ui/src/main/kotlin/org/pcsoft/app/hocal/app/ui/window`, Component, View, ViewModel) and
  becomes the template for every further dialog
  * It MUST be placed in `app/ui/src/main/kotlin/org/pcsoft/app/hocal/app/ui/dialog`
  * The object class `HocalDialog` and the demo source set below are created with it

## Structure

* The dialog is based on the `Alert` class
  * Integration always happens through the `dialogPane`
* A dialog that the plain `Alert` already covers MUST stay a plain `Alert`
  * This is the case when caption, message, icon and buttons are all it shows
  * FORBIDDEN: an FXML, a view, a view model or a dialog class of its own for such a dialog
  * It is built where it is shown, in the object class `HocalDialog`
  * The standard buttons of Java FX (`ButtonType.OK`, `ButtonType.YES`, ...) MUST be used, because
    Java FX translates them itself; a button MUST NOT be translated through the message bundle again
* Only a dialog that shows more than the plain `Alert` offers - a details pane for instance - gets
  content of its own
  * The FXML then holds the dialog content, which is defined there completely
  * The content is placed into the `dialogPane`, whose own header MUST be dropped
* You MUST ask the user which properties apply to the dialog:
  * Dialog title
  * Dialog parent?
  * Dialog buttons (options)?
  * Dialog default action (when the dialog is closed with ESCAPE or the 'X' button)?
  * Is an icon required, and if so, which one from the standard Java FX icon set, or must a new one be generated?
* ALL properties are relevant

## Manual check

* A dialog is looked at by a human being, because an assertion does not see the contrast of an icon,
  the wrapping of a text or a window that does not grow with its content
* EVERY dialog MUST be reachable in the demo `app/ui/src/demo/kotlin/.../ui/dialog/DialogDemo.kt`
  * A new dialog MUST be added there with one button per variant (buttons and details pane)
  * The demo is started with `gradlew :app:hocal-ui:runDialogDemo`
* The demo lives in the source set `demo` and MUST stay disabled
  * FORBIDDEN: `build`, `check` or `test` starting it, or another task depending on `runDialogDemo`
  * FORBIDDEN: shipping it with the application - it MUST NOT end up in the JAR or the distribution
  * FORBIDDEN: any test class inside the source set - it holds no test
  * `build` compiles the source set, so a demo that no longer matches its dialogs breaks the build
    instead of rotting unnoticed
* After a dialog was created or changed, the user MUST be asked to look at it in the demo, in the
  light and in the dark colour scheme

## Location

* Dialog classes MUST be placed in `ui.dialog` under the root package
  * Every required class MUST live in its own file
* Dialog FXML MUST be placed in the matching directory of the resource directory
* All application dialogs are registered in the object class `HocalDialog` in the sub package `ui` under the root package
