# FP-004-IP-05: Stub-Server und Integrationstests

## Voraussetzungen

* IP-03, IP-04

## Aufgabe 1: Vorbereitung

* [ ] Skill `testing` laden, Testklassen mit Suffix `IT` für Integrationstests

## Aufgabe 2: HTTP-Stub

* [ ] WireMock-Stubs für alle Routen der Fassade mit festen Antworten
* [ ] Systemvergebener Port, um Konflikte in der CI zu vermeiden
* [ ] Lizenzen der Unterabhängigkeiten von WireMock prüfen, nicht gelistete dem Benutzer vorlegen

## Aufgabe 3: WebSocket-Stub

* [ ] Handgeschriebener Stub auf `ServerSocket` mit Handshake und Frames
* [ ] Skriptbare Ereignisfolgen: Erfolg, Fehler, Abbruch, Vorschau, Verbindungsabbruch

## Aufgabe 4: Integrationstests

* [ ] Vollständiger Lauf: Einreichen, Ereignisse bis zum Ende, History, Download des Ergebnisses
* [ ] Fehlerfälle: Server nicht erreichbar, 500, ungültiges JSON, abgelehnter Workflow
* [ ] WebSocket-Verlust und erfolgreiches Wiederverbinden prüfen
* [ ] Nach Stop des Hooks sind Thread und Socket frei
* [ ] Keine Tests gegen einen echten ComfyUI-Server

## Aufgabe 5: Pipeline und Doku

* [ ] CI-Pipeline für die neuen Tests prüfen, README und MkDocs-Hinweise ergänzen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
