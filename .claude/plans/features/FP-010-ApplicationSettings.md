# Feature Plan: Application Settings

## 1. Objective

Give the application persistent settings. Two kinds exist: hidden settings that the application keeps
by itself (window position, size and state, splitter positions) and visible settings that the user
changes in a settings dialog opened from `File -> Settings`. The dialog is divided into categories:
`General`, `View`, `Models`, `ComfyUI` and `Generation`. The model list of FP-009 moves into the
`Models` category. The same feature adds the Help menu with an About dialog and the list of the
components with their licences. It also creates an own theme (light, dark, following the system) and
extends the application to many languages, including languages with other scripts and a right-to-left
writing direction.

## 2. Current State

* The application has no settings storage; no preferences, no config file, no remembered window state
  (FP-005 states this; FP-002 explicitly leaves the splitter positions out)
* FP-002 (planned) defines the main window with three splitters (dividers 0.3, 0.5, and 0.2/0.8 in
  the content area), all with fixed initial positions
* FP-003 (planned) defines the File menu (New, Open, Save, Save As, Exit) with icons and the dialog
  pattern; it decided "no recent files list for now, it would need a stored setting" - this feature
  supplies the setting and the list
* FP-009 (planned) IP-06 provides the model list as a self-contained component opened from a menu
  entry and announces its move into a settings dialog - this feature
* FP-001 (planned) leaves a size limit for the project file open; FP-008 leaves a size limit for
  imported files open
* FP-004/FP-005 (planned) leave the server address, port and install settings to a later feature;
  FP-005 IP-07 plans the restart control of the service
* FP-005 (planned) fixes a data directory per operating system for ComfyUI; there is no config
  directory for the application itself
* The Java FX theme was removed earlier; the controls run on the default look; icons may have light
  and dark variants (FP-003)
* `lib/model` (POJOs), `lib/fx-model` (property models, `ProjectManager` singleton) are the pattern for
  data and its holder; the `fx-model` skill demands a property model for every POJO there

## 3. Target State

* A settings file in the per-user configuration directory of the operating system, JSON, versioned,
  written atomically; a missing or broken file falls back to defaults without a crash
* The main window opens where it was closed: position, size, maximized state and all splitter
  positions are restored; a position outside every connected screen is corrected
* `File -> Settings` opens a modal dialog with a category list on the left and the page of the chosen
  category on the right; buttons OK, Cancel, Apply and "Restore defaults" (per category)
* The categories: `General`, `View`, `Models`, `ComfyUI`, `Generation`
* An own theme (stylesheets with light and dark colour schemes) styles every window; the appearance
  follows the system or the user's choice
* The application is available in English (base) and in about 40 further languages, grouped in waves
  (see requirements); languages with other scripts and right-to-left languages work in layout, fonts
  and formatting
* The limits are enforced where media and project files are read or added
* The File menu lists the recently used projects; the Help menu opens an About dialog with the
  components and their licences

## 4. Requirements

### Functional Requirements

* Hidden settings (not shown in the dialog):
    * window: x, y, width, height of the normal state, maximized flag
    * splitters: the divider positions of the outer splitter, the inner vertical splitter and the two
      dividers of the content area
    * stored on a change with a delay (coalesced), and when the window closes; restored at start
    * a stored position that is on no connected screen is corrected to the primary screen; a stored
      size is limited to the screen
    * dialog sizes are not stored
* Category `General`:
    * maximum image size: width and height in pixels, default 4096 x 4096; applies to imported media
      and to input images sent to ComfyUI; larger images are refused with a clear message (no resizing)
    * maximum project file size in gigabytes (1 GB = 1,000,000,000 bytes); default 2 GB, range 50 MB to
      16 GB; applies on open (a larger file is refused before it is read), on save and when adding a
      medium (a medium that would exceed the limit is refused, the message shows size and limit)
    * confirmations: ask before deleting a medium, folder or reference, on by default
    * recent projects: number of entries remembered (default 10), 0 turns the list off; the File menu
      shows the entries and a clear entry; a missing file is reported and removed from the list
    * preview threads: number of threads that load the thumbnails of the file list at the same time,
      default 5, range 1 to 16; a change applies to loads that start afterwards (the file list of FP-002
      offers `loadThreads` for it)
