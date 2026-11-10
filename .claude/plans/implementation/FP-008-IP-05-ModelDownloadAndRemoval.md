# FP-008-IP-05: Modell-Download und Entfernen

## Voraussetzungen

* IP-02, FP-005 IP-01, FP-005 IP-02, FP-005 IP-04

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing` und `project-docs` laden
* [ ] Prüfen, ob ComfyUI neue Dateien ohne Neustart sieht

## Aufgabe 2: Installer

* [ ] `ModelInstaller` in `lib/comfy-runtime`, Paket `models`
* [ ] Zielpfad je Datei aus Zielordner und Katalog bestimmen
* [ ] Installiert-Prüfung: alle Dateien vorhanden mit richtiger Größe, ohne laufenden Dienst
* [ ] Halb installiertes Modell zählt als nicht installiert

## Aufgabe 3: Download

* [ ] Dateien von der festgeschriebenen Revision laden, mit Fortschritt, Abbruch und Fortsetzen
* [ ] Teildateien im temporären Bereich des Datenverzeichnisses, nie in den Modellordnern
* [ ] Größe und SHA-256 vor dem Verschieben prüfen, freien Speicher vorab prüfen
* [ ] Mehrere Modelle gleichzeitig, ein Download je Modell, ohne Begrenzung
* [ ] Dasselbe Modell kann nicht zweimal starten

## Aufgabe 4: Entfernen und Fehler

* [ ] Entfernen eines installierten Modells löscht alle seine Dateien
* [ ] Typisierte Fehler: nicht erreichbar, nicht gefunden, gesperrt, Prüfsumme falsch, Speicher voll

## Aufgabe 5: Tests und Doku

* [ ] Tests mit WireMock: Erfolg, Abbruch, Fortsetzen, falsche Prüfsumme, parallele Downloads, Entfernen
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
