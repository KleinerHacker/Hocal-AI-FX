# FP-002-IP-06: Eingabeteil mit Prompt

## Voraussetzungen

* IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `dialog`, `fx-component-lifecycle` und `ui-styling` laden
* [ ] Agenten `component-explore`/`component-creator` und `dialog-explore`/`dialog-creator` verwenden

## Aufgabe 2: Eingabeteil

* [ ] Komponente `PromptInput` unter dem Bild anlegen, mehrzeilige Textbox für den Prompt
* [ ] Links über der Textbox den Button `+`, mit Text statt Icon, Icons sind nicht Teil eines Plans
* [ ] Rechts über der Textbox die ComboBox für das Modell, darunter die Toggle-Buttons (Platzhalter)
* [ ] Modellliste als Platzhalter hinter einer Schnittstelle
* [ ] Absenden, Generierung und Speichern des Prompts sind nicht Teil dieses Plans

## Aufgabe 3: Galerie-Dialog

* [ ] Dialog `GalleryDialog` zeigt die interne virtuelle Verzeichnisstruktur
* [ ] Nur Referenzen (Einträge) wählbar, Ordner nicht
* [ ] Das letzte Medium der Referenz zählt und wird angezeigt
* [ ] Gewählte Bilder bleiben im Eingabeteil, identifiziert über die UUID der Referenz, bis sie benutzt werden
* [ ] Hinzugefügte Bilder werden nicht als Generierungsdetail in der Historie gespeichert

## Aufgabe 4: Tests und Doku

* [ ] Nachrichtenschlüssel ergänzen, Übersetzung über Agent `translator`
* [ ] Tests: Auswahl im Dialog, Ordner nicht wählbar, Bilder im Eingabeteil, Bedienelemente
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