* Category `View`:
    * language: system default or one of the supported languages, listed by their own names (for
      example Deutsch, Français, 日本語, العربية) so a user finds his language; takes effect after a
      restart (the bundle is chosen at start), a hint is shown at the field
    * appearance: system default, light, dark; takes effect at once in every open window
* Theme (own theme of the application):
    * a base stylesheet and two colour schemes (light, dark) defined by named colours and fonts, so
      all windows, dialogs, the splash screen, menus and the controls used look consistent
    * "system default" follows the colour scheme of the operating system, also when it changes while
      the application runs
    * the icon variants (`<name>-dark@32.png`) follow the scheme; contrast is checked
    * the design reference `docs/docs/assets/images/file.png` (to be delivered by the user) gives the
      look; without it a neutral palette is used
* Languages (supported UI languages; the base text is English; models and flows keep plain names):
    * wave 1, major languages: German (de), French (fr), Spanish (es), Italian (it), Brazilian Portuguese (pt),
      Dutch (nl), Polish (pl), Russian (ru), Ukrainian (uk), Turkish (tr)
    * wave 2, further European languages: Swedish (sv), Danish (da), Norwegian Bokmal (nb), Finnish (fi),
      Czech (cs), Slovak (sk), Hungarian (hu), Romanian (ro), Bulgarian (bg), Greek (el), Croatian (hr),
      Serbian (sr, Cyrillic and Latin), Slovenian (sl), Lithuanian (lt), Latvian (lv), Estonian (et),
      Catalan (ca)
    * wave 3, other scripts and forms: Simplified Chinese (zh-Hans), Traditional Chinese (zh-Hant),
      Japanese (ja), Korean (ko), Arabic (ar, right to left), Hebrew (he, right to left), Persian
      (fa, right to left), Hindi (hi, Devanagari), Bengali (bn), Thai (th), Vietnamese (vi),
      Indonesian (id), Malay (ms)
    * further languages are not part of this feature (candidates: Icelandic, Irish, Maltese, Albanian,
      Macedonian, Bosnian, Belarusian, Basque, Galician, Welsh, Urdu); a language is added by a bundle
      alone and the entry in the language list
    * a language is offered only when its bundle has exactly the key set of the base bundle
    * right-to-left languages mirror the layout completely (menus, panes including the order of the
      splitter parts, dialogs, directional icons)
    * the languages are shipped by waves; a release contains the finished waves only
    * text is shown with fonts that cover the script (system fallback fonts); number, date and size
      formats follow the language
    * texts avoid sentences that depend on plural forms ("{0} waiting"); where a count is shown it is
      written as a label with the number ("Waiting: {0}"), because `MessageFormat` cannot express the
      plural rules of languages such as Russian, Polish or Arabic
    * all translations are produced by the `translator` agent; they are machine quality and are marked
      as such in the README until a native speaker has reviewed a language
* Category `Models`:
    * the list of all configured models of the catalog with state, licence, Hugging Face link, a
      download button or a delete button, progress and cancel (the component of FP-009 IP-06)
    * all models can be downloaded from here, on demand only, never automatically
    * a separate setting inside the list page: maximum number of parallel downloads (default not
      limited); a change does not touch running downloads and applies to downloads started afterwards;
      downloads above the limit wait with the state "waiting" and start when a slot frees up
    * the model directory is shown (read-only)
    * the download indicator at the top right opens the dialog on this category
* Category `ComfyUI` (service, from FP-004/FP-005):
    * restart of the service (the control planned in FP-005 IP-07 moves here, the confirmation about
      running jobs stays)
    * start mode: on first use (default) or at application start
    * shows the installed ComfyUI version, the Python environment, the log directory and the data
      directory (read-only); a button opens the log directory
    * backend choice (CUDA, ROCm, MPS, CPU) with "reinstall"; the port is automatic (decided in FP-005)
      and is not offered
* Category `Generation` (from FP-006):
    * a finished generation tile closes automatically after N seconds (0 = stays until closed)
    * maximum number of queued jobs (default 10)
    * default for the seed: random for every send, or fixed
    * live preview during generation on or off (the preview method of ComfyUI); it is a start option of
      the service, so a change applies at the next start of the service and shows the restart hint
