# FP-009-IP-01: Einstellungsmodell und Speicher

## Voraussetzungen

* FP-001 IP-01, FP-001 IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `fx-model`, `testing`, `project-docs` und `ci-pipeline` laden
* [ ] Agent `model-explore` ausführen, danach `model-creator` für POJOs und Property-Modelle verwenden

## Aufgabe 2: Modell

* [ ] Paket `settings` in `lib/model`: `AppSettings` mit `schemaVersion` und Kategorien `WindowSettings`, `GeneralSettings`
* [ ] `ViewSettings`, `ModelSettings`, `ComfySettings`, `GenerationSettings` als verschachtelte POJOs anlegen
* [ ] Property-Modelle je POJO in `lib/fx-model`, gespiegeltes Paket
* [ ] Vorgaben an einer Stelle definieren, auch für „Standard wiederherstellen“
* [ ] Wertebereiche der Grenzen im Modell prüfen: Bildgröße, Projektgröße in Bytes, Warteschlange

## Aufgabe 3: Speicher

* [ ] `SettingsStore`: Konfigurationsordner je Betriebssystem, Unterordner mit Anwendungsnamen, getrennt vom Datenverzeichnis
* [ ] JSON mit Jackson lesen und schreiben, unbekannte Felder und Kategorien beim Schreiben erhalten
* [ ] Atomar schreiben über temporäre Datei und Ersetzen
* [ ] Fehlende oder kaputte Datei ergibt Vorgaben ohne Absturz, kaputte Datei wird gesichert

## Aufgabe 4: Verwalter

* [ ] `SettingsManager` als Singleton in `lib/fx-model` nach dem Muster von `ProjectManager`
* [ ] `load()` beim Start vor dem Hauptfenster, `save()` verzögert und gebündelt, nie auf dem UI-Thread blockierend
* [ ] `resetCategory(...)` setzt eine Kategorie auf die Vorgaben

## Aufgabe 5: Tests und Doku

* [ ] Tests: Round-Trip, fehlende Datei, kaputte Datei, unbekanntes Feld bleibt, atomares Schreiben, Wertebereiche
* [ ] Singleton-Zustand pro Test zurücksetzen
* [ ] README-Strukturabschnitt und Pipeline prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
