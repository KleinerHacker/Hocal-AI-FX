# FP-001-IP-03: ZIP-Storage

## Voraussetzungen

* IP-01 und IP-02 abgeschlossen, GitHub-Build grün

## Aufgabe 1: Grundlagen

* [ ] Paket `org.pcsoft.app.hocal.model.storage` in `lib/model`, ohne Zustand, in `module-info` exportieren
* [ ] Sealed `ProjectFileException` mit `InvalidProjectFileException`, `DamagedProjectFileException`, `UnsupportedFormatVersionException`
* [ ] Interner Jackson-Mapper: eingerückt, Zeiten ISO-8601 in UTC, Streams werden nicht geschlossen
* [ ] Prüfen, ob Jackson die Data Classes ohne Kotlin-Modul liest, sonst Rückfrage wegen Abhängigkeit
* [ ] Medien-Senke und Medien-Quelle als Schnittstellen, damit kein Medium ganz im Speicher liegen muss

## Aufgabe 2: Lesen

* [ ] `ProjectStorage.read(input, mediaSink)` liest über `ZipInputStream`, `project.json` muss erster Eintrag sein
* [ ] Medien-Einträge `<uuid>/<NNN>.<Endung>` einzeln an die Senke streamen
* [ ] Zip-Slip-Schutz: absolute Pfade, `..`, Rückwärts-Schrägstrich und unbekannte Struktur ablehnen
* [ ] Größenschutz: Obergrenzen für `project.json`, Medien und Eintragsanzahl, Überschreitung ist `Damaged`
* [ ] Validierung: Formatversion, eindeutige Referenz-UUIDs, Eintragsverweise, mindestens ein Medium je Referenz
* [ ] Validierung: Medienanzahl je Referenz gleich Anzahl der Details, Nummern lückenlos ab `000`
* [ ] Neuere Formatversion als bekannt wird abgelehnt

## Aufgabe 3: Schreiben

* [ ] `ProjectStorage.write(output, project, mediaSource)` schreibt `project.json` zuerst, alle Einträge `STORED`
* [ ] Größe und CRC je Medium im ersten Durchlauf messen, zweiter Durchlauf schreibt den Eintrag
* [ ] Datei-Variante speichert atomar: temporäre Datei im Zielordner, dann Verschieben mit `ATOMIC_MOVE`
* [ ] Bei Fehler temporäre Datei löschen, vorhandene Datei bleibt unberührt
* [ ] Stream-Einstiegspunkte bleiben öffentlich, damit IP-06 sie umschließen kann

## Aufgabe 4: Tests und Doku

* [ ] Tests: Round-Trip, beschädigtes ZIP, fehlende `project.json`, `project.json` nicht erster Eintrag
* [ ] Tests: Traversal, zu großer Eintrag, hängender Verweis, Referenz ohne Medium, falsche Medienanzahl
* [ ] Tests: neuere Formatversion, abgebrochenes Speichern lässt Datei intakt
* [ ] Tests nach Löschen eines Mediums (Anfang, Mitte, Ende): Nummern wieder lückenlos ab `000`
* [ ] Tests nach Löschen: Medieninhalt und Details gehören je Nummer weiterhin zusammen
* [ ] Tests nach Löschen: Schreiben und erneutes Lesen liefert gleiche Medienanzahl und Reihenfolge
* [ ] Test: Löschen des letzten Mediums einer Referenz wird beim Schreiben als ungültig abgelehnt
* [ ] Regressionstest `…RT` gegen eine feste Referenzdatei im Testverzeichnis
* [ ] KDoc und README-Funktionsliste prüfen

## Abschluss

* [ ] Kein lokaler Build möglich, pushen und auf den GitHub-Build warten
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG unverändert
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
