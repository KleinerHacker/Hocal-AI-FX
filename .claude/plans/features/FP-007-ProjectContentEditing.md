# Feature Plan: Project Content Editing

## 1. Objective

Let the user change the content of the project in the main window through context menus: create and
delete folders and import files in the directory tree, import and delete files in the file list, and
delete media in the history list.

## 2. Current State

* FP-001 (planned) provides the operations in the model and the `ProjectManager`: virtual tree
  operations (create, rename, move, delete folders and entries), removal of a reference, media
  history operations (append, delete with renumbering), and the rules (`000` cannot be deleted, a
  reference keeps at least one medium, entries never delete media)
* FP-002 (planned) provides the directory tree (folders only), the file list (files only, with
  original and current preview) and the history list; all are read-only, there are no context menus
* FP-003 (planned) provides the file menu, the progress and error dialogs
* FP-006 (planned) appends generated results to a reference and keeps running jobs by reference UUID
* No way exists to bring an existing image into a project; the only source of media will be a generation
* The media type registry of FP-002 names the supported types (standard Java FX image formats)
* Context menus and icons: icons are only planned for the File menu of FP-003

## 3. Target State

* Right click in the directory tree opens a menu: new folder, delete folder, import files
* Right click in the file list opens a menu: import files, delete file
* Right click in the history list opens a menu: delete medium
* Imported files become new references (one UUID, medium `000` = the imported file) with an entry in
  the selected folder
* Destructive actions ask for confirmation, say what is removed, and leave the project consistent
* Every change goes through the `ProjectManager`, marks the project as changed, and all three lists
  follow immediately

## 4. Requirements

### Functional Requirements

* Directory tree menu:
    * New folder: asks for a name, creates it below the selected folder (below the root without
      selection)
    * Delete folder: with all subfolders and entries after confirmation naming the counts; the root
      cannot be deleted
    * Import files: opens a file chooser (multiple selection), imports into the selected folder
* File list menu:
    * Import files: same as in the tree, into the folder shown in the list
    * Delete file: removes the entry from the folder after confirmation
    * available also on an empty list (import)
* History list menu:
    * Delete medium only; no other entries
    * medium `000` cannot be deleted; the entry is disabled with an explaining tooltip
    * a medium that is the only one of its reference cannot be deleted (same rule)
    * deleting from the middle renumbers the following media; the history details move with them
    * deleting the newest medium makes its predecessor the current medium
* Import:
    * supported types come from the media type registry; other files are reported and skipped
    * each file becomes a new reference with medium `000`, named after the file name, unique within
      the folder (a number is appended on a clash)
    * a failure of one file does not stop the others; a summary lists the skipped files
    * the import runs in the background with the progress dialog for large selections
* After a deletion, the selection moves to a sensible neighbour in the affected list
* A deleted reference that is the target of a running or waiting generation (FP-006) makes the result
  be discarded with a message

### Technical Requirements

* Operations only through the `ProjectManager`; the lists and the context menus never touch the
  storage or the POJOs
* Menus are built with the project's component patterns (`component`, `dialog`, `ui-styling`,
  `fx-component-lifecycle`); texts are I18N keys, German through the `translator`
* Context menu entries carry no icons (icons are not part of any plan except the File menu)
* Confirmation, name input and import report dialogs follow the `dialog` skill
* Changes on the FX thread, the file reading of an import in the background (`javafx.concurrent`)
* Tests with TestFX for the menus and unit tests for the import operation

## 5. Architecture

* `lib/fx-model` (`ProjectManager`) gets an import operation: reads a file, checks the type, creates the
  reference, medium `000` and the tree entry as one change; all other operations come from FP-001
* `app/ui`:
    * context menu support in `ProjectTree`, `FileList` and `HistoryList` and their view models
    * dialogs: name input, delete confirmation (with counts and consequences), import summary
    * file chooser for the import, filters taken from the media type registry
* Data flow: menu entry -> view model -> `ProjectManager` -> POJO change, `refresh()`, `dirty` -> the
  lists update through the existing bindings and keep or move the selection
* Persistence: only the changed `project.json` and the media files on the next save

## 6. Implementation Plan Overview

| ID    | Implementation Plan        | Objective                                                          | Dependencies                         |
|-------|----------------------------|--------------------------------------------------------------------|--------------------------------------|
| IP-01 | Media Import Operation     | Import files as new references with medium `000` and a tree entry  | FP-001 IP-04, IP-05, IP-07, FP-002 IP-03 |
| IP-02 | Directory Tree Menu        | Context menu: new folder, delete folder, import; shared dialogs    | IP-01, FP-002 IP-02, FP-003 IP-04    |
| IP-03 | File List Menu             | Context menu: import files, delete file                            | IP-02, FP-002 IP-03                  |
| IP-04 | History Menu               | Context menu: delete medium with the rules of the history          | IP-02, FP-002 IP-05, FP-001 IP-04    |

