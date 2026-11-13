# FP-007-IP-04: Bildanalyse

## Voraussetzungen

* IP-03

## Aufgabe 1: Vorbereitung

* [ ] Skill `testing` laden
* [ ] Bildformate und Größen der Projekt-Medien aus FP-001 und FP-010 übernehmen

## Aufgabe 2: Signale je Bild

* [ ] Paket `image` in `lib/guard` anlegen
* [ ] Je Bild höchste M-Stufe aller Personen und höchste S-Stufe ermitteln
* [ ] Scheinbares Alter unter 20 ergibt M `LIKELY`
* [ ] Nicht sicher als 20 oder älter beurteilbares Alter ergibt M `POSSIBLE`
* [ ] Gezeichnete, gerenderte und stilisierte Darstellungen wie realistische behandeln
* [ ] Teilweise sichtbare Personen in die Bewertung einbeziehen

## Aufgabe 3: Verarbeitung

* [ ] Bilder nur im Speicher dekodieren, nichts auf die Platte schreiben
* [ ] Verkleinerung für die Engine so wählen, dass Alter und Nacktheit beurteilbar bleiben
* [ ] Zu große oder nicht dekodierbare Bilder ergeben `UNDECIDABLE`
* [ ] Ergebnis je Bild über den Inhalts-Hash im Speicher halten, nichts speichern

## Aufgabe 4: Vorschaubilder

* [ ] Stichprobe: jedes N-te Vorschaubild mit Mindestabstand prüfen
* [ ] Treffer in der Vorschau meldet den Abbruch des Laufs an den Aufrufer
* [ ] Vorschaubilder nie speichern

## Aufgabe 5: Tests und Doku

* [ ] Tests mit Engine-Attrappe und neutralen Bildern, kein explizites Material
* [ ] Tests: mehrere Personen, Altersgrenze 20, unsicheres Alter
* [ ] Tests: Hash-Zwischenspeicher, zu großes Bild, defektes Bild
* [ ] Tests: Stichprobe der Vorschau und Abbruchmeldung
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
