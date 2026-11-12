# Feature Plan: AI Model Catalog

## 1. Objective

Introduce the AI models of the application as data. A new library `lib/ai-model` holds, as resources,
the JSON files that describe the models (name, metadata, Hugging Face reference, the flows they offer)
and the flows themselves (plain ComfyUI workflows), together with the classes that read and check
these files. One model is included as a demonstration. The model list and the flow choice of the main
window are fed from this catalog, and the user installs and removes the model files from a model list in the main window (download from
Hugging Face on demand).

## 2. Current State

* FP-002 (planned) plans the model combo box and the flow toggle button bar of the prompt input part;
  the model list is a placeholder behind an interface
* FP-006 (planned) IP-01 plans workflow templates, a registry and a builder inside `lib/comfy`, and a
  model list read from the ComfyUI server (`/object_info`); there is no definition of a model
* FP-001 (planned) stores `model` and `flow` as plain strings in the history details of a medium
* FP-005 (planned) installs ComfyUI and delivers the verified download with progress, resume and
  checksum (`lib/comfy-runtime`, IP-02) and the data directory layout (IP-01); it leaves the model
  files to a later feature, which is this one
* `.claude/rules/architecture.md` lists `lib/model` and `lib/fx-model` as the libraries; Jackson comes
  with FP-001 and FP-004
* There is no model, no flow and no Hugging Face reference in the repository

## 3. Target State

* The library `lib/ai-model` (`hocal-ai-model`) free of Java FX with a read-only catalog API
* Resources in the library, in a fixed folder structure:
    * one JSON file per model with its metadata and its flow references
    * one JSON file per flow containing only the ComfyUI workflow, unchanged
    * an index file that lists the model files
* The catalog is loaded and checked on first use; broken or inconsistent files are reported with a
  precise message
* One demonstration model with at least one flow is part of the resources
* The model combo box shows the models of the catalog, the flow toggle bar shows the flows of the
  selected model, and the workflow builder takes its templates from the catalog
* The fields of the selected flow are shown in a parameter area of the prompt input part, built from
  the field definitions and preselected with their defaults
* A model list in the main window shows all configured models with their state; per model a download
  button (not installed) or a delete button (installed)
* Model files are downloaded only on demand by the user, after he has seen the licence, never
  automatically and never all at once; models that are not installed are shown as such
* The values stored in the history details (`model`, `flow`) are the stable ids of the catalog
* A skill, an agent and two commands for Claude guide the integration of a new model and of a new
  flow for an existing model, so both follow the format and the checks of the catalog

## 4. Requirements

### Functional Requirements

* Model file (JSON), the superordinate file with all metadata, fields:
    * `schemaVersion`, `id` (stable, unique, used in the project file), `name` (plain text)
    * `huggingface`: `url` (page of the model), `revision` (pinned) and `files`: the files the model
      needs (a checkpoint or, for newer models, diffusion model, text encoders and VAE); each with
      an `id`, its path in the repository, the target directory in ComfyUI, `sha256` and `sizeBytes`
    * `license`: name and URL
    * optional hardware hint, for example the minimum video memory
    * `flows`: one or several flow entries; each entry has the flow `id`, a display `name` in plain
      text, the `standard` fields and the list of further `parameters`
* Flow file (JSON): only the ComfyUI API workflow, an unchanged export that can be run in ComfyUI as
  it is; no header, no placeholders, no names, no internationalisation; the flow id is the file name
* Field definitions (per flow entry in the model file) say what the user can enter and where the value
  goes in the workflow; they are about data types, not about functions:
    * `standard`: fixed keys known to the application; each key maps to the target in the workflow
        * required: `prompt`, `model`, `seed`
        * optional: `negativePrompt`, `inputImage`, `extraImages` (the image keys exist only in image
          to image flows, a text to image flow has none)
        * `model` binds the file names of the model to the loader nodes, one target per file of the
          model (by the file `id`); `seed` is a new random value at each send unless the user fixes it
    * `parameters`: a list of further parameters, each with
        * `id` (stable key, used in the history details), `name` (display text, plain), `type`
        * `default` and the type's limits: `min`, `max`, `step` for numbers, `values` for an enum
        * `mode`, one of `EDITABLE` (shown, the user can change it), `READ_ONLY` (shown, cannot be
          changed) and `CONSTANT` (not shown, the application writes the value into the workflow)
        * `target`: a node id and an input name of the workflow, written `"<node>.<input>"`, or a list
          of them
    * types: `text`, `integer`, `number`, `enum`, `boolean`; the type decides how the field is
      displayed (text field, number field or slider, selection list, switch)
    * there are no special types for single functions: steps, guidance and size are plain parameters
    * the same lists are what the user interface shows, with the defaults preselected
