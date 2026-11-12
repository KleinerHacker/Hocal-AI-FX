# Feature Plan: Project File Operations

## 1. Objective

Make projects usable from the user interface: create a new project (reset), open, save and save as,
including encrypted files. The `ProjectManager` is extended for this, a File menu is added to the
main window, and the dialogs for unsaved changes, passwords, encryption options, errors and progress
are created.

## 2. Current State

* The main window has a menu bar with File > Exit (`MainWindowView.fxml`, accelerator `Shortcut+Q`)
* FP-001 plans the `ProjectManager` (singleton in `lib/fx-model`) with new, open (optional password),
  save, save as and close; the operations are synchronous and the caller blocks during file access
* FP-001 plans the storage in `lib/model` and the AES-256-GCM encryption with typed errors (wrong
  password, manipulated file, unsupported version)
* FP-002 plans the main window layout and the binding of the window to the `ProjectManager`; it
  deliberately has no file system access
* The `ProjectManager` exposes the properties `project`, `path` and `dirty`
* Dialogs follow the `dialog` skill; windows follow the `window` skill; texts are I18N keys with an
  English base bundle and a German translation through the `translator` agent
* The Java FX theme was removed; controls run on the default look
* The project file has no registered extension yet

## 3. Target State

* The File menu contains: New, Open, Save, Save As, Exit
* Every file operation works on the project held by the `ProjectManager` and replaces or updates it
* Encrypted files can be opened (password dialog) and saved (encryption options dialog)
* Unsaved changes are never lost silently: New, Open, Exit and closing the window ask first
* File access runs in the background; the window is locked with a progress dialog meanwhile
* Errors are shown as understandable messages; a failed operation leaves the current project unchanged
* The window title has the format `Hocal AI - <file name>`, with a `*` appended while the project
  has unsaved changes; a project without a file shows "Unbenannt" / "Untitled" as file name

## 4. Requirements

### Functional Requirements

* File menu entries and accelerators:
    * New (`Shortcut+N`) - resets to a new empty project
    * Open (`Shortcut+O`) - choose a file, enter the password if the file is encrypted
    * Save (`Shortcut+S`) - saves to the known path; without a path it behaves as Save As
    * Save As (`Shortcut+Shift+S`) - choose a path, then the encryption options; the only place to
      add, change or remove the password
    * Exit (`Shortcut+Q`) - as before
* There is no Close entry; "New" is the way to a clean project
* Every File menu entry has an icon: New, Open, Save, Save As and Exit
* The project file extension is `.hai`; it is a ZIP with the structure of FP-001
    * the file chooser filters on `.hai`; Save As appends it when it is missing
* Save is disabled while the project has no unsaved changes and a path
* New, Open, Exit and closing the window through the window frame ask first when the project has
  unsaved changes: Save, Discard, Cancel
    * Save runs the Save flow first; if it is cancelled or fails, the original action is cancelled
* Open:
    * the current project is replaced only after the new one was loaded successfully
    * a file that needs a password opens the password dialog
    * a wrong password keeps the dialog open with a hint; the user can try again or cancel
    * cancelling leaves the current project untouched
* There is no default file name and no start directory in the file chooser
* Save As asks for the encryption options after the path:
    * a switch "Encrypt", a password and a repeated password; both passwords must match
    * the dialog is preset with the current state of the project
    * an empty password is not allowed; a project without password is unencrypted, which is the
      switch turned off
    * adding, changing and removing the password is only possible here
* After a successful Save As, the project takes the new path
* The password stays in memory while the project is open, so Save does not ask again; it is wiped
  when the project is closed, replaced or reset
* Errors are shown in an error dialog with a clear message for: invalid file, damaged ZIP, newer
  format version, wrong password, manipulated file, file could not be written

### Technical Requirements

* The `ProjectManager` stays the only place that changes the project and talks to the storage
* File access runs off the FX thread (`javafx.concurrent`, part of `javafx.base`, so `lib/fx-model`
  stays within its allowed JavaFX use) and reports back on the FX thread
* Only one file operation runs at a time; further ones are rejected while the manager is busy
* The manager exposes a read-only `busy` property
* The password is held as `CharArray`, wiped after use, never logged, never stored on disk
    * the JavaFX `PasswordField` delivers a `String`; the field is cleared right after reading, and
      the remaining `String` is a known limit
* Dialogs and windows follow the `dialog` and `window` skills; every text is I18N
* Menu enabling and the window title are bound to the manager properties, not set by hand
* Icons follow the `icons` skill:
    * PNG with transparent background, named `new@32.png`, `open@32.png`, `save@32.png`,
      `save-as@32.png`, `exit@32.png`, stored flat in `app/ui/src/main/resources/icons`
    * every icon may need a light and a dark variant (`<name>-dark@32.png`), where the contrast
      requires it; `HocalIcons` hands out the matching variant
    * drawn by the `icon-creator` agent, moved and added through git
    * reached only through `HocalIcons` in the root package of `app/ui`, which is created with the
      first icon; no file name appears anywhere else
    * the design reference `docs/docs/assets/images/file.png` does not exist yet; the user delivers it
      before the first icon is drawn