* Help menu:
    * entry `About`: application name, version, short description, own licence
    * entry for the components: list of the used components with name, version and licence (libraries
      from the build, ComfyUI as downloaded GPL-3.0 software, Python, `uv`, PyTorch, and the models of
      the catalog with their licences), each with a link where one exists
    * the list is generated at build time from the data the licence check of the build already has
* Not in this feature: project settings (default encryption, backup copy), autosave, proxy settings,
  remote servers, updates
* Dialog behaviour:
    * OK stores and closes, Cancel discards, Apply stores and stays; "Restore defaults" resets the
      shown category after a confirmation
    * invalid input is shown at the field and blocks OK and Apply; numeric limits are checked against
      their ranges
    * a setting that needs a restart (language, ComfyUI start mode) shows a hint at the field and
      changes only after the restart
    * the `Models` page acts immediately (a download or a delete cannot be undone by Cancel); this is
      shown on the page
    * unknown categories and fields of newer versions are kept when the file is written (no data loss)
    * the dialog has the keyboard shortcut `Ctrl+,`
    * every text through message bundles, German through the `translator`

### Technical Requirements

* The settings are a model: POJOs in `lib/model`, property models in `lib/fx-model` (skill `fx-model`,
  agents `model-explore` and `model-creator`), a holder singleton like `ProjectManager`
  (`SettingsManager`), the file access in `lib/model` without Java FX
* The JSON file is written with Jackson (already a dependency), a `schemaVersion`, unknown fields are
  preserved, atomic write via temporary file and replace
* The configuration directory: Windows `%APPDATA%`, macOS `~/Library/Application Support`, Linux
  `$XDG_CONFIG_HOME` or `~/.config`, below a folder with the application's name; it is separate from the
  data directory of FP-005 (settings stay when the runtime is reinstalled)
* Settings are read once at start (before the main window opens) and written with the delay described
  above; the write never blocks the UI
* Consumers read a value at the moment of use, not once at start, so a change applies to the next
  operation without a restart; exceptions are the restart-bound settings
* Languages are `.properties` bundles next to the base bundle (UTF-8, naming per `ui-styling`); the
  loading follows the Java `ResourceBundle` rules; the file naming of scripts and regions
  (`zh_Hans`, `zh_Hant`, `sr_Latn`, `nb`, `he`, `id`) and the fallback chain are checked at
  implementation; the runtime image must contain the locale data (`jdk.localedata`)
* A build check fails when a bundle misses or adds a key against the base bundle (already demanded by
  `ui-styling`) and when a placeholder differs
* Layout direction is taken from the language (`NodeOrientation`); controls are written so they
  mirror (no fixed left/right assumptions in FXML and CSS)
* The theme is CSS (skill `ui-styling`), applied to every scene through one place; the colour scheme
  of the system is read from the JavaFX platform preferences (checked against the JavaFX version)
* No new dependency for plural rules or fonts without asking the user first
* The licence list is produced by the build (a Gradle task using the data of the existing licence
  check); no new dependency without asking the user first
* Tests need no UI for the store and the validation; the dialogs use TestFX (skill `testing`)
* Skills before work: `fx-model`, `fx-component-lifecycle`, `ui-styling`, `dialog`, `component`,
  `window`, `icons`, `translation`, `testing`, `project-docs`, `ci-pipeline`, `startup`

## 5. Architecture

* `lib/model`, package `settings`: `AppSettings` (root with `schemaVersion`), `WindowSettings`,
  `GeneralSettings`, `ViewSettings`, `ModelSettings`, `ComfySettings`, `GenerationSettings`
  (categories as nested POJOs); `SettingsStore` (read, write, defaults, merge of unknown fields)
* `lib/fx-model`: `<Pojo>Property` for each; `SettingsManager` singleton with an `AppSettingsProperty`,
  `load()`, `save()` (delayed, coalesced), `resetCategory(...)`
* `app/ui`:
    * `SettingsDialog` (dialog with a category list and a content area), one view and view model per
      category page as a component
    * `File -> Settings` menu entry (icon, see risks), the recent projects entries, the shortcut
    * `WindowStateKeeper` observes the main stage and the three splitters, restores them at start
    * the model list component of FP-009 IP-06 is placed in the `Models` page
    * the download indicator opens the dialog on the `Models` page
    * Help menu with the About dialog and the components dialog
