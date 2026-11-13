# Feature Plan: Main Window Layout

## 1. Objective

Design the main window around the project: the virtual directory tree and the file list on the left,
the content of the selected file on the right, all connected to the FX model of the project. The
content area shows the media history of the selected file, the selected image and the place where
the prompt for a new generation is entered. The project is always in the state "new"; there is no
connection to the file system yet. The model choice and the prompt input are placeholders; the
history details come from the model of FP-001.

## 2. Current State

* `MainWindow` loads `MainWindowView` (MVVM FX, FXML in
  `app/ui/src/main/resources/org/pcsoft/app/hocal/app/ui/window/MainWindowView.fxml`), 1280 x 800
* Layout is a `BorderPane`: menu bar (File > Exit) on top, the `FlowList` component on the left
  (`prefWidth` 280), a `Label` with the selection hint in the center
* `MainWindowViewModel` owns an example list of three flow names and the selected flow
* `FlowList` is a component (`ui/component`) with its own view and view model
* The JavaFX theme was removed; controls run on the default look
* UI texts are I18N keys in `messages/bundle.properties` (English) and `bundle_de.properties`
* `module-info.java` opens the `window` and `component` packages to `javafx.fxml` and `mvvmfx`
* The project model does not exist yet; FP-001 creates `lib/model`, `lib/fx-model` and the
  singleton `ProjectManager`, which holds the one loaded project as `ProjectProperty`
* FP-001 stores media as numbered history files per reference; it stores no data about how a medium
  was generated (prompt, model)
* Tests: TestFX with Monocle in `app/ui`, window tests carry the `IT` suffix (`MainWindowIT`)

## 3. Target State

* The main window shows two panes separated by a splitter, the left one taking 0.3 of the width
* The left pane is a vertical splitter dividing at 0.5:
    * top: the virtual directory tree of the project
    * bottom: the files of the directory picked in the tree; each file shows its media as icons -
      first the original medium, directly behind it the last created (current) medium
* The right pane is the content of the file selected in the file list, a horizontal splitter of
  three parts with the dividers at 0.2 and 0.8:
    * left part (0.2): at its top, beside the image, the generation data of the selected medium:
      first line the model and the flow, second line the beginning of the prompt, ending with `...`
    * center part (0.6): the selected medium of the history shown as image, below it the input
      part: prompt text box, a "+" button to add images from the gallery, toggle buttons and a
      combobox for the model (placeholders)
    * right part (0.2): a list of all media of the file's history, from `000` to the last one;
      the last one is preselected automatically
* The selection in the tree decides which files the list shows; the selection in the list decides
  what the content area shows
* Previews exist for images; further media types can be added without changing the file list
* Everything is bound to the FX model of the project, which is a new, empty project
* The window follows the project held by the `ProjectManager`, not a copy of it

## 4. Requirements

### Functional Requirements

* A splitter separates the whole left side from the rest of the window; initial divider 0.3
* A splitter separates the directory tree from the file list on the left side; initial divider 0.5
* The directory tree shows only folders of the virtual tree of the project, never files (entries)
    * behind the name of a folder, in grey and in brackets, the number of its direct child elements,
      subfolders included
* The selection in the directory tree decides what the file list shows: picking a folder shows its
  entries, changing the selection changes the list, no selection shows an empty list
* The file list shows only files (entries), never folders; subfolders of the picked folder are
  reached through the directory tree only
* Every file entry shows two icons made from its media, in this order:
    * the original medium (`000`)
    * directly behind it the last created medium, which is the current one (highest number)
* The icon is a rendering of the medium itself; for an image it is the image, scaled
* The same applies to other media types: each type renders its own icon for the original and the
  current medium
* Other media types without a renderer show a neutral placeholder icon
* A file entry is one row: its name and the preview images of its media
* Both icons carry a tooltip that tells original and current medium apart
* An entry with only one medium shows the same icon twice
* OPEN: the size of the preview image in a row
* The selection in the file list decides what the content area shows; nothing selected shows an
  empty content area
* The content area is a splitter with three parts and the dividers at 0.2 and 0.8: left 0.2,
  center 0.6, right 0.2 (the history list)
* The history list shows every medium of the selected file, beginning with `000` up to the last
  one; the last one is selected automatically whenever a file is selected
* The middle of the content area shows the medium selected in the history list
* The generation data of the selected medium are shown at the top left beside the image, taken from
  the history details of FP-001:
    * first line: the model and the flow
    * second line: the beginning of the applied prompt, cut off with `...` when it is longer
    * the lines are empty for a medium without details
