# FP-008: Übersicht

Feature-Plan: `.claude/plans/features/FP-008-AiModelCatalog.md`

## Teilpläne

* IP-01 Bibliotheks-Grundlage und Format: `FP-008-IP-01-LibraryFoundationAndFormat.md`
* IP-02 Katalog-Lader und Validierung: `FP-008-IP-02-CatalogLoaderAndValidation.md`
* IP-03 Demonstrationsmodell: `FP-008-IP-03-DemonstrationModelEntry.md`
* IP-04 Katalog-Anbindung in der Anwendung: `FP-008-IP-04-CatalogBindingInTheApplication.md`
* IP-05 Modell-Download und Entfernen: `FP-008-IP-05-ModelDownloadAndRemoval.md`
* IP-06 Modell-Verwaltungsliste: `FP-008-IP-06-ModelManagementList.md`

## Reihenfolge

* IP-01: Voraussetzung FP-001 IP-01
* IP-02: Voraussetzung IP-01
* IP-03: Voraussetzung IP-02; parallel zu IP-04 und IP-05 möglich
* IP-04: Voraussetzung IP-02, FP-002 IP-06, FP-006 IP-01; parallel zu IP-03 und IP-05 möglich
* IP-05: Voraussetzung IP-02, FP-005 IP-01, IP-02, IP-04; parallel zu IP-03 und IP-04 möglich
* IP-06: Voraussetzung IP-04, IP-05, FP-003 IP-02, IP-04
