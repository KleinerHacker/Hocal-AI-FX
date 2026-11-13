# Feature Plan: ComfyUI Runtime

## 1. Objective

Make ComfyUI usable without manual set-up. The application checks on the first start for a suitable
Python, installs it and the ComfyUI runtime after the user agreed, starts ComfyUI as a background
service on the first communication, stops it when the application ends and lets the user restart it.

## 2. Current State

* The architecture rule says ComfyUI runs as a separate process and no module embeds or links it
* FP-004 (planned) delivers the network connection: `ComfyClient`, `ComfyEventHook`, `ComfyConfig`
  (local server, default `http://127.0.0.1:8188`); it explicitly does not start or stop ComfyUI
* The `startup` skill describes a startup area (`SplashStage`, `Startup`, `StartupStep`) that is not
  built yet; steps must finish on their own, keep the splash short and do file and network work on
  the background thread
* `Launcher` shows the main window directly; no preferences or settings storage exists
* No code starts a process, downloads a file or unpacks an archive
* The application has no data directory of its own yet

## 3. Target State

* On the first start (and whenever the runtime is missing or broken) the application detects it,
  explains what is missing and its size, and asks the user before anything is downloaded
* After agreement Python and the ComfyUI runtime are downloaded, verified and installed into a
  directory owned by the application, without administrator rights and without touching a system
  Python
* The main window opens only after the runtime is installed; no function is available without it
* The first call to ComfyUI starts the service in the background and waits until it answers
* The service is stopped when the application ends, also when it ends abnormally on the next start
* The user can see the service state and restart the service from the application

## 4. Requirements

### Functional Requirements

* Environment check: is there a usable Python (version range supported by the pinned ComfyUI), is
  the ComfyUI runtime present and intact
* Installation after a user dialog: what is installed, from where, how large, where to
    * progress with cancel; a cancelled or failed installation leaves no half-installed state behind
* Service start on the first communication (lazy), not at application start
* Service stop on application exit and window close; no orphaned process
* Restart from the user interface; a restart warns when a generation is running
* ComfyUI always comes from the application's own installation; an existing or external ComfyUI is
  neither configured nor used, and the application never stops a process it did not start
* The service listens on a free port chosen by the application at each start
* The service runs offline and without paid or cloud nodes: it is started with `--offline` (switches
  off the paid API nodes); an engine version that does not know this switch is started with the older
  `--disable-api-nodes` instead
* The node `GemmaAPITextEncode` is blocked on two ways: through the built-in list of disabled nodes of
  ComfyUI 0.42 and in the application before a workflow is submitted (FP-004 IP-03); flow files that
  contain it are rejected when the catalog is read (FP-009 IP-02)
* Telemetry of the started process is switched off with the environment variables `DO_NOT_TRACK=1` and
  `HF_HUB_DISABLE_TELEMETRY=1`
* States visible to the user: not installed, installing, stopped, starting, running, failed

### Technical Requirements

* Python is provided as an application-managed environment (virtual environment in the application
  data directory); a system Python is never modified
* All downloads over HTTPS, checked against a pinned checksum before use; versions of Python
  provisioning tool, ComfyUI and the PyTorch backend are pinned in the application
* The ComfyUI process is started without a shell, its output is read continuously and written to a
  log file, it is bound to `127.0.0.1` only
* Process tree termination works on Windows, Linux and macOS
* The library has no Java FX; user interface parts live in `app/ui`
* No new dependency without asking the user first (archive formats, see open questions)
* Tests never download or start the real ComfyUI; process and network are replaced by fakes
* Plan and files in English, user texts as I18N keys with a German translation by the `translator`

## 5. Architecture

* New module `lib/comfy-runtime` (`hocal-comfy-runtime`), package `org.pcsoft.app.hocal.comfy.runtime`,
  free of Java FX, using `hocal-comfy` (FP-004) for the health check and the base address:
    * `layout` - data directory structure, path rules, platform detection
    * `download` - download with progress, resume, checksum, cancel, archive extraction
    * `python` - detection of a usable Python, provisioning of an application-managed one, virtual
      environment
    * `install` - ComfyUI download, dependency installation, PyTorch backend choice, verification
    * `service` - `ComfyService`: start, stop, restart, state, health check, log capture, orphan
      cleanup, lazy `ensureRunning()`
* `app/ui`: a startup step that checks the environment, dialogs for consent and progress (reuse of the
  progress dialog of FP-003 IP-04), a service control (state display, restart)
