# FP-009-IP-07: Kategorien ComfyUI und Erzeugung

## Voraussetzungen

* IP-03, FP-005 IP-07, FP-006 IP-02, FP-006 IP-04

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `ui-styling`, `translation` und `testing` laden

## Aufgabe 2: Seite ComfyUI

* [ ] Neustart des Dienstes, die Steuerung aus FP-005 IP-07 zieht hierher, Rückfrage bei laufenden Aufträgen bleibt
* [ ] Startmodus: beim ersten Gebrauch (Vorgabe) oder beim Programmstart, Wirkung nach Neustart mit Hinweis
* [ ] Nur lesend: ComfyUI-Version, Python-Umgebung, Log- und Datenverzeichnis; Knopf öffnet das Log-Verzeichnis
* [ ] Backend-Wahl CUDA, ROCm, MPS, CPU mit „Neu installieren“, Port bleibt automatisch

## Aufgabe 3: Seite Erzeugung

* [ ] Schließzeit der fertigen Kachel in Sekunden, 0 bleibt bis zum Schließen
* [ ] Höchstzahl wartender Aufträge, Vorgabe 10
* [ ] Vorgabe für den Seed: bei jedem Senden zufällig oder fest
* [ ] Live-Vorschau an oder aus, schaltet die Vorschau-Methode von ComfyUI

## Aufgabe 4: Anbindung und Tests

* [ ] Jede Einstellung wird beim Gebrauch vom Verbraucher gelesen, nicht nur beim Start
* [ ] Eine Einstellung wird nur angeboten, wenn ihr Verbraucher existiert
* [ ] Tests der Seiten

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
