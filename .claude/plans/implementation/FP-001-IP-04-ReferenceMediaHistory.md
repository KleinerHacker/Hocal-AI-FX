# FP-001-IP-04: Medien-Historie

## Voraussetzungen

* IP-01 und IP-03 abgeschlossen, GitHub-Build grün

## Aufgabe 1: Medien-Ablage

* [ ] Schnittstelle `MediaStore` in `org.pcsoft.app.hocal.model.storage`, erweitert `MediaSource` und `MediaSink`
* [ ] `MediaStore` zusätzlich: `delete(reference, number)`, `move(reference, from, to)`, `numbers(reference)` aufsteigend sortiert
* [ ] `DirectoryMediaStore(root: Path)`: Datei `<root>/<uuid>/<NNN>.<Endung>`, Endung bleibt beim Verschieben erhalten
* [ ] Schreiben über temporäre Datei und atomares Verschieben, nie halbe Medien sichtbar
* [ ] UUID und Nummer gegen Pfad-Tricks prüfen, gleiche Regeln wie `ProjectValidator`
* [ ] Fehlendes Medium beim Öffnen: `NoSuchFileException`

## Aufgabe 2: Historie

* [ ] Objekt `MediaHistory`: `current(reference)` liefert das letzte Medium, `currentNumber(reference)` dessen Nummer
* [ ] `append(store, reference, medium, extension, content)`: Inhalt zuerst in die Ablage, dann Liste erweitern, Rückgabe Nummer
* [ ] Anhängen mit Nummer über `999` lehnt `MediaHistoryFullException` ab, Liste und Ablage bleiben unverändert
* [ ] `remove(store, reference, number)`: Medium löschen, folgende aufsteigend um eins nach unten verschieben, dann Liste kürzen
* [ ] Löschen von `000` lehnt `OriginalMediumException` ab, damit bleibt immer mindestens ein Medium
* [ ] Unbekannte Nummer lehnt `MediumNotFoundException` ab
* [ ] Löschen des aktuellen Mediums macht den Vorgänger zum aktuellen
* [ ] `verify(store, reference)`: Nummern der Ablage gleich `0` bis Medienanzahl minus eins, sonst `InconsistentHistoryException`
* [ ] Sealed `MediaHistoryException` als Basis der vier Fehler, ohne neue POJO-Felder, `lib/fx-model` bleibt unberührt
* [ ] Dokumentation: nach Fehler in der Ablage `verify` aufrufen oder Projekt neu laden

## Aufgabe 3: Tests und Doku

* [ ] `MemoryMedia` der Tests wird ebenfalls `MediaStore`, gemeinsame Fälle laufen gegen beide Ablagen
* [ ] Tests: Anhängen, Überlauf bei 1000 Medien, aktuelles Medium, gemischte Endungen
* [ ] Tests: Löschen aus Anfang, Mitte und Ende, Nummern lückenlos, Inhalt und Details gehören weiterhin zusammen
* [ ] Tests: `000` ablehnen, unbekannte Nummer, Löschen des aktuellen Mediums, Fehler lässt Zustand unverändert
* [ ] Tests: `verify` mit Lücke, überzähliger Datei und Ordner ohne Medium
* [ ] Tests `DirectoryMediaStore`: Round-Trip, Verschieben mit Endung, Pfad-Tricks, kein Rest einer temporären Datei
* [ ] Test: Anhängen und Löschen, dann Speichern als ZIP und Lesen, Nummern und Medien passen zusammen
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Kein lokaler Build möglich, pushen und auf den GitHub-Build warten
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Feature-Plan: Abschnitt „As Built“ ergänzen, Abweichung (Liste statt Ordner im ZIP, Ablage-Schnittstelle) benennen
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