* A model references flows; a flow may be used by several models, each with its own entry and name
* The catalog API: list models, find a model by id, list the flow entries of a model, get the
  workflow template of a flow
* Validation on load:
    * schema version known, ids unique, every flow reference resolves to a flow file
    * the Hugging Face URL is an `https` URL of the host `huggingface.co`
    * every file has a SHA-256 value, a positive size and a known target directory
    * every target points to an existing node and input of the workflow; the required standard keys
      are present; the default fits the type and the limits; parameter ids are unique within the flow
      entry; the `mode` is one of the three values
    * errors name the file and the field
* Model list in the main window (opened from a menu entry; a model that is not installed offers it
  as a hint):
    * one row per configured model: name, size, licence with link, Hugging Face link, state
    * state: not installed, downloading with progress, installed
    * a download button for a model that is not installed, a delete button for an installed one
    * nothing is downloaded automatically and not at the start of the application
    * several models can download at the same time; each row shows its own progress and a cancel button
    * a compact download indicator at the top right of the window (right end of the menu bar) shows
      only the total state: a circle with the summed progress in percent inside it, and the number of
      running downloads; a click opens the model list; it does not use the bottom right corner,
      which belongs to the generation tile and the queue
* Download:
    * the files are fetched from the pinned revision on Hugging Face into their target directories of
      the ComfyUI installation, with progress, cancel, resume and verification of the checksums
    * before the download the user sees name, size, licence and its link and agrees
    * an installed model (all files present with the right size) is detected without a running service
    * a failed or cancelled download leaves no partial file in the model directories
* Delete: removes all files of the model after a confirmation; refused while a generation uses the model
* A send with a model that is not installed is not possible; the user is pointed to the model list
* The Hugging Face URL is shown as a link where models are listed
* The demonstration model is Krea 2 (named by the user); its licence and conditions, whether it is
  gated, its files, sizes and checksums are verified before IP-03

* Integration tooling for Claude (files under `.claude`):
    * skill `ai-model`: the format of model and flow files, the rules and the two procedures
      (new model, new flow for an existing model), loaded before a file under `models` or `flows`
      is created or changed
    * agent `ai-model-integrator`: carries out one of the two procedures after the user named the
      model or the flow and the sources
    * commands `/add-model` and `/add-flow`: entry points that load the skill and start the agent
    * the procedures name the steps in a fixed order: source and licence check, files with checksums,
      model file, flow file, index entry, catalog test, documentation; the agent asks the user for
      every fact it cannot verify and never invents a URL, revision, checksum or node binding

### Technical Requirements

* Module `lib/ai-model`, Gradle project `hocal-ai-model`, free of Java FX; it has no dependency on
  `lib/comfy`; the workflow is held as an opaque JSON tree
* The download lives in `lib/comfy-runtime` (it knows the directories) and reuses the download of
  FP-005 IP-02; `lib/ai-model` itself opens no connection
* JSON with Jackson, the same dependency as FP-001; no further dependency
* The resources lie in folders that are not packages of the module (`models`, `flows`), so they are
  not encapsulated; they are read with the class loader of the module
* A directory cannot be listed inside a JAR or a jlink image; the catalog therefore reads an explicit
  index file instead of scanning
* Catalog objects are immutable; no FX property models are created (the `fx-model` skill concerns the
  POJOs of `lib/model`; the catalog is read-only configuration)
* Flow files are written by the project itself; they are not copied from ComfyUI examples (licence)
* Gated models that need a Hugging Face login are not supported; no token is stored
* Tests need no network and no ComfyUI; the download is tested with WireMock
* Skills before work: `ci-pipeline`, `project-docs`, `testing`; the architecture rule and the README
  structure are updated

## 5. Architecture

* `lib/ai-model`, package `org.pcsoft.app.hocal.aimodel`:
    * `AiModel`, `FlowEntry`, `HuggingFaceRef`, `LicenseRef` - immutable data classes
    * `FlowTemplate` - the workflow of a flow file as an opaque JSON tree
    * `ModelCatalog` - read-only API, loads once, thread-safe
    * `CatalogLoader` - reads the index, the model files and the flow files with Jackson, validates,
      builds the catalog
    * `CatalogException` hierarchy - file not found, invalid JSON, schema version, unresolved flow
      reference, invalid URL, invalid checksum, duplicate id, missing standard key, invalid target
