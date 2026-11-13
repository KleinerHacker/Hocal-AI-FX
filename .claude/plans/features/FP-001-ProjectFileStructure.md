# Feature Plan: Project File Structure

## 1. Objective

Define and implement the file format in which a Hocal AI project is stored, together with the
libraries and code that read, write and edit it:

* a project is one standard ZIP file with `project.json` in its root
* every reference owns a numbered media history
* the project file can optionally be encrypted with AES
* the libraries `lib/model` and `lib/fx-model` are created as part of this feature

## 2. Implementation

### Modules and packages

* `lib/model` (`hocal-model`), free of Java FX
    * `org.pcsoft.app.hocal.model.project` - the plain objects as Kotlin data classes: `ProjectFile` (root),
      `ProjectData`, `Reference` (UUID and its `media` list), `Medium` (prompt, model, flow, seed, parameters),
      the tree `VirtualNode` with `VirtualFolder` and `VirtualEntry`, and the constants `ProjectFormat`
    * `org.pcsoft.app.hocal.model.tree` - the name rules `TreeNames` and the tree errors
    * `org.pcsoft.app.hocal.model.storage` - the errors of the storage and of the media history
    * `org.pcsoft.app.hocal.model.storage.internal` and `org.pcsoft.app.hocal.model.tree.internal` - the project
      storage with its encryption, the media store, the media history operations and the tree operations
* The two `internal` packages are exported to `hocal-fx-model` only (qualified exports in the module
  descriptor), because Kotlin `internal` does not reach across Gradle modules; `ModuleEncapsulationTest` checks
  the descriptor. Everything else is public
* `lib/fx-model` (`hocal-fx-model`), on `javafx.base` only
    * `org.pcsoft.app.hocal.fx.model.project` - one `<Pojo>Property` per POJO (`ProjectFileProperty` is the
      root) with nested change propagation and `refresh()`; the internal `BeanFields` connects them to the POJOs
    * `org.pcsoft.app.hocal.fx.model.manager` - the singleton `ProjectManager`
* Jackson 3 (`tools.jackson.core:jackson-databind`) is the only new dependency; the model packages are opened to its
  databind module, and `lib/model` exposes it as `api`

### File format

* ZIP, every entry stored without compression, `project.json` always the first entry
    * media as `<reference-uuid>/<NNN>.<extension>`, `NNN` from `000` to `999`, the extension may differ per medium
    * `project.json` holds `project` (data object, UTC time stamps in ISO-8601, format version 1), `references`
      (UUID and media list, the medium number is its list position) and `tree` (a node has `"type": "folder"` with
      children or `"type": "entry"` with one reference UUID)
* Reading rejects a missing or misplaced `project.json`, unsafe entry names (zip slip), oversized entries or too
  many entries (`StorageLimits`) and a newer format version. Writing validates first, so an invalid project is
  never written. A save goes through a temporary file and an atomic move; the stream variants never close their
  streams
* Encrypted file: a 78 byte plain header (magic `HOCALENC`, version, key derivation id, iteration count, salt, base
  IV, block size, password check value) followed by the ZIP content in blocks of 64 KiB
    * AES-256-GCM per block with an own tag; the block IV is the base IV with a block counter; the header and a
      last-block flag are authenticated data, so cutting off, swapping and header changes are detected
    * key from the password by PBKDF2-HMAC-SHA256 with 600,000 iterations (read from the header), fresh salt and IV for
      every save; a wrong password is found by the check value before any block is read
    * reading is stream-only and verifies every block before its data is used; the rest of the stream is consumed so
      the last block is checked

### Behaviour

* Validation (reading and writing): format version, UUID form, no duplicate reference, 1 to 1000 media per
  reference, every entry links an existing reference, the root folder has no name
* Media history: the last medium is the current one; appending adds number + 1 (error beyond `999`); removing a
  medium moves the following ones down; the original `000` cannot be removed, so a reference never has no medium
    * the content lives in a `MediaStore` (`DirectoryMediaStore` in a temporary folder while a project is open)
