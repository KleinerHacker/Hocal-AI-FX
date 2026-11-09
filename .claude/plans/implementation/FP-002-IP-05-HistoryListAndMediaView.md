# FP-002-IP-05: Historienliste und Bildansicht

## Voraussetzungen

* IP-04, FP-001 IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `fx-component-lifecycle` und `ui-styling` laden
* [ ] Agent `component-explore`, danach Agent `component-creator` verwenden

## Aufgabe 2: Historienliste

* [ ] Komponente `HistoryList` im rechten Teil des Inhaltsbereichs anlegen
* [ ] Alle Medien der gewählten Datei von `000` bis zum letzten in Reihenfolge anzeigen
* [ ] Das letzte Medium bei jeder Dateiwahl automatisch vorauswählen, auch wenn die Historie wächst
* [ ] Icons der Einträge über `MediaPreview` erzeugen
* [ ] Keine Auswahl: leere Liste

## Aufgabe 3: Bildansicht

* [ ] Komponente `MediaView` in der Mitte, zeigt das gewählte Medium als Bild
* [ ] Bild an den verfügbaren Platz anpassen, lazy und außerhalb des FX-Threads laden
* [ ] Nicht darstellbares Medium zeigt Platzhalter statt Fehler

## Aufgabe 4: Tests und Doku

* [ ] Nachrichtenschlüssel ergänzen, Übersetzung über Agent `translator`
* [ ] Tests: Reihenfolge, Vorauswahl des letzten, Wachsen der Historie, Wechsel des Mediums
* [ ] `MainWindowIT` für den Inhaltsbereich, KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
