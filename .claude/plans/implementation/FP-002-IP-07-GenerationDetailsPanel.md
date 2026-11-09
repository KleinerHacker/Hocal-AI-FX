# FP-002-IP-07: Panel mit Generierungsdaten

## Voraussetzungen

* IP-05, FP-001 IP-02

## Aufgabe 1: Panel

* [ ] Skills `component`, `fx-component-lifecycle` und `ui-styling` laden
* [ ] Komponente `GenerationDetails` oben im linken Teil des Inhaltsbereichs, neben dem Bild
* [ ] Erste Zeile: Modell und Flow des gewählten Mediums
* [ ] Zweite Zeile: Anfang des Prompts, bei Überlänge mit `...` abgeschnitten
* [ ] Abschneidelänge folgt der Breite des Panels
* [ ] Daten stammen aus den Historien-Details des gewählten Mediums aus FP-001
* [ ] Medium ohne Details zeigt leere Zeilen
* [ ] Zusätzlich verwendete Bilder werden nicht angezeigt, sie sind nicht Teil der Daten

## Aufgabe 2: Tests und Doku

* [ ] Nachrichtenschlüssel ergänzen, Übersetzung über Agent `translator`
* [ ] Tests: beide Zeilen, Abschneiden mit `...`, leere Details, Wechsel des Mediums
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
