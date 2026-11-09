# FP-005-IP-01: Runtime-Bibliotheks-Grundlage

## Voraussetzungen

* FP-004 IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `ci-pipeline`, `project-docs` und `testing` laden
* [ ] Installationsort mit dem Benutzer klären: Standard je Betriebssystem, vom Benutzer änderbar oder nicht

## Aufgabe 2: Modul

* [ ] Modul `lib/comfy-runtime` (`hocal-comfy-runtime`) in `settings.gradle.kts` eintragen
* [ ] `build.gradle.kts` ohne JavaFX anlegen, Abhängigkeit auf `hocal-comfy`
* [ ] `module-info.java` anlegen, Pakete exportieren

## Aufgabe 3: Verzeichnisse

* [ ] Datenverzeichnis je Betriebssystem bestimmen, außerhalb der Programminstallation
* [ ] Unterverzeichnisse Runtime, Python, Umgebung, Logs sowie Manifest und PID-Datei festlegen
* [ ] Pfade mit Leerzeichen und Nicht-ASCII-Zeichen unterstützen, besonders unter Windows

## Aufgabe 4: Plattform und Zustand

* [ ] Betriebssystem, Architektur und GPU-Hinweis erkennen
* [ ] `RuntimeState`: nicht installiert, installiert sich, gestoppt, startet, läuft, fehlgeschlagen
* [ ] Zustandshalter mit Listener-Anmeldung, ohne JavaFX

## Aufgabe 5: Tests und Doku

* [ ] Tests für Pfade, Plattformerkennung und Zustandsübergänge mit KDoc-Anwendungsfall
* [ ] CI-Pipeline und README-Strukturabschnitt prüfen und anpassen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
