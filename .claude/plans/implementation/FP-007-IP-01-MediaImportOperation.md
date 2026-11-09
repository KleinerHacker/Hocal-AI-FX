# FP-007-IP-01: Medien-Import-Operation

## Voraussetzungen

* FP-001 IP-04, FP-001 IP-05, FP-001 IP-07, FP-002 IP-03

## Aufgabe 1: Vorbereitung

* [ ] Skills `fx-model`, `fx-component-lifecycle` und `testing` laden
* [ ] Mit dem Benutzer klären: Größenlimit für sehr große Dateien

## Aufgabe 2: Import

* [ ] Import-Operation im `ProjectManager`: eine oder mehrere Dateien in einen Ordner
* [ ] Typprüfung über die Medientyp-Registry aus FP-002 IP-03
* [ ] Je Datei neue Referenz mit Medium `000` und Baumeintrag als eine Änderung
* [ ] Name nach dem Dateinamen, im Ordner eindeutig, bei Konflikt eine Nummer anhängen
* [ ] Datei vollständig lesen, bevor das Modell geändert wird, unlesbare Datei überspringen

## Aufgabe 3: Ergebnis und Hintergrund

* [ ] Typisiertes Ergebnis mit importierten und übersprungenen Dateien samt Grund
* [ ] Fehler bei einer Datei stoppt die anderen nicht
* [ ] Dateizugriff im Hintergrund über `javafx.concurrent`, Änderung am Modell auf dem FX-Thread
* [ ] Projekt wird `dirty`, Property-Modell per `refresh()` aktualisiert

## Aufgabe 4: Tests und Doku

* [ ] Tests: Erfolg, mehrere Dateien, nicht unterstützter Typ, Namenskonflikt, unlesbare Datei, Teilerfolg
* [ ] Testdateien in einem temporären Verzeichnis, KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
