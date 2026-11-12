# Feature Plan: Project File Structure

## 1. Objective

Define and implement the file format in which a Hocal AI project is stored, together with the
libraries and code that read, write and edit it:

* a project is one standard ZIP file
* `project.json` in the ZIP root describes everything
* every reference owns a folder in the ZIP holding its media as a numbered history
* the project file can optionally be encrypted with AES through a cipher stream
* the libraries `lib/model` and `lib/fx-model` are created as part of this feature

## 2. Current State

* Only the module `app/ui` (`hocal-ui`) exists; `lib/model` and `lib/fx-model` do not exist yet
* No JSON library is in use; `app/ui/build.gradle.kts` lists commons-lang3, controlsfx, mvvmfx,
  slf4j and Log4j 2 only
* A new dependency needs the user's decision first (`dependencies.md`); licences are checked by the
  `licensee` plugin
* No cryptography is in use anywhere
* Conventions to reuse: KDoc on every public member, JUnit tests per class (`testing` skill),
  `IT` suffix for integration tests, the mirroring rule of the `fx-model` skill

## 3. Target State

* `lib/model` (`hocal-model`) holds the JavaFX-free POJOs of the file format
* `lib/fx-model` (`hocal-fx-model`) holds the matching property models
* A storage in `lib/model` reads and writes the ZIP; it keeps no state
* The media history of a reference can be read (current medium, history) and extended
* The virtual directory structure can be edited without touching the ZIP layout
* A singleton project manager holds the one loaded project and handles every change
* A project file can be written encrypted and is read back with the password

## 4. Requirements

### Functional Requirements

* An unencrypted project is a standard ZIP file readable by any ZIP tool
* `project.json` MUST exist in the ZIP root; a ZIP without it is not valid
* `project.json` consists of exactly three parts:
    * the project data - ONE object holding the general data:
        * name
        * description
        * project UUID
        * author
        * creation time and time of the last change
        * tags
        * format version
        * version of the application that saved the file last
    * the references - a list; each reference holds its UUID and its media as a list; each medium
      carries its own history details, its number `000` to `999` is its position in the list
    * the virtual directory structure
* Every reference has a folder in the ZIP, named after its UUID, holding the media of the reference
* The media of a reference are numbered by file name from `000` to `999`, followed by the file
  extension of the medium (`000.png`, `001.png`, `002.webp`); the extensions of the media of one
  reference may differ
    * the numbering is always continuous: `000` up to `n` without a gap
    * the highest number in the folder is the current state of the counter; no counter is stored
      anywhere else
    * the next medium gets the highest number + 1; an overflow beyond `999` is an error
    * if a medium is removed from the middle, the following media are renumbered so that the
      numbering is continuous again
* The media of a reference are a history
    * `000` is the original, unchanged medium
    * the medium with the highest number is the current one and the only valid one
    * all others are history only
* A reference always has at least one medium; a reference without a medium does not exist
    * the original `000` can never be deleted; as it always exists, the last remaining medium of a
      reference cannot be deleted either
    * to get rid of a reference, the whole reference is deleted, which removes its folder and its
      UUID
    * every other medium, the current one included, can be deleted; the one before it becomes the
      current medium
* Every medium has history details: the prompt, the model and the flow it was generated with, the seed
  and the values of the editable parameters of the flow (by parameter id, see FP-008), each of which
  may be empty
    * explicitly used additional images are NOT recorded
    * the details are stored in `project.json` below their reference, under the number of the medium
    * the details belong to their medium: they follow it when media are appended, removed or
      renumbered
* The virtual directory structure is a tree of folders and entries stored in `project.json`
    * an entry links a reference by UUID
    * the link is 1:n - one UUID can be linked by several entries
* Encryption is optional per project file
    * an encrypted file is the ZIP content passed through an AES cipher stream
    * the password decides the key; a wrong password or a manipulated file is reported as such
    * reading detects on its own whether a file is encrypted
    * a file can be saved encrypted, saved unencrypted, and switched between both
* An invalid, damaged or unknown-version file is reported with a clear error, never half-loaded

### Technical Requirements

* Kotlin and Gradle only
* POJOs in `lib/model` are free of JavaFX; `lib/fx-model` depends on `javafx.base` only
  (`architecture.md`)