* Limit checks: `MediaLimits` in `lib/model` (pure functions on pixel size and byte size) used by the
  `ProjectManager` (open, add medium, save) and by the import and input operations of FP-008/FP-006
* `app/ui` theme: stylesheet files under the resources, an `AppearanceManager` that applies the scheme
  to all scenes and listens to the system scheme; `HocalIcons` chooses the icon variant
* `app/ui` languages: `LanguageRegistry` (supported languages, own names, direction, fallback),
  start-up locale selection from the setting, orientation handling, bundles per language
* `lib/comfy-runtime`: the download of FP-009 IP-05 gets a slot limit and the state "waiting"
* Build: a Gradle task writes the component and licence list as a resource of `app/ui`
* Data flow: file <-> `SettingsStore` <-> `SettingsManager` <-> property models <-> dialog pages and
  consumers
* Persistence: one JSON file in the configuration directory; nothing is written to the project file

Example of the shape (illustration only, not a decision on the final names):

```json
{
  "schemaVersion": 1,
  "window": { "x": 120, "y": 80, "width": 1600, "height": 900, "maximized": false,
              "dividers": { "outer": 0.3, "inner": 0.5, "content": [0.2, 0.8] } },
  "general": { "maxImageWidth": 4096, "maxImageHeight": 4096, "maxProjectBytes": 2000000000,
               "confirmDelete": true, "recentProjects": 10, "recent": [], "previewThreads": 5 },
  "view": { "language": "system", "appearance": "system" },
  "models": { "maxParallelDownloads": 0 },
  "comfy": { "startMode": "onDemand" },
  "generation": { "tileCloseSeconds": 5, "maxQueue": 10, "randomSeedDefault": true, "livePreview": true }
}
```

## 6. Implementation Plan Overview

| ID    | Implementation Plan                  | Objective                                                                   | Dependencies                                          |
|-------|--------------------------------------|-----------------------------------------------------------------------------|-------------------------------------------------------|
| IP-01 | Settings Model And Store             | POJOs, property models, `SettingsManager`, JSON file, defaults, validation  | FP-001 IP-01, IP-02                                   |
| IP-02 | Window State Persistence             | Store and restore window bounds, state and all splitter positions           | IP-01, FP-002 IP-01                                   |
| IP-03 | Settings Dialog Frame                | `File -> Settings`, dialog with category list, buttons, validation, hints   | IP-01, FP-003 IP-02                                   |
| IP-04 | General Category                     | The `General` page: limits, confirmations, recent projects and their menu   | IP-03, FP-003 IP-02                                   |
| IP-05 | View Category                        | The `View` page: choose language and appearance                             | IP-03, IP-10, IP-11                                   |
| IP-06 | Models Category                      | Model list in the dialog, parallel download setting, waiting state          | IP-03, FP-009 IP-05, IP-06                            |
| IP-07 | ComfyUI And Generation Categories    | The `ComfyUI` and `Generation` pages and the settings they control          | IP-03, FP-005 IP-07, FP-006 IP-02, IP-04              |
| IP-08 | Limit Enforcement                    | Apply the maximum image size and project file size where media and files enter | IP-04, FP-001 IP-07, FP-008 IP-01, FP-006 IP-03    |
| IP-09 | Help Menu And Licence Dialog         | Help menu, About dialog, generated component and licence list               | FP-002 IP-01, FP-009 IP-02                            |
| IP-10 | Theme And Appearance                 | Own theme with light and dark scheme, system following, icon variants       | FP-002 IP-01                                          |
| IP-11 | Language Support                     | Language registry, loading, right-to-left layout, fonts, formats, checks    | IP-01                                                 |
| IP-12 | Translations                         | Bundles for the languages in three waves through the `translator` agent     | IP-11                                                 |

## 7. Implementation Plans

### IP-01: Settings Model And Store

**Objective**

Create the settings as a model with a persistent store.

**Scope**

* POJOs for the categories and the root, property models, `SettingsManager` singleton
* `SettingsStore`: configuration directory per operating system, JSON, schema version, atomic write,
  defaults for missing or broken content, preserving unknown fields
* range validation of the numeric limits in the model, including the number of preview threads
* delayed, coalesced writing
* tests: round trip, missing file, broken file, unknown field kept, atomic write, ranges
* not: any UI, window state handling

**Affected Areas**