* Listeners on the manager are released with the view (`fx-component-lifecycle`)
* A failed operation never changes the loaded project, the path or the password
* Tests per `testing` skill: developer tests for the manager, TestFX integration tests for the
  flows; test files go into a temporary directory
* README, MkDocs (user documentation of the File menu) and KDoc are updated; the CHANGELOG gets an
  entry because the user sees the new menu

## 5. Architecture

### Components

* `lib/fx-model`
    * `ProjectManager` extended by:
        * background execution of open and save with `javafx.concurrent`
        * `busy` property, `encrypted` property
        * password holding and wiping
        * mapping of storage errors to a small set of typed outcomes the UI can show
* `app/ui`
    * File menu in `MainWindowView` and a view model part for the actions and their enabling
    * `FileActions` (view model / helper) - runs the flows: ask for unsaved changes, choose file,
      ask for password, call the manager, report errors
    * dialogs: `UnsavedChangesDialog`, `PasswordDialog`, `EncryptionOptionsDialog`,
      `ErrorDialog`, `ProgressDialog`
    * file chooser with the `.hai` filter
    * `HocalIcons` - central access to the icons, created with the first icon

### Flows

```text
Open:    unsaved changes? -> file chooser -> manager.open
           -> password required? -> password dialog -> manager.open(password)
           -> wrong password? -> password dialog again
Save:    path known? -> manager.save | else Save As
Save As: file chooser -> encryption options dialog -> manager.saveAs(path, password?)
New:     unsaved changes? -> manager.new
Exit:    unsaved changes? -> hide window
```

### Data Flow

* menu action -> view model -> dialogs for input -> `ProjectManager` -> storage -> result on the FX
  thread -> properties (`project`, `path`, `dirty`, `encrypted`, `busy`) -> UI

### Persistence

* The project file only; no recent-files list and no remembered directory in this feature

### External Integrations

* FP-001 (manager, storage, encryption) and FP-002 (main window); nothing else

## 6. Implementation Plan Overview

| ID    | Implementation Plan                  | Objective                                                          | Dependencies |
|-------|--------------------------------------|--------------------------------------------------------------------|--------------|
| IP-01 | Project Manager File Operations      | Background execution, busy and encrypted state, password holding, error mapping | FP-001 IP-06, FP-001 IP-07 |
| IP-02 | File Menu And File Actions           | File menu, flows for New, Open, Save, Save As, unsaved-changes dialog, window title | IP-01, FP-002 IP-01 |
| IP-03 | Password And Encryption Dialogs      | Password dialog and encryption options dialog | IP-01        |
| IP-04 | Error And Progress Dialogs           | Error dialog and progress dialog for file operations               | IP-01        |

## 7. Implementation Plans

### IP-01: Project Manager File Operations

**Objective**

Give the manager everything the user interface needs for file operations.

**Scope**

* Run open and save in the background and report on the FX thread
* Properties `busy` and `encrypted`, next to `project`, `path` and `dirty`
* Holding and wiping of the password
* Typed outcomes for the errors of the storage and the encryption
* Rejecting a second operation while busy
* A failed operation leaves project, path and password unchanged
* NOT: any dialog or menu

**Affected Areas**

* `lib/fx-model` (manager package), tests

**Dependencies**

FP-001 IP-06, FP-001 IP-07.

**Expected Result**

The manager opens and saves without blocking the FX thread and tells the caller what happened in
terms the UI can show.

**Technical Considerations**

* Changes the contract of FP-001 IP-07 (blocking calls) into asynchronous ones; FP-001 IP-07 must be
  implemented with that in mind or extended here
* Wiping the password on close, reset, replace and failed open
* Test cases: success, each error, busy rejection, password wiped, project untouched after failure

### IP-02: File Menu And File Actions

**Objective**

Add the File menu and the flows behind its entries.

**Scope**

* Entries New, Open, Save, Save As, Exit with the accelerators
* File chooser with the `.hai` filter, `.hai` appended on Save As
* Unsaved-changes dialog on New, Open, Exit and closing the window through the frame
* Enabling of Save from `dirty` and `path`
* Window title `Hocal AI - <file name>` with `*` for unsaved changes
* Icons for all entries: New, Open, Save, Save As, Exit, through `HocalIcons`, each with a light
  and, where the contrast requires it, a dark variant
* NOT: password and encryption dialogs, error and progress dialogs themselves

**Affected Areas**

* `MainWindowView`, `MainWindowView.fxml`, view model, `MainWindow`, message bundles, `module-info.java`,
  tests, `HocalIcons`, `app/ui/src/main/resources/icons`

**Dependencies**

IP-01, FP-002 IP-01.