* Resources: `models/index.json`, `models/<model>.json`, `flows/<flow>.json`
* `lib/comfy-runtime`, package `models`: `ModelInstaller` - target paths, installed check, download with
  verification through the download of FP-005 IP-02, removal of partial files, deletion of a model
* Use:
    * `app/ui`: the model list interface of FP-002 IP-06 is implemented by the catalog; the toggle bar
      shows the flows of the selected model; the model list with download and delete
    * `lib/comfy`: the workflow builder of FP-006 IP-01 gets its templates from the catalog
* `.claude`: skill `ai-model`, agent `ai-model-integrator`, commands `add-model` and `add-flow`; they
  change only resources and tests of `lib/ai-model` and documentation, no production code
* Data flow: resources -> loader -> catalog -> UI lists / builder -> history details (ids); catalog
  entry -> `ModelInstaller` -> Hugging Face -> model directories of ComfyUI
* Persistence: none for the catalog (fixed per application version); the downloaded file lies in the
  ComfyUI installation

Example, only to illustrate the shape (not a decision on the final field names):

```json
{
  "schemaVersion": 1,
  "id": "example-model",
  "name": "Example Model",
  "huggingface": {
    "url": "https://huggingface.co/<owner>/<repo>",
    "revision": "<commit>",
    "files": [
      { "id": "checkpoint", "path": "<file in the repository>",
        "targetDir": "<checkpoints | diffusion_models | text_encoders | vae>",
        "sha256": "<hash>", "sizeBytes": 0 }
    ]
  },
  "license": { "name": "<licence>", "url": "<link>" },
  "flows": [
    { "id": "text-to-image", "name": "Text to image",
      "standard": { "prompt": "6.text", "model": { "checkpoint": "4.ckpt_name" }, "seed": "3.seed" },
      "parameters": [
        { "id": "steps", "name": "Steps", "type": "integer", "default": 20, "min": 1, "max": 100,
          "mode": "EDITABLE", "target": "3.steps" },
        { "id": "sampler", "name": "Sampler", "type": "enum", "values": ["euler", "dpmpp_2m"],
          "default": "euler", "mode": "EDITABLE", "target": "3.sampler_name" },
        { "id": "batch", "name": "Batch size", "type": "integer", "default": 1,
          "mode": "CONSTANT", "target": "5.batch_size" }
      ] },
    { "id": "image-edit", "name": "Image edit",
      "standard": { "prompt": "6.text", "model": { "checkpoint": "4.ckpt_name" }, "seed": "3.seed",
                    "inputImage": "10.image" },
      "parameters": [] }
  ]
}
```

## 6. Implementation Plan Overview

| ID    | Implementation Plan                | Objective                                                              | Dependencies                                   |
|-------|------------------------------------|------------------------------------------------------------------------|------------------------------------------------|
| IP-01 | Library Foundation And Format      | Module `lib/ai-model`, data classes, JSON format, rules and docs       | FP-001 IP-01 (Jackson)                         |
| IP-02 | Catalog Loader And Validation      | Read index, models and flows from resources, validate, typed errors    | IP-01                                          |
| IP-03 | Demonstration Model Entry          | Model JSON, Hugging Face reference and flow files of one real model    | IP-02                                          |
| IP-04 | Catalog Binding In The Application | Model list, flow toggle bar and workflow templates from the catalog    | IP-02, FP-002 IP-06, FP-006 IP-01              |
| IP-05 | Model Download And Removal        | Download and verify the model files, installed check, delete, partial cleanup | IP-02, FP-005 IP-01, IP-02, IP-04 |
| IP-06 | Model Management List             | List of all models in the main window with download and delete buttons | IP-04, IP-05, FP-003 IP-02, IP-04 |
| IP-07 | Model And Flow Integration Tooling | Skill, agent and commands to add a model or a flow to the catalog      | IP-02, IP-03                                   |

## 7. Implementation Plans

### IP-01: Library Foundation And Format

**Objective**

Create the module and fix the format of the model and flow files.

**Scope**

* module `lib/ai-model` in `settings.gradle.kts`, build file, `module-info.java`, Jackson dependency
* immutable data classes and the JSON format with `schemaVersion`
* decision on the final names and the format of the standard keys and the parameter list (types,
  limits, defaults, `mode`, targets, ids)
* update of `.claude/rules/architecture.md`, README structure section and the CI pipeline module list
* not: loading, resources of a concrete model