* Every POJO gets its property model in the same change (`fx-model` skill); a storage gets none
* The storage keeps no state
* The format carries a version so later changes can be migrated
* Time stamps are stored in UTC, ISO-8601
* Reading protects against path traversal in ZIP entry names (zip slip) and oversized entries
* Saving is atomic: a failed save MUST NOT destroy the existing file
* A medium file is never overwritten in place; a new medium always gets the next number
* ZIP entries are always stored without compression (`STORED`), media and `project.json` alike
    * a stored entry needs its size and CRC before its header is written, so entries are measured
      first
* A file with a newer format version than the application knows is rejected
* Removing a medium renumbers the following ones, so the numbering stays continuous
* A reference folder without a medium, or with gaps in the numbering, is an invalid file
* Encryption uses the JDK (`javax.crypto`: `Cipher`, `CipherOutputStream`, `CipherInputStream`,
  `SecretKeyFactory`) with AES; no cryptography dependency
    * AES with a 256 bit key in GCM mode (`AES/GCM/NoPadding`), which authenticates the content
    * key derived from the password with PBKDF2 (`PBKDF2WithHmacSHA256`, JDK), a random salt and
      600,000 iterations; salt and iteration count are stored in the header
    * fresh random salt and IV for every save
    * reading is stream-only: the decrypted stream goes straight into a `ZipInputStream`, no
      temporary file is ever written; therefore `project.json` MUST be the first ZIP entry
    * password handled as `CharArray` and wiped after use; never logged, never stored
* JSON is handled with Jackson (decided); it is the only new dependency of this feature
    * Jackson reads and writes the POJOs through their getters and setters, so the Kotlin module
      is added only if it turns out to be needed
    * the Java module system needs the model packages opened to Jackson's databind module
    * its licence must pass the `licensee` check; a missing licence in the allowed part is
      reported to the user
* No further new dependency without asking the user first
* Every new public member has KDoc; every test method has a KDoc describing its use case
* README, MkDocs and CHANGELOG are checked afterwards (`project-docs` skill); a changelog entry
  only if an end user can notice the change

## 5. Architecture

### Components

* `lib/model` (`hocal-model`) - new module
    * `ProjectData` - the general data object
    * `ProjectFile` (name open) - root POJO mirroring `project.json`: project data, references
      with their history details, tree
    * `VirtualNode` (folder / entry) - the tree; an entry carries a reference UUID
    * storage - reads and writes the ZIP and `project.json`
    * media history access per reference
    * encryption - wraps the streams of the storage in AES cipher streams
* `lib/fx-model` (`hocal-fx-model`) - new module
    * one `<Pojo>Property` per POJO, nested change propagation

### ZIP Layout

```text
project.zip
├── project.json
├── <reference-uuid-1>/
│   ├── 000.png      original medium
│   ├── 001.png
│   └── 002.webp     current medium (highest number)
└── <reference-uuid-2>/
    └── 000.png
```

### Encrypted File

```text
encrypted file
├── header   magic, format version, key derivation parameters, salt, IV
└── body     ZIP content passed through the AES cipher stream
```

The header is plain, so the file is recognisable and the key can be derived. Everything else, the
ZIP structure and `project.json` included, is encrypted.

### project.json

```text
{
  "project":    { name, description, uuid, author, created, modified, tags, formatVersion, applicationVersion },
  "references": [ { "uuid": "<uuid>", "media": [ { prompt, model, flow, seed, parameters }, { ... } ] }, ... ],
  "tree":       folder with children; a node has "type": "folder" (children) or "entry" (one "reference": "<uuid>")
}
```

The key names are fixed by IP-01.

### Data Flow

* Read: file -> (decrypt with password if encrypted) -> storage -> POJOs -> property models
* Save: property models (wrap the POJOs) -> storage -> (encrypt if requested) -> temporary file ->
  atomic replace
* New medium: reference UUID -> folder -> highest number + 1 -> new medium

### Persistence

* `project.json` and the reference folders inside the ZIP are the only persistence

## 6. Implementation Plan Overview

