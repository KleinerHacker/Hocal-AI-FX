---
name: Architecture
---

# Architecture

## Module Structure

* all Applications (end user apps) are stored in 'app'
  * Java FX UI application is stored in 'app/ui' (`hocal-ui`)
    * no other module SHOULD CONTAIN Java FX parts or frameworks
* Libraries are stored in 'lib'; none exists yet - a library is created when the first code needs
  to leave 'app/ui'
  * the plain model POJOs go into 'lib/model' (`hocal-model`), free of Java FX
  * their property models go into 'lib/fx-model' (`hocal-fx-model`), see the `fx-model` skill
  * the encryption (block stream cipher of the project file, key derivation, record cipher) goes into 'lib/crypto'
    (`hocal-crypto`), free of Java FX and of every other module; 'lib/model' uses it and maps its errors, but
    contains no cipher code itself
* A module under 'lib' MAY carry Java FX only in this one case; every other one stays free of it
  * a property model library on `javafx.base` - 'lib/fx-model'; from `javafx.graphics` it uses ONLY the
    package `javafx.application` (`Platform`, to recognize the FX thread); no other toolkit module and no
    other package
* Operations on the project (project storage, media store, media history, tree operations) live in
  `...internal` packages of 'lib/model'
  * the module descriptor exports them to `hocal-fx-model` ONLY (qualified export), so the application
    reaches them through the `ProjectManager` and nowhere else
  * plain objects, name rules and errors stay in public packages the user interface may use
  * a new package of 'lib/model' is NOT exported until it is decided whether it is public or protected
* The full Java FX toolkit lives ONLY in 'app/ui'; the one package of `javafx.graphics` that 'lib/fx-model' may
  use is named above and nothing else of it
* The application drives ComfyUI as a separate process through its HTTP and WebSocket API; no module
  embeds or links ComfyUI itself