* Lazy start: a wrapper in front of the facade calls `ensureRunning()` before the first request;
  `ComfyClient` itself stays unaware of the process
* Data flow: check -> consent -> install -> `ensureRunning()` -> `ComfyClient` / `ComfyEventHook`
* Persistence: installation directory with a small manifest (installed versions, checksums), a PID file
  of the running service, a log directory; the location per operating system follows the user data
  directory convention
* The service state is exposed as an observable value; the user interface adapts it to a property

## 6. Implementation Plan Overview

| ID    | Implementation Plan             | Objective                                                              | Dependencies         |
|-------|---------------------------------|------------------------------------------------------------------------|----------------------|
| IP-01 | Runtime Library Foundation      | Module, data directory layout, platform detection, state model         | FP-004 IP-01         |
| IP-02 | Download And Archive Handling   | Verified download with progress and cancel, archive extraction         | IP-01                |
| IP-03 | Python Environment              | Detect or provision Python, virtual environment, version check         | IP-02                |
| IP-04 | ComfyUI Runtime Installation    | Pinned ComfyUI download, dependencies, GPU backend, verification       | IP-03                |
| IP-05 | Service Process Management      | Start, stop, restart, health check, logs, orphan cleanup, lazy start   | IP-01, FP-004 IP-03  |
| IP-06 | First-Start Setup Flow          | Startup check, consent dialog, progress, error handling                | IP-04, IP-05         |
| IP-07 | Service Control In Application  | State display, restart, stop on exit and window close                  | IP-05, FP-003 IP-02  |

## 7. Implementation Plans

### IP-01: Runtime Library Foundation

**Objective**

Create the module and the vocabulary shared by all other plans.

**Scope**

* module `lib/comfy-runtime`, dependency on `hocal-comfy`, build and `module-info.java`
* data directory layout (runtime, python, venv, logs, manifest, PID file) per operating system
* platform detection: operating system, architecture, GPU vendor hint
* `RuntimeState` and a state holder with listener registration
* not: downloads, processes, user interface

**Affected Areas**

* `settings.gradle.kts`, `lib/comfy-runtime`, pipeline module list, README structure section

**Dependencies**

FP-004 IP-01 (module `lib/comfy`, `ComfyConfig`).

**Expected Result**

* the module builds; the layout and the platform detection are tested

**Technical Considerations**

* paths with spaces and non-ASCII characters must work, especially on Windows
* the data directory is created with the application's name; it is not inside the installation of
  the application, which may be read-only
* skills: `ci-pipeline`, `project-docs`, `testing`

### IP-02: Download And Archive Handling

**Objective**

Provide one safe way to fetch and unpack the files all later plans need.

**Scope**

* download with progress callback, cancel, resume of an interrupted file, checksum verification
* ZIP extraction with the Zip-Slip protection also used in FP-001; tar.gz if required
* temporary files in the data directory, atomic move on success, cleanup on failure
* proxy settings of the system are respected
* not: knowledge about Python or ComfyUI

**Affected Areas**

* packages `download` of `lib/comfy-runtime`

**Dependencies**

IP-01.

**Expected Result**

* a file is downloaded, verified and unpacked, or nothing remains on the disk

**Technical Considerations**

* the HTTP client of FP-004 (Apache HTTP Client) is reused for downloads
* a tar.gz or tar.zst archive needs a library (for example Commons Compress); a decision and the
  user's approval are needed before it is added
* free disk space is checked before the download

### IP-03: Python Environment

**Objective**

Make sure a Python of the right version is available, owned by the application.

**Scope**

* find an existing Python and read its version; accept it only inside the supported range
* otherwise provision an application-managed Python (see open questions: provisioning route)
* create a virtual environment for ComfyUI, check `pip`
* report clear errors: unsupported version, provisioning failed, no network
* not: installing packages of ComfyUI

**Affected Areas**

* package `python` of `lib/comfy-runtime`

**Dependencies**

IP-02.

**Expected Result**

* a working virtual environment with a supported Python exists in the data directory

**Technical Considerations**

* the supported version range is taken from the pinned ComfyUI release, not hard-coded loosely
* a system Python is not trusted by default: missing `venv`/`pip`, wrong architecture, Microsoft
  Store stub on Windows, externally managed environments on Linux
* no administrator rights and no changes to `PATH` or the registry

### IP-04: ComfyUI Runtime Installation

**Objective**

Install a pinned ComfyUI version with the dependencies matching the machine.

**Scope**

