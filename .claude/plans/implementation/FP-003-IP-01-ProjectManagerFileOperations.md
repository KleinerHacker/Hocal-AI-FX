# FP-003-IP-01: Projekt-Manager: Dateioperationen

## Voraussetzungen

* FP-001 IP-06, FP-001 IP-07

## Aufgabe 1: Vorbereitung

* [ ] Skills `fx-model`, `fx-component-lifecycle` und `testing` laden
* [ ] Vertrag aus FP-001 IP-07 prüfen: blockierende Dateiaufrufe werden asynchron, Plan bei Bedarf anpassen

## Aufgabe 2: Hintergrundausführung

* [ ] Öffnen und Speichern über `javafx.concurrent` im Hintergrund ausführen
* [ ] Ergebnis auf dem FX-Thread melden
* [ ] Schreibgeschützte Property `busy` bereitstellen
* [ ] Zweite Operation während `busy` abweisen

## Aufgabe 3: Zustand und Passwort

* [ ] Property `encrypted` ergänzen, neben `project`, `path` und `dirty`
* [ ] Passwort als `CharArray` halten, nie loggen, nie auf die Platte schreiben
* [ ] Passwort überschreiben bei Schließen, Neu, Ersetzen und fehlgeschlagenem Öffnen
* [ ] Leeres Passwort ablehnen, kein Passwort heißt unverschlüsselt
* [ ] Speichern unter nimmt Pfad und optionales Passwort entgegen, Speichern nutzt den gehaltenen Zustand

## Aufgabe 4: Fehler

* [ ] Typisierte Ergebnisse: ungültig, beschädigt, neuere Version, falsches Passwort, Passwort nötig, manipuliert, Schreibfehler
* [ ] Fehlgeschlagene Operation lässt Projekt, Pfad und Passwort unverändert

## Aufgabe 5: Tests und Doku

* [ ] Tests: Erfolg, jeder Fehler, Abweisung bei `busy`, Passwort überschrieben, Projekt unverändert nach Fehler
* [ ] Testdateien in einem temporären Verzeichnis, KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
