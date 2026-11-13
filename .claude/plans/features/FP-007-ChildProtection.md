# Feature Plan: Child Protection

## 1. Objective

Build a protection of minors into the generation path. The application must not create or process
nude or sexualized depictions of persons under 18. For every generation three checkpoints decide:

1. the text of the prompt (all prompt texts of the run, alone and together)
2. all input images of the run (if any)
3. the generated image

A generation that violates the golden rules (section 4) is stopped or its result is discarded. The
protection cannot be switched off by the user, by a setting, by a flow file or by an environment
variable. If it cannot decide, it blocks (fail closed).

## 2. Current State

* FP-006 (planned) delivers the generation path: `PromptInput` send button, `WorkflowBuilder`,
  `GenerationService` (`lib/comfy`), the progress tile and the storage of the result in the project
* FP-006 IP-02 already refuses a workflow that contains a blocked node (`BlockedNodes` of FP-004
  IP-03); this is the only content related protection that is planned so far, and it covers nodes, not
  content
* FP-002 IP-06 (planned) lets the user attach additional images from the gallery; FP-006 sends them
  together with the medium shown in the main window as input images
* FP-005 (planned) provides an application-owned Python environment (`uv`, virtual environment), the
  verified download with checksum (IP-02), the process management of a background service (IP-05) and
  the first-start setup flow (IP-06); the service runs offline, without cloud nodes
* FP-009 (planned, catalog) lists the AI models the user can pick; it has its own download and the
  `Models` page of the settings; flows are curated files, the user does not edit workflows
* FP-010 (planned, settings) has a `Generation` category, a settings dialog frame and the licence
  list of the Help menu
* No code exists for content analysis, no classifier, no text lexicon, no age estimation
* The architecture rule says the application drives ComfyUI as a separate process; no module embeds
  it; the full Java FX toolkit lives only in `app/ui`
* The dependency rule says: a new dependency needs the approval of the user first

## 3. Target State

* A library `lib/guard` (`hocal-guard`), free of Java FX, with the facade
  `ContentGuard`
* An internal properties file in the resources of the library names the models used for the checks
  and the prompts given to them, so neither is scattered through the code
* Every generation is checked at three points:
    * before upload and submit: all prompt texts and all input images (pre-check)
    * while the run lasts: sampled preview frames (optional protection against a long running
      violation; see open questions)
    * after the end event: the generated image, before it is shown as a result or stored (post-check)
* The decision is the combination of two independent signals per source: "a person under 18 is meant
  or depicted" and "nudity or a sexualized depiction is meant or depicted"
* A blocked pre-check sends nothing to ComfyUI and uploads nothing; a blocked post-check discards the
  result: it is not stored in the project, not shown as an image, and its files in the data directory
  of ComfyUI are deleted
* The user gets a neutral message ("not allowed: protection of minors") that does not tell which word
  or image part triggered the block
* The check engine runs locally, offline, in an own process of the application; no prompt and no image
  leaves the computer
* The protection has no off switch; if the engine or its models are missing or fail, a generation is
  refused with a clear message (fail closed)
* Nothing of the checked content is logged or kept; only the stage, the rule and the verdict are logged

## 4. Requirements

### Functional Requirements

Definitions:

* signal M ("minor"): a person under 18 is meant (text) or depicted (image); includes realistic,
  drawn, rendered and otherwise stylized depictions
* signal S ("sexual"): nudity or a sexualized depiction is requested (text) or shown (image); nudity
  alone is already S, it does not need to be sexual
* each signal has the levels `NONE`, `POSSIBLE`, `LIKELY`
* a source is one prompt text, one input image or the generated image

Golden rules (the minimum that has to hold, given by the product owner):

* G1 prompt: the text intentionally describes persons under 18 in connection with nudity or a
  sexualized depiction: M and S within the prompt texts -> block
* G2 prompt with images: the text describes nudity or a sexualized depiction while an input image
  shows a person under 18: S of the prompt texts and M of an input image -> block
* G3 image: a person under 18 is depicted in connection with nudity or a sexualized depiction: M and
  S in the same image -> block; this applies to every input image and to the generated image

Combination rule (proposed extension, to be confirmed, see open questions):

