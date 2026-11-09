# FP-005-IP-03: Python-Umgebung

## Voraussetzungen

* IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing` und `project-docs` laden
* [ ] Unterstützten Python-Bereich aus der festgelegten ComfyUI-Version bestimmen und mit dem Benutzer klären

## Aufgabe 2: uv

* [ ] Festgelegte `uv`-Version herunterladen, prüfen und entpacken
* [ ] `uv` installiert Python in der geforderten Version in das Datenverzeichnis
* [ ] Kein System-Python verwenden, kein Eingriff in `PATH` oder Registry

## Aufgabe 3: Virtuelle Umgebung

* [ ] Virtuelle Umgebung für ComfyUI mit `uv` anlegen
* [ ] Version und Funktion der Umgebung prüfen
* [ ] Vorhandene intakte Umgebung erkennen und nicht neu anlegen

## Aufgabe 4: Fehler

* [ ] Typisierte Fehler: Provisionierung fehlgeschlagen, keine Verbindung, nicht unterstützte Version
* [ ] Prozessaufrufe ohne Shell, Ausgabe vollständig lesen

## Aufgabe 5: Tests und Doku

* [ ] Tests mit Attrappen für Prozess und Netzwerk, kein echter Download
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
