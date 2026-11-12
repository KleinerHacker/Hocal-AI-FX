# FP-003-IP-04: Fehler- und Fortschrittsdialoge

## Voraussetzungen

* IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `dialog` und `ui-styling` laden
* [ ] Agent `dialog-explore`, danach Agent `dialog-creator` verwenden

## Aufgabe 2: Dialoge

* [ ] `ErrorDialog` mit eigener Meldung je Ergebnis aus IP-01
* [ ] Meldungen enthalten niemals Passwörter oder Dateiinhalte
* [ ] `ProgressDialog`, modal, ohne Abbrechen, an `busy` des Managers gebunden
* [ ] Kurze Verzögerung vor dem Anzeigen, damit schnelle Operationen nicht flackern

## Aufgabe 3: Tests und Doku

* [ ] Nachrichtenschlüssel ergänzen, Übersetzung über Agent `translator`
* [ ] Tests: jede Fehlermeldung, Fortschritt bei langer und kurzer Operation
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
