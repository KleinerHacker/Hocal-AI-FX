# FP-008-IP-03: Dateilisten-Menü

## Voraussetzungen

* IP-02, FP-002 IP-03

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `dialog`, `ui-styling`, `fx-component-lifecycle` und `translation` laden
* [ ] Mit dem Benutzer klären: Referenz ohne Eintrag nach dem Löschen behalten oder entfernen

## Aufgabe 2: Menü

* [ ] Kontextmenü in `FileList`: Dateien importieren, Datei löschen, Datei exportieren
* [ ] Import auch bei leerer Liste verfügbar
* [ ] Rechtsklick wählt zuerst die Zeile unter dem Mauszeiger aus
* [ ] Keine Icons in den Menüeinträgen

## Aufgabe 3: Aktionen

* [ ] Import in den angezeigten Ordner mit den Dialogen aus IP-02
* [ ] Löschbestätigung nennt die Datei und die Folge für die Referenz
* [ ] Eintrag über den `ProjectManager` entfernen, Medien bleiben nach den Regeln aus FP-001 erhalten
* [ ] Auswahl wechselt auf einen Nachbarn in der Liste
* [ ] Datei exportieren: nur das aktuelle Medium der Referenz, Zieldatei wählen, Überschreiben bestätigt der FileChooser
* [ ] Einfachauswahl in der Liste, keine Mehrfachauswahl

## Aufgabe 4: Texte, Tests und Doku

* [ ] Nachrichtenschlüssel anlegen, Übersetzung nur über den Agenten `translator`
* [ ] Tests: Menü auf Zeile und auf leerer Liste, Bestätigung, Auswahl nach Löschen, Import
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