## 7. Implementation Plans

### IP-01: Media Import Operation

**Objective**

Bring existing files into the project as references.

**Scope**

* import operation in the `ProjectManager` for one or several files into a folder
* type check through the media type registry; typed result with imported and skipped files
* unique names within the folder
* background execution and progress, partial success
* not: user interface

**Affected Areas**

* `lib/fx-model` (`ProjectManager`), the storage use of FP-001, tests

**Dependencies**

FP-001 IP-04, IP-05, IP-07, FP-002 IP-03 (registry).

**Expected Result**

* an import changes the model consistently or not at all per file

**Technical Considerations**

* a file is read completely before the change; an unreadable file is skipped
* very large files and a size limit are open
* the overflow rule of `999` does not matter here (new reference starts at `000`)

### IP-02: Directory Tree Menu

**Objective**

Give the directory tree its context menu and the dialogs the other menus reuse.

**Scope**

* menu in `ProjectTree`: new folder, delete folder, import files
* dialogs: name input (with validation by the tree operation rules), delete confirmation with counts,
  import summary
* selection handling after create and delete
* not: the file list and the history list menus

**Affected Areas**

* `app/ui` `ProjectTree`, view model, dialogs, message bundles

**Dependencies**

IP-01, FP-002 IP-02, FP-003 IP-04 (progress and error dialogs).

**Expected Result**

* folders can be created and deleted and files imported from the tree

**Technical Considerations**

* a right click selects the item under the cursor first
* the root has a reduced menu (no delete)
* skills: `component`, `dialog`, `ui-styling`, `fx-component-lifecycle`, `translation`

### IP-03: File List Menu

**Objective**

Give the file list its context menu.

**Scope**

* menu in `FileList`: import files, delete file; import also on an empty list
* delete confirmation naming the file and the consequence for the reference
* selection moves to a neighbour after the deletion
* not: the tree and history menus

**Affected Areas**

* `app/ui` `FileList`, view model, message bundles

**Dependencies**

IP-02, FP-002 IP-03.

**Expected Result**

* files can be imported into and deleted from the shown folder

**Technical Considerations**

* removing the last entry that links a reference raises the open question about unlinked references
* skills as in IP-02

### IP-04: History Menu

**Objective**

Allow deleting single media from the history.

**Scope**

* menu in `HistoryList` with the one entry delete medium
* entry disabled with an explaining tooltip for medium `000` and for a single medium
* confirmation, then the deletion with renumbering through the `ProjectManager`
* selection after the deletion: the predecessor, so the main window shows a valid medium
* not: any other action in the history

**Affected Areas**

* `app/ui` `HistoryList`, view model, message bundles

**Dependencies**

IP-02, FP-002 IP-05, FP-001 IP-04.

**Expected Result**

* a medium can be deleted from the middle or the end; the list, the shown medium and the file list
  preview follow

**Technical Considerations**

* the renumbering changes the medium numbers; a running generation result still appends at the end
* the preview "current medium" in the file list is recomputed
* skills as in IP-02

## 8. Dependency Graph

```text
IP-01
└── IP-02
    ├── IP-03
    └── IP-04
```

## 9. Risks and Open Questions

Risks:

* Deleting is destructive and without undo; the confirmation must be precise
* A deletion while a generation targets the reference needs the discard rule of FP-006
* Large imports must not block the window

Open questions:

* Is rename part of the menus (the model supports it), and in the tree and the file list
* Delete key and the accelerator for the menu entries
* Reference without an entry after deleting its last entry: keep or remove (open since FP-001)
* Multiple selection in the file list for delete, and in the history for delete
* Import of a whole directory tree, and import by drag and drop from the system
* Size limit and error handling of very large files
* Undo and redo of deletions (not planned)
* Moving entries and folders (drag and drop inside the project) is not part of this feature

## 10. Feature Completion Criteria

* The three context menus exist with exactly the agreed entries
* Files import as references with medium `000`; unsupported files are reported and skipped
* Folders can be created and deleted with confirmation; the root stays
* Files can be deleted from the list; media can be deleted from the history, never `000`
* After each change all lists update, the selection is valid, and the project is marked as changed
* The Gradle `build` passes; tests cover the import and each menu; README and KDoc are up to date
