<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/docs/assets/logo-dark.png">
    <img src="docs/docs/assets/logo.png" alt="Hocal AI logo" width="400">
  </picture>
</p>

# Hocal AI (hocal-ai-fx)

Hocal AI is a JavaFX desktop application for generating images and videos with AI models that run
locally. It drives [ComfyUI](https://docs.comfy.org/) as a separate process through its HTTP and
WebSocket API and offers the work as **flows** - ready-made generation pipelines the user picks and
runs without building a ComfyUI graph.

## Documentation

* User documentation (MkDocs, GitHub Pages): <https://kleinerhacker.github.io/Hocal-AI-FX/latest/>
* API documentation (Dokka): part of the user documentation, section "API Docs"
* Licence report of all dependencies: part of the user documentation, section "Licences"

## State of implementation

| Feature                                                        | State       |
|----------------------------------------------------------------|-------------|
| Main window with File menu (Exit)                              | Implemented |
| Main window splitter layout (placeholder parts)                | Implemented |
| Project folder tree component (not yet shown in the window)    | Implemented |
| File list with media previews (not yet shown in the window)    | Implemented |
| English and German user interface                              | Implemented |
| Application icon in the main window                            | Implemented |
| Portable distribution archive (ZIP) and jlink image            | Implemented |
| Connection to a local ComfyUI process                          | Planned     |
| Flows read from ComfyUI                                        | Planned     |
| Running a flow with its inputs and showing the result          | Planned     |
| Model download and installation                                | Planned     |

## Project structure

* `app/ui` (`hocal-ui`) - the JavaFX application (Kotlin, MVVM FX)
* `lib/model` (`hocal-model`) - the JavaFX-free data model of the project file and its (optionally encrypted) ZIP storage
* `lib/fx-model` (`hocal-fx-model`) - the Java FX property models mirroring the data model and the project manager that owns the loaded project
* `docs` - the MkDocs user documentation

## Prerequisites

* JDK 25 (the Gradle toolchain downloads one if none is installed)
* Git

## Check out

```shell
git clone https://github.com/KleinerHacker/Hocal-AI-FX.git
cd Hocal-AI-FX
```

## Build

```shell
./gradlew build
```

`build` compiles, runs the developer tests and the integration tests (`*IT`) headless, and packs the
distribution archive. On Windows use `gradlew.bat` instead of `./gradlew`.

## Run

```shell
./gradlew run
```

## Consume the artifacts

* `./gradlew packageDist` writes the portable archive
  `app/ui/build/distributions/hocal-ui-<version>-<platform>.zip` for the platform of the building
  host. Unpack it and start `hocal-ui.sh` (Linux, macOS) or `hocal-ui.bat` (Windows) from its root;
  a Java 25 runtime has to be installed.
* `./gradlew :app:hocal-ui:jlink` builds a self-contained runtime image under `app/ui/build/image`
  that carries its own Java runtime.
* The release workflow builds the archive on Linux, Windows and macOS and attaches them to the
  GitHub release of each tag.

## Licence

Apache License 2.0, see [LICENSE](LICENSE).