| ID    | Implementation Plan      | Objective                                                              | Dependencies |
|-------|--------------------------|------------------------------------------------------------------------|--------------|
| IP-01 ✅ | Model Library And POJOs  | Create `lib/model` with project data, reference list and tree POJOs     | -            |
| IP-02 ✅ | FX Property Models       | Create `lib/fx-model` mirroring all POJOs                              | IP-01        |
| IP-03 ✅ | Project ZIP Storage      | Read and write ZIP and `project.json`, validation, atomic save          | IP-01        |
| IP-04 ✅ | Reference Media History | Reference folders, continuous numbering 000-999, current medium, add and remove media | IP-01, IP-03 |
| IP-05 ✅ | Virtual Tree Operations | Edit the virtual directory structure consistently with the references   | IP-01        |
| IP-06 ✅ | Project File Encryption | Optional AES encryption of the project file through cipher streams      | IP-03        |
| IP-07 | Project Manager          | Singleton manager holding the one loaded project and handling all changes | IP-02, IP-03, IP-04, IP-05, IP-06 |

## 7. Implementation Plans

### IP-01: Model Library And POJOs ✅ COMPLETED

**Objective**

Create `lib/model` (`hocal-model`) and the POJOs of `project.json`.

**Scope**

* Gradle module registered in `settings.gradle.kts`, set up like `app/ui` (Kotlin, KDoc, licence
  check, Kover, tests)
* POJOs for the project data object, the root, the virtual tree (folder, entry) and the history
  details of a medium (prompt, model, flow, seed, parameter values)
* Reference POJO: UUID and the media as a list, each medium carrying its history details
* Format version constant and the fixed key names
* NOT: serialization, ZIP handling, encryption, FX models

**Affected Areas**

* `settings.gradle.kts`, root `build.gradle.kts`, new `lib/model`, CI pipeline (`ci-pipeline` skill)

**Dependencies**

None.

**Expected Result**

A JavaFX-free, documented and unit-tested data model of the file.

**Technical Considerations**

* Get/set convention required by `BeanFields` of the `fx-model` skill
* The tree is recursive
* An entry links a UUID that must exist in the reference list; checking this belongs to the storage
  validation and the tree operations
* README structure section and pipeline are checked after adding the module

**As Built**

* Package `org.pcsoft.app.hocal.model.project`: the constants object `ProjectFormat` (format version 1,
  file name, key names, JSON types, medium range `000` to `999`, `mediumKey()`) and the POJOs
    * Data classes (`var` properties with defaults, so getters, setters and a no-argument constructor
      exist): `ProjectFile` (root), `ProjectData`, `Reference`, `Medium`
    * `Reference.media` is a list of `Medium`; a medium carries its history details itself and its
      number is its position, so no key is stored
    * Virtual tree: `sealed class VirtualNode` (name) with the plain classes `VirtualFolder`
      (children) and `VirtualEntry` (reference); the JSON field `type` (`folder` / `entry`) decides
      the class when reading; `ProjectFile.tree` is a `VirtualFolder`
* Jackson 3 (`tools.jackson.core:jackson-databind:3.3.0`); the model packages are opened to the
  module `tools.jackson.databind`, the annotations come from the module
  `com.fasterxml.jackson.annotation`
* UUIDs are stored as text (`String`), time stamps as `Instant?`
* `Medium.seed` is `Long?` (ComfyUI seeds are 64 bit); IP-02 must find a property form for it
* `Medium.parameters` is `Map<String, String>`: every value is stored as text, the parameter
  definition of the flow (FP-008) tells how it is read
* IP-02 mirrors the node hierarchy with a property model per class
* The pipeline needed no change: it calls Gradle tasks that cover every module

### IP-02: FX Property Models ✅ COMPLETED

**Objective**

Create `lib/fx-model` (`hocal-fx-model`) with the property model of every POJO of IP-01.

**Scope**

* Gradle module on `javafx.base` only
* One `<Pojo>Property` per POJO, nested change propagation, `refresh()`
* Mandatory tests per the `fx-model` skill
* NOT: usage in `app/ui`

**Affected Areas**

* new `lib/fx-model`, build files, pipeline

**Dependencies**

IP-01.

**Expected Result**

Every POJO has a tested property counterpart; a change deep in the tree reaches the root property.

**Technical Considerations**

* Executed with the `model-explore` and `model-creator` agents (agent rules)
* Recursive tree needs a multi-level propagation test
* UUID lists are registered as `list`

**As Built**

