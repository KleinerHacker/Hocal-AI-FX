# FP-010-IP-08: Durchsetzung der Grenzen

## Voraussetzungen

* IP-04, FP-001 IP-07, FP-008 IP-01, FP-006 IP-03

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing` und `project-docs` laden

## Aufgabe 2: Prüfungen

* [ ] `MediaLimits` in `lib/model`: reine Prüfungen für Bildgröße in Pixel und Größe in Bytes
* [ ] Bildgröße prüfen beim Import, bei Eingabebildern für die Erzeugung und bei empfangenen Ergebnissen
* [ ] Pixelgröße aus dem Dateikopf lesen, ohne das Bild zu dekodieren
* [ ] Projektgröße prüfen beim Öffnen vor dem Lesen, beim Speichern und beim Hinzufügen eines Mediums
* [ ] Meldung nennt den tatsächlichen Wert und die Grenze, nichts wird verkleinert oder geändert
* [ ] Bestehendes zu großes Projekt lässt sich nach Anheben der Grenze öffnen
* [ ] Grenzen werden beim Gebrauch gelesen, Änderungen gelten sofort

## Aufgabe 3: Tests

* [ ] Tests mit zur Laufzeit geänderten Grenzen, Grenzfälle genau auf der Grenze

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
