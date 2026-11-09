---
name: startup
description: The background area behind the splash screen that runs every job the application needs before its first window - not built yet, created with the first such job. Load before a startup background process is added or changed.
---

# Startup Area

## What it is

* NOT BUILT YET: today `Launcher` shows the main window directly. The area below is created
  with the first job the application has to do before its first window, and from then on these
  rules apply

* While the application starts, `SplashStage`
  (`app/ui/src/main/kotlin/org/pcsoft/app/hocal/app/ui/splash/SplashStage.kt`) floats on the
  desktop - borderless, transparent, animated.
* Behind it, `Startup` (`app/ui/src/main/kotlin/org/pcsoft/app/hocal/app/startup/Startup.kt`) runs
  a background thread:
  1. the splash is held for one second (`Thread.sleep`)
  2. `Startup.discoverSteps()` scans the package `org.pcsoft.app.hocal.app.startup.step` with
     ClassGraph for `StartupStep` implementations, builds each one and sorts them by `@StartupOrder`
  3. every step runs, one after another
  4. `Startup.run` calls its `onDone` on the FX thread; `Launcher` then installs the theme, shows
     the main window and fades the splash out
* `Launcher` (`app/ui/src/main/kotlin/org/pcsoft/app/hocal/app/Launcher.kt`) is the only caller of
  `Startup.run`.

## The rule

* EVERY job the application has to do before its first window is a `StartupStep`
  (`app/ui/src/main/kotlin/org/pcsoft/app/hocal/app/startup/StartupStep.kt`) and MUST live in the
  package `org.pcsoft.app.hocal.app.startup.step`
  * Examples: loading plugins, warming a cache, checking for updates
* A step is picked up by the scan on its own - there is NO list to add it to
* FORBIDDEN: doing such work directly in `Launcher.start`, in `main`, or in a view model
* FORBIDDEN: a `StartupStep` implementation outside `org.pcsoft.app.hocal.app.startup.step`

## Writing a step

* Put the class in `org.pcsoft.app.hocal.app.startup.step`
* It MUST be a concrete class with a public no-argument constructor - the scan builds it that way;
  a Kotlin `object` does NOT work
* Give it a `@StartupOrder(n)` when it must run before or after another step; lower runs earlier,
  a step without the annotation runs at `StartupOrder.DEFAULT` (0); a step reading the preferences
  belongs at `@StartupOrder(0)` and everything that needs them ranks above it
* A step runs on the startup background thread - keep file and network work here
* Work that needs the FX thread (a dialog, a stage, the theme) MUST go through
  `StartupContext.onFxThread { ... }`; never touch the toolkit directly from a step
* Reach the running application through `StartupContext.app` (to stop it, to read its parameters) -
  never through a constructor argument, the scan cannot pass one
* A step that handles its own problem (dialog, `app.stop()`) does so inside an `onFxThread` block
* An unhandled exception from a step aborts the startup: `Startup` reports it
  (`text.startup.error.*`) and stops the application
* A step MUST finish on its own - no endless wait, no blocking on user input outside a dialog
* The splash stays on screen until every step returned, so keep steps reasonably quick

## Module system

* The scan needs `requires io.github.classgraph;` and
  `opens org.pcsoft.app.hocal.app.startup.step to io.github.classgraph;` in
  `app/ui/src/main/java/module-info.java` - keep both when the package moves or is renamed

## Tests

* A new step gets a test in the mirrored package under
  `app/ui/src/test/kotlin/org/pcsoft/app/hocal/app/startup/step/`, following the `testing` skill
* Cover: the step is built with a no-argument constructor, its `@StartupOrder` (if any), that
  `onFxThread` work is marshalled, and the failure path
* `StartupTest` proves the scan and the ranking; it does not need to know the concrete steps