* download of the pinned ComfyUI release archive and its verification
* install of the requirements into the virtual environment
* PyTorch backend choice: NVIDIA (CUDA), AMD (ROCm), Apple (MPS), CPU; user can change the choice
* verification run (import check, version query), manifest with versions and checksums
* repair of a broken installation; update is not part of this plan
* not: models, custom nodes, workflows

**Affected Areas**

* package `install` of `lib/comfy-runtime`

**Dependencies**

IP-03.

**Expected Result**

* the runtime is installed and verified; a second run detects it as intact and does nothing

**Technical Considerations**

* the PyTorch download is several gigabytes and differs per backend; the size is shown in the
  consent dialog
* a wrong or missing GPU detection yields a very slow CPU installation; the choice is visible
* ComfyUI is licensed GPL-3.0; the application downloads it and runs it as a separate process and
  does not ship it
* without a checkpoint model ComfyUI cannot generate; model download is a later feature

### IP-05: Service Process Management

**Objective**

Run ComfyUI as a controlled background service.

**Scope**

* start the process with the venv's Python, `127.0.0.1` and the configured port, without a shell
* wait for readiness by polling `systemStats` of the FP-004 facade; timeout and failure report
* stop: graceful first, then forced, including child processes
* restart; refuse or warn while a generation runs (queue read through the facade)
* `ensureRunning()` for the lazy start, safe against concurrent callers
* output pipes read continuously into a log file with rotation
* choose a free port at each start and pass it to the client configuration; never kill a foreign
  process
* PID file and cleanup of an orphaned service of an earlier crashed run
* JVM shutdown hook as safety net
* offline start: pick `--offline` or, for an older engine, `--disable-api-nodes` by probing which of
  the two switches the installed ComfyUI knows (its `--help` output), cached per installed version
* if the engine knows neither switch the start is refused with a clear error; the process never runs
  with the paid nodes enabled
* hand `GemmaAPITextEncode` to the built-in list of disabled nodes of ComfyUI 0.42, in the way that
  version provides it (checked against the documentation at implementation)
* start environment: `DO_NOT_TRACK=1`, `HF_HUB_DISABLE_TELEMETRY=1` set on the child process
* README: a section says that ComfyUI runs offline without paid API nodes, that telemetry is switched
  off and that the node `GemmaAPITextEncode` is blocked
* not: installation, user interface

**Affected Areas**

* package `service` of `lib/comfy-runtime`

**Dependencies**

IP-01, FP-004 IP-03. Can run in parallel to IP-02 to IP-04 with a configured executable path.

**Expected Result**

* the service starts on demand, is reported as running, and is gone after the application ends

**Technical Considerations**

* startup takes tens of seconds (Python and PyTorch import); the state `starting` is shown
* the event hook of FP-004 reconnects by itself while the service restarts
* an undrained output pipe blocks the child process; the reader runs on its own thread
* the free port is handed to `ComfyConfig` of FP-004 before the client is used; the client and the
  event hook therefore take the address from the service, not from a fixed default
* an unknown switch makes ComfyUI exit at once; the probe therefore runs before the real start and
  the version of the engine is not compared by number
