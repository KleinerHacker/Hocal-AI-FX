# FP-004-IP-02: HTTP-Transport

## Voraussetzungen

* IP-01
* Routen und Felder gegen `docs.comfy.org` prüfen, sobald die Domain freigegeben ist

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing` und `project-docs` laden

## Aufgabe 2: Client

* [ ] Ein gemeinsamer Apache-HTTP-Client mit Verbindungspool und Zeitlimits für Verbindung und Antwort
* [ ] `close` gibt Client und Pool frei, der Besitzer der Fassade ruft es auf
* [ ] Antworten vollständig lesen und schließen, damit die Verbindung zurückgegeben wird

## Aufgabe 3: Anfragen

* [ ] GET und POST mit JSON in und aus über Jackson
* [ ] Binärer Download als Bytes und als Stream mit Größenlimit
* [ ] Multipart-Upload für `/upload/image`
* [ ] Pfadbestandteile wie `filename` und `subfolder` kodieren

## Aufgabe 4: Fehlerabbildung

* [ ] Statuscodes, I/O-Fehler, Zeitüberschreitung und ungültiges JSON auf die typisierten Ausnahmen abbilden
* [ ] Antwortauszug im Serverfehler begrenzen, keine Workflow-Inhalte loggen

## Aufgabe 5: Tests und Doku

* [ ] WireMock-Tests: JSON, Binär, Multipart, jeder Fehlerfall, Zeitüberschreitung, parallele Aufrufe
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
