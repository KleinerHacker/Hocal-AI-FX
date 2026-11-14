# Feature Plan: Main Window Layout

## 1. Objective

The main window is built around the project: the virtual directory tree and the file list on the left, the content
of the selected file on the right. The content area shows the media history of the selected file, the selected
image, its generation details and the input part for a new generation. The window follows the project the
`ProjectManager` holds; it starts with a new, empty project and has no connection to the file system.

## 2. Implementation

### Layout

* `MainWindowView` holds the menu bar (File > Exit) and three nested `SplitPane`s: `splMain` (project side 0.3 |
  content area), `splProject` (tree above file list, 0.5), `splContent` (generation details | media with prompt
  input | history list, dividers 0.2 and 0.8)
* The divider positions are set once the pane has a size; the project side, the generation details and the history
  list are `resizableWithParent=false` and keep their width, the media with the prompt input takes the difference

### Components (`app/ui`, package `ui.component`, MVVM FX, `fx:root`)

* `ProjectTree` - the folders of the project with a grey child count; selection and expanded state by object identity
* `ProjectFileList` - the files of the selected folder, one row with the original and the current medium as preview
  and the name
* `HistoryList` - all media of the selected file from `000`; the last one is selected when another file is chosen or
  the history grows
* `MediaView` - the selected medium scaled to the available space; a placeholder icon for a medium that cannot be shown
* `GenerationDetails` - first line model and flow of the selected medium, second line the beginning of its prompt; a
  label cuts it with `...` at the width of the panel; empty for a medium without details
* `PromptInput` - prompt box, send and add button, model combobox, flow `SegmentedButton` (placeholders behind
  `ModelProvider`), chips of the added images; the add button opens the `GalleryDialog`
* `GalleryDialog` (with `HocalDialog`) - the first dialog and the template of the `dialog` skill; the source set
  `demo` with `runDialogDemo` shows it
* `MainWindowViewModel` follows the project: `tree`, `selectedFolder`, `listFolder`, `selectedFile`, `historyFile`,
  `selectedMedium` and `mediaSource`; the project and the media changes are observed between `onShow` and `onHide`

### Media and previews (`ui.preview`)

* `MediaSource` (count, extension, content, `details`) with `ProjectMediaSource` on the `ProjectManager`; every access
  from a background thread moves to the FX thread
* `MediaPreview`, `ImageMediaPreview`, `MediaPreviewRegistry` as extension point in code; `ThumbnailLoader`,
  `ThumbnailService` (the shared cache), `MediumDetails`, `MediaChanges`
* Previews are loaded lazily in the background; a broken medium shows a placeholder and breaks nothing

### Libraries

* `lib/crypto` (`hocal-crypto`) - the encryption moved out of `lib/model`: block stream cipher, key derivation, record
  cipher, sealed `CryptoException`; `ProjectStorage` maps its errors to the project errors
* `lib/thumbnail` (`hocal-thumbnail`) - generic caches (`MemoryThumbnailCache`, `SizeClassCache`,
  `TieredThumbnailCache`), `ZipThumbnailStore` (uncompressed ZIP, encrypted per entry for encrypted projects),
  `ImageThumbnails`; from the toolkit only `javafx.scene.image`
* `ProjectManager` owns the store: `thumbnailCache` forwards to it, `thumbnailStorage` (`MEMORY` default, `DISK`)
  selects the system, the file `thumbnails-<n>.zip` lives in the session folder and is removed on close; `saveAs`
  renews the store when the password changes

### Other

* Icons `media-placeholder`, `add`, `remove`, `send` (light and dark); buttons show icons only
* `AppearanceObserver` and `RefreshableObjectProperty<T>` are shared building blocks of the newer components

## 3. Decisions that differ from the plan

* The window keeps the width of the side parts on resize instead of the divider ratios
* The toggle button bar chooses the flow; the model is chosen with the combobox, and the flows follow the model
* `app/ui` depends on `hocal-fx-model`; the title bar with the file name belongs to FP-003 IP-02
* A thumbnail cache and a send button with a handler without implementation were added
* The encryption and the thumbnail cache got their own libraries (IP-08, IP-09); memory is the default of the
  thumbnail system and its selection in the settings dialog follows in FP-010 (General category)
* Generation details show the ids of model and flow, without captions and without message keys, until a catalog
  delivers names

## 4. Implementation Plans

| ID    | Implementation Plan               | Result                                                                          |
|-------|-----------------------------------|---------------------------------------------------------------------------------|
| IP-01 | Main Window Splitter Layout       | Nested splitters with placeholders; `FlowList` and its example data removed     |
| IP-02 | Directory Tree Component          | `ProjectTree` with child counts, selection by identity                          |
| IP-03 | File List And Media Preview       | `ProjectFileList`, preview extension point, thumbnail cache                     |
| IP-04 | Project Binding And Window Wiring | Window follows the `ProjectManager`; `ProjectMediaSource`, `MediaChanges`       |
| IP-05 | History List And Media View       | `HistoryList` with last medium selected, `MediaView`                            |
| IP-06 | Prompt Input Part                 | `PromptInput`, `GalleryDialog`, placeholder models and flows, icons             |
| IP-07 | Generation Details Panel          | `GenerationDetails` with `MediumDetails` from `MediaSource.details`             |
| IP-08 | Crypto Library                    | `lib/crypto` with block stream cipher, key derivation and record cipher         |
| IP-09 | Disk Thumbnail Cache              | `lib/thumbnail`, ZIP store, `ProjectManager` owns the session store             |

## 5. Known Limits

* Only the image formats of standard JavaFX are previewed; other media show the placeholder
* `MediaChanges.changed` is not called by any flow yet; the flows that append or remove media (FP-006) have to
* The file list has its own thread pool (`loadThreads`), to be set from the settings of FP-010 IP-04
* The model combobox, the flow bar and the send button are placeholders until FP-006 and FP-009
* The positions of the dividers are not stored between runs (FP-010 IP-02)
* OPEN: the user still has to look at the gallery dialog in the demo, in the light and in the dark colour scheme
  (`dialog` skill)

## 6. Completion Criteria

* [x] The main window shows an outer splitter at 0.3 and, on its left side, an inner vertical splitter at 0.5, both
  draggable; the content area has a splitter with the dividers at 0.2 and 0.8
* [x] The directory tree shows the virtual tree of the project held by the `ProjectManager`
* [x] Changing the selection in the tree changes the files shown in the list
* [x] Every file shows the icon of its original medium, directly behind it the icon of its last created medium
* [x] Selecting a file lists its complete history on the right of the content area, with the last medium selected,
  and shows the selected medium in the middle
* [x] The generation data (prompt, model, flow) of the selected medium are shown
* [x] Below the image the prompt text box, the send and add buttons, the model combobox and the flow toggle button bar
  are present
* [x] A new project shows an empty tree, list and content area without errors
* [x] A medium that is not an image, and a broken image, show a placeholder and do not break the window
* [x] A further media type can be added by adding a provider only
* [x] English and German texts exist for every new key
* [x] `build` passes; README, MkDocs and KDoc are up to date
