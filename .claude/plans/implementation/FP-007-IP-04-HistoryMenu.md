# FP-007-IP-04: Historien-Menü

## Voraussetzungen

* IP-02, FP-002 IP-05, FP-001 IP-04

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `dialog`, `ui-styling`, `fx-component-lifecycle` und `translation` laden

## Aufgabe 2: Menü

* [ ] Kontextmenü in `HistoryList` mit genau einem Eintrag: Medium löschen
* [ ] Rechtsklick wählt zuerst die Zeile unter dem Mauszeiger aus
* [ ] Eintrag gesperrt für Medium `000` und für das einzige Medium einer Referenz
* [ ] Gesperrter Eintrag erklärt den Grund per Tooltip
* [ ] Keine Icons in den Menüeinträgen

## Aufgabe 3: Löschen

* [ ] Bestätigung vor dem Löschen, nennt die Mediennummer
* [ ] Löschen über den `ProjectManager` mit lückenloser Neunummerierung und Mitverschiebung der Historiendetails
* [ ] Beim Löschen des neuesten Mediums wird der Vorgänger aktuell
* [ ] Auswahl danach auf den Vorgänger, Hauptfenster zeigt ein gültiges Medium, Dateiliste berechnet die Vorschau neu

## Aufgabe 4: Texte, Tests und Doku

* [ ] Nachrichtenschlüssel anlegen, Übersetzung nur über den Agenten `translator`
* [ ] Tests: gesperrte Einträge samt Tooltip, Löschen aus der Mitte und am Ende, Auswahl, Neunummerierung
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