* `--offline` stops the paid nodes only; it does not make the process refuse the network for
  downloads of models done by FP-009 (that is the application's own download)
* the telemetry variables are set for the child process only, not for the application itself

### IP-06: First-Start Setup Flow

**Objective**

Guide the user through the check and the installation inside the splash screen.

**Scope**

* environment check and installation as `StartupStep`s; the splash screen stays until the runtime is
  installed, so no function is available before it
* the startup area is created according to the `startup` skill if it does not exist yet
* the splash screen shows the progress (steps, download size, bar) and a cancel button
* consent dialog before the first download with content, sources, sizes and target directory
* declining or cancelling ends the application; nothing half-installed remains
* error dialogs with understandable text and the log location; retry or exit
* the `startup` skill is changed: long-running steps with progress are allowed for this one job
* not: the service control, see IP-07

**Affected Areas**

* `app/ui`: `SplashStage` (progress display), startup steps, dialogs, message bundles (English and
  German through the `translator`), the `startup` skill

**Dependencies**

IP-04, IP-05.

**Expected Result**

* a fresh start without ComfyUI shows the consent dialog and, after agreement, the progress on the
  splash screen; the main window opens only when the installation is verified
* a start with an intact installation passes the step in a moment

**Technical Considerations**

* skills: `startup`, `dialog`, `ui-styling`, `fx-component-lifecycle`, `translation`
* work on the FX thread only through `StartupContext.onFxThread`
* the rule "keep steps quick" of the `startup` skill gets an explicit exception with a progress
  report; an endless wait stays forbidden, the download has timeouts and can be cancelled
* the dialogs must appear above the splash screen and be reachable while it is shown

### IP-07: Service Control In Application

**Objective**

Let the user see and control the service, and tie its life to the application.

**Scope**

* menu entry to restart ComfyUI and a state display (stopped, starting, running, failed)
* confirmation before a restart when a generation is running
* stop of the service on Exit, on closing the window and on a fatal error
* accelerator and icon for the restart entry, following the `icons` skill
* not: the installation, settings for port and directory
* the restart control later moves into the `ComfyUI` page of the settings dialog (FP-010 IP-07)

**Affected Areas**

* `app/ui`: main window menu, status display, view model, message bundles

**Dependencies**

IP-05; the menu structure of FP-003 IP-02.

**Expected Result**

* the user restarts the service from the menu; after leaving the application no ComfyUI process of
  this application remains

**Technical Considerations**

* the stop on exit must not block the window; it runs with a time limit and then forces
* skills: `ui-styling`, `icons`, `translation`, `fx-component-lifecycle`

## 8. Dependency Graph

```text
FP-004 IP-01
└── IP-01
    ├── IP-02
    │   └── IP-03
    │       └── IP-04
    │           └── IP-06
    └── IP-05 (also FP-004 IP-03)
        ├── IP-06
        └── IP-07
```

## 9. Risks and Open Questions

Risks:

* Python provisioning differs per operating system: installers need administrator rights or a
  package manager; they are avoided by an application-managed Python
* PyTorch: several gigabytes, backend depends on the hardware, ROCm only on Linux, Apple uses MPS;
  a wrong choice is slow or does not run
* Security: the application downloads and runs code; checksums, HTTPS and pinned versions are
  mandatory, the consent dialog names the sources
* Orphaned processes after a crash or a killed JVM; a PID file and cleanup at the next start
* A foreign process may occupy a port; the application picks a free one and never stops foreign processes
* Long start time and cold caches make the first generation slow; feedback must be visible
* A long installation behind the splash screen looks like a hang without visible progress
* Disk space of 10 GB and more; the check happens before the download
* Offline use, proxies, interrupted downloads
* Antivirus software may flag or block a freshly downloaded Python or process start
* The switches `--offline` and `--disable-api-nodes` and the node block list of ComfyUI 0.42 come from
  the user; they could not be verified against docs.comfy.org (not reachable from the planning
  session); a changed switch name in a later engine version would stop the start
* FP-004 states that the application does not start ComfyUI; this feature supersedes it, FP-004
  stays as it is because the client remains independent of the process

Decided:

* Python provisioning with a downloaded `uv`; always an application-managed Python and virtual
  environment, a system Python is never reused
* ComfyUI source: release archive from GitHub, no `git clone`
* ComfyUI always comes from the application; no configurable or external installation
* The installation runs completely in the splash screen; the main window opens afterwards
* A free port is chosen at each service start
* Declining or cancelling the installation ends the application
* The service starts with `--offline`, or with `--disable-api-nodes` where the engine does not know
  `--offline`; telemetry is off (`DO_NOT_TRACK=1`, `HF_HUB_DISABLE_TELEMETRY=1`)
* `GemmaAPITextEncode` is blocked in the engine list and in the application (before submit and when
  flow files are read)

Open questions:

* Which ComfyUI version is pinned and how the supported Python range is determined; to be clarified
  at implementation (IP-03, IP-04)
* Install location (default per operating system) and whether the user may choose it; to be
  clarified at implementation (IP-01)
* Where models are stored and who downloads the first checkpoint: covered by FP-009 (model download)
* Updates of ComfyUI and of the PyTorch backend (later feature)

## 10. Feature Completion Criteria

* On a machine without Python and ComfyUI the application leads the user through the consent and
  installs a verified runtime without administrator rights
* The main window opens only with an installed and verified runtime; declining ends the application
* The first call of the facade starts the service; the state is shown to the user
* After closing the application, also after a crash and a new start, no ComfyUI process of the
  application remains
* The user can restart the service from the application; foreign processes are never stopped
* ComfyUI runs with the paid API nodes switched off and with telemetry disabled; a workflow with
  `GemmaAPITextEncode` is refused before it is sent
* A cancelled or failed installation leaves no half-installed state; a retry works
* No Java FX in `lib/comfy-runtime`; tests run without network and without a real ComfyUI
* The Gradle `build` passes; README, KDoc and the CI pipeline describe the new module
