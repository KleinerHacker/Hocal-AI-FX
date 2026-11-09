# FP-007-IP-04: Historien-Menü

## Voraussetzungen

* IP-02, FP-002 IP-05, FP-001 IP-04

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `dialog`, `ui-styling`, `fx-component-lifecycle` und `translation` laden

## Aufgabe 2: Menü

* [ ] Kontextmenü in `HistoryList` mit drei Einträgen: Medium löschen, In Galerie exportieren, In Datei exportieren
* [ ] Rechtsklick wählt zuerst die Zeile unter dem Mauszeiger aus
* [ ] Löschen gesperrt für Medium `000` und für das einzige Medium einer Referenz, Tooltip nennt den Grund
* [ ] Exportieren ist für jedes Medium möglich
* [ ] Keine Icons in den Menüeinträgen

## Aufgabe 3: Löschen

* [ ] Bestätigung vor dem Löschen, nennt die Mediennummer
* [ ] Löschen über den `ProjectManager` mit lückenloser Neunummerierung und Mitverschiebung der Historiendetails
* [ ] Beim Löschen des neuesten Mediums wird der Vorgänger aktuell
* [ ] Auswahl danach auf den Vorgänger, Hauptfenster zeigt ein gültiges Medium, Dateiliste berechnet die Vorschau neu

## Aufgabe 4: Export

* [ ] Dialog zur Ordnerwahl mit den Ordnern des Projekts, Standard ist der in der Dateiliste angezeigte Ordner
* [ ] In Galerie exportieren: Kopie als neue Referenz über den `ProjectManager`, Projekt wird `dirty`
* [ ] In Datei exportieren: Dateiauswahl mit Standardname aus Referenzname und Mediennummer
* [ ] Überschreiben bestätigt der FileChooser, Export in Datei ändert das Projekt nicht
* [ ] Einfachauswahl in der Liste, keine Mehrfachauswahl
* [ ] Neue Referenz erscheint in der Dateiliste, sobald ihr Ordner angezeigt wird

## Aufgabe 5: Texte, Tests und Doku

* [ ] Nachrichtenschlüssel anlegen, Übersetzung nur über den Agenten `translator`
* [ ] Tests: gesperrte Einträge samt Tooltip, Löschen aus der Mitte und am Ende, Auswahl, Neunummerierung
* [ ] Tests: Ordnerwahl, Kopie in die Galerie, Export in Datei, Projekt unverändert bei Dateiexport
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