* Below the image there is an input part:
    * a multi-line text box for the prompt
    * above its left side a "+" button that opens the gallery dialog
    * above its right side toggle buttons for the selected model (placeholders)
    * above these toggle buttons a combobox to select the model (placeholder)
* The gallery dialog shows the internal virtual directory structure; only references (entries) can
  be selected, folders cannot
* An image added with "+" is identified by the UUID of its reference; its last medium always counts
  and is the one shown; it is only held in the input part until used and is not stored in the
  history as detail of the generation
* A new project shows an empty tree, an empty file list and an empty content area without errors
* The splitters can be dragged; the window can be resized freely

### Technical Requirements

* MVVM FX throughout; every new view has a view model (`ui-styling`, `component`, `window` skills)
* New views are components in `ui/component`, the main window only composes them
* All texts through I18N; English bundle first, German through the `translator` agent
* Tree, list and content bind to the property models of `lib/fx-model` (`ProjectProperty`,
  `VirtualNodeProperty`); no copy of the project data lives in `app/ui`
* The project is taken from the `ProjectManager` of FP-001; the UI does not create it itself
* Media preview is an extension point: a type is added by adding a provider, the file list is not
  changed (open/closed)
* The medium source delivers, per entry, the complete history in order, so the original, the current
  and every medium between are available; it matches the media history of FP-001 IP-04
* Previews and images are loaded lazily and off the FX thread; a failing medium never breaks a list
* Listeners or subscriptions registered on the global manager are released when the view leaves
  the screen (`fx-component-lifecycle` skill)
* No file system access; the media bytes come from a source the feature defines as an interface
* The model choice is a placeholder behind an interface; the generation data come from the history
  details of FP-001 (prompt, model, flow; no additional images)
* Only the image formats of standard JavaFX (`Image`) are supported
* Media types are registered in a registry in code; no `ServiceLoader`
* The `ProjectManager` creates the new project; the window only follows it
* Icons are not part of any plan; the user delivers the orientation separately
* Tests per `testing` skill: developer tests per class, TestFX integration test for the window
* README, MkDocs (main window page) and KDoc are updated; CHANGELOG only for visible changes

## 5. Architecture

### Components

* `MainWindowView` - composes menu bar, outer splitter and the components below
* `ProjectTree` (component) - tree of the virtual directory structure, exposes the selected folder
* `ProjectFileList` (component) - files of the selected folder with two icons each, exposes the
  selected file
* `MediaPreview` extension point - interface to decide whether a medium type is supported and to
  create the icon node of a medium; `ImageMediaPreview` is the first implementation
* `ContentArea` (component) - splitter holding the center part and the history list
* `HistoryList` (component) - all media of the selected file, last one preselected
* `MediaView` (component) - the selected medium, as image at first
* `GenerationDetails` (component) - applied prompt, model and flow from the history details of FP-001
* `GalleryDialog` - dialog showing the virtual directory structure, only references selectable
* `PromptInput` (component) - prompt text box, "+" button, model toggle buttons, model combobox
* `MainWindowViewModel` - follows the project of the `ProjectManager`, connects the selections

### Layout

```text
SplitPane (horizontal, divider 0.3)
├── SplitPane (vertical, divider 0.5)
│   ├── ProjectTree
│   └── ProjectFileList
└── ContentArea: SplitPane (horizontal, dividers 0.2 and 0.8)
    ├── left (0.2):   GenerationDetails at the top (model and flow, prompt start with `...`)
    ├── center (0.6)
    │   ├── MediaView
    │   └── PromptInput
    │       ├── above, left:  "+" button
    │       ├── above, right: model combobox, below it the model toggle buttons
    │       └── prompt text box
    └── right (0.2):  HistoryList
```

### Data Flow

* `ProjectManager.project` -> `MainWindowViewModel` -> `ProjectTree`
* selected folder of the tree -> `MainWindowViewModel` -> `ProjectFileList`
* entry of the list -> medium source -> original medium and current medium -> `MediaPreview` ->
  two icon nodes
* selected file of the list -> `HistoryList` -> all media; last one selected -> `MediaView` and
  `GenerationDetails`
* "+" -> gallery dialog -> reference UUID -> last medium at the time of use

### Persistence

* None; splitter positions, prompt and model choice are not stored in this feature

### External Integrations

* FP-001 (`lib/model`, `lib/fx-model`, `ProjectManager`); nothing else

## 6. Implementation Plan Overview

