# Feature Plan: ComfyUI Connection

## 1. Objective

Connect the application to a running ComfyUI server over the network. A new library offers a facade
for the most important ComfyUI operations, built on the Apache HTTP Client, and a hook that delivers
the events of a running generation (queued, started, progress, finished, failed) to registered
listeners. The feature contains no user interface and no workflow logic; it is the foundation for the
later generation features.

## 2. Current State

* The architecture rule says: the application drives ComfyUI as a separate process through its HTTP
  and WebSocket API; no module embeds or links ComfyUI
* No code talks to ComfyUI yet; no network library is a dependency
* `lib/` holds no library yet; FP-001 plans `lib/model` and `lib/fx-model`, which also bring Jackson
* The settings.gradle.kts includes only `:app:ui`; `app/ui` runs as a JPMS module (`module-info.java`)
  and logs against the SLF4J API only
* The external documentation `docs.comfy.org` could not be reached from the planning session (host not
  allowed); the protocol below is derived from the official example
  `script_examples/websockets_api_example.py` of the ComfyUI repository and from known ComfyUI
  behaviour, and has to be verified against the documentation before IP-02

## 3. Target State

* A library `lib/comfy` (`hocal-comfy`) free of Java FX with a facade `ComfyClient`
* The facade covers: submit a workflow, read the history of a run, download a result image, upload an
  image, read the node catalogue, read the queue, interrupt, read the system status and check the
  connection
* A hook `ComfyEventHook` keeps a WebSocket to ComfyUI open and delivers typed events to listeners
* Every request and the event hook identify themselves with one `clientId` (UUID), so ComfyUI routes
  the events of this application's runs to it
* Network errors, server errors and invalid answers are mapped to typed exceptions
* The UI can use the facade and the hook later without knowing HTTP details

## 4. Requirements

### Functional Requirements

* Server address (host, port) is configurable; default `http://127.0.0.1:8188`
* Only local servers are supported: plain `http`, no authentication, no remote server
* Facade operations (HTTP):
    * `POST /prompt` - submit a workflow (API format JSON) with `client_id`; result: `prompt_id`,
      queue number, node errors
    * `GET /history/{prompt_id}` - read outputs of a finished run; `GET /history` for the list
    * `GET /view` - download an output file (`filename`, `subfolder`, `type`) as bytes or stream
    * `POST /upload/image` - multipart upload of an input image (optional subfolder and overwrite)
    * `GET /object_info` - node catalogue, used later to offer models and flows
    * `GET /queue` - running and pending runs; `POST /queue` to delete pending entries
    * `POST /interrupt` - stop the running generation
    * `GET /system_stats` - used as connection and health check
* Event hook (WebSocket `/ws?clientId=...`):
    * text frames are parsed into typed events: `status` (queue remaining), `execution_start`,
      `execution_cached`, `executing` (node; `node = null` with the `prompt_id` marks the end of the
      run), `progress` (value, max, node), `executed` (node outputs), `execution_success`,
      `execution_error`, `execution_interrupted`
    * binary frames are latent preview images: delivered as a preview event with the image bytes,
      or dropped when nobody listens
    * unknown message types are delivered as a generic event and logged, never fail the connection
    * listeners register and deregister; delivery never blocks the receive loop
* Connection state of the hook (connecting, connected, disconnected) is observable by listeners
* The hook reconnects automatically after a lost connection, with growing waiting time

### Technical Requirements

* Apache HTTP Client 5 (`org.apache.httpcomponents.client5:httpclient5`, Apache-2.0) is the HTTP
  transport; the dependency was requested by the user
* Kotlin and Gradle; no Java FX in the library; no dependency on `app/ui`
* The WebSocket of the event hook uses the JDK `java.net.http.WebSocket` (module `java.net.http`); no
  further library is added
* JSON with Jackson, the same library FP-001 introduces; the Jackson dependency is shared
* The library is a JPMS module with its own `module-info.java`; the Apache HTTP Client is an automatic
  module and needs a check for module path, jlink and `licensee`
* The facade is blocking and thread-safe; the caller decides on the thread (the UI later calls it
  from `javafx.concurrent` tasks)
* Events are delivered on a library-owned thread, never on the FX thread; the UI marshals itself
* Logging against the SLF4J API only
* The user-visible parts of the facade never log workflow content or full URLs with credentials
* Tests run without a ComfyUI server, against a local stub server; the HTTP routes are simulated with
  WireMock as test dependency (approved by the user, Apache-2.0)

