# Feature Plan: Bundled Java Runtime

## 1. Objective

Ship the application with every Java part it needs, so that no Java runtime (JRE or JDK) has to be
installed on the target system. The user unpacks the release archive of their operating system and
starts the application; nothing else is required.

## 2. Current State

* `app/ui/build.gradle.kts` already applies the `org.beryx.jlink` plugin (`jlink` block: merged
  module for `mvvmfx`/`typetools`, `addExtraDependencies("javafx", "kotlin")`, compress options,
  launcher `hocal-ui`, `imageZip` to `build/distributions/hocal-ui-image-<platform>.zip`)
* The README mentions `./gradlew :app:hocal-ui:jlink` as building a self-contained image, but no
  task, test, CI job or release asset uses it; its result is not verified to start
* The shipped artifact is `packageDist`: a ZIP with `hocal-ui.sh` / `hocal-ui.bat` at the root and all
  JARs in `libs`; the scripts call `java` (or `$JAVA_HOME/bin/java`), so Java 25 must be installed
  (README, `docs/docs/index.md`)
* The application is a named module (`org.pcsoft.app.hocal.ui`, `module-info.java`); `log4j-core` and
  `log4j-slf4j2-impl` are `runtimeOnly` and only found through the service loader at run time
* `JavaFX` resolves to the classifier of the building host (`javafx.platform.classifier`); the release
  workflow therefore builds on Linux, Windows and macOS and uploads `hocal-ui-*.zip`
* Tests run on the classpath, because Monocle and `ResourceBundle.Control` are not usable inside a
  named module (comment in `app/ui/build.gradle.kts`)
* The application drives ComfyUI as a separate process (FP-004, FP-005); ComfyUI brings its own Python
  and is not part of the Java runtime question
* `release.yml` and `ci.yml` build with Temurin 25 via `actions/setup-java`

## 3. Target State

* Every release archive contains an application image with its own minimal Java runtime (created with
  `jlink`), the application modules and a native launcher; the archive starts on a machine without
  any Java installation
* The runtime contains exactly the modules the application needs, including the service providers
  that are only found at run time (logging) and the locale data for all supported languages
* The image is built and verified in the pipeline on every host family; a start failure of the image
  fails the build
* README, MkDocs and CHANGELOG describe the self-contained archive; the bundled runtime is named in
  the licence information

## 4. Requirements

### Functional Requirements

* The release archive of each platform starts the application without `java` on `PATH` and without
  `JAVA_HOME`
* The behaviour of the application inside the image equals the behaviour of `./gradlew run`:
  FXML and resource bundles load, logging writes, English and German texts appear, themes apply
* The launcher passes the same JVM options as today (`--enable-native-access=javafx.graphics`)
* One archive per platform (Linux, Windows, macOS) is attached to the GitHub release

### Technical Requirements

* Gradle and Kotlin stay the build technology; the existing `org.beryx.jlink` plugin is used - no new
  dependency without asking the user (`dependencies.md`)
* A jlink image is platform specific and cannot be cross-built; the existing OS matrix is kept
* The runtime comes from the JDK 25 toolchain of the build; the same distribution (Temurin) as in CI
* The module descriptor stays the single source of truth for the modules; no manual module list that
  can drift from `module-info.java`
* Test and developer workflow (`run`, `test`, `build`) stays on the classpath/module path as now
* The licence of the bundled runtime (GPL-2.0 with Classpath Exception) is documented; the
  `licensee` check is not weakened

## 5. Architecture

* Module `app/ui` stays the only module with the toolkit; no new module is needed
* Build: the `jlink` configuration in `app/ui/build.gradle.kts` becomes the main distribution path;
  `packageDist` either wraps the image or is kept as secondary archive (see open questions)
* Required runtime modules: JavaFX (`controls`, `fxml` and their requirements), Kotlin stdlib and
  `mvvmfx` merged module, `org.slf4j`, Log4j 2 providers (service providers via `uses`/`provides`),
  `jdk.localedata` (German and all further languages of FP-009 IP-11), plus what `jdeps` reports
