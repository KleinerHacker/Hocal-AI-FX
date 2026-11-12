---
name: git and GitHub
---

# GIT

* All changes are made through GIT:
    * Rename / move: `git mv`
    * Delete: `git rm`
    * Create: add with `git add` after creation
* Commits, pushes, pulls or any other actions communicating with the Git server depend on where the
  session runs:
    * Cloud environment - the environment variable `CCR_AGENT_PROXY_ENABLED` is set:
        * commits and pushes to the branch assigned to the session are allowed without asking
        * creating a pull request stays the task of the user
        * every other branch, force pushes and history rewrites still need an explicit order of the user
    * User's own machine - the variable is NOT set:
        * they MUST NEVER be invoked, should it be required, the user MUST be asked
    * Environment cannot be determined: treat it as the user's own machine
* Plans and plan status files (`.claude/plans`) are NOT excluded:
    * they MUST be added and committed like every other file, status files included
    * this applies to feature plans, implementation plans and their status files

## Branches

* ONE branch and ONE pull request per Feature Plan
    * ALL implementation plans of a Feature Plan are delivered through that one branch
    * The branch is named after its Feature Plan: `FP-<NUMBER>-<FeatureName>`, exactly as in the file
      name of the Feature Plan (`FP-001-ProjectFileStructure`)
    * A new branch is started only for the next Feature Plan, never per implementation plan

## Target Environment

* GitHub MUST be used
* All files around GitHub reside in `.github`
* Structure of the pipelines is in the `ci-pipeline` skill, which MUST be loaded before a workflow
  file is created or changed and after structural project changes