* Module `lib/fx-model` (`hocal-fx-model`, module `org.pcsoft.app.hocal.fx.model`) on `javafx.base`
  only; `BeanFields` and the test helper `ChangeRecorder` are part of it
* Property models in `org.pcsoft.app.hocal.fx.model.project`: `ProjectFileProperty` (root),
  `ProjectDataProperty`, `ReferenceProperty`, `MediumProperty`, `VirtualFolderProperty`,
  `VirtualEntryProperty`; the `of` factory wraps a picked list entry without copying it
* Lists (`references`, `media`, `children`, `tags`) are registered as plain lists; a change INSIDE a
  list entry is only picked up by `refresh()`
* `seed` (`Long?`), `created` / `modified` (`Instant?`) and `parameters` (`Map<String, String>`) are
  registered through `reference`
* `VirtualFolder` and `VirtualEntry` got hand-written `equals`, `hashCode` and `toString` by value
* The field properties are rebound through an `InvalidationListener`: a `ChangeListener` stays silent
  when the new object equals the old one (data classes) and the fields would stay tied to the old object
* `lib/model` exposes Jackson as `api`, because its module descriptor requires the Jackson annotations
* Known limitation: exchanging a nested model object against an equal one keeps the old instance in
  the parent POJO, as `BeanFields.write()` compares by `==`

### IP-03: Project ZIP Storage ✅ COMPLETED

**Objective**

Read and write the ZIP with `project.json` in its root.

**Scope**

* Serialize and deserialize `project.json`
* Open a ZIP, validate `project.json`, format version, UUIDs and entry links
* Save atomically through a temporary file
* Zip-slip and size protection while reading
* Typed errors for invalid, damaged and unsupported-version files
* Read and write the history details of the media inside `project.json`
* Validate that the keys of the history details match the media of the reference folder
* Store every ZIP entry uncompressed
* A stream-based entry point (input and output stream) so IP-06 can wrap it
* NOT: media numbering logic, tree editing, encryption

**Affected Areas**

* `lib/model` (storage package), build file (JSON dependency)

**Dependencies**

IP-01.

**Expected Result**

The file written is read back identically; broken files fail with a clear error.

**Technical Considerations**

* JSON is done with Jackson (decided)
* The ZIP itself uses the JDK API (`java.util.zip`) and needs no dependency
* Test cases: round trip, damaged ZIP, missing `project.json`, traversal entry, dangling entry link,
  reference without medium, interrupted save

**As Built**

* Package `org.pcsoft.app.hocal.model.storage` in `lib/model`; the object `ProjectStorage` is the entry
  point and holds no state
* Stream variants (`read`, `write`) never close the streams they are given, so IP-06 can wrap them;
  file variants save through a temporary file in the target folder and an atomic move
* Media are not held in memory: `MediaSink` receives them while reading, `MediaSource` delivers them
  while writing; the source is opened twice per medium (size and CRC first, content second)
* Medium number is the list index, the ZIP entry is `<reference uuid>/<NNN>.<extension>`; the extension
  comes from the source and is not part of the POJOs
* `ProjectValidator` checks the format version, the references (UUID form, no duplicates, 1 to 1000 media)
  and the entry links of the tree, for reading as well as for writing, so an invalid project is never written
* A newer format version is detected before the full parse, so unknown fields do not hide it
* `StorageLimits` bounds `project.json`, one medium and the number of entries
* Errors: sealed `ProjectFileException` with `InvalidProjectFileException`, `DamagedProjectFileException`
  and `UnsupportedFormatVersionException`; I/O errors while reading a stream are reported as damaged
* The module descriptor of `lib/model` now requires `tools.jackson.core` and `tools.jackson.databind`
* Tests include the deletion cases: after a medium is removed, numbers and media still belong together;
  the regression test reads a fixed ZIP from the test resources

### IP-04: Reference Media History ✅ COMPLETED

**Objective**

Implement the media history of a reference inside the ZIP.

**Scope**

* Folder per reference UUID
* File names `<NNN>.<extension>` with `NNN` from `000` to `999`
* The highest number is the counter and the current medium
* Read the current medium and the whole history
* Append a new medium as highest number + 1; an overflow beyond `999` is an error
* Remove a medium and renumber the following ones, so the numbering stays continuous
* The history details are part of the medium, so they follow it through append, remove and renumbering
* The original `000` cannot be removed; consequently the last remaining medium of a reference
  cannot be removed either, the whole reference has to be removed, which drops its UUID and its
  folder