* `lib/model` package `settings`, `lib/fx-model`, README structure, docs

**Dependencies**

FP-001 IP-01 (Jackson, module setup), FP-001 IP-02 (the property model pattern); the singleton follows
FP-001 IP-07.

**Expected Result**

* settings can be loaded, changed through property models and saved; defaults apply on any problem

**Technical Considerations**

* the `fx-model` skill and its agents are mandatory for the POJOs and property models
* defaults live in one place and are used by the dialog for "Restore defaults"
* a settings file of a newer version is read as far as possible and written back without loss

### IP-02: Window State Persistence

**Objective**

Open the main window as the user left it.

**Scope**

* observe position, size and the maximized state of the main window and the divider positions of the
  three splitters
* restore at start before the window shows; apply divider positions once the scene has a size
* correct positions outside connected screens; limit size to the screen; handle several monitors
* write on a delay and when the window closes
* tests: correction of off-screen positions as pure logic; TestFX for dividers
* not: storing dialog sizes

**Affected Areas**

* `app/ui` main window view, `WindowStateKeeper`, tests

**Dependencies**

IP-01, FP-002 IP-01 (the splitter layout).

**Expected Result**

* a second start reproduces position, size, maximized state and all splitter positions

**Technical Considerations**

* store the normal bounds while maximized so un-maximizing returns to the right size
* the skills `fx-component-lifecycle` (listeners) and `window` apply

### IP-03: Settings Dialog Frame

**Objective**

Provide the dialog around the category pages.

**Scope**

* `File -> Settings` with icon and shortcut `Ctrl+,`; dialog (skill `dialog`) with category list and
  page area
* OK, Cancel, Apply, "Restore defaults" per category with confirmation
* page contract: a page offers its fields, validation state and "needs restart" markers
* dirty tracking, validation display at the field, OK and Apply blocked on invalid input
* opening on a given category (used by the download indicator)
* message bundles, German through the `translator`
* not: the content of the pages

**Affected Areas**

* `app/ui` dialog, File menu, icon `settings@32.png` with variants, bundles

**Dependencies**

IP-01, FP-003 IP-02 (File menu).

**Expected Result**

* the dialog opens with placeholder pages and handles the buttons and the validation

**Technical Considerations**

* the edit works on a copy of the settings; Apply and OK write it back as one change
* the icon needs the design reference, which is still missing (see risks)

### IP-04: General Category

**Objective**

Build the `General` page and the recent projects list.

**Scope**

* maximum image size (width and height) and maximum project file size in GB
* confirmations before deleting
* preview threads (thumbnail loading of the file list), applied to `ProjectFileListViewModel.loadThreads`
* recent projects: number, the list in the File menu, a clear entry, handling of missing files
* validation messages with the allowed ranges
* tests of the page, its view model and the list
* not: the enforcement (IP-08)

**Affected Areas**

* `app/ui` page component, view model, File menu, bundles, `lib/model` settings (recent entries)

**Dependencies**

IP-03; FP-003 IP-02 (the open flow that the entries call).

**Expected Result**

* the user changes the limits and options, they are stored, and the File menu offers recent projects

**Technical Considerations**

* the list holds paths only; an encrypted project asks for its password as always
* the delete confirmation setting is read by the delete flows of FP-008 at the moment of use

### IP-05: View Category

**Objective**

Build the `View` page with the choice of language and appearance.

**Scope**

* language list with the own names, "system default" first; only languages that are complete are shown
* appearance: system, light, dark
* stored; the appearance applies at once, the language after a restart with a hint
* tests of the page and of the selection logic
* not: the mechanisms (IP-10, IP-11), the translations (IP-12)

**Affected Areas**

* `app/ui` page component, view model, bundles

**Dependencies**

IP-03; IP-10 (appearance), IP-11 (languages).

**Expected Result**

* the user chooses language and appearance; the appearance changes at once, the language after a restart

**Technical Considerations**

* the page lists the languages from the registry, so a new bundle needs no change of the page
* the language list scrolls; its entries are sorted by the own name or by the code (decided there)

### IP-06: Models Category

**Objective**

Make the model list a page of the settings dialog and limit parallel downloads.

**Scope**

* place the self-contained model list component of FP-009 IP-06 in the `Models` page
* remove the model menu entry; the download indicator opens the dialog on this page
* setting "maximum parallel downloads" inside the page, separate from the list; 0 or empty means not
  limited