**Affected Areas**

* `settings.gradle.kts`, `lib/ai-model`, `.claude/rules/architecture.md`, README, `.github`

**Dependencies**

FP-001 IP-01 (shares the Jackson dependency; whichever plan comes first adds it).

**Expected Result**

* the module builds; the format is documented in KDoc and a docs page

**Technical Considerations**

* the binding format (node id and input name) must match the builder of FP-006 IP-01; both plans
  agree on it
* the format stays extendable by `schemaVersion`

### IP-02: Catalog Loader And Validation

**Objective**

Read the catalog from the resources safely.

**Scope**

* loader: index file, model files, flow files; resolution of the flow references
* the validation rules of section 4 with precise errors
* the read-only `ModelCatalog` API
* tests with test resources: a valid catalog and every error case
* not: the real demonstration model

**Affected Areas**

* `lib/ai-model` package `aimodel`, test resources

**Dependencies**

IP-01.

**Expected Result**

* a valid catalog loads and answers queries; an invalid one is rejected with a useful message

**Technical Considerations**

* the class loader of the module is used; JPMS opens nothing because the folders are no packages
* the catalog is loaded once and cached
* unknown JSON fields are rejected or ignored by an explicit decision

### IP-03: Demonstration Model Entry

**Objective**

Put one real model into the catalog as a working demonstration.

**Scope**

* the model Krea 2: check licence and conditions and whether it is gated, find the Hugging Face
  repository, pin the revision, list the files with target directory, size and checksum
* model JSON with name, metadata, Hugging Face reference, licence and flow entries
* the flow file or files, written by the project and exported from ComfyUI in API format, with the
  field definitions and bindings in the model file
* entry in the index file
* a test that the real catalog loads and passes the validation
* not: the download itself, a run against ComfyUI

**Affected Areas**

* `lib/ai-model/src/main/resources`, tests

**Dependencies**

IP-02.

**Expected Result**

* the real catalog contains the demonstration model and its flows and validates

**Technical Considerations**

* URL, revision, file name, checksum and flow have to be checked by hand against Hugging Face and a
  real ComfyUI; the planning session could reach neither
* the checksum is computed from the real file when the entry is written
* a flow that cannot be verified by a run stays marked as unverified until FP-006 runs it

### IP-04: Catalog Binding In The Application

**Objective**

Make the catalog the source of models and flows in the application.

**Scope**

* implementation of the model list interface of FP-002 IP-06 with the catalog
* the toggle bar shows the flows of the selected model and changes with the selection
* a parameter area built from the selected flow: `prompt` is the text box, `negativePrompt` a second
  text field when present, `seed` a number with a switch "random" that is on by default
* the `parameters` become inputs by their type, preselected with the default: `EDITABLE` as input,
  `READ_ONLY` shown disabled, `CONSTANT` not shown
* the standard key `inputImage` decides whether the shown medium is sent (image to image) or nothing
  (text to image); `extraImages` offers the gallery button
* a link to the Hugging Face page of the model where the list shows it
* the workflow builder of FP-006 IP-01 reads its templates from the catalog
* the stored `model` and `flow` values are the catalog ids
* not: the download, adding own models

**Affected Areas**

* `app/ui` `PromptInput` and its view model, `lib/comfy` package `workflow`, message bundles

**Dependencies**

IP-02, FP-002 IP-06, FP-006 IP-01; agree with them before they are implemented.

**Expected Result**

* the combo box lists the demonstration model, the toggle bar its flows, a send uses its template

**Technical Considerations**

* if a model has exactly one flow the toggle bar shows that one selected
* the flow names are plain text from the model file and are not translated
* skills: `component`, `ui-styling`, `translation`

### IP-05: Model Download And Removal

**Objective**

Fetch the files of a model from Hugging Face into the ComfyUI installation and remove them again.

**Scope**

* `ModelInstaller` in `lib/comfy-runtime`: target paths per file, installed check
* download of every file from the pinned revision with progress, cancel and resume
* several models download at the same time, one download per model; the same model cannot start twice;
  the number of parallel downloads is not limited for now; FP-009 IP-06 adds an optional slot limit
  with the state "waiting", so the download takes a slot count as a parameter
* verification of size and SHA-256 before a file is moved into place
* partial files lie in a temporary area of the data directory, so a resume is possible and the model
  directories never contain a partial file; they are removed after a cancel; free disk space is
  checked first and counts the running downloads
