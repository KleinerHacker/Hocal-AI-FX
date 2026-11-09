---
name: icons
description: Create, name, store and use icons of the Java FX UI. Load before an icon is drawn, moved, renamed or registered, and before referencing an icon file from Kotlin or FXML.
---

# Icon Creation

* `docs/docs/assets/images/file.png` MUST be used as the design reference for color, stroke, shapes, curves, fills, etc.
  * It does not exist yet: before the first icon is drawn, ask the user for the design reference
* Every icon MUST be created in PNG format with a transparent background
* If the contrast requires it, separate icons MUST be created for dark and light mode (with darker or brighter colors)
* The `icon-creator` agent MUST be used to draw an icon
  * It returns the PNG; creating, moving and renaming the files is done through git, as described by the `git.md` rules
* The application icon is the exception: it is derived 1:1 from `docs/docs/assets/images/icon.png`
  * Sizes at which scaling destroys the motive MUST be redrawn simplified by the `icon-creator` agent

## Naming Icons

* Every icon file MUST be named `<name>@<size>.png`
  * `<name>` - lower kebab-case, describing the purpose, e.g. `save`, `project-settings`
  * `<size>` - edge length in pixels of the square image, e.g. `16`, `32`
  * Example: `new@16.png`, `project-settings@32.png`
* A dark mode variant MUST be named `<name>-dark@<size>.png`
* FORBIDDEN: any other suffix scheme, e.g. `@2x`, `_16`, `-16`
* Menu icons are stored at `32` px and scaled down for display, so they stay sharp on HiDPI screens
* The application icon uses the name `app`, one file per size, e.g. `app@256.png`

## Storing Icons

* All icons are stored flat in `app/ui/src/main/resources/icons` - no sub folders
* All icons MUST be accessible centrally through the object class `HocalIcons` in the root package `org.pcsoft.app.hocal.app` of `app/ui`
  * `HocalIcons` is created with the first icon
  * The file name MUST NOT appear anywhere else; every consumer goes through `HocalIcons`
  * `HocalIcons` builds the file name from name and size, so the scheme lives in exactly one place