* Every other medium, the current one included, can be removed; the one before it becomes current
* Validate a folder: at least one medium, names of the form `<NNN>.<extension>`, continuous numbers
  starting at `000`
* NOT: tree editing

**Affected Areas**

* `lib/model` (storage package)

**Dependencies**

IP-01, IP-03.

**Expected Result**

For every reference the current medium is the highest-numbered file, new media extend the history,
removing a medium leaves a continuous numbering, and a reference never ends up without a medium.

**Technical Considerations**

* No stored counter; always derived from the folder content
* The number is the part of the file name before the extension; the extension can change from
  medium to medium
* Appending, removing and renumbering mean rewriting the ZIP; this is done through the atomic save
  of IP-03
* Renumbering must never lose or overwrite a medium on the way; it is done on the complete set
  before it is written
* Removing the current medium makes its predecessor current; `000` stays the original at all times
* Test cases: append, overflow at `999`, remove in the middle, remove the current medium,
  remove `000` (rejected), folder with gap, folder without medium, mixed extensions

**As Built**

* The history is the `media` list of the `Reference` POJO (the index is the medium number), not a folder
  scanned in the ZIP; the content lives in a `MediaStore`, the place of the media while a project is open
* `MediaStore` extends `MediaSource` and `MediaSink` by `delete`, `move` and `numbers`;
  `DirectoryMediaStore` keeps the media as `<root>/<uuid>/<NNN>.<extension>`, writes through a temporary
  file and checks UUID, number and extension against path tricks
* Operations are extension functions of `Reference` in `MediaHistory.kt`: `currentMedium`, `currentNumber`,
  `appendMedium`, `removeMedium` and `verifyMedia`
* Appending puts the content into the store first and extends the list afterwards, so a failing store
  leaves the list untouched; a removal deletes the medium, moves the following ones down one by one
  and shortens the list
* Typed errors: `MediaHistoryFullException`, `OriginalMediumException`, `MediumNotFoundException`,
  `InconsistentHistoryException`
* No POJO field was added, so `lib/fx-model` is unchanged
* The same cases run against an in-memory and the directory store; one test saves the history as ZIP
  and reads it back with numbers and media still matching

### IP-05: Virtual Tree Operations ✅ COMPLETED

**Objective**

Provide consistent editing of the virtual directory structure.

**Scope**

* Create, rename, move and delete folders and entries
* Link an entry to a reference UUID; one UUID can be linked by several entries
* Unique sibling names, no cycle on move, valid root
* Handling of a reference no entry links to any longer
* NOT: media handling, UI

**Affected Areas**

* `lib/model` (tree operations)

**Dependencies**

IP-01.

**Expected Result**

Every edit keeps the structure valid; every invalid edit is rejected with a defined error.

**Technical Considerations**

* Name rules (empty, separators, case sensitivity) must be defined and documented
* Deleting an entry never deletes media; removing a reference is a separate, explicit action

**As Built**

* Package `org.pcsoft.app.hocal.model.tree` in `lib/model`; the operations are extension functions of
  `ProjectFile`: `parentOf`, `createFolder`, `createEntry`, `rename`, `move`, `orphansOf`, `delete` and
  `removeReference`; nodes are found by identity, not by value
* Name rules in `TreeNames`: not empty, no space at the start or end, at most 255 characters, no control
  character and none of `/ \ : * ? " < > |`, neither `.` nor `..`; siblings are unique without regard to
  upper and lower case, folders and entries share one name space
* The root folder always exists (`ProjectFile.tree`), has the empty name and cannot be renamed, moved or
  deleted; reading and writing a project file reject any other root name
* Orphaned references: when a deletion would take the last entry of a reference, `delete` changes nothing and
  returns `false`; only with `removeOrphans = true` are these references and their media removed.
  `orphansOf` tells beforehand which ones, so the UI can warn. This is normal control flow and no exception
* `removeReference` removes all entries that link a reference, the reference and its media
* The tree and the reference list change before the media are deleted; a failing store leaves a valid project
  and only unused media behind
