# FP-009-IP-06: Kategorie Modelle

## Voraussetzungen

* IP-03, FP-008 IP-05, FP-008 IP-06

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `ui-styling`, `translation`, `fx-component-lifecycle` und `testing` laden

## Aufgabe 2: Seite

* [ ] Modellliste aus FP-008 IP-06 als Komponente in die Seite setzen, Menüeintrag der Liste entfernen
* [ ] Download-Anzeiger oben rechts öffnet den Dialog auf dieser Seite
* [ ] Hinweis in der Modell-ComboBox verweist auf diese Seite
* [ ] Hinweis auf der Seite: Download und Löschen wirken sofort, Abbrechen macht sie nicht rückgängig
* [ ] Modellordner schreibgeschützt anzeigen

## Aufgabe 3: Limit paralleler Downloads

* [ ] Eigene Einstellung innerhalb der Seite, getrennt von der Liste: höchste Anzahl paralleler Downloads
* [ ] Vorgabe unbegrenzt, Leer oder 0 bedeutet unbegrenzt
* [ ] Plätze in `lib/comfy-runtime` Paket `models`: Downloads über dem Limit stehen auf „wartend“ und starten bei freiem Platz
* [ ] Änderung wirkt nur auf danach gestartete Downloads, laufende bleiben unberührt
* [ ] Zustand „wartend“ in Zeilen und im Anzeiger, Gesamtfortschritt zählt nur laufende Downloads

## Aufgabe 4: Tests

* [ ] Tests der Platzlogik mit WireMock, Änderung des Limits bei laufenden Downloads
* [ ] TestFX für die Seite

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
