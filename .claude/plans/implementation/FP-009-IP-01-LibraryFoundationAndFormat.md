# FP-009-IP-01: Bibliotheks-Grundlage und Format

## Voraussetzungen

* FP-001 IP-01 (Jackson-Abhängigkeit wird geteilt, wer zuerst kommt, ergänzt sie)

## Aufgabe 1: Vorbereitung

* [ ] Skills `ci-pipeline`, `project-docs` und `testing` laden

## Aufgabe 2: Modul

* [ ] Modul `lib/ai-model` (`hocal-ai-model`) in `settings.gradle.kts` eintragen
* [ ] `build.gradle.kts` ohne JavaFX und ohne Abhängigkeit auf `lib/comfy` anlegen
* [ ] Jackson-Abhängigkeit ergänzen, `module-info.java` anlegen
* [ ] Pakete `org.pcsoft.app.hocal.aimodel` exportieren

## Aufgabe 3: Datenklassen

* [ ] Unveränderliche Klassen `AiModel`, `HuggingFaceRef`, `ModelFile` (Id, Pfad, Zielordner, SHA-256, Größe) und `LicenseRef`
* [ ] `FlowEntry` mit Id, Name, Standardfeldern und Parameterliste
* [ ] `StandardFields`: Pflicht `prompt`, `model`, `seed`; optional `negativePrompt`, `inputImage`, `extraImages`
* [ ] `Parameter` mit Id, Name, Typ, Vorgabe, Grenzen, Werteliste, `mode` und Ziel
* [ ] Enums `ParameterType` (`text`, `integer`, `number`, `enum`, `boolean`) und `ParameterMode` (`EDITABLE`, `READ_ONLY`, `CONSTANT`)
* [ ] `FlowTemplate` hält den Workflow als undurchsichtigen JSON-Baum

## Aufgabe 4: Format

* [ ] JSON-Format mit `schemaVersion` festlegen, Ziel als `"<Knoten-Id>.<Eingabename>"` oder Liste davon
* [ ] Endgültige Feldnamen festlegen, Format als KDoc und Seite unter `docs` beschreiben

## Aufgabe 5: Regeln und Tests

* [ ] `.claude/rules/architecture.md`, README-Strukturabschnitt und Modulliste der CI-Pipeline anpassen
* [ ] Tests der Datenklassen mit KDoc-Anwendungsfall

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