* the signals of a run are united over all sources: prompt texts, all input images, generated image;
  the run is blocked when the union contains M and S together at the block level (below); G1 to G3
  are special cases of it; further cases it adds: M from the prompt with S from an input image, and
  M from an input image with S from another input image or from the generated image

Block level:

* block when (M is `LIKELY` and S is at least `POSSIBLE`) or (M is at least `POSSIBLE` and S is
  `LIKELY`)
* an uncertain age is treated conservatively: a person who appears to be near 18 counts as `POSSIBLE`
  for M
* thresholds live in code, are covered by tests and are not user settings

Prompt texts (checkpoint 1):

* all text values that the model receives are checked: the prompt, the negative prompt, every text
  parameter of the flow (FP-009 fields) and every text input written into the built workflow; the
  texts are checked each alone and joined together (several prompts together can form the violation)
* "intentionally" means explicit statements: age below 18, words for children and adolescents,
  school age, "looks like a child", and indirect forms (age numbers, relations such as "daughter",
  fantasy ages); an adult marker ("adult", "35 years old") lowers M only when it is consistent in
  the whole text
* the negative prompt only counts for the other side of a rule when it does not exclude the term
  (a negative prompt that lists "child" is not an intention to depict one); a negative prompt that
  asks for the opposite of a protection is not a way around it
* text in several languages: the rule layer covers English and the languages of wave 1 of FP-010,
  the model layer is multilingual (see architecture)
* obfuscation is normalized before the check: spacing, leetspeak, homoglyphs, zero-width characters,
  unusual casing

Input images (checkpoint 2):

* every input image of the run is checked, also the medium shown in the main window (it is always an
  input) and every gallery image; images are checked before they are uploaded
* per image: the highest M level of any person, the highest S level, and whether both belong to the
  same image (G3); the combination of several images follows the combination rule
* a verdict for an image is held in memory by its content hash for the lifetime of the application,
  so an unchanged image is not analyzed again; nothing is persisted

Generated image (checkpoint 3):

* the result bytes are downloaded into memory and checked before they are shown, stored or handed to
  the project; G3 applies to the result, and the union with the pre-check signals applies as well
* a blocked result is deleted from the data directory of ComfyUI (output, temp, input of the run) and
  never appended to the project history

Behaviour:

* a blocked pre-check ends the job at once with the state `BLOCKED`; nothing is submitted
* a blocked post-check ends the job with the state `BLOCKED`; the tile shows no image
* the message to the user is the same for every rule; the rule id appears only in the log
* waiting jobs in the queue were pre-checked at send time; their results are post-checked when they
  end
* there is no override: no confirmation dialog, no setting, no "send anyway"
* engine or models missing, process not started, timeout or error: the generation is refused with the
  message "protection cannot decide" and the reason for repair (install, restart)
* the guard models are mandatory components: they are installed with the first-start setup (FP-005
  IP-06) and are shown with their licence; they do not appear as selectable models

### Technical Requirements

* Kotlin and Gradle; the library is free of Java FX and has a JPMS `module-info.java`; its internals
  are not exported
* `GenerationService` receives the `ContentGuard` as a constructor parameter that cannot be null; no
  production code path builds a service without it; an architecture test checks it
* Check engine as an own local process on the Python environment of FP-005 IP-03; communication
  through the standard input and output of the process (JSON lines), no open network port
* Candidate engine models and their Python packages are selected and licence-checked in IP-03; any
  new dependency (JVM or Python) is approved by the user before it is added
* Model files are downloaded with the verified download of FP-005 IP-02 (checksum, resume); the
  models are not part of the user catalog of FP-009 and not deletable on the `Models` page
* Internal properties file `guard.properties` in the resources of `lib/guard`:
    * per check (text, image) the model used: source, file, pinned revision and checksum
    * the prompts given to the models, one key per prompt (text check, image check, preview check)
    * the code reads models and prompts only from this file; no model name and no prompt text is a
      literal in the code; a test checks that every key the code needs is present and not empty
    * the file is internal: its package is not exported or opened to another module, it is not a
      user setting, it is not read from outside the library and it cannot be overridden at runtime
    * a change of a model or a prompt is a change of this file alone, followed by the evaluation of
      IP-07