* `MediaStore.deleteReference` is new; `DirectoryMediaStore` also removes the empty folder of the reference
* Typed errors: `InvalidNameException`, `DuplicateNameException`, `NodeNotFoundException`,
  `TreeCycleException`, `RootNodeException`, `UnknownReferenceException`

### IP-06: Project File Encryption ✅ COMPLETED

**Objective**

Make the project file optionally encrypted with AES through a cipher stream.

**Scope**

* Encrypted file header: magic, version, key derivation parameters, salt, IV
* Encrypt on save: the ZIP output of IP-03 passes through a `CipherOutputStream`
* Decrypt on read: the file passes through a `CipherInputStream` into the ZIP reader
* Detect an encrypted file by its header, an unencrypted one by the ZIP signature
* Key derivation from the password with PBKDF2-HMAC-SHA256, random salt, 600,000 iterations, 256 bit
  key; the parameters are read from the header, so the count can be raised later
* The writer always writes `project.json` as the first ZIP entry
* Typed errors: wrong password, manipulated file, unsupported encryption version
* Switching an existing project between encrypted and unencrypted when saving
* NOT: key storage, password prompts, UI

**Affected Areas**

* `lib/model` (storage and encryption package)

**Dependencies**

IP-03.

**Expected Result**

A project saved with a password is unreadable without it, reads back identically with it, and the
unencrypted form stays a standard ZIP.

**Technical Considerations**

* AES-256-GCM detects a wrong password and manipulation (decided)
* The JDK GCM implementation holds a whole decrypted stream in memory until its tag is verified, so the
  file is encrypted block by block (decided in IP-06), each block with its own tag
* Reading is stream-only (decided): `ZipInputStream` on a block-decrypting stream, no temporary file
* Stream-only reading is sequential: reading one medium or the project data scans the file up to
  that entry, and the result counts only after the whole stream was consumed and the tag verified
* A medium is therefore read fully into memory or into a consumer while scanning; very large media
  need a size limit (the user setting is planned in FP-009 IP-04 and IP-08), see open questions
* GCM limits the amount of data per key and IV; a fresh IV per save keeps this safe
* Appending a medium (IP-04) rewrites and re-encrypts the complete file
* Tests: round trip, wrong password, flipped byte, truncated file, header of unknown version,
  unencrypted file read without password, switching between both forms
* Passwords never appear in logs or error messages

**As Built**

* Block-wise AES-256-GCM instead of one `CipherOutputStream` and `CipherInputStream`: blocks of 64 KiB,
  each with its own 16 byte tag, so memory stays bounded and every block is verified before its data is used
* The block IV is the base IV from the header combined with an 8 byte block counter; the header and a
  last-block flag are authenticated data of every block, so cutting off, swapping and header changes fail
* Header of 78 bytes: magic `HOCALENC`, version, key derivation id, iteration count, salt, base IV,
  block size, password check value (HMAC-SHA256 of a fixed text under the derived key)
* A wrong password is recognised by the check value before any block is read; a failed block is
  `ManipulatedProjectFileException`, also for a cut-off file
* After the last ZIP entry the reader consumes the rest of the stream, so the last block is verified;
  the sink may already have received media of the valid part, the result is then to be discarded
* `ProjectStorage.read` and `write` got the optional last parameter `password`; without it a standard
  ZIP is written, an unencrypted file is read even when a password is passed
* New errors: `PasswordRequiredException`, `WrongPasswordException`, `ManipulatedProjectFileException`,
  `UnsupportedEncryptionVersionException`
* The regression test reads a fixed encrypted file produced by an independent script following the format

### IP-07: Project Manager

**Objective**

Organise the project through a singleton project manager that handles all changes.

**Scope**

* `ProjectManager` as a Kotlin `object` in `lib/fx-model`, on `javafx.base` only
* Exactly one project is loaded at a time, held as `ProjectProperty`
* Read-only properties for the loaded project, its path and its dirty state
* New, open (optional password), save, save as, close
* All changes (tree operations of IP-05, media operations of IP-04) go through the manager
* NOT: UI, file chooser, password prompt

**Affected Areas**

* `lib/fx-model` (manager package), tests

**Dependencies**

IP-02, IP-03, IP-04, IP-05, IP-06.

**Expected Result**

One place owns the loaded project; no other code writes to the storage or edits the tree directly.

**Technical Considerations**