* deletion of an installed model (all its files)
* typed errors: not reachable, not found, gated or refused, checksum mismatch, disk full
* not: user interface, gated models with a token

**Affected Areas**

* `lib/comfy-runtime` package `models`, tests with WireMock

**Dependencies**

IP-02, FP-005 IP-01, IP-02 and IP-04 (the ComfyUI directory exists).

**Expected Result**

* a model is installed verified or not at all; an installed model is detected without a running service

**Technical Considerations**

* the files are several gigabytes; resume and cancel matter
* the redirect of Hugging Face to its file host is followed by the HTTP client; only `https`
* a model is installed only when all its files are there; a half-installed model counts as not installed
* whether ComfyUI sees new files without a restart is to be checked at implementation

### IP-06: Model Management List

**Objective**

Let the user see all models and install or remove them from the main window.

**Scope**

* a model list opened from a menu entry of the main window; a model that is not installed offers it
  as a hint in the model combo box
* one row per model: name, size, licence link, Hugging Face link, state
* download button for a model that is not installed, delete button for an installed one
* before the download a dialog with name, size and licence; the user agrees; then progress with cancel
* delete with a confirmation; refused while a generation uses the model
* several downloads at once, each with progress and cancel in its row
* download indicator at the top right of the window: a circle with the summed progress in percent
  inside it and the number of running downloads next to it, nothing more; a click opens the model list
* leaving the application while downloads run asks: cancel them and leave, or wait
* the send is disabled while the chosen model is not installed, with a hint to the list
* error dialogs with the reason and a retry
* not: gated models, download at the start of the application, download of all models

**Affected Areas**

* `app/ui` dialog or view for the list, main window menu, `PromptInput`, view models, message bundles
  (German through the `translator`)

**Dependencies**

IP-04, IP-05, FP-003 IP-02 (menu structure), FP-003 IP-04 (progress and error dialogs).

**Expected Result**

* the user installs the demonstration model on demand with consent, uses it and can remove it again

**Technical Considerations**

* the licence is only shown, the application does not interpret it
* the indicator is part of the main window layout next to the menu bar; it needs no icon (a progress
  circle and text), and it is hidden while no download runs
* the summed progress is the sum of the downloaded bytes over the sum of the sizes of all running
  downloads; the circle may be a JavaFX `ProgressIndicator`, which shows the percentage inside by
  default (to be checked)
* the progress of a row follows the model state through a view model; updates are coalesced
* the model list is a menu entry first; a later feature plan moves it into a settings dialog, so the
  list is a self-contained component that can be placed in another window
* the menu entry carries no icon (icons are only planned for the File menu)
* skills: `dialog`, `component`, `ui-styling`, `fx-component-lifecycle`, `translation`

### IP-07: Model And Flow Integration Tooling

**Objective**

Give Claude a repeatable way to add a new model, or a new flow for an existing model, to the catalog.

**Scope**

* skill `ai-model`: format, validation rules, naming of ids and files, both procedures as ordered
  steps, the list of facts that must be verified by hand
* agent `ai-model-integrator`: runs a procedure, writes the files, runs the catalog test, reports
  what is verified and what is not
* commands `/add-model` and `/add-flow` as entry points with arguments
* the procedures for a new flow: the flow file as an unchanged ComfyUI export, the flow entry in the
  model file with standard keys, parameters and targets
* the procedures for a new model: licence and gating check, repository, pinned revision, files with
  target directory, size and checksum, model file, index entry, at least one flow
* the demonstration model of IP-03 serves as the worked example
* not: changes of production code, download or run of a model, adding gated models

**Affected Areas**

* `.claude/skills/ai-model`, `.claude/agents`, `.claude/commands`, `.claude/CLAUDE.md` (skill list)

**Dependencies**

IP-02 (rules and checks), IP-03 (worked example).

**Expected Result**

* `/add-model` and `/add-flow` lead through the steps; the result loads and validates in the catalog test

**Technical Considerations**

* the skill repeats no rule of the format twice: it points to the KDoc and the docs page of IP-01
* the agent does not touch the network where the project rules forbid it; checksums are computed
  only from files the user provides or downloads, and are otherwise requested from the user
* every new model or flow ends with the catalog test and the documentation check

## 8. Dependency Graph

```text
FP-001 IP-01 (Jackson)
└── IP-01
    └── IP-02
        ├── IP-03
        │   └── IP-07 (also IP-02)
        ├── IP-04 (also FP-002 IP-06, FP-006 IP-01)
        │   └── IP-06 (also IP-05, FP-003 IP-02, IP-04)
        └── IP-05 (also FP-005 IP-01, IP-02, IP-04)
            └── IP-06
```

