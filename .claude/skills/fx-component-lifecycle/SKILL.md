---
name: fx-component-lifecycle
description: Pattern for a Java FX component that registers itself somewhere global while it is on screen - listener, subscription, timer - and releases the registration again. Load before a view registers or deregisters anything outside itself, and before touching showingBinding.
---

# Component Lifecycle

## Scope

* Applies to EVERY Java FX component that registers itself outside its own object graph while it is
  on screen
  * Listener of a storage or a service, subscription, timer, scheduled task, watch service
* The registration MUST be bound to the SCREEN STATE OF THE COMPONENT, never to the window

## Screen state

* The screen state MUST be read through `Node.showingBinding()`
  * Located in `../../../app/ui/src/main/kotlin/org/pcsoft/app/hocal/app/ui/FxNodes.kt`
  * It is `true` while the node is visible, belongs to a scene and that scene belongs to a shown
    window
* FORBIDDEN: `window.onShowing` / `window.onHiding` or `setOnShown` in a view
  * They assign instead of add and silently replace a handler the stage already carries
* FORBIDDEN: following the window yourself through `sceneProperty` plus `windowProperty` listeners
  * The listeners pile up on every scene change and the window is the wrong reference point anyway
* FORBIDDEN: binding the registration to `visibleProperty` alone
  * A closed window leaves the node visible, so the registration would survive the window

## Pattern

* The view holds the binding in a FIELD and registers exactly one listener on it

```kotlin
// Kept as a field: a garbage collected binding stops notifying, and the registration would stay.
private lateinit var showing: BooleanBinding

override fun initialize(location: URL?, resources: ResourceBundle?) {
    showing = pnlRoot.showingBinding()
    showing.addListener { _, _, onScreen -> if (onScreen) viewModel.onShow() else viewModel.onHide() }
}
```

* The registration itself MUST live in the view model, not in the view
  * The view model offers `onShow()` and `onHide()`, and the two MUST be symmetric
* The listener object MUST be kept as a field of the view model
  * A method reference creates a new instance per call, so removing it again would not find it

## Memory leak

* The binding MUST NOT be stored in a local variable
  * `Bindings.select*` attaches itself with weak listeners; a binding nobody references is collected
    and never fires again, leaving the global registration in place forever
* The chain that leaks is `global registry -> view model -> view`
  * It MUST exist only between `onShow` and `onHide`

## Threading

* A global registry MAY notify on any thread, while the bound property belongs to the FX thread
  * The view model MUST dispatch with `Platform.runLater` unless it already is on the FX thread

## Testing

* The pattern MUST be covered for all three ways off screen
  * Component set invisible, component removed from the scene, window hidden
* Reaching the state again MUST restore the registration, which is proven by a further change being
  noticed
