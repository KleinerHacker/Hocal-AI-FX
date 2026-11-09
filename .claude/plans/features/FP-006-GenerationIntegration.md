# Feature Plan: Generation Integration

## 1. Objective

Connect the main window to ComfyUI: the user sends the prompt with all needed data, the application
builds and submits the workflow, follows its progress and stores the generated image in the history
of the reference. While a generation runs, a tile at the bottom right of the window shows the
emerging image, a percentage and a progress bar.

## 2. Current State

* FP-002 (planned) creates the prompt input part: text box, `+` button (gallery), model combo box
  and toggle buttons as placeholders; it has no send button and no generation
* FP-001 (planned) defines history details per medium (prompt, model, flow) and the
  `ProjectManager`, which appends media to a reference; additional images are not stored
* FP-004 (planned) delivers `ComfyClient` (submit, history, view, upload, queue, interrupt) and
  `ComfyEventHook` (typed events, previews as bytes); workflows are opaque JSON trees there
* FP-005 (planned) installs and runs ComfyUI as a service; `ensureRunning()` starts it on the first
  communication and hands out the port
* FP-003 (planned) delivers a progress dialog and error dialogs; no tile or pop-up exists
* There is no workflow template, no workflow builder and no list of models or flows

## 3. Target State

* A send button sits behind the prompt text box; sending needs a non-empty prompt and a selected
  reference
* A send collects: prompt, model, flow, the medium shown in the main window (the one selected in the
  history list) and the additionally chosen images from the gallery
* The application uploads the images, builds the workflow from a template of the chosen flow,
  submits it and follows the events of exactly this run
* A tile at the bottom right of the window shows the emerging preview image, next to it the percent
  value, at the lower edge a progress bar
* When the run ends, the result image is appended as the newest medium of the reference, with the
  history details prompt, model and flow; the history list jumps to it
* Errors and cancellations are shown understandably and leave the project unchanged
* Further sends while a run is active go into the queue of ComfyUI; the queue is shown below the tile

## 4. Requirements

### Functional Requirements

* Send button behind the prompt text box, accelerator `Ctrl+Enter`, disabled while the prompt is
  empty, no reference is selected or no project is open
* Data of a send: prompt, model (combo box), flow (toggle button bar of the input part), the medium
  shown in the main window as input image (always, not necessarily the newest), additional gallery
  images (by reference UUID, their current media)
* Queue: every send is submitted at once to the queue of ComfyUI and stays enabled while runs are
  active; the input data (prompt, model, flow, images) are captured and uploaded at send time, later
  changes of the selection do not affect a waiting run
* Models and flows are real lists: models from the ComfyUI server (`/object_info`), flows from the
  workflow templates shipped with the application
* A send starts ComfyUI on demand and waits for it; the tile shows "starting" meanwhile
* Progress tile:
    * pop-up at the bottom right of the main window, follows the window, does not take the focus
    * left the preview image (latent preview of the running sampler), next to it the percent value,
      at the lower edge a progress bar
    * before the first preview a placeholder, after the end the final image, then the tile closes by
      itself after a short time; on error it stays until closed
    * a `x` button closes the tile; while the run lasts it is the same as cancelling the run
    * after the end the tile closes by itself after a delay; a click on it selects the result in all
      lists: folder in the tree, file in the file list, new medium in the history list
* Queue display, below the tile of the running job, space is scarce:
    * exactly one waiting job: one compact row (flow, model, start of the prompt, `x`)
    * two or more waiting jobs: collapsed to one summary line "N waiting"; a click expands the list
    * `x` on a row removes the waiting job from the queue; the expanded list can be collapsed again
* Percent value: derived from the sampler step events and the nodes of the workflow already done
* The result is appended with the history details; the additional images are not stored
* Closing or replacing the project while a run is active asks: wait, or discard the result

### Technical Requirements

* Building the workflow and following the run live in a JavaFX-free library (`lib/comfy`); only the
  tile, the send button and the view models live in `app/ui`
* Events arrive on library threads; the UI takes them over with `Platform.runLater` and coalesces
  preview and progress updates
* Changes to the project only through the `ProjectManager`, on the FX thread
* Run data is held by the `ProjectManager`/view model only as long as needed; no workflow content
  and no prompt is logged
* Follow the project rules: `ui-styling`, `component`, `dialog`, `window`, `fx-model`,
  `fx-component-lifecycle`, `icons`, `translation` (German through the `translator`), `testing`
* Tests run with fakes for `ComfyClient` and `ComfyEventHook`, no ComfyUI

## 5. Architecture

* `lib/comfy`, new package `workflow`: `WorkflowTemplate` (API-format JSON with named placeholders
  for prompt, seed, model, input image and extra images), `WorkflowRegistry` (templates of the
  application resources, name = flow), `WorkflowBuilder` (fills the placeholders, new seed per run)
