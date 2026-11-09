---
name: translation
description: Rules for translating the UI message bundles of app/ui. Load whenever a message bundle key is added, renamed, removed or its English text changes, and before any translation content is written or reviewed.
---

# Translation

* The structural rules for the bundles (file names, UTF-8, placeholder identity, when a new
  language needs the user's confirmation) live in the `ui-styling` skill and stay binding here as
  well - this skill does not repeat them, only adds to them
* The `translator` agent MUST be used to carry out a translation; translating a key inline in the
  main session is FORBIDDEN
  * Applies whenever, for example `bundle_de.properties` (or a future further-language bundle) needs a new key,
    an existing key's text changed because the English source changed, or a translation is
    reviewed for correctness
  * The main session prepares the English base bundle key(s) itself (adding, renaming, or changing
    the English text is ordinary `app/ui` work under the `ui-styling` skill), then hands the exact
    set of changed keys to the `translator` agent to translate into every other bundle
* The `translator` agent MUST be handed, for each key: the key name, the exact English text
  including any `MessageFormat` placeholders, and short context (which screen, dialog or control
  the text appears on) - a translation decided without context is not acceptable

## Language quality

* Terminology MUST stay consistent across the whole German bundle: the same domain word (e.g.
  "Kapitel", "Klappentext", "Entwurf") is translated the same way everywhere it appears
* Tone MUST match the existing bundle: direct, factual, no marketing language, addressing the user
  formally ("Sie") unless the existing bundle already established otherwise for that area
* A `MessageFormat` placeholder (`{0}`, `{1}`, ...) MUST appear in the translated text in the same
  logical position it carries the same meaning in English; its doubled apostrophes (`''{0}''`)
  MUST be kept doubled
* A translation MUST fit the control it appears on at a glance - a button label or a menu item
  stays short; a message or a tooltip may be a full sentence
* When the correct translation is ambiguous or depends on a product decision (a term with no
  established German equivalent yet, for instance), the `translator` agent asks the main session
  instead of guessing, and the main session asks the user

## Verification

* After a translation is written, `bundle.properties` and every translation file MUST carry the
  exact same set of keys - a missing or a surplus key is a build-breaking condition per
  `ui-styling`
* The `build` Gradle target MUST be run afterwards (through an agent, per the global concurrency
  rule) to confirm nothing regressed