| ID    | Implementation Plan                | Objective                                                           | Dependencies |
|-------|------------------------------------|---------------------------------------------------------------------|--------------|
| IP-01 | Main Window Splitter Layout (COMPLETED) | Rebuild the main window layout with the splitters and placeholders   | -            |
| IP-02 | Directory Tree Component (COMPLETED) | Folder tree with child counts bound to the FX model, adapt the component rules | IP-01, FP-001 IP-02, FP-001 IP-07 |
| IP-03 | File List And Media Preview        | File list with original and current icon and the preview extension point | IP-01, FP-001 IP-02 |
| IP-04 | Project Binding And Window Wiring  | Follow the `ProjectManager`, connect tree, list and content area     | IP-02, IP-03, FP-001 IP-07 |
| IP-05 | History List And Media View        | History list with last medium preselected and the selected image     | IP-04, FP-001 IP-02 |
| IP-06 | Prompt Input Part                  | Prompt box, "+" button, model toggle buttons and combobox (placeholders) | IP-01        |
| IP-07 | Generation Details Panel           | Applied prompt, model and flow of the selected medium                | IP-05, FP-001 IP-02 |

## 7. Implementation Plans

### IP-01: Main Window Splitter Layout (COMPLETED)

**Objective**

Give the main window its new structure without any project content.

**Scope**

* Outer horizontal `SplitPane` with divider 0.3, inner vertical `SplitPane` with divider 0.5
* Content area as splitter with three parts and the dividers 0.2 and 0.8, placeholders for all parts
* Remove `FlowList`, the example flows, the selection label and everything that belongs to them
  (view, view model, FXML, message keys, tests)
* NOT: tree, list, previews, content parts, project binding

**Affected Areas**

* `MainWindowView`, `MainWindowView.fxml`, `MainWindowViewModel`, message bundles, `MainWindowIT`

**Dependencies**

None; can start before FP-001 is finished.

**Expected Result**

A resizable window with the nested splitters at their initial positions and the menu bar.

**Technical Considerations**

* Divider positions are set once the scene has a size; JavaFX does NOT keep ratios on resize (extra
  space is shared equally, CI of IP-01 proved it): the project side, the generation details and the
  history list are `resizableWithParent=false`, the media with the prompt input takes the difference
* Placeholders carry no logic and are replaced by the following plans
* Load `ui-styling`, `window` and `component` skills first

**Built**

* Differs from the plan: the resize behaviour. The project side, the generation details and the history
  list are `resizableWithParent=false` and keep their width, instead of keeping the divider ratios
* The placeholders are `StackPane`s with the ids `pnlTree`, `pnlFiles`, `pnlDetails`, `pnlMedia` and
  `pnlHistory`, the splitters have the ids `splMain`, `splProject` and `splContent`
* `MainWindowViewModel` stays as an empty class; the package `ui.component` is no longer opened or
  exported in `module-info.java` until IP-02 adds the first component again
* The `component` skill still names `FlowList` as template; IP-02 switches it

### IP-02: Directory Tree Component (COMPLETED)

**Objective**

Show the virtual directory structure of the project.

**Scope**

* `ProjectTree` component with its view and view model
* Tree built from `VirtualNodeProperty`, updating when the model changes
* Only folders are shown, never files; behind the name the number of direct child elements, in grey and in brackets
* Adapt the Claude rule set (`.claude/skills/component/SKILL.md`) so the directory tree is the
  example component instead of `FlowList`
* Exposes the selected folder as a read-only property
* Empty tree for a new project
* NOT: editing, drag and drop, context menus

**Affected Areas**

* `ui/component`, `module-info.java`, message bundles, tests

**Dependencies**

IP-01, FP-001 IP-02 (property models), FP-001 IP-07 (`ProjectManager`).

**Expected Result**

The tree shows the folders of the loaded project with their child counts and reports the picked
folder; the rules of the Claude rule set use it as example.

**Technical Considerations**

* The tree is recursive and must follow nested changes of the property model
* Listeners on the model must not leak; see `fx-component-lifecycle`
* The count of child elements follows the model, so it changes when entries or folders are added or removed

**Built**

* `ProjectTree` (a `TreeView<VirtualFolder>` with `fx:root`), `ProjectTreeView`, `ProjectTreeViewModel`,
  an extra `ProjectTreeCell` and a stylesheet `ProjectTreeView.css` (grey `child-count`)
* Differs from the plan: `app/ui` now depends on `:lib:hocal-fx-model` (approved by the user), which
  needed `requires org.pcsoft.app.hocal.fx.model` in `module-info.java`