* `lib/comfy`, new package `generation`: `GenerationService`:
    1. `ensureRunning()` of FP-005
    2. upload of the input and the extra images
    3. build and submit; the `prompt_id` identifies the run
    4. follow the events of this `prompt_id`: queued, running, progress in percent, preview bytes,
       finished, failed, cancelled
    5. on the end event read the history and download the result
  The run is a `GenerationJob` with an observable state for listeners; no JavaFX types; the service
  holds all jobs (running and waiting) in the order of the ComfyUI queue
* `app/ui`: `PromptInput` gets the send button; a view model hands the data to the service;
  `GenerationTile` (tile component) and a tile host (overlay layer of the main window) show the jobs
* Result path: `GenerationJob` end -> view model on the FX thread -> `ProjectManager` appends the
  medium with prompt, model and flow -> history list selects it
* Data flow: send -> `GenerationService` -> `ComfyClient` / `ComfyEventHook` -> job state -> tile;
  end -> `ProjectManager`
* Persistence: only the appended medium and its history details in the project file; no job is stored

## 6. Implementation Plan Overview

| ID    | Implementation Plan             | Objective                                                          | Dependencies                  |
|-------|---------------------------------|--------------------------------------------------------------------|-------------------------------|
| IP-01 | Workflow Templates And Builder  | Templates per flow, placeholders, models list, builder             | FP-004 IP-03                  |
| IP-02 | Generation Service              | Run orchestration with job state, upload, submit, events, result   | IP-01, FP-004 IP-04, FP-005 IP-05 |
| IP-03 | Send Button And Prompt Submission | Send button, collected data, real model and flow lists, errors    | IP-02, FP-002 IP-06           |
| IP-04 | Progress Tile                   | Pop-up tile with preview, percent and progress bar, tile host      | IP-02, FP-002 IP-01, IP-04    |
| IP-05 | Result Storage In Project       | Append result with history details, selection, project change rules | IP-02, FP-001 IP-07, FP-002 IP-05 |

## 7. Implementation Plans

### IP-01: Workflow Templates And Builder

**Objective**

Turn "flow" and "model" from placeholders into real, usable data.

**Scope**

* workflow templates in ComfyUI API format as application resources, with named placeholders
* registry of the templates; the name of a template is the flow name of the history details
* builder: fills prompt, new random seed, model, input image and extra images
* model list: checkpoint names read from `/object_info` through the facade
* validation of a template (placeholders present, nodes known to the server)
* not: workflow editing by the user, custom nodes

**Affected Areas**

* `lib/comfy` package `workflow`, resources, tests

**Dependencies**

FP-004 IP-03.

**Expected Result**

* the builder returns a ready workflow JSON for a template and a data set; the lists of models and
  flows are available

**Technical Considerations**

* which flows exist (text to image, image edit, image to image) and which models they need is open
* a template needs the right input nodes; extra images need a template with several image inputs
* the seed is not part of the history details and is not stored

### IP-02: Generation Service

**Objective**

Run one generation from send to result and report its state.

**Scope**

* `GenerationService` and `GenerationJob` as described in the architecture
* state: preparing, starting service, waiting (position in the queue), running with percent,
  finished, failed, cancelled
* several jobs at once; the list of jobs is observable; waiting jobs are removed through the queue
  operations of the facade
* percent: sampler step progress plus the share of finished nodes, cached nodes counted as done
* preview bytes as latest preview of the job; coalescing is up to the UI
* cancel through `interrupt` and removal from the queue; closing the tile with `x` triggers it
* result download after the end event; typed errors from FP-004
* not: user interface, storage in the project

**Affected Areas**

* `lib/comfy` package `generation`

**Dependencies**

IP-01, FP-004 IP-04, FP-005 IP-05.

**Expected Result**

* a run reports its whole life to listeners; the result bytes are available at its end

**Technical Considerations**

* correlation by `prompt_id`; events can arrive before `submit` has returned, so they are buffered
* previews only arrive when the server is started with a preview method; FP-005 IP-05 starts it
  with `--preview-method auto`; without it the job reports no previews
* a lost WebSocket during a run: the job queries the history to find out the end
* events are matched by `prompt_id` because the hook delivers all events of the client; the order
  of the jobs follows the queue of ComfyUI
* a restart of the service drops its queue; the jobs are then marked as cancelled

### IP-03: Send Button And Prompt Submission

**Objective**

Let the user start a generation from the prompt input part.

**Scope**

* send button behind the prompt text box and the accelerator `Ctrl+Enter`
* enabling rules; a send stays possible while runs are active and while the service starts
* the combo box shows the real models, the flow is chosen with the toggle button bar already planned
  in FP-002 IP-06, which now carries the real flows
* collect the data and hand it to the `GenerationService`; text box stays filled, send does not clear
* error dialogs for a failed send (service not available, rejected workflow, no connection)
* not: tile, storage

**Affected Areas**

* `app/ui` `PromptInput`, its view model, message bundles (German through the `translator`), icon
  for the send button if needed