* The rule layer for text has no dependency and works without the engine; the engine adds the semantic
  and the image analysis
* Decisions are deterministic for the same input and the same engine version
* No prompt, no workflow content and no image content is logged or stored; logs carry only the
  stage, the rule id, the levels and the duration
* The check never runs on the FX thread; a timeout per check is set; the checks of the pre-check run
  in parallel where independent
* No explicit or illegal material is created, stored or committed in the repository; tests use fakes
  and neutral inputs only; the calibration with real material is the owner's task outside the
  repository (IP-07)
* Follow the project rules: `ui-styling`, `dialog`, `component`, `translation` (German through the
  `translator`), `testing`, `startup`, `ci-pipeline`, `project-docs`

## 5. Architecture

* `lib/guard` (`hocal-guard`), package `org.pcsoft.app.hocal.guard`:
    * `api` - facade `ContentGuard`: `checkRequest(RequestSnapshot)` and `checkResult(ImageBytes,
      RequestVerdict)` return a `Verdict` (`ALLOWED`, `BLOCKED` with rule id, `UNDECIDABLE` with
      reason); `RequestSnapshot` holds the typed prompt texts (positive, negative, other) and the
      input images; `GuardException` hierarchy
    * `policy` - signals, levels, the golden rules G1 to G3, the combination rule, the thresholds;
      pure functions, no I/O
    * `text` - normalization, lexicons per language, age expression parser, negative prompt handling,
      joined evaluation; the rule layer of the text analysis
    * `config` - `GuardConfig`, the typed reader of `guard.properties` (models, prompts);
      loaded once, validated at load, a missing or empty key is an error that leads to `UNDECIDABLE`
    * `engine` - interface `ClassifierEngine` (text classification, image classification), the
      process based implementation, protocol, lifecycle (start on demand, health check, restart,
      timeout), model provisioning
    * `image` - per image analysis, hash cache, preview sampling
    * `audit` - content free event log
* `lib/comfy` depends on the `lib/guard` API; `GenerationService` calls it:
    1. build the workflow (FP-006 IP-01), collect the `RequestSnapshot` from the texts really written
       into the workflow and from all input images
    2. `checkRequest`; on `BLOCKED` or `UNDECIDABLE` the job ends, nothing is uploaded
    3. upload, submit, follow the run (FP-006 IP-02); sampled preview frames go to `checkPreview`
    4. after the end event download the result into memory, `checkResult`; only an `ALLOWED` result is
       handed on
    5. on a block, delete the files of the run in the ComfyUI data directory
* `app/ui`: the dialog for a blocked or undecidable generation, the `BLOCKED` state of the tile, the
  read-only guard state in the `Generation` category (FP-010 IP-07), the guard setup in the
  first-start flow (FP-005 IP-06); no decision logic in the UI
* Data flow: send -> `GenerationService` -> `ContentGuard` -> `ClassifierEngine` process -> verdict ->
  job state -> tile; end -> `ContentGuard` -> `ProjectManager`
* Persistence: none for checked content; the in-memory hash cache is lost at exit; the guard models
  live in the data directory of FP-005 next to the runtime
* Engine approaches considered:
    * local process on the Python environment of FP-005 (leading option): no JVM native code, the
      models of the Python ecosystem, own lifecycle; costs an additional installation and memory
    * ONNX Runtime inside the JVM: no extra process; needs a native dependency and a conversion of the
      models; a new dependency to approve
    * ComfyUI custom node: rejected, the application drives ComfyUI as an external process and flows
      are curated
    * cloud service: rejected, the application is local and offline

## 6. Implementation Plan Overview

| ID    | Implementation Plan             | Objective                                                                 | Dependencies         |
|-------|---------------------------------|---------------------------------------------------------------------------|----------------------|
| IP-01 | Guard Library And Policy        | Module `lib/guard`, signals, golden rules, verdicts, facade, fail closed  | -                    |
| IP-02 | Text Analysis                   | Rule layer for prompt texts, joined evaluation, intent and age detection  | IP-01                |
| IP-03 | Classification Engine Process   | Local engine process, protocol, model provisioning, engine decision       | IP-01, FP-005 IP-02, IP-03, IP-05 |
| IP-04 | Image Analysis                  | M and S levels per image, hash cache, preview sampling                    | IP-03                |
| IP-05 | Generation Integration          | Pre-check, post-check, cleanup, `BLOCKED` state, non-bypassable wiring    | IP-02, IP-04, FP-006 IP-02 |
| IP-06 | Notices And Setup In UI         | Block dialog, tile state, guard state in settings, first-start setup      | IP-05, FP-006 IP-03, IP-04, FP-005 IP-06, FP-010 IP-03 |
| IP-07 | Evaluation Tooling And Documentation | Offline evaluation task, calibration report, docs, licences, pipeline | IP-05                |

