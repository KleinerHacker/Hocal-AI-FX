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
    * the references - a list of UUIDs, nothing else
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
    * `ProjectFile` (name open) - root POJO mirroring `project.json`: project data, reference
      UUIDs, tree
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
  "references": [ "<uuid>", "<uuid>", ... ],
  "tree":       folders and entries; an entry holds one "<uuid>"
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
| IP-01 | Model Library And POJOs  | Create `lib/model` with project data, reference list and tree POJOs     | -            |
| IP-02 | FX Property Models       | Create `lib/fx-model` mirroring all POJOs                              | IP-01        |
| IP-03 | Project ZIP Storage      | Read and write ZIP and `project.json`, validation, atomic save          | IP-01        |
| IP-04 | Reference Media History  | Reference folders, continuous numbering 000-999, current medium, add and remove media | IP-01, IP-03 |
| IP-05 | Virtual Tree Operations  | Edit the virtual directory structure consistently with the references   | IP-01        |
| IP-06 | Project File Encryption  | Optional AES encryption of the project file through cipher streams      | IP-03        |

## 7. Implementation Plans

### IP-01: Model Library And POJOs

**Objective**

Create `lib/model` (`hocal-model`) and the POJOs of `project.json`.

**Scope**

* Gradle module registered in `settings.gradle.kts`, set up like `app/ui` (Kotlin, KDoc, licence
  check, Kover, tests)
* POJOs for the project data object, the root, the virtual tree (folder, entry)
* The reference list is a list of UUIDs; no own reference POJO
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

### IP-02: FX Property Models

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

### IP-03: Project ZIP Storage

**Objective**

Read and write the ZIP with `project.json` in its root.

**Scope**

* Serialize and deserialize `project.json`
* Open a ZIP, validate `project.json`, format version, UUIDs and entry links
* Save atomically through a temporary file
* Zip-slip and size protection while reading
* Typed errors for invalid, damaged and unsupported-version files
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

### IP-04: Reference Media History

**Objective**

Implement the media history of a reference inside the ZIP.

**Scope**

* Folder per reference UUID
* File names `<NNN>.<extension>` with `NNN` from `000` to `999`
* The highest number is the counter and the current medium
* Read the current medium and the whole history
* Append a new medium as highest number + 1; an overflow beyond `999` is an error
* Remove a medium and renumber the following ones, so the numbering stays continuous
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

### IP-05: Virtual Tree Operations

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

### IP-06: Project File Encryption

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
* `CipherInputStream` with GCM delivers the plaintext before the tag is verified and reports a bad
  tag only at the end of the stream; the reader therefore must not trust a result before the stream
  is fully consumed
* Reading is stream-only (decided): `ZipInputStream` on the `CipherInputStream`, no temporary file
* Stream-only reading is sequential: reading one medium or the project data scans the file up to
  that entry, and the result counts only after the whole stream was consumed and the tag verified
* A medium is therefore read fully into memory or into a consumer while scanning; very large media
  need a size limit, see open questions
* GCM limits the amount of data per key and IV; a fresh IV per save keeps this safe
* Appending a medium (IP-04) rewrites and re-encrypts the complete file
* Tests: round trip, wrong password, flipped byte, truncated file, header of unknown version,
  unencrypted file read without password, switching between both forms
* Passwords never appear in logs or error messages

## 8. Dependency Graph

```text
IP-01
├── IP-02
├── IP-03
│   ├── IP-04
│   └── IP-06
└── IP-05
```

IP-02, IP-03 and IP-05 can run in parallel after IP-01; IP-04 and IP-06 in parallel after IP-03.

## 9. Risks and Open Questions

* Decided: JSON with Jackson; media with file extension; continuous numbering with renumbering
  after a removal in the middle; error on overflow beyond `999`; no reference without a medium;
  the original `000` cannot be removed, so the last medium cannot be removed either
* Reference no entry links to: keep or remove on save?
* Decided: empty folders are allowed; an entry always links a reference, so there is no entry
  without one
* Format version policy: reject newer versions strictly or read tolerantly?
* Decided: project data object with name, description, project UUID, author, creation time, time of
  the last change, tags, format version and application version of the last save
* Large media: compression level and memory behaviour while rewriting the ZIP on append
* Decided: AES-256-GCM, PBKDF2-HMAC-SHA256 with 600,000 iterations (my proposal for the iteration
  count, stored in the header, can be changed), stream-only reading
* Decided: unencrypted and encrypted files are read by the same stream-only reader (one code path)
* Not blocking, to be settled in IP-04 / IP-06: a medium read through the stream is held back until
  the GCM tag of the whole file is verified, so a very large medium costs memory; whether a size
  limit is needed
* Is the existence of `project.json` visible without the password? Planned: no, everything is
  encrypted except the header
* Lost password: no recovery is possible by design
* Use in `app/ui` (creating, opening, saving, password prompt) is deliberately NOT part of this
  feature

## 10. Feature Completion Criteria

* `lib/model` and `lib/fx-model` exist, are built by `build` and covered by tests
* A saved unencrypted file is a standard ZIP with `project.json` in its root, holding the project
  data object, the UUID list and the virtual tree
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
* README, MkDocs, pipeline and structure documentation are up to date
