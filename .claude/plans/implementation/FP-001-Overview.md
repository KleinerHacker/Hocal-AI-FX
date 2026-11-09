# FP-001: Übersicht

Feature-Plan: `.claude/plans/features/FP-001-ProjectFileStructure.md`

## Teilpläne

* IP-01 Modell-Bibliothek und POJOs: `FP-001-IP-01-ModelLibraryAndPojos.md`
* IP-02 FX-Property-Modelle: `FP-001-IP-02-FxPropertyModels.md`
* IP-03 ZIP-Storage: `FP-001-IP-03-ProjectZipStorage.md`
* IP-04 Medien-Historie: `FP-001-IP-04-ReferenceMediaHistory.md`
* IP-05 Operationen auf dem virtuellen Baum: `FP-001-IP-05-VirtualTreeOperations.md`
* IP-06 Verschlüsselung: `FP-001-IP-06-ProjectFileEncryption.md`
* IP-07 Projekt-Manager (Singleton): `FP-001-IP-07-ProjectManager.md`

## Reihenfolge

* IP-01: Voraussetzung keine
* IP-02: Voraussetzung IP-01
* IP-03: Voraussetzung IP-01
* IP-04: Voraussetzung IP-01, IP-03
* IP-05: Voraussetzung IP-01
* IP-06: Voraussetzung IP-03
* IP-07: Voraussetzung IP-02, IP-03, IP-04, IP-05, IP-06