## 7. Implementation Plans

### IP-01: Guard Library And Policy

**Objective**

Create the library with the decision logic, independent of any engine.

**Scope**

* module `lib/guard` in `settings.gradle.kts`, build file, `module-info.java`
* signals M and S, levels, `RequestSnapshot`, `Verdict`, rule ids
* policy: G1 to G3, the combination rule, the block level and the thresholds
* facade `ContentGuard` with `checkRequest`, `checkResult`, `checkPreview`; interface
  `ClassifierEngine`
* fail closed: an engine error, timeout or missing engine gives `UNDECIDABLE`, never `ALLOWED`
* `guard.properties` with its key scheme and the typed reader `GuardConfig`; the entries
  for models and prompts are filled in IP-03
* content free audit log
* test fakes of `ClassifierEngine`
* not: lexicons, process, image analysis, UI

**Affected Areas**

* `settings.gradle.kts`, `lib/guard`, `.github` workflows (module list), README structure section

**Dependencies**

None. Shares the Jackson and SLF4J dependencies with FP-001 and FP-004.

**Expected Result**

* with fake engines every combination of the golden rules and the union rule gives the expected
  verdict; the module builds and the licence check passes

**Technical Considerations**

* the policy is a pure function of signals; changing a threshold changes tests, not code paths
* the union semantics (what counts as "same image", what as "run") is fixed here and documented
* skill `testing` before tests; `project-docs` after

### IP-02: Text Analysis

**Objective**

Detect the intention to depict minors and nudity or sexualized depiction in prompt texts.

**Scope**

* normalization against obfuscation
* lexicons for M and S, English first, then the languages of wave 1 of FP-010; lexicons are resources
  that can be extended by a file alone
* age expression parser (numbers, ranges, "-year-old", school grades, relations), adult markers and
  their consistency
* negative prompt rule; joined evaluation of all texts; each text alone and all together
* the `ClassifierEngine` text classification is called for the semantic part and merged by the
  higher level
* not: image analysis, engine process

**Affected Areas**

* `lib/guard` packages `text`, resources, tests

**Dependencies**

IP-01.

**Expected Result**

* the rule layer alone decides the clear cases without an engine; the semantic result can only
  raise a level, never lower it

**Technical Considerations**

* the lexicon files contain sensitive terms; they are plain resources with a documented format and are
  never shown in the UI or in logs
* false positives (for example "18+" adult content markers, adult school settings) are accepted at
  the uncertain level and are listed in the tests as known cases
* tests use neutral and abbreviated placeholders, no explicit text

### IP-03: Classification Engine Process

**Objective**

Provide the local engine that classifies texts and images.

**Scope**

* decision gate: select the engine approach and the candidate models, check their licences and
  hardware demands, ask the user before any new dependency (JVM or Python)
* local process on the Python environment of FP-005 IP-03; protocol over standard input and output
  (JSON lines); health check, start on demand, restart, timeout, stop at application end
* model provisioning with the verified download of FP-005 IP-02; the models with revision and
  checksum and the prompts are entries of `guard.properties`; models in the data directory
* engine capabilities: text classification (M and S levels), image classification (M and S levels,
  several persons)
* not: use in the generation path, UI

**Affected Areas**

* `lib/guard` packages `engine` and `config`, `guard.properties`, Python worker
  script as a resource, tests with a stub worker

**Dependencies**

IP-01, FP-005 IP-02, IP-03, IP-05.

**Expected Result**

* the engine answers classification requests for texts and images and reports its readiness; without
  models or process the answer is `UNDECIDABLE`

**Technical Considerations**

* the engine must not open a network port and must not use the network except for the model download
  that the user agreed to