* slot logic in the download of FP-009 IP-05: downloads above the limit are "waiting" and start when a
  slot frees up; a changed limit applies only to downloads started afterwards
* the state "waiting" in the rows and in the indicator count
* the model directory display; the note that this page acts immediately
* tests of the slot logic and the setting

**Affected Areas**

* `app/ui` page, `lib/comfy-runtime` package `models`, FP-009 IP-05 and IP-06 components

**Dependencies**

IP-03, FP-009 IP-05, IP-06.

**Expected Result**

* all models can be downloaded or removed from `File -> Settings -> Models`; the limit works as described

**Technical Considerations**

* FP-009 IP-06 keeps its list component free of window assumptions, so the move is only a placement
* FP-009 IP-05 is adjusted so that its download accepts a slot count; the plans are aligned
* the hint in the model combo box links to this page

### IP-07: ComfyUI And Generation Categories

**Objective**

Add the `ComfyUI` and `Generation` pages.

**Scope**

* `ComfyUI`: restart, start mode, read-only information, open log directory, backend with reinstall
* `Generation`: tile closing time, queue limit, seed default, live preview
* each setting is read by its consumer at the moment of use
* tests of the pages
* not: project settings, proxy, remote servers

**Affected Areas**

* `app/ui` pages, consumers in FP-005 and FP-006

**Dependencies**

IP-03; FP-005 IP-07 (service control), FP-006 IP-02 (generation), IP-04 (progress tile).

**Expected Result**

* both pages work and every setting takes effect

**Technical Considerations**

* the restart control of FP-005 IP-07 moves; its confirmation about running jobs stays
* a setting is only offered if its consumer exists; otherwise it waits
* the start mode change takes effect at the next start

### IP-08: Limit Enforcement

**Objective**

Make the maximum image size and the maximum project file size effective.

**Scope**

* `MediaLimits`: pure checks for pixel size and bytes
* image size: on import, on input images, on received results
* project size: on open (before the file is read), on save, when adding a medium
* clear messages naming the actual value and the limit; no automatic resizing
* tests with limits changed at run time

**Affected Areas**

* `lib/model`, `ProjectManager`, import operations (FP-008), generation input (FP-006), dialogs

**Dependencies**

IP-04, FP-001 IP-07, FP-008 IP-01, FP-006 IP-03.

**Expected Result**

* a too large image or project is refused with a precise message and nothing is changed

**Technical Considerations**

* reading the pixel size must not decode the whole image (read the header only)
* a project over the limit that already exists can still be opened after the limit is raised

### IP-09: Help Menu And Licence Dialog

**Objective**

Show what the application is and which components with which licences it uses.

**Scope**

* Help menu with the entries About and Components
* About dialog: name, version, short description, own licence
* Components dialog: list with name, version, licence and link
* a Gradle task that generates the list from the data of the existing licence check into a resource
* fixed entries for what is not a Gradle dependency: ComfyUI (GPL-3.0, downloaded, not shipped),
  Python, `uv`, PyTorch
* the models of the catalog with their licences, read from the catalog at run time
* a README note on the licences (as proposed earlier)
* not: update checks, third-party notices of the models beyond name, licence and link

**Affected Areas**

* `app/ui` menu bar and dialogs, `app/ui/build.gradle.kts`, resources, README, bundles

**Dependencies**

FP-002 IP-01 (menu bar), FP-009 IP-02 (catalog).

**Expected Result**

* the user sees the application information and a complete, build-generated component list

**Technical Considerations**

* the existing licence check (`licensee`) is used; a new dependency would have to be asked first
* the list is reproducible and checked by the build so it cannot go stale
* the About version comes from the build

### IP-10: Theme And Appearance

**Objective**

Create the own theme and make the appearance switchable.

**Scope**

* base stylesheet and the colour schemes light and dark with named colours, fonts and sizes
* styling of every control the application uses, of menus, dialogs, splitters, list views, the model
  list, the generation tile and the splash screen
* `AppearanceManager`: applies the scheme to all scenes, switches at run time, follows the system
  scheme and its changes
* icon variants chosen by the scheme; missing dark variants are created (skill `icons`, agent
  `icon-creator`)