## 9. Risks and Open Questions

Decided:

* The model file is the superordinate file with the name and all metadata; the flow file contains
  only the ComfyUI workflow
* No internationalisation of model and flow names; they are plain text
* The model description carries the files with target directory, size and checksum, licence and
  licence URL, a hardware hint and the pinned revision
* The download from Hugging Face is part of this feature, happens in the main window, only on demand
  by the user and never automatically; no model is downloaded at the start of the application
* A model list with a download button or a delete button per model
* Flow fields are generic data types (`text`, `integer`, `number`, `enum`, `boolean`); fixed standard
  keys (`prompt`, `model`, `seed` required; `negativePrompt`, `inputImage`, `extraImages` optional) map
  to targets; further `parameters` carry `id`, `name`, `type`, default, `mode` and `target`
* Visibility and editability are one enum `mode`: `EDITABLE`, `READ_ONLY`, `CONSTANT`
* A target is written `"<node id>.<input name>"`; the flow file is an unchanged ComfyUI export
  without placeholders
* An input image is an optional standard key of the flow: text to image flows have none, image to image
  flows have one
* Only the values of `EDITABLE` parameters are stored in the history details, by parameter id; image
  inputs and constants are not stored; this extends FP-001 and has to be aligned there
* The `seed` is a standard key with a new random value at each send unless the user fixes it; as an
  exception it is stored in the history details although it is not an `EDITABLE` parameter
* The `model` standard key binds the files of a model to the loader nodes, one target per file `id`
* The fields for the demonstration: prompt, seed, steps, guidance and size, checked against the real
  Krea 2 workflow
* The demonstration model is Krea 2; its conditions are checked at implementation
* The model list is a menu entry first and moves into a settings dialog in a later separate feature plan
* Several downloads can run at the same time, not limited for now; the progress is shown in the rows of
  the list and in an indicator at the top right of the window (circle with percent inside and the
  number of downloads), never in the bottom right corner (generation tile, queue)
* A skill, an agent and two commands cover the integration of a new model and of a new flow for an
  existing model; they are the last step of this feature (IP-07)
* Package and project name: `hocal-ai-model` and `org.pcsoft.app.hocal.aimodel`

Risks:

* The conditions of Krea 2 could not be verified (Hugging Face is not reachable from the planning
  session): licence, commercial use, whether the model is gated and needs a login, repository and
  files; if it is gated, the decision "no gated models" has to be revisited
* A current model may consist of several files (diffusion model, text encoders, VAE); the format
  therefore carries a list of files and not a single checkpoint
* The Hugging Face URL, revision, checksums and the flow cannot be verified without network and a
  ComfyUI; a wrong flow fails only at the first real run
* Flow files depend on node names of ComfyUI that change between versions; the pinned ComfyUI version
  of FP-005 has to match
* The files are several gigabytes; disk space, bandwidth and interrupted downloads
* FP-006 IP-01 overlaps with this feature (templates, model list); both plans have to be aligned
  before implementation; the same holds for FP-001 (generic parameter values in the history details)
  and FP-002 IP-06 (parameter area in the prompt input part)

Open questions:

* The exact Hugging Face repository, files, licence and gating of Krea 2; checked at implementation, before IP-03
* Own models added by the user later (a second source next to the resources); the loader is kept open
  for more sources but this is not part of this feature

## 10. Feature Completion Criteria

* `lib/ai-model` builds, has no Java FX and no dependency on `lib/comfy`
* The resources contain the index, the demonstration model and its flows; the catalog loads and validates
* Every error case of the validation is covered by a test with a precise message
* The model combo box and the flow toggle bar of the main window show the catalog content; the
  parameter area shows the fields of the selected flow with their defaults
* The workflow builder takes its template from the catalog; the history details hold the catalog ids
* The model list shows all models with their state; a download button installs a model verified by
  checksum after the user agreed, a delete button removes it
* Nothing is downloaded without a click of the user; a partial or cancelled download leaves nothing
  in the model directories
* A model that is not installed is shown as such and cannot be used for a send
* Several downloads run in parallel; the rows and the indicator at the top right show their progress
* The skill, the agent and the commands exist; a new model and a new flow for an existing model can be
  added with them and pass the catalog test
* The Gradle `build` passes; README, KDoc, the architecture rule and the CI pipeline describe the new module