* Tree operations: create, rename and move folders and entries, delete, remove a reference; names are not empty, have
  no space at the ends, at most 255 characters, none of `/ \ : * ? " < > |`, siblings are unique without regard to
  case; no cycle on move; the root cannot be renamed, moved or deleted
    * deleting the last entry of a reference changes nothing and answers `false`; only with `removeOrphans` the
      reference and its media are removed, `orphansOf` tells beforehand which ones
* `ProjectManager` owns the one loaded project and is the only way to change it
    * properties `project`, `loadedProperty`, `pathProperty`, `dirtyProperty`, `encryptedProperty`
    * `createNew`, `open`, `save`, `saveAs`, `close(discardChanges)` and `isPasswordRequired(path)`; the password is
      changed only by `saveAs`; saving sets the time of the last change and the application version
    * every operation of the tree and the media history is offered by the manager, refreshes the property models
      and marks the project as changed; a field edited through a property model counts as a change as well
    * the FX thread is checked through `threadCheck`, which the application sets at start together with
      `applicationVersion`
* Errors are typed: `ProjectFileException` (invalid, damaged, unsupported format version, password required,
  wrong password, manipulated, unsupported encryption version), `TreeException` and `MediaHistoryException`

### Deviations from the plan

* Block-wise GCM instead of one cipher stream, because the JDK holds a whole decrypted stream in memory until its
  tag is verified
* The history is the `media` list of the `Reference` and a `MediaStore`, not a folder scanned in the ZIP
* Normal control flow answers with a Boolean instead of an exception: `close` with unsaved changes, `delete` with
  orphaned references
* The operations live in protected packages exported to `hocal-fx-model` only, so no other code can call them
* The root folder must have no name; `isPasswordRequired` was added so a user interface can ask before `open`

## 3. Implementation Plans

| ID    | Implementation Plan     | Result                                                                                     |
|-------|-------------------------|--------------------------------------------------------------------------------------------|
| IP-01 ✅ | Model Library And POJOs | `lib/model` with the data classes of the file, format constants and the Jackson setup       |
| IP-02 ✅ | FX Property Models      | `lib/fx-model` with one property model per POJO and change propagation to the root          |
| IP-03 ✅ | Project ZIP Storage     | Reading and writing the ZIP, validation, limits, atomic save, typed errors                  |
| IP-04 ✅ | Reference Media History | Media list per reference, `MediaStore`, append and remove with continuous numbering         |
| IP-05 ✅ | Virtual Tree Operations | Name rules and tree operations with orphan handling                                         |
| IP-06 ✅ | Project File Encryption | Optional block-wise AES-256-GCM with a password derived key                                 |
| IP-07 ✅ | Project Manager         | Singleton that owns the loaded project and is the only way to change it                     |

## 4. Known Limits

* The POJOs are mutable, because Jackson and the property models need setters; a change made directly on them, past the
  manager, does not mark the project as changed. `ProjectManager.project` is a writable property, replacing its value
  bypasses the manager
* The module protection of the `internal` packages works on the module path only; the tests run on the class path, and
  `app/ui` does not use the libraries yet
* Exchanging a nested model object against an equal one keeps the old instance in the parent POJO, because
  `BeanFields` compares by `==`
* Stream-only reading is sequential and a result counts only after the whole stream was consumed; the sink may already
  have received media of the valid part before a failure is reported
* Appending a medium rewrites and re-encrypts the whole file on the next save
* A lost password cannot be recovered by design; only the header of an encrypted file is plain
* Use in `app/ui` (creating, opening, saving, password prompt) is not part of this feature; a user setting for the
  maximum size of a medium is planned in FP-010

## 5. Completion Criteria

* [x] `lib/model` and `lib/fx-model` exist, are built by `build` and covered by tests
* [x] A saved unencrypted file is a standard ZIP with `project.json` in its root, reading restores everything identically
* [x] Every reference has media numbered from `000`; appending and removing keep the numbering continuous, the
  original `000` cannot be removed, an overflow beyond `999` is an error
* [x] One UUID linked by several entries is read and written correctly
* [x] A file saved with a password cannot be read without it; a wrong password and a manipulated file are rejected
* [x] A file without `project.json`, a damaged ZIP and a newer format version are rejected; a failed save leaves the
  previous file intact
* [x] Every POJO has a tested property model, and only one project is loaded at a time, every change goes through the
  project manager
* [x] README, MkDocs, pipeline and structure documentation are up to date