**Expected Result**

The File menu works for unencrypted files; closing the window asks about unsaved changes.

**Technical Considerations**

* Closing through the frame needs a close-request handler on the stage
* Save from the unsaved-changes dialog runs the Save flow and cancels the original action if that fails
* Until IP-03 and IP-04 exist, errors and password needs use simple placeholders
* Load `ui-styling`, `window`, `dialog`, `fx-component-lifecycle` and `icons` skills
* The icons need the design reference of the user first; without it IP-02 cannot finish the icons
* Menu icons are stored at 32 px and scaled down for display

### IP-03: Password And Encryption Dialogs

**Objective**

Handle passwords for opening and saving encrypted files.

**Scope**

* `PasswordDialog` for opening an encrypted file, with retry and hint on a wrong password
* `EncryptionOptionsDialog` with the switch "Encrypt", password and repeated password
* Adding, changing and removing the password happens only in this dialog, through Save As
* Preset of the options dialog from the current state
* Check that both passwords match
* An empty password is rejected; no further password rules
* NOT: a menu entry of its own for the encryption

**Affected Areas**

* `app/ui` (dialogs, menu), message bundles (English, German via `translator`), tests

**Dependencies**

IP-01.

**Expected Result**

An encrypted file can be opened and saved from the UI; the password of a project is added, changed
or removed through Save As.

**Technical Considerations**

* Clear the `PasswordField` right after reading; keep the password as `CharArray`
* The switch "Encrypt" decides; with it on, an empty password is refused
* Load the `dialog` skill and use the `dialog-explore` and `dialog-creator` agents

### IP-04: Error And Progress Dialogs

**Objective**

Show errors and long operations understandably.

**Scope**

* `ErrorDialog` with a message per outcome of IP-01
* `ProgressDialog`, modal, no cancel, shown while the manager is busy
* Avoid flicker for fast operations (short delay before showing)
* NOT: logging, retry logic

**Affected Areas**

* `app/ui` (dialogs), message bundles (English, German via `translator`), tests

**Dependencies**

IP-01.

**Expected Result**

Every failed operation shows a clear message; long operations lock the window with a progress dialog.

**Technical Considerations**

* The progress dialog is bound to `busy`
* No cancel, as saving must stay atomic
* Messages must not contain passwords or file contents

## 8. Dependency Graph

```text
IP-01
├── IP-02
├── IP-03
└── IP-04
```

IP-02, IP-03 and IP-04 can run in parallel after IP-01. IP-01 needs FP-001 IP-06 and IP-07; IP-02
also needs FP-002 IP-01.

## 9. Risks and Open Questions

* FP-001 IP-07 is planned with blocking file access; this feature makes it asynchronous - the
  contract of that plan has to be adjusted together
* The `PasswordField` returns a `String`, so the password cannot be fully kept out of ordinary
  memory; this is accepted
* Decided: no recent files list for now; it would need a stored setting and may follow later
* Decided: an empty password is not allowed; no password means unencrypted
* Decided: the window title is `Hocal AI - <file name>`; a project without a file shows "Unbenannt" /
  "Untitled" as file name; the `*` for unsaved changes is appended (assumed, not stated); the
  project name no longer appears in the title
* Decided: the file chooser has no default file name and no start directory
* Decided: file extension `.hai`
* Decided: password stays in memory while the project is open
* Decided: no password rules, no Close entry, closing through the window frame asks as well
* Decided: there is no "Encryption" menu entry; the password is added, changed or removed only in
  Save As
* A long running save cannot be cancelled; the user waits
* Icons: the design reference `docs/docs/assets/images/file.png` is missing; IP-02 is blocked for the
  icons until the user delivers it. Earlier icon wishes of FP-002 (tree, entries, "+") remain
  outside every plan; only these menu icons belong to this feature
* Decided: all icons may need a light and a dark variant, as far as the contrast requires it
* Open: how the application picks the light or the dark variant - the theme, which followed the
  operating system colour scheme, was removed; proposal: `HocalIcons` reads the colour scheme of the
  operating system once at start (`Platform.getPreferences().colorScheme`); FP-009 IP-10 replaces this
  with an own theme and the appearance setting, which then decides the variant

## 10. Feature Completion Criteria

* The File menu offers New, Open, Save, Save As and Exit with the accelerators, each with an icon
* A new project can be saved as `.hai`, closed, opened again and is identical
* An encrypted project can be saved with a password, is asked for the password on opening, and a
  wrong password is reported with a retry
* New, Open, Exit and closing the window ask about unsaved changes; Cancel keeps everything as it was
* A failed open or save shows a clear error and leaves the current project unchanged
* File operations do not block the window; a progress dialog is shown while they run
* The window title is `Hocal AI - <file name>` ("Unbenannt" without a file) with `*` for unsaved
  changes
* English and German texts exist for every new key
* `build` passes; README, MkDocs, CHANGELOG and KDoc are up to date
