# FP-005-IP-02: Download und Archivbehandlung

## Voraussetzungen

* IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skill `testing` laden
* [ ] Entscheidung zu tar.gz oder tar.zst einholen, neue Bibliothek nur nach Rückfrage

## Aufgabe 2: Download

* [ ] Download über den HTTP-Client aus FP-004, nur HTTPS
* [ ] Fortschritts-Callback und Abbruch unterstützen
* [ ] Unterbrochene Datei fortsetzen
* [ ] Prüfsumme gegen den festen Wert prüfen, bei Abweichung verwerfen
* [ ] Zeitlimits setzen, Proxy-Einstellungen des Systems beachten

## Aufgabe 3: Entpacken

* [ ] ZIP entpacken mit Zip-Slip-Schutz wie in FP-001
* [ ] tar.gz entpacken, falls für `uv` nötig
* [ ] Ausführbare Dateirechte unter Linux und macOS setzen

## Aufgabe 4: Aufräumen

* [ ] Temporäre Dateien im Datenverzeichnis, atomares Verschieben bei Erfolg
* [ ] Bei Fehler oder Abbruch nichts zurücklassen
* [ ] Freien Speicherplatz vor dem Download prüfen

## Aufgabe 5: Tests und Doku

* [ ] Tests mit WireMock: Erfolg, falsche Prüfsumme, Abbruch, Fortsetzen, Zip-Slip, voller Speicher
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