* memory and GPU share with ComfyUI: the engine prefers the CPU or a small model; the decision gate
  records the demands
* no third party class is decompiled; the docs of the chosen packages are the source
* a crash or a hang of the process never leads to `ALLOWED`
* the worker receives model and prompt from the Kotlin side, which reads them from the properties
  file; the worker script holds neither
* the prompts are instructions for the classification only; they contain no explicit wording

### IP-04: Image Analysis

**Objective**

Turn the engine's image classification into the signals of the policy.

**Scope**

* per image: highest M level over all persons, highest S level, same image flag
* age uncertainty, partially visible persons, stylized depictions (drawn, rendered, anime) are
  treated like realistic ones
* image formats and sizes accepted by the project media; oversize or undecodable images give
  `UNDECIDABLE`
* hash cache in memory
* sampled preview checks: every N-th frame within a minimum interval, a hit interrupts the run
* not: generation wiring, UI

**Affected Areas**

* `lib/guard` package `image`, tests

**Dependencies**

IP-03.

**Expected Result**

* an image yields M and S levels in time; a repeated image is answered from the cache

**Technical Considerations**

* the preview frames are low resolution latent previews; their classification is weaker and is a
  protection against long violating runs only, the post-check stays the decision
* decoding happens in memory; no image is written to disk
* the downscaling for the engine keeps what an age and nudity decision needs

### IP-05: Generation Integration

**Objective**

Put the three checkpoints into the generation path so they cannot be skipped.

**Scope**

* `GenerationService`: constructor parameter `ContentGuard`, pre-check before upload, post-check
  after the end event, preview sampling, job state `BLOCKED` with the kind (`REQUEST`, `RESULT`,
  `UNDECIDABLE`)
* the `WorkflowBuilder` hands over the texts it really writes and the list of input images
* cleanup of the files of a blocked run in the data directory of ComfyUI (output, temp, uploaded
  inputs), best effort with a logged failure; the history entry of the server is removed through the
  facade if it offers the call, otherwise an extension of the facade is part of this plan
* the result handed to `ProjectManager` is always a checked one
* architecture test: no production class builds a `GenerationService` without a guard
* not: dialogs, tile drawing

**Affected Areas**

* `lib/comfy` packages `generation`, `workflow`; `lib/guard` API use; tests with fake engines

**Dependencies**

IP-02, IP-04, FP-006 IP-02.

**Expected Result**

* a violating request sends nothing; a violating result is never visible or stored; a missing engine
  stops the generation

**Technical Considerations**

* the check happens on a copy of the data that is really sent, not on the UI fields only
* the order is fixed: check, then upload; check, then show or store
* waiting jobs keep their snapshot; their result check uses the pre-check signals as well

### IP-06: Notices And Setup In UI

**Objective**

Make the protection visible and usable without exposing what triggered it.

**Scope**

* dialog for a blocked request, a blocked result and an undecidable check, one neutral text for
  blocks, one with the repair hint for `UNDECIDABLE`
* tile state `BLOCKED`: no image, message, stays until closed
* read-only guard state in the `Generation` category: ready, not installed, failed; a button for
  repair (install or restart the engine); no switch to turn it off
* guard models in the first-start setup flow and in the download progress; their licences appear in
  the licence list
* message bundles, German through the `translator`
* not: decision logic

**Affected Areas**

* `app/ui` dialogs, tile, settings page, first-start flow; message bundles

**Dependencies**

IP-05, FP-006 IP-03, IP-04, FP-005 IP-06, FP-010 IP-03.

**Expected Result**

* a user sees why a generation did not run or its result is missing, and can repair a missing
  engine; nothing tells how to avoid the check

**Technical Considerations**

* the text never names the word, the image part or the rule; the log has it
* skills: `ui-styling`, `dialog`, `component`, `translation`, `fx-component-lifecycle`, `icons`

### IP-07: Evaluation Tooling And Documentation

**Objective**

Make the quality of the protection measurable and document it honestly.

**Scope**

* Gradle task that runs the guard over a folder of labeled samples that the owner provides locally and
  writes a report with counts (blocked, allowed, undecidable) and no content; nothing is committed
