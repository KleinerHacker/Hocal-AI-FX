# FP-009: Übersicht

Feature-Plan: `.claude/plans/features/FP-009-ApplicationSettings.md`

## Teilpläne

* IP-01 Einstellungsmodell und Speicher: `FP-009-IP-01-SettingsModelAndStore.md`
* IP-02 Fensterzustand: `FP-009-IP-02-WindowStatePersistence.md`
* IP-03 Rahmen des Einstellungsdialogs: `FP-009-IP-03-SettingsDialogFrame.md`
* IP-04 Kategorie Allgemein: `FP-009-IP-04-GeneralCategory.md`
* IP-05 Kategorie Ansicht: `FP-009-IP-05-ViewCategory.md`
* IP-06 Kategorie Modelle: `FP-009-IP-06-ModelsCategory.md`
* IP-07 Kategorien ComfyUI und Erzeugung: `FP-009-IP-07-ComfyUiAndGenerationCategories.md`
* IP-08 Durchsetzung der Grenzen: `FP-009-IP-08-LimitEnforcement.md`
* IP-09 Hilfe-Menü und Lizenzdialog: `FP-009-IP-09-HelpMenuAndLicenceDialog.md`
* IP-10 Theme und Erscheinungsbild: `FP-009-IP-10-ThemeAndAppearance.md`
* IP-11 Sprachunterstützung: `FP-009-IP-11-LanguageSupport.md`
* IP-12 Übersetzungen: `FP-009-IP-12-Translations.md`

## Reihenfolge

* IP-01: Voraussetzung FP-001 IP-01, IP-02
* IP-02: Voraussetzung IP-01, FP-002 IP-01; parallel zu IP-03 möglich
* IP-03: Voraussetzung IP-01, FP-003 IP-02; parallel zu IP-02 möglich
* IP-10: Voraussetzung FP-002 IP-01; kann früh beginnen
* IP-11: Voraussetzung IP-01; kann früh beginnen
* IP-04: Voraussetzung IP-03, FP-003 IP-02
* IP-05: Voraussetzung IP-03, IP-10, IP-11
* IP-06: Voraussetzung IP-03, FP-008 IP-05, IP-06
* IP-07: Voraussetzung IP-03, FP-005 IP-07, FP-006 IP-02, IP-04
* IP-08: Voraussetzung IP-04, FP-001 IP-07, FP-007 IP-01, FP-006 IP-03
* IP-09: Voraussetzung FP-002 IP-01, FP-008 IP-02; unabhängig von den anderen
* IP-12: Voraussetzung IP-11; je Welle einzeln
