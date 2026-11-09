---
name: ui-styling
description: Rules for the Java FX UI module app/ui - MVVM FX architecture pattern, and I18N resource bundles. Load before creating or changing anything under app/ui, any FXML, view model or message bundle.
---

# Architecture Pattern

* Always use MVVM based on MVVM FX framework

# I18N

* ALWAYS use I18N for UI

## Languages

* The base bundle `app/ui/src/main/resources/messages/bundle.properties` is ENGLISH and is the single
  source of truth for all keys
* The following translations MUST be present, one file per language, next to the base bundle:
  * `bundle_de.properties` - German
* Adding, renaming or removing a key in the base bundle MUST be applied to EVERY translation in the
  same change
  * FORBIDDEN: a translation carrying a key the base bundle does not have
  * FORBIDDEN: a translation missing a key of the base bundle
* Every file MUST be UTF-8; `\uXXXX` escapes are FORBIDDEN
* MessageFormat placeholders (`{0}`, `{1}`, ...) MUST be kept identical to the base bundle, including
  the doubled apostrophes of `''{0}''`
* Adding another language MUST be confirmed with the user first
