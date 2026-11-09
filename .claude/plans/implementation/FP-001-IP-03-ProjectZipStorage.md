# FP-001-IP-03: ZIP-Storage

## Voraussetzungen

* IP-01

## Aufgabe 1: Lesen

* [ ] Storage-Paket in `lib/model` anlegen, ohne Zustand
* [ ] Jackson-Mapping für `project.json`, Zeiten in UTC im Format ISO-8601
* [ ] Lesen als Stream über `ZipInputStream`, `project.json` muss der erste Eintrag sein
* [ ] Validierung von Version, UUID-Liste und Eintragsverweisen
* [ ] Validierung: jeder Referenzordner enthält mindestens ein Medium
* [ ] Zip-Slip- und Größenschutz beim Lesen
* [ ] Typisierte Fehler für ungültig, beschädigt und unbekannte Version

## Aufgabe 2: Schreiben

* [ ] Schreiben mit `project.json` als erstem ZIP-Eintrag
* [ ] Atomares Speichern über temporäre Datei und anschließendes Ersetzen
* [ ] Stream-Einstiegspunkte (Ein- und Ausgabestrom), damit IP-06 sie umschließen kann

## Aufgabe 3: Tests und Doku

* [ ] Tests: Round-Trip, beschädigtes ZIP, fehlende `project.json`, Traversal-Eintrag
* [ ] Tests: hängender Eintragsverweis, Referenz ohne Medium, abgebrochenes Speichern
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