* update of the `ui-styling` skill for the theme rules
* tests: the scheme choice logic; TestFX that a scene carries the stylesheet
* not: custom themes of the user, accent colour choice

**Affected Areas**

* `app/ui` resources (stylesheets, icons), `HocalIcons`, all views, `ui-styling` skill, README

**Dependencies**

FP-002 IP-01 (the main window); the settings of IP-01 are used by IP-05 to store the choice.

**Expected Result**

* the application looks the same in all windows and switches between light and dark without restart

**Technical Considerations**

* the earlier removal of the Java FX theme is the starting point: the own theme is plain CSS on the
  default control skins
* the look is taken from the design reference of the user; without it a neutral palette is used
* contrast of text and icons is checked in both schemes
* every new window has to take part; a central place applies the stylesheet to every scene

### IP-11: Language Support

**Objective**

Make the application ready for many languages, scripts and writing directions.

**Scope**

* `LanguageRegistry`: supported languages with code, own name, direction and fallback
* locale selection at start from the setting, `system default` resolved to the best match
* file naming and loading of the bundles for scripts and regions; fallback to English for a missing key
* the build check of the key set and the placeholders of every bundle against the base bundle
* right-to-left layout through `NodeOrientation`; mirrored directional icons; review of the layouts
* fonts: use system fallback fonts for CJK, Arabic, Hebrew, Devanagari, Bengali and Thai; check the
  line height and the clipping of controls
* number, date and size formats follow the language; locale data in the runtime image
* rule against plural-dependent sentences in the `translation` and `ui-styling` skills
* update of the `translation` skill and the `translator` agent to be language-neutral (today they name
  German and "Sie")
* tests: registry, locale resolution, key set check; TestFX for the orientation
* not: the translation content, live language switching

**Affected Areas**

* `app/ui` (`Messages`, start-up, layouts), build (check task), `.claude/skills/translation`,
  `.claude/skills/ui-styling`, `.claude/agents/translator.md`

**Dependencies**

IP-01 (the stored language).

**Expected Result**

* adding a language is a bundle plus a registry entry; right-to-left languages render and mirror correctly

**Technical Considerations**

* the key set rule of `ui-styling` already forbids missing or surplus keys; the check becomes a build
  task over all bundles
* fonts missing on a system cannot be fixed by the application; shipping fonts is a separate decision
  (size, licence) and not part of this feature
* an input method (IME) for Chinese, Japanese and Korean must work in the prompt field; check at
  implementation
* the confirmation of a new language by the user, demanded by `ui-styling`, is given by this plan for
  the languages listed here

### IP-12: Translations

**Objective**

Create the bundles of all supported languages in three waves.

**Scope**

* wave 1: de, fr, es, it, pt (Brazilian), nl, pl, ru, uk, tr
* wave 2: sv, da, nb, fi, cs, sk, hu, ro, bg, el, hr, sr (Cyrillic and Latin), sl, lt, lv, et, ca
* wave 3: zh-Hans, zh-Hant, ja, ko, ar, he, fa, hi, bn, th, vi, id, ms
* every wave through the `translator` agent, key by key with context, one language at a time
* the German bundle already exists and serves as the reference for terms; a glossary of the domain
  terms (model, flow, prompt, history, gallery, project) is kept per language for consistency
* the key set check of IP-11 per wave; the registry lists a language only when complete
* README note: machine translations, review welcome
* not: native review, further languages

**Affected Areas**

* `app/ui/src/main/resources/messages`, registry entries, README

**Dependencies**

IP-11 (rules, check, neutral agent). The base keys of other features are translated when their plans
add them, for all languages already present; the key set rule makes that a build condition.

**Expected Result**

* all languages of the three waves are offered in the `View` page with complete bundles

**Technical Considerations**

* each new key from here on costs one translation per language; the `translator` agent is run per
  wave, and the build blocks incomplete bundles
* the quality of right-to-left and Asian languages cannot be checked by the project itself; the
  README says so
* wave 3 needs the font and layout checks of IP-11 first
* a wave is a separate step so a release can ship with the finished waves only

## 8. Dependency Graph