* Launcher: the generated `bin/hocal-ui` (and `.bat`) of the plugin replaces `src/dist/hocal-ui.sh|bat`
  inside the image archive
* Verification: a Gradle task runs the launcher of the built image headless and checks the module
  list; the CI jobs run it on each host family
* Pipeline: `release.yml` and `ci.yml` get the image build and the image check; the release job
  attaches the image archives

## 6. Implementation Plan Overview

| ID    | Implementation Plan        | Objective                                                                 | Dependencies |
|-------|----------------------------|---------------------------------------------------------------------------|--------------|
| IP-01 | Runtime Image Build        | A jlink image that really contains every needed module and starts         | -            |
| IP-02 | Image Verification         | Automatic check that the image starts without an installed Java           | IP-01        |
| IP-03 | Image Distribution Archive | The release archive of each platform is the self-contained image          | IP-01        |
| IP-04 | Pipeline Integration       | CI and release build, verify and publish the image archives per host      | IP-02, IP-03 |
| IP-05 | Documentation And Licences | README, MkDocs, CHANGELOG and licence information describe the new state  | IP-03        |

## 7. Implementation Plans

### IP-01: Runtime Image Build

**Objective**

Make `jlink` produce a working application image on the building host.

**Scope**

* Fix the `jlink` configuration until the image starts: merged module, extra dependencies, module
  list, service providers of Log4j 2, `jdk.localedata`
* Make resource bundles, FXML loading and reflection access (`opens` in `module-info.java`) work from
  the image
* Launcher with the native-access JVM option
* Not in scope: archive layout, pipeline, documentation

**Affected Areas**

* `app/ui/build.gradle.kts` (`jlink` block, `application`)
* `app/ui/src/main/java/module-info.java` (only if `uses`/`provides` or `opens` are missing)

**Dependencies**

None.

**Expected Result**

`./gradlew :app:hocal-ui:jlink` writes `app/ui/build/image`; its launcher opens the main window
without an installed Java.

**Technical Considerations**

* `mvvmfx` and `typetools` have no descriptor; the merged module is generated - the existing
  workaround must be re-verified with JDK 25 and JavaFX 25.0.1
* Log4j providers are `runtimeOnly`; `jdeps` does not see them - they must be added explicitly
* `ResourceBundle.Control` is unsupported in named modules (see test configuration); the image runs
  on the module path, so the bundle loading of the application must be checked there
* Without `jdk.localedata` only English formats are available in the image
* `--strip-debug`, `--compress` and `--no-header-files` options are kept; check that stack traces in
  logs stay usable

### IP-02: Image Verification

**Objective**

Detect automatically when the image misses a module or fails to start.

**Scope**

* A Gradle task that starts the image launcher in headless mode and fails on a non-zero exit or on
  a missing module (checked against a fixed expected list)
* Run the check with `JAVA_HOME` unset and `java` removed from `PATH`
* Not in scope: CI wiring (IP-04); UI behaviour tests

**Affected Areas**

* `app/ui/build.gradle.kts` (new verification task)
* `app/ui/src/test` (integration test `...IT`, per the `testing` skill, only if it fits the pattern)

**Dependencies**

IP-01.

**Expected Result**

`./gradlew :app:hocal-ui:verifyImage` (name open) passes on a good image and fails on an image
without Log4j providers or locale data.

**Technical Considerations**

* A real toolkit start needs a display; the check uses the Monocle/headless route or a start mode of
  the launcher that exits right after initialisation - decision in the implementation plan
* The task must work on Windows, macOS and Linux and use full qualified paths
* Load the `testing` skill before a test class is created

### IP-03: Image Distribution Archive

**Objective**

Turn the image into the release archive for each platform.

**Scope**