## 5. Architecture

* Module `lib/comfy` (`hocal-comfy`), package `org.pcsoft.app.hocal.comfy`:
    * `config` - `ComfyConfig` (base URI, timeouts, `clientId`)
    * `http` - transport on the Apache HTTP Client: shared client with connection pool, JSON helpers,
      binary download, multipart upload, error mapping
    * `api` - facade `ComfyClient` and its request/response types
    * `event` - `ComfyEvent` hierarchy, `ComfyEventListener`, `ComfyEventHook`
    * `error` - `ComfyException` hierarchy: connection failed, timeout, server error (with status and
      body excerpt), invalid response, node errors of a rejected workflow
* Data flow: caller -> `ComfyClient` -> transport -> ComfyUI; ComfyUI -> WebSocket -> `ComfyEventHook`
  -> listeners
* A generation is: `submit` returns the `prompt_id`; the hook delivers events for that `prompt_id`;
  after the end event the caller reads `history` and downloads outputs with `view`
* Correlation by `prompt_id` is a job of the listener; the hook delivers all events of the client
* Persistence: none; no ComfyUI data is stored by this feature
* Server connection settings in the UI and the use of the facade in the main window are not part of
  this feature

## 6. Implementation Plan Overview

| ID    | Implementation Plan             | Objective                                                          | Dependencies |
|-------|---------------------------------|--------------------------------------------------------------------|--------------|
| IP-01 | Library Foundation              | Module `lib/comfy`, dependencies, config, exceptions, DTO types    | -            |
| IP-02 | HTTP Transport                  | Apache HTTP Client wrapper: JSON, download, multipart, errors      | IP-01        |
| IP-03 | ComfyUI Facade                  | `ComfyClient` with the important operations on top of the transport | IP-02       |
| IP-04 | Execution Event Hook            | WebSocket receive loop, typed events, listeners, reconnect         | IP-01        |
| IP-05 | Stub Server And Integration Tests | Local stub of ComfyUI for facade and hook tests                  | IP-03, IP-04 |

## 7. Implementation Plans

### IP-01: Library Foundation

**Objective**

Create the library module so that the other plans have a place and a vocabulary.

**Scope**

* module `lib/comfy` in `settings.gradle.kts`, build file, `module-info.java`
* dependencies `httpclient5`, Jackson (shared with FP-001), SLF4J API; test dependency WireMock
* `ComfyConfig` with defaults and validation, `clientId` generation
* exception hierarchy and the DTO types of requests and responses
* not: any network code

**Affected Areas**

* `settings.gradle.kts`, `lib/comfy`, `.github` workflows (module list), README structure section

**Dependencies**

None. Shares the Jackson dependency with FP-001 IP-01; whichever plan comes first adds it.

**Expected Result**

* the module builds, its tests run, the licence check passes

**Technical Considerations**

* a new dependency needs the user's approval; `httpclient5` and WireMock (test only) are approved, its transitive artifacts
  (`httpcore5`, `httpcore5-h2`) and a possibly new licence entry are to be shown to the user
* automatic-module names of Apache artifacts must work in jlink; otherwise merge like mvvmfx
* skills: `ci-pipeline`, `project-docs`, `testing`

### IP-02: HTTP Transport

**Objective**

Wrap the Apache HTTP Client so the facade works on a small, tested interface.

**Scope**

* one shared client with pooling, timeouts (connect, response), clean shutdown (`close`)
* GET and POST with JSON in and out; binary download as bytes and as stream
* multipart upload for `/upload/image`
* mapping of status codes, I/O errors and invalid JSON to the typed exceptions
* not: any ComfyUI-specific path or type

**Affected Areas**

* package `http` and `error` of `lib/comfy`

**Dependencies**

IP-01.

**Expected Result**

* the transport talks to any local test server; every failure class has a typed exception

**Technical Considerations**

* the client is thread-safe and closeable; the owner of the facade closes it
* responses are consumed completely and closed to return the connection
* a size limit protects the binary download from running out of memory

### IP-03: ComfyUI Facade

**Objective**

Offer the important ComfyUI operations as a small, typed Kotlin API.

**Scope**

* `ComfyClient`: submit, history (one and list), view, upload, object info, queue (read, delete),
  interrupt, system stats, connection check
* the workflow stays an opaque JSON tree; building workflows is a later feature
* node errors of a rejected submit become a typed exception with the node list
* `BlockedNodes`: a list of node class types that are never sent; `submit` checks the workflow before
  the call and refuses it with a typed exception; the list contains `GemmaAPITextEncode`
