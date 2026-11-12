# FP-005-IP-04: ComfyUI-Runtime-Installation

## Voraussetzungen

* IP-03

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing` und `project-docs` laden
* [ ] ComfyUI-Version festlegen und mit dem Benutzer klären, Prüfsumme hinterlegen

## Aufgabe 2: ComfyUI

* [ ] Festgelegtes Release-Archiv von GitHub herunterladen, prüfen und entpacken
* [ ] Abhängigkeiten in die virtuelle Umgebung installieren
* [ ] Lizenzhinweis GPL-3.0 beachten: nur herunterladen, nicht ausliefern

## Aufgabe 3: GPU-Backend

* [ ] PyTorch-Backend wählen: CUDA, ROCm unter Linux, MPS, sonst CPU
* [ ] Gewähltes Backend und Downloadgröße für den Dialog bereitstellen
* [ ] Wahl durch den Benutzer änderbar machen

## Aufgabe 4: Prüfung

* [ ] Verifikationslauf: Import prüfen und Version abfragen
* [ ] Manifest mit Versionen und Prüfsummen schreiben
* [ ] Intakte Installation beim zweiten Lauf erkennen und nichts tun
* [ ] Defekte Installation reparieren, bei Abbruch nichts Halbfertiges lassen

## Aufgabe 5: Tests und Doku

* [ ] Tests mit Attrappen für Download und Prozess: Erfolg, Fehler, Abbruch, Reparatur
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