* Archive name `hocal-ui-<version>-<platform>.zip` containing the image (runtime, modules, launcher)
* Decide the fate of the JRE-dependent `packageDist` archive and `src/dist` scripts
* `assemble` / `build` produce the new archive
* Not in scope: upload to the release (IP-04)

**Affected Areas**

* `app/ui/build.gradle.kts` (`jlink.imageZip`, `packageDist`, `assemble`)
* `app/ui/src/dist/hocal-ui.sh`, `hocal-ui.bat`

**Dependencies**

IP-01.

**Expected Result**

The archive unpacks to a folder that starts with `bin/hocal-ui` (or `.bat`) on a machine without Java.

**Technical Considerations**

* Unix file permissions of the launcher must survive the archive
* The version from `-PreleaseVersion` flows into the file name as today
* The archive size grows by the runtime (about 40-60 MB more); acceptable for a release asset

### IP-04: Pipeline Integration

**Objective**

Build, verify and publish the self-contained archives from the pipelines.

**Scope**

* `release.yml`: build job produces the image archive per host; the verification of IP-02 runs on
  every host family; the release job attaches the archives
* `ci.yml`: the image build and verification run on pull requests
* Not in scope: signing and notarisation, installers

**Affected Areas**

* `.github/workflows/release.yml`, `.github/workflows/ci.yml`

**Dependencies**

IP-02, IP-03.

**Expected Result**

A tag creates a GitHub release with three archives that each start without Java.

**Technical Considerations**

* Load the `ci-pipeline` skill before a workflow file is changed; keep the prescribed job structure
* The verification step must not get Java from `setup-java` on its `PATH` (the image check removes it)
* Path of the uploaded artifact changes with the archive name

### IP-05: Documentation And Licences

**Objective**

Document the self-contained distribution and the licence of the bundled runtime.

**Scope**

* README: prerequisites for users vs. developers, archive description, remove "Java 25 has to be
  installed"
* MkDocs: installation page and `docs/docs/index.md`
* CHANGELOG `[Unreleased]` entry
* Licence information: name the bundled runtime and its licence (GPL-2.0 with Classpath Exception)
* Not in scope: new features in the About dialog (FP-009 IP-09 owns it; this plan only supplies text)

**Affected Areas**

* `README.md`, `docs/docs/`, `CHANGELOG.md`, licence report configuration if needed

**Dependencies**

IP-03.

**Expected Result**

The documentation matches the delivered archives; the mkdocs `--strict` build passes.

**Technical Considerations**

* Load the `project-docs` skill; documentation text in English
* Keep README's "State of implementation" table in line with the new state

## 8. Dependency Graph

```text
IP-01
├── IP-02
│   └── IP-04
└── IP-03
    ├── IP-04
    └── IP-05
```

## 9. Risks and Open Questions

* Open: Keep the JRE-dependent `packageDist` archive next to the image archive, or replace it?
  Proposal: replace it, one archive per platform
* Open: Is a native installer (`jpackage`: MSI/DMG/DEB) wanted later? Not part of this feature
* Risk: the merged module of `mvvmfx`/`typetools` may not link with JDK 25 / JavaFX 25
* Risk: service providers or reflective access missing in the image only show at run time - IP-02
  must start the real application, not only list modules
* Risk: `ResourceBundle.Control` is not supported in named modules; the language support of FP-009
  IP-11 must stay compatible with the image
* Risk: unsigned executables on Windows and macOS trigger warnings (Gatekeeper, SmartScreen)
* Risk: FP-009 IP-11 adds many languages; `jdk.localedata` size and its inclusion must be kept in
  sync
* Assumption: the Temurin 25 JDK of the CI is the runtime source for all platforms

## 10. Feature Completion Criteria

* The archive of each of the three platforms starts the application on a machine without Java
* The pipeline builds and checks the image on every host family; a defect of the image fails the build
* A release attaches only archives that carry their own runtime (or the JRE-dependent archive too, per
  the decision in the open questions)
* README, MkDocs, CHANGELOG and licence information state that no Java installation is needed