* not: events, UI, workflow creation

**Affected Areas**

* package `api` of `lib/comfy`

**Dependencies**

IP-02.

**Expected Result**

* every listed operation can be called against a stub server and returns typed results

**Technical Considerations**

* `/object_info` can be large; it is parsed lazily or returned as a JSON tree
* the facade is an interface plus an implementation, so the UI and tests can replace it
* paths are built with encoding of `filename` and `subfolder`
* the check sits in the facade so no code path can bypass it; the engine blocks the node as well
  (FP-005 IP-05); the list is exposed so the catalog validation (FP-008 IP-02) can use the same list

### IP-04: Execution Event Hook

**Objective**

Receive the events of running generations and hand them to listeners.

**Scope**

* WebSocket connection to `/ws?clientId=...`
* parser from text frames to the `ComfyEvent` types; binary preview frames
* listener registration, delivery on an own thread, connection state events
* reconnect with growing waiting time; start and stop
* not: correlation to a project or media, progress display

**Affected Areas**

* package `event` of `lib/comfy`

**Dependencies**

IP-01. Independent of IP-02 and IP-03 and can run in parallel.

**Expected Result**

* a listener sees the whole event sequence of a run from `execution_start` to its end event
* a lost connection is restored without a restart of the application

**Technical Considerations**

* the Apache HTTP Client 5 has no WebSocket client; the hook uses the JDK `java.net.http.WebSocket`
  (decided)
* slow listeners must not block the receive loop: a bounded queue and a drop rule for previews
* a listener exception is logged and does not stop the others
* skill `fx-component-lifecycle` governs the later UI registration of listeners

### IP-05: Stub Server And Integration Tests

**Objective**

Prove the facade and the hook against a simulated ComfyUI.

**Scope**

* WireMock stubs for the HTTP routes of IP-03 (JSON, binary, multipart, error answers)
* a small hand-written WebSocket stub in the test sources for scripted event sequences
* integration tests ("IT" suffix) for the full run: submit, events, history, view
* error cases: server down, 500, invalid JSON, rejected workflow, lost WebSocket, reconnect
* not: tests against a real ComfyUI

**Affected Areas**

* test sources of `lib/comfy`, `testing` skill, CI pipeline

**Dependencies**

IP-03, IP-04.

**Expected Result**

* the whole feature is tested without network access to a real server

**Technical Considerations**

* WireMock covers HTTP only and has no WebSocket support; the JDK has no WebSocket server either, so
  the WebSocket stub implements handshake and frames itself on a `ServerSocket`; no further dependency
* WireMock is used on a system-chosen port; its transitive artifacts need a licence check
* the ports are chosen by the system to avoid clashes on the CI

## 8. Dependency Graph

```text
IP-01
├── IP-02
│   └── IP-03
│       └── IP-05
└── IP-04
    └── IP-05
```

## 9. Risks and Open Questions

Decided:

* WebSocket: JDK `java.net.http.WebSocket`; the Apache HTTP Client stays the HTTP transport only
* Only local servers; no authentication, no `https`, no remote server
* One `clientId` per application run; it is not stored
* The application does not start or stop ComfyUI itself (architecture rule: separate process)

Open:

* The ComfyUI documentation was not reachable (host not allowed); routes, message names and field
  names have to be checked against `docs.comfy.org` before IP-02
* Binary preview frame: its header layout (event type and format) has to be confirmed before the
  frames are decoded into images
* Large downloads and uploads: size limit and progress for them
* WireMock brings transitive artifacts (for example Jetty) whose licences must pass the `licensee`
  check; unlisted licences are to be shown to the user
* Where the server address is stored and edited (settings dialog) is a later feature
* JPMS: Apache automatic modules could force a merged module as with mvvmfx; to be checked in IP-01

## 10. Feature Completion Criteria

* The facade runs every listed operation against the stub server and returns typed results
* A scripted run delivers the complete event sequence to a listener, including progress and error
  end events
* A lost connection is restored by the hook; a stopped hook releases its thread and socket
* All failure classes (no connection, timeout, server error, invalid answer, rejected workflow) are
  distinguishable by exception type
* `lib/comfy` contains no Java FX and no dependency on `app/ui`
* The Gradle `build` passes, including the licence check and the jlink image
* README, KDoc and the CI pipeline describe the new module
