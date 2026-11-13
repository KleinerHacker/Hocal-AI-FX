---
name: ci-pipeline
description: Required structure of the GitHub Actions pipelines ci.yml and release.yml under .github. Load before creating or changing any workflow file, and after structural project changes (new module, new Gradle task, new artifact) to check the pipeline.
---

# Target Environment

* GitHub MUST be used
* All files around GitHub reside in `.github`
* For deeper structural changes the pipeline MUST be checked and adjusted if necessary

## Pipeline

* There MUST be a pipeline for the regular build in `ci.yml`
    * It contains: [Build] Build -> Developer tests / Integration tests, [Verify] Licences / Build and verify MkDocs
    * `Build` and `Verify` MUST be processed in parallel; everything within `Verify` MUST also run in parallel
* There MUST be a pipeline for a tag based release named `release.yml`
    * It contains: [Changelog] Verify against version -> ([Build] Build and package per host -> Developer tests / Integration tests, [Verify] Verify licences, [MkDocs] Build -> Deploy, [Release] Write release with the archives)
    * `Changelog` runs first
    * `Build`, `Verify`, `MkDocs` in parallel afterwards
        * `Verify` internally in parallel as well
    * `Release` at the end
        * Deployment errors MUST be ignored, but shown as a warning

## Change Detection

* The job `changes` of `ci.yml` decides which jobs run; a job runs only when something it needs changed
    * `.github/**` - everything, the pipeline itself changed; except `.github/dependabot.yml`, which builds nothing
    * `docs/**`, `README.md`, `CHANGELOG.md`, `LICENSE` - only the docs build
    * tests (`**/src/test/**`) - only the code jobs, the docs embed no test code
    * everything else - sources, build files and anything unknown - the code jobs and the docs build, because
      the docs embed KDoc and the licence report
    * `.claude/**`, `.gitignore`, `.gitattributes`, `.github/dependabot.yml` - nothing
* Files under `.claude/**` and `.github/dependabot.yml` NEVER start a run: `ci.yml` lists them in `paths-ignore` for
  `push` and `pull_request`
    * a change of ONLY such files reports no check at all, so a branch protection MUST NOT require checks
      for such a change
* A new top-level path MUST be placed in this table when it is added