* The root folder is shown as topmost node; with an empty name it shows the text of
  `component.projectTree.root`
* Selection and expanded state are kept by object identity, because `VirtualFolder` is equal by value
* The component is not placed in the main window yet; the placeholder `pnlTree` is replaced in IP-04
* The `component` skill names `ProjectTree*.kt` as template; no agent referred to `FlowList`


### IP-03: File List And Media Preview

**Objective**

List the entries of the selected folder, each with the icons of its original and its current medium.

**Scope**

* `ProjectFileList` component with view and view model, following the folder selected in the tree
* Only files (entries) are listed, never folders
* One row per entry with its name and two preview images: original medium first, directly behind it
  the last created medium; one medium only is shown twice
* `MediaPreview` interface with `ImageMediaPreview` as first implementation
* Registry that picks a provider by media type, with a neutral placeholder as fallback
* Lazy, off-thread loading of the icons
* Medium source as an interface (no file system)
* Exposes the selected file as a read-only property
* NOT: media of other types, editing of media

**Affected Areas**

* `ui/component`, new preview package, `module-info.java`, message bundles, tests

**Dependencies**

IP-01, FP-001 IP-02.

**Expected Result**

The list shows, per entry, its name and two image icons (original, then current); the list follows
the selection of the tree; unknown types show a placeholder; a broken image does not break the
list.

**Technical Considerations**

* JavaFX `Image` reads PNG, JPEG, GIF and BMP only; this is accepted, no other decoder is added
* Preview size and scaling (thumbnail, aspect ratio) must be fixed
* An entry with one medium only has `000` as original and current medium and shows its icon twice
* A tooltip on each icon names it as original or current medium
* Cancel loading when the folder changes or the view leaves the screen
* The extension mechanism is a registry in code
* OPEN: the size of the preview image in a row

### IP-04: Project Binding And Window Wiring

**Objective**

Connect the window to the project and the tree and the list to each other.

**Scope**

* `MainWindowViewModel` follows the project of the `ProjectManager`
* Selected folder of the tree drives the file list; the selected file is available for the content
* Initial state: a new project, empty tree, list and content area
* TestFX integration test for the left side of the window
* README, MkDocs main window page, CHANGELOG check
* NOT: open, save, file system access, the content parts

**Affected Areas**

* `MainWindowViewModel`, `MainWindowView`, `MainWindowIT`, `docs`, `README.md`

**Dependencies**

IP-02, IP-03, FP-001 IP-07.

**Expected Result**

The running window shows the empty new project; filled in tests it shows tree, files and icons, and
the selected file is handed on to the content area.

**Technical Considerations**

* The `ProjectManager` creates the new project on start; the view model only follows it
* Tests fill the model directly through the manager's change operations
* Registration on the manager is released with the window

### IP-05: History List And Media View

**Objective**

Show the media history of the selected file and the selected medium.

**Scope**

* `HistoryList` component on the right side of the content area, medium `000` up to the last one
* The last medium is selected automatically whenever a file is selected
* `MediaView` component showing the selected medium as image, via `MediaPreview`
* Empty content for no selection and for a medium that cannot be shown
* Window integration test for the content area
* NOT: generation details, prompt input

**Affected Areas**

* `ui/component`, `MainWindowView`, `MainWindowViewModel`, message bundles, tests

**Dependencies**

IP-04, FP-001 IP-02.

**Expected Result**

Selecting a file lists its complete history with the last medium selected and shown in the middle;
selecting another medium changes the image.

**Technical Considerations**

* The history list is bound to the media of the file in order; the order is the medium number
* Auto selection must also happen when the history grows (a medium added later)
* Entries of the history list also use `MediaPreview` for their icons
* Large images need scaling to the available space and lazy loading

### IP-06: Prompt Input Part

**Objective**

Build the input part below the image.

**Scope**

* `PromptInput` component: multi-line prompt text box
* "+" button above the left side of the text box; it opens the gallery dialog
* Gallery dialog: virtual directory structure, only references selectable, the last medium counts
* Model toggle buttons above the right side of the text box (placeholders)
* Model combobox above the toggle buttons (placeholder)
* Placeholder model list behind an interface
* Placeholder area for further parameters, filled by FP-009 IP-04 from the fields of the flow
* An added image is held by the UUID of its reference; its last medium is the one used
* NOT: sending the prompt, generation, storing the prompt, real models

**Affected Areas**

