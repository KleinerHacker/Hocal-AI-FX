# FP-008-IP-02: Verzeichnisbaum-Menü

## Voraussetzungen

* IP-01, FP-002 IP-02, FP-003 IP-04

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `dialog`, `ui-styling`, `fx-component-lifecycle` und `translation` laden
* [ ] Agenten `component-explore` und `dialog-explore` verwenden
* [ ] Mit dem Benutzer klären: Umbenennen, Entf-Taste und Tastenkürzel

## Aufgabe 2: Menü

* [ ] Kontextmenü in `ProjectTree`: Neuer Ordner, Ordner löschen, Dateien importieren, Ordner exportieren
* [ ] Rechtsklick wählt zuerst das Element unter dem Mauszeiger aus
* [ ] Wurzelordner hat ein reduziertes Menü ohne Löschen
* [ ] Keine Icons in den Menüeinträgen

## Aufgabe 3: Dialoge

* [ ] Namensdialog mit Prüfung nach den Namensregeln der Baum-Operation
* [ ] Löschbestätigung mit Anzahl der Unterordner und Einträge
* [ ] Import- und Export-Zusammenfassung mit übersprungenen Objekten und Grund
* [ ] Konfliktdialog beim Ordnerexport: überschreiben, überspringen, abbrechen
* [ ] Dialoge so anlegen, dass Dateiliste und Historie sie wiederverwenden

## Aufgabe 4: Aktionen

* [ ] Neuer Ordner legt den Ordner unter dem gewählten an und wählt ihn aus
* [ ] Ordner löschen über den `ProjectManager`, Auswahl wechselt auf den Elternordner
* [ ] Import: Dateiauswahl mit Mehrfachauswahl und Filter aus der Registry, Fortschrittsdialog bei großen Mengen
* [ ] Export: Zielverzeichnis wählen, Fortschrittsdialog, bei vorhandener Datei je Konflikt nachfragen

## Aufgabe 5: Texte, Tests und Doku

* [ ] Nachrichtenschlüssel anlegen, Übersetzung nur über den Agenten `translator`
* [ ] Tests: Menüeinträge, Wurzel ohne Löschen, Bestätigung, Auswahl nach Anlegen und Löschen, Import
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