```text
FP-001 IP-01, IP-02
└── IP-01
    ├── IP-02 (also FP-002 IP-01)
    └── IP-03 (also FP-003 IP-02)
        ├── IP-04
        │   └── IP-08 (also FP-001 IP-07, FP-008 IP-01, FP-006 IP-03)
        ├── IP-05
        ├── IP-06 (also FP-009 IP-05, IP-06)
        └── IP-07 (also FP-005 IP-07, FP-006 IP-02, IP-04)

IP-09 (FP-002 IP-01, FP-009 IP-02) - independent of IP-01 to IP-08
IP-10 (FP-002 IP-01) ─┐
IP-11 (after IP-01)   ─┼── IP-05 (also IP-03)
IP-11 └── IP-12
```

## 9. Risks and Open Questions

Decided (by the user):

* hidden settings: window position, size, state and splitter positions; not shown in the dialog
* visible settings in `File -> Settings`, categories `General`, `View`, `Models`, `ComfyUI`,
  `Generation`; no category `Project`
* `General`: maximum image size (4096 x 4096), maximum project file size (default 2 gigabytes),
  confirmations, recent projects
* `View`: language and appearance
* `Models`: all models downloadable; the maximum number of parallel downloads is a separate setting
  in the list page; a change affects only downloads started afterwards
* the Help menu with About and the list of components and licences is part of this feature
* the `View` category includes an own theme (IP-10) and many languages (IP-11, IP-12): all common and
  widespread languages, at least the European ones and those with other scripts and forms
* Languages: Portuguese is Brazilian Portuguese (`pt`); Serbian has one bundle per script (`sr`
  Cyrillic, `sr_Latn` Latin); right-to-left languages mirror the layout completely; the languages are
  shipped by waves, a release contains the finished waves only
* The dialog has OK, Cancel and Apply; the maximum image size applies to inputs and to results
* the shortcut `Ctrl+,`, no stored dialog sizes
* no autosave, no project settings (default encryption, backup copy)

Risks:

* FP-001 stores the project in a ZIP; whether the project is held in memory while open decides whether
  2 GB is sensible; to be checked against FP-001 IP-03 and IP-04
* The appearance setting touches the removed theme; the mechanism depends on the JavaFX version and
  has to be checked at implementation; every window and the icon variants must follow it
* The icon for `File -> Settings` needs the design reference `docs/docs/assets/images/file.png`, which
  does not exist yet
* Window restore on several monitors, mixed scaling and a changed screen layout are easy to get wrong
* A limit on parallel downloads changes FP-009 IP-05 (a "waiting" state); both plans have to be aligned
* Several plans (FP-003, FP-005, FP-006, FP-009) have to be aligned: where the restart control, the
  model list and defaults live after the move; FP-003 must keep the recent list open for IP-04
* The component list depends on the data of the licence check; ComfyUI, Python, `uv` and PyTorch are
  fixed entries that have to be kept up to date by hand
* About 40 languages multiply the translation effort for every later key; quality is machine level
  and not reviewed by native speakers; right-to-left and Asian layouts are easy to get wrong
* Plural rules of many languages cannot be expressed with `MessageFormat`; the rule "no plural
  dependent sentences" restricts the wording
* Missing system fonts for some scripts (Devanagari, Bengali, Thai, Arabic) on a given system
* The runtime image must contain the locale data; without it formats fall back to English
* The theme needs the design reference of the user; its colours and look are decided with it
* A settings file of a newer version opened by an older one must not lose data

Open questions:

* The look of the theme: the design reference of the user decides

## 10. Feature Completion Criteria

* The main window opens with the position, size, state and splitter positions of the last run, also
  after a screen layout change
* `File -> Settings` opens the dialog; OK, Cancel, Apply and "Restore defaults" behave as described
* `General` stores the limits and options; the limits are enforced with precise messages; the File menu
  shows the recent projects
* `View` switches the appearance at once and the language after a restart; every window, dialog and
  the splash screen follow the chosen scheme
* All languages of the three waves are offered with complete bundles; right-to-left languages mirror
  the layout; scripts render with system fonts; formats follow the language
* The `Models` page shows all models and installs and removes them; the former menu entry is gone;
  the parallel download limit works without touching running downloads
* `ComfyUI` and `Generation` pages exist and their settings take effect
* The Help menu shows About and the generated component and licence list
* A broken or missing settings file never prevents the start; defaults apply
* Restart-bound settings show a hint
* The Gradle `build` passes; README, KDoc and MkDocs describe the settings; all texts are translated