* `ui/component`, gallery dialog, message bundles (English, German via `translator`), tests;
  icons are not part of this plan, the "+" button carries a text until they exist

**Dependencies**

IP-01.

**Expected Result**

The input part is shown below the image with all controls; the controls react but trigger no
generation.

**Technical Considerations**

* The "+" button opens a gallery dialog (decided); the dialog follows the `dialog` skill
* Added images are shown (for instance as small chips) so the user sees what was added
* Added images are not stored as generation detail in the history
* Toggle buttons and combobox are placeholders behind a model interface

### IP-07: Generation Details Panel

**Objective**

Show the applied prompt and further generation data of the selected medium.

**Scope**

* `GenerationDetails` component beside the image, shown as far as space allows
* Show the applied prompt, the model and the flow of the selected medium from the FP-001 history details
* Explicitly used additional images are not part of the generation data
* Two lines: model and flow, then the beginning of the prompt ending with `...`
* Empty fields for a medium without details
* NOT: creating generation data, storing generation data

**Affected Areas**

* `ui/component`, message bundles, tests

**Dependencies**

IP-05, FP-001 IP-02.

**Expected Result**

A panel shows the applied prompt, the model and the flow of the selected medium, empty for a medium
without details.

**Technical Considerations**

* "As far as possible" means the panel gives way when the window is narrow; a minimum width or
  hiding is to be fixed
* FP-001 now stores prompt, model and flow per medium (done first); the additionally used images
  are not part of them
* Two lines: model and flow, then the beginning of the prompt cut off with `...`; the cut-off length
  follows the width of the panel

## 8. Dependency Graph

```text
IP-01 (COMPLETED)
├── IP-02 (COMPLETED) ──┐
├── IP-03 ──┴── IP-04 ── IP-05 ── IP-07
└── IP-06
```

IP-02, IP-03 and IP-06 can run in parallel after IP-01. IP-02 to IP-05 and IP-07 also need parts of FP-001
(IP-02 property models and IP-07 `ProjectManager`); IP-01 and IP-06 do not.

## 9. Risks and Open Questions

* FP-001 is completed: the property models and the `ProjectManager` exist, so IP-02 to IP-05 are
  not blocked
* Decided: `FlowList`, the example flows and the selection label are removed (demo only); the
  Claude rule set then uses the directory tree as example component (IP-02)
* Decided: the content area has three parts, dividers 0.2 and 0.8
* Decided: the generation data sit at the top left beside the image: model and flow in the first
  line, the beginning of the prompt ending with `...` in the second
* Decided: the history details (prompt, model, flow; no additional images) are part of FP-001 and
  done first; where they are stored in the ZIP is open there
* Decided: the "+" button opens a gallery dialog; only references selectable through the internal
  virtual directory structure; the last medium counts and is shown
* Decided: images added with "+" stay in the input part until used
* Decided: the tree shows only folders, with a grey child count in brackets (direct children,
  subfolders included)
* Decided: the file list shows only the files (entries) of the selected folder, never folders; the
  tree shows only folders, never files
* Decided: only the image formats of standard JavaFX
* Decided: a file is a row with preview images
* OPEN: the size of the preview image in a row
* Decided: an entry with one medium shows the icon twice; a tooltip tells original and current
  medium apart
* Decided: registry in code for further media types
* Decided: icons are needed, but the user delivers the orientation and they are not part of any plan
* Decided: the `ProjectManager` creates the initial new project
* Persistence of divider positions between runs is not part of this feature (FP-010 IP-02)
* Without file system access the previews are only visible in tests; a new project is empty

## 10. Feature Completion Criteria

* The main window shows an outer splitter at 0.3 and, on its left side, an inner vertical splitter
  at 0.5, both draggable; the content area has a splitter with the dividers at 0.2 and 0.8
* The directory tree shows the virtual tree of the project held by the `ProjectManager`
* Changing the selection in the tree changes the files shown in the list
* Every file shows the icon of its original medium, directly behind it the icon of its last created
  medium
* Selecting a file lists its complete history on the right of the content area, with the last medium
  selected, and shows the selected medium in the middle
* The generation data (prompt, model, flow) of the selected medium are shown
* Below the image the prompt text box, the "+" button, the model toggle buttons and the model
  combobox are present
* A new project shows an empty tree, list and content area without errors
* A medium that is not an image, and a broken image, show a placeholder and do not break the window
* A further media type can be added by adding a provider only
* English and German texts exist for every new key
* `build` passes; README, MkDocs and KDoc are up to date
