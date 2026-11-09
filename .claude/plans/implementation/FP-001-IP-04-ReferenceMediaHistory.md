# FP-001-IP-04: Medien-Historie

## Voraussetzungen

* IP-01, IP-03

## Aufgabe 1: Lesen und Anhängen

* [ ] Datei-Namen `<NNN>.<Endung>` je Referenzordner, Nummer vor der Endung
* [ ] Aktuelles Medium als höchste Nummer lesen, gesamte Historie lesen
* [ ] Anhängen mit höchster Nummer plus 1
* [ ] Überlauf über `999` als Fehler
* [ ] Validierung: Namensform, lückenlos ab `000`, mindestens ein Medium

## Aufgabe 2: Löschen

* [ ] Löschen eines Mediums mit lückenloser Neunummerierung der folgenden
* [ ] Löschen von `000` ablehnen, damit auch das letzte Medium nie löschbar ist
* [ ] Löschen des aktuellen Mediums macht den Vorgänger zum aktuellen
* [ ] Neunummerierung auf der Gesamtmenge vor dem Schreiben, nie in-place
* [ ] Schreiben über das atomare Speichern aus IP-03

## Aufgabe 3: Tests und Doku

* [ ] Tests: Anhängen, Überlauf, Löschen aus der Mitte, Löschen des aktuellen Mediums
* [ ] Tests: `000` ablehnen, Lücke, Ordner ohne Medium, gemischte Endungen
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
