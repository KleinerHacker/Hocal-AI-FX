# FP-010-IP-04: Kategorie Allgemein

## Voraussetzungen

* IP-03, FP-003 IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `ui-styling`, `translation` und `testing` laden

## Aufgabe 2: Seite

* [ ] Maximale Bildgröße: Breite und Höhe in Pixel, Vorgabe 4096 × 4096
* [ ] Maximale Projektdateigröße in GB (1 GB = 1.000.000.000 Bytes), Vorgabe 2 GB, Bereich 50 MB bis 16 GB
* [ ] Rückfragen vor dem Löschen von Medium, Ordner und Referenz, Vorgabe an
* [ ] Anzahl zuletzt geöffneter Projekte, Vorgabe 10, 0 schaltet die Liste ab
* [ ] Vorschau-Threads: Anzahl der Threads, die Vorschaubilder gleichzeitig laden, Vorgabe 5, Bereich 1 bis 16
* [ ] Wertebereiche mit Meldung am Feld anzeigen

## Aufgabe 3: Zuletzt geöffnet

* [ ] Liste der Pfade in den Einstellungen führen, neueste zuerst
* [ ] Datei-Menü zeigt die Einträge und „Liste leeren“, Aufruf über den Öffnen-Ablauf aus FP-003
* [ ] Fehlende Datei melden und aus der Liste entfernen, verschlüsselte Projekte fragen wie immer nach dem Passwort

## Aufgabe 4: Tests und Doku

* [ ] Tests für Seite, View-Modell und Liste
* [ ] Lösch-Rückfrage wird von den Abläufen aus FP-008 beim Gebrauch gelesen
* [ ] Vorschau-Threads setzen `ProjectFileListViewModel.loadThreads` (FP-002), wirken für danach gestartete Ladevorgänge; Test dazu

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