* calibration notes for the thresholds
* README, MkDocs page (what is protected, what it cannot do), KDoc, `CHANGELOG.md`
* licences of the engine components and models in the licence list (FP-010 IP-09)
* pipeline: module list, the test split, no sample data in CI; module list of the runtime image
  (FP-011 IP-01)
* not: collecting or generating sample material

**Affected Areas**

* `lib/guard` tooling, `docs`, `README.md`, `CHANGELOG.md`, `.github`

**Dependencies**

IP-05.

**Expected Result**

* the owner can measure false positives and misses with their own legal data; the documentation says
  what the protection does and where its limits are

**Technical Considerations**

* skills: `project-docs`, `ci-pipeline`, `testing`
* the report is the basis for the threshold decision, not an automatic tuning

## 8. Dependency Graph

```text
IP-01
├── IP-02 ────────────┐
└── IP-03 (also FP-005 IP-02, IP-03, IP-05)
    └── IP-04 ────────┤
                      └── IP-05 (also FP-006 IP-02)
                          ├── IP-06 (also FP-006 IP-03, IP-04, FP-005 IP-06, FP-010 IP-03)
                          └── IP-07
```

Prerequisite features: FP-004, FP-005 and FP-006 (generation path). The number of this plan marks its
place in the plan sequence after the connection to ComfyUI; the dependencies above decide the order
of implementation (FP-006 itself needs the catalog FP-009).

## 9. Risks and Open Questions

Risks:

* No classifier is perfect: misses (a violating content passes) and false positives (a family photo or
  an adult is blocked) both occur; the protection lowers the risk and does not remove it
* Estimating the age of a person from an image is uncertain, most near 18; the conservative level
  raises false positives
* A text can describe a violation in a way no lexicon knows (new terms, other languages, code words);
  the semantic layer helps and has its own errors
* The application is open source; a user who changes the code or the build can remove the check; the
  protection is a barrier for accidental and casual misuse, not a tamper-proof lock
* The engine costs disk, memory and time per generation; a slow check lengthens every send
* Preview frames may show a violating image before the run is interrupted; they are never stored
* Real material for testing must not be created or kept; calibration depends on legal data of the
  owner
* A blocked result may still have left files in ComfyUI directories if the cleanup fails

Decided:

* the three checkpoints and the golden rules G1 to G3 of the product owner
* no off switch, no override, fail closed
* the engine is local, offline and separate from ComfyUI
* the guard models are mandatory and are not part of the user model catalog
* the library is `lib/guard`; models and prompts are fixed in its internal
  `guard.properties`, not in the code
* the ComfyUI client stays in `lib/comfy` as planned in FP-004; no separate connector module
* the user message is neutral, the rule appears only in the log
* nudity alone counts as signal S

Open questions:

* Is the union (combination) rule accepted as the standard (stricter than G1 to G3)?
* Which engine approach and which models (decision gate in IP-03); the approval of new dependencies
  for the JVM and for Python
* Are previews checked (sampled interruption) or only suppressed until the end?
* Is the import of media into a project (FP-008) also checked, or only the generation path?
* Which languages beyond wave 1 need a rule layer, and who reviews the lexicons?
* Is the engine allowed to use the GPU, or CPU only?
* Does the first-start setup have to ask for the guard models separately, or are they part of the
  base installation without an own consent step?

## 10. Feature Completion Criteria

* A prompt that describes persons under 18 together with nudity or a sexualized depiction is refused
  before anything is sent to ComfyUI
* A prompt that describes nudity or a sexualized depiction together with an input image of a person
  under 18 is refused; so is an input image that shows a person under 18 in that connection
* A generated image that shows a person under 18 in that connection is never shown as a result and
  never stored in the project; its files are removed from the ComfyUI directories
* All prompt texts of a run, and all input images including the medium from the main window, are
  part of the check
* The user sees a neutral message for a block, and a repair hint when the check is not possible
* No setting, flow file, environment variable or dialog turns the protection off; with a missing or
  failing engine no generation runs
* No checked content is logged or persisted; the logs show stage, rule and verdict only
* Tests run with fakes; the Gradle `build` passes; README, MkDocs, KDoc, `CHANGELOG.md` and the CI
  pipeline are up to date and state the limits of the protection
