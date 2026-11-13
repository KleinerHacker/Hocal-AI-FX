# FP-007-IP-06: Hinweise und Einrichtung in der Oberfläche

## Voraussetzungen

* IP-05, FP-006 IP-03, FP-006 IP-04, FP-005 IP-06, FP-010 IP-03

## Aufgabe 1: Vorbereitung

* [ ] Skills `ui-styling`, `dialog`, `component`, `fx-component-lifecycle`, `icons`, `translation` und `testing` laden
* [ ] Agent `dialog-explore` für das Muster der Fehlerdialoge aus FP-003 IP-04 nutzen

## Aufgabe 2: Dialoge

* [ ] Dialog für blockierte Anfrage und blockiertes Ergebnis mit einem neutralen Text anlegen
* [ ] Dialog für nicht mögliche Prüfung mit Reparatur-Hinweis anlegen
* [ ] Text nennt weder Wort, Bildteil noch Regel
* [ ] Dialoge über den Agenten `dialog-creator` erstellen

## Aufgabe 3: Kachel

* [ ] Kachelzustand `BLOCKED` ergänzen: kein Bild, Hinweistext, bleibt bis zum Schließen
* [ ] Vorschaubild bei einem Block sofort entfernen
* [ ] Klick auf eine blockierte Kachel wählt kein Ergebnis aus

## Aufgabe 4: Einstellungen und Einrichtung

* [ ] Schutz-Zustand in der Kategorie `Generation` nur lesend zeigen: bereit, nicht installiert, fehlgeschlagen
* [ ] Schaltfläche für Reparatur ergänzen: Modelle installieren oder Engine neu starten
* [ ] Keinen Schalter zum Abschalten anbieten
* [ ] Schutz-Modelle in den Einrichtungsablauf des ersten Starts aufnehmen, mit Lizenz und Größe
* [ ] Fortschritt des Modell-Downloads im vorhandenen Fortschrittsdialog zeigen

## Aufgabe 5: Texte

* [ ] Schlüssel im englischen Basis-Bundle ergänzen
* [ ] Übersetzungen ausschließlich über den Agenten `translator` erzeugen
* [ ] Symbol für den Schutz-Zustand bei Bedarf über den Agenten `icon-creator` erstellen

## Aufgabe 6: Tests und Doku

* [ ] TestFX-Tests: Dialoge, Kachelzustand `BLOCKED`, Schutz-Zustand in den Einstellungen
* [ ] Test: kein Bedienelement schaltet den Schutz ab
* [ ] KDoc, README und MkDocs prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG ergänzen
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
