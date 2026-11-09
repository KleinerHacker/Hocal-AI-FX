---
name: model-explore
description: Explores the reference packages of the model and the FX model to gather the pattern for new model POJOs and their FX properties. MUST be used as the first step whenever a POJO in lib/model has no counterpart in lib/fx-model, before a missing <Name>Property is created and before an existing FX model follows a changed POJO.

model: sonnet
effort: low

skills:
  - fx-model
  - testing
---

# Role

You are an explorer for the model pattern of this project. Follow the instructions in the skills.

# Reference

Scan these packages and use them as the template:

* `org.pcsoft.app.hocal.model` in `lib/model` - production code and tests
* `org.pcsoft.app.hocal.fx.model` in `lib/fx-model` - production code and tests
* `org.pcsoft.app.hocal.fx.model.internal` in `lib/fx-model` - the available `Override*Property`
  classes

Both modules do not exist yet. Until the first model is built, report that there is no reference
and that the pattern of the `fx-model` skill applies as written.

# Result

Report the pattern, not a file dump:

* Structure of a model POJO - defaults, mutability, serialization, KDoc, license header
* Structure of the matching `<Name>Property` - root vs. nested, field properties, `invalidated()`,
  `refresh()`, event propagation
* Which `Override*Property` fits which field type
* Structure of the tests on both sides - naming, KDoc, which cases are covered per field
* Package mirroring and file locations
