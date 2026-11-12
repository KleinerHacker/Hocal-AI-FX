# FP-004-IP-04: Ausführungs-Ereignis-Hook

## Voraussetzungen

* IP-01, unabhängig von IP-02 und IP-03
* Nachrichtennamen und Vorschau-Header gegen `docs.comfy.org` prüfen, sobald die Domain freigegeben ist

## Aufgabe 1: Vorbereitung

* [ ] Skills `fx-component-lifecycle` und `testing` laden

## Aufgabe 2: Ereignistypen

* [ ] Hierarchie `ComfyEvent`: Status, Start, Cache, Ausführung, Fortschritt, Ergebnis, Erfolg, Fehler, Abbruch
* [ ] Ende eines Laufs: `executing` mit `node = null` und passender `prompt_id`
* [ ] Vorschaubild als Ereignis mit Bytes, unbekannte Nachrichten als allgemeines Ereignis, nur loggen

## Aufgabe 3: Verbindung

* [ ] WebSocket über `java.net.http.WebSocket` zu `/ws?clientId=...`
* [ ] Textframes mit Jackson in Ereignisse umwandeln, Binärframes als Vorschau
* [ ] Verbindungszustand verbunden, getrennt, verbindet als Ereignis melden
* [ ] Start und Stop, Stop gibt Thread und Socket frei

## Aufgabe 4: Listener

* [ ] Listener an- und abmelden, Zustellung auf eigenem Thread, nie auf dem FX-Thread
* [ ] Begrenzte Warteschlange, Vorschaubilder bei Überlauf verwerfen
* [ ] Ausnahme eines Listeners loggen, andere Listener weiter beliefern

## Aufgabe 5: Wiederverbinden

* [ ] Nach Verbindungsverlust automatisch mit wachsender Wartezeit neu verbinden
* [ ] Ohne Neustart der Anwendung, bei Stop kein Wiederverbinden

## Aufgabe 6: Tests und Doku

* [ ] Tests mit WebSocket-Stub aus IP-05 oder einer lokalen Attrappe: Reihenfolge, Fehler, Verlust, Stop
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