**Dependencies**

IP-02, FP-002 IP-06.

**Expected Result**

* a click on send or `Ctrl+Enter` starts a run with all data

**Technical Considerations**

* the toggle button bar of FP-002 IP-06 becomes the flow choice
* skills: `component`, `ui-styling`, `fx-component-lifecycle`, `icons`, `translation`

### IP-04: Progress Tile

**Objective**

Show the progress of a run as a tile at the bottom right.

**Scope**

* `GenerationTile`: preview image left, percent next to it, progress bar at the lower edge
* tile host: an overlay layer of the main window, bottom right: the tile of the running job and
  below it the queue rows
* queue display: one waiting job as a row, from two waiting jobs one collapsed summary line that
  expands on click; `x` removes a waiting job
* states: starting, running, finished (final image, closes after a delay), failed (stays), cancelled
* `x` button: cancels a running job, otherwise only closes the tile
* click on the tile selects reference and result in the tree, the file list and the history list
* update coalescing so previews do not flood the FX thread
* not: send button, storing the result

**Affected Areas**

* `app/ui`: tile component, host in the main window, view model, message bundles

**Dependencies**

IP-02, FP-002 IP-01.

**Expected Result**

* a running job shows a live tile; waiting jobs are shown as one row or one collapsed line; a
  finished job closes it by itself after a delay

**Technical Considerations**

* overlay layer in the window instead of a separate `Popup` window: it follows the window and
  needs no extra focus handling; an own pop-up window is the alternative
* the tile does not take the focus and does not block the window
* skills: `component`, `ui-styling`, `fx-component-lifecycle`, `translation`

### IP-05: Result Storage In Project

**Objective**

Bring the finished image into the project history.

**Scope**

* on the end of a job: append the result as newest medium of the reference through the
  `ProjectManager`, with the history details prompt, model and flow
* the history list selects the new medium; the file list preview shows it as latest
* the project is `dirty` afterwards
* project closed, replaced or another one opened while jobs are active or waiting: ask once for all
  of them, then wait or discard
* every result goes to the reference captured at send time
* result for a reference that no longer exists is discarded with a message
* not: additional images in the history, deleting media

**Affected Areas**

* `app/ui` view models, `ProjectManager` use, `lib/fx-model` if the append needs an addition

**Dependencies**

IP-02, FP-001 IP-07, FP-002 IP-05.

**Expected Result**

* a finished run shows up as the latest history entry without further action

**Technical Considerations**

* the medium file extension follows the result file of ComfyUI
* the selection jump only happens when the user still looks at the same reference
* an overflow beyond `999` media is a typed error that the user sees

## 8. Dependency Graph

```text
FP-004 IP-03
└── IP-01
    └── IP-02 (also FP-004 IP-04, FP-005 IP-05)
        ├── IP-03 (also FP-002 IP-06)
        ├── IP-04 (also FP-002 IP-01, IP-04)
        └── IP-05 (also FP-001 IP-07, FP-002 IP-05)
```

## 9. Risks and Open Questions

Risks:

* Latent previews appear only when ComfyUI runs with a preview method; FP-005 IP-05 must start it
  that way, and a preview is a rough picture and sometimes missing
* The percent value of a workflow with several samplers or cached nodes is only an estimate
* Workflow templates depend on installed models and nodes; a missing model gives a node error
* Many preview frames can flood the FX thread; coalescing is required
* A restart of the service drops the queue; the confirmation of FP-005 IP-07 has to name the number
  of running and waiting jobs
* A run lasts minutes; the project may change meanwhile; the rules of IP-05 have to be strict

Decided:

* The input image is always the medium shown in the main window (selected in the history list)
* A queue: every send goes into the queue of ComfyUI; the input is captured at send time
* The queue is shown below the tile: one waiting job as a row, from two collapsed to one line
* The tile closes after a delay; a click on it selects reference and result in all lists
* The flow choice is the toggle button bar of FP-002 IP-06
* The tile is an overlay layer of the main window with a `x` that cancels a running job

Open questions:

* Which flows and models are delivered first and which model files the runtime installs; to be
  clarified at implementation of IP-01
* The length of the closing delay of the tile; chosen at implementation of IP-04

## 10. Feature Completion Criteria

* Send button and `Ctrl+Enter` start a run with prompt, model, flow, current medium and extra images
* The tile shows preview, percent and progress bar while the run lasts; `x` cancels the run; after the
  end it closes by itself, a click selects the result in all lists
* The result is the newest medium of the reference with prompt, model and flow in the project file
* A failed or cancelled run shows an understandable message and leaves the project unchanged
* Several sends form a queue; it is shown collapsed from two waiting jobs and can be expanded;
  waiting jobs can be removed
* ComfyUI is started on demand by the first send; no function needs a manual start
* Changing the project during a run follows the agreed rules; no result is lost silently
* Tests run with fakes; the Gradle `build` passes; README, KDoc and the CI pipeline are up to date
