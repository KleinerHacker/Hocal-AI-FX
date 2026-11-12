---
name: git and GitHub
---

# GIT

* All changes are made through GIT:
    * Rename / move: `git mv`
    * Delete: `git rm`
    * Create: add with `git add` after creation
* Commits, pushes, pulls or any other actions communicating with the Git server MUST NEVER be invoked.
    * Should it be required, the user MUST be asked
* Plans and plan status files (`.claude/plans`) are NOT excluded:
    * they MUST be added and committed like every other file, status files included
    * this applies to feature plans, implementation plans and their status files

## Pull Requests

* ONE pull request per Feature Plan
    * ALL implementation plans of a Feature Plan are delivered through the same branch and the same pull
      request
    * A new pull request is opened only for the next Feature Plan, never per implementation plan
* The user creates the pull request; the session subscribes to it and waits for the GitHub build
* An implementation plan counts as finished only after the GitHub build of its last push is green
    * Waiting for the GitHub build is ALWAYS required; a local build is not a replacement
* After the pull request of a Feature Plan is merged, the next Feature Plan starts on a fresh branch from
  the latest default branch

## Target Environment

* GitHub MUST be used
* All files around GitHub reside in `.github`
* Structure of the pipelines is in the `ci-pipeline` skill, which MUST be loaded before a workflow
  file is created or changed and after structural project changes