* Opening or creating while a project is loaded requires closing first; with unsaved changes it
  fails unless discarding is allowed explicitly
* A change edits the POJO, then calls `refresh()` of the property model and marks the project dirty
* Changes only on the FX thread; file input and output block the caller
* Saving sets the time of the last change and the application version
* The singleton state must be resettable for tests

## 8. Dependency Graph

```text
IP-01 ✅
├── IP-02 ✅
├── IP-03 ✅
│   ├── IP-04 ✅
│   └── IP-06 ✅
└── IP-05 ✅
IP-07 follows after IP-02, IP-03, IP-04, IP-05 and IP-06 are completed.
```

Completed plans: IP-01, IP-02, IP-03, IP-04, IP-05, IP-06.

IP-02, IP-03 and IP-05 can run in parallel after IP-01; IP-04 and IP-06 in parallel after IP-03.

## 9. Risks and Open Questions

* Decided: JSON with Jackson; media with file extension; continuous numbering with renumbering
  after a removal in the middle; error on overflow beyond `999`; no reference without a medium;
  the original `000` cannot be removed, so the last medium cannot be removed either
* Decided in IP-05: a reference that loses its last entry is removed with its media only when the caller
  allows it explicitly; the caller can ask beforehand which references that would be
* Decided: empty folders are allowed; an entry always links a reference, so there is no entry
  without one
* Decided: a file with a newer format version is rejected
* Decided: project data object with name, description, project UUID, author, creation time, time of
  the last change, tags, format version and application version of the last save
* Decided: ZIP entries are never compressed; memory behaviour while rewriting the ZIP on append
  stays open
* Decided: JSON stays the format of the metadata, no embedded database; the data are small, the
  project is held in memory as one, and a database would need a real file next to the stream-only,
  encrypted ZIP. Revisit only for very many nodes, search over the whole project or partial loading
* Decided: AES-256-GCM, PBKDF2-HMAC-SHA256 with 600,000 iterations (confirmed, stored in the
  header, so it can be raised later), stream-only reading
* Decided: `ProjectManager` lives in `lib/fx-model`; opening or creating needs a closed project, and
  unsaved changes fail unless discarding is allowed explicitly
* Decided: unencrypted and encrypted files are read by the same stream-only reader (one code path)
* Settled in IP-06: block-wise GCM verifies every block before use, so a large medium no longer costs
  memory; the read limits of IP-03 (`StorageLimits`) stay in place
* Is the existence of `project.json` visible without the password? Planned: no, everything is
  encrypted except the header
* Lost password: no recovery is possible by design
* Use in `app/ui` (creating, opening, saving, password prompt) is deliberately NOT part of this
  feature
* Decided: the history details (prompt, model, flow; no additional images) are part of this
  feature and are done first, as FP-002 shows them
* Decided: the history details are part of the medium; `project.json` holds the media of a reference
  as a list, the medium number `000` to `999` is the position in that list
* Decided (FP-008): the history details also hold the used seed and the values of the `EDITABLE`
  parameters of the flow by parameter id; image inputs and constants are not stored; the type of
  the values (text, number, boolean) is settled with the `fx-model` skill in IP-01

## 10. Feature Completion Criteria

* `lib/model` and `lib/fx-model` exist, are built by `build` and covered by tests
* A saved unencrypted file is a standard ZIP with `project.json` in its root, holding the project
  data object, the references with their history details and the virtual tree
* Reading a saved file restores all three parts identically
* Every reference has a folder with media numbered from `000`; the highest number is current
* Appending a medium adds highest number + 1 and leaves all older media untouched
* Removing a medium from the middle leaves a continuous numbering; the original `000` cannot be
  removed; an overflow beyond `999` is an error
* One UUID linked by several entries is read and written correctly
* A file saved with a password cannot be read without it and reads back identically with it
* A wrong password and a manipulated encrypted file are rejected with a clear error
* A file without `project.json`, a damaged ZIP and a newer format version are rejected with a clear
  error; a failed save leaves the previous file intact
* Every POJO has a tested property model
* The history details of a medium (prompt, model, flow) are saved and read with the project and
  follow their medium when media are appended, removed or renumbered
* Only one project is loaded at a time, and every change goes through the project manager
* README, MkDocs, pipeline and structure documentation are up to date
