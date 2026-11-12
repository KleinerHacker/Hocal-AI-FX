# FP-005-IP-05: Dienst-Prozessverwaltung

## Voraussetzungen

* IP-01, FP-004 IP-03
* Kann parallel zu IP-02 bis IP-04 mit konfiguriertem Programmpfad laufen

## Aufgabe 1: Vorbereitung

* [ ] Skills `fx-component-lifecycle` und `testing` laden

## Aufgabe 2: Start

* [ ] Prozess ohne Shell mit Python der Umgebung starten, gebunden an `127.0.0.1`
* [ ] Freien Port bei jedem Start wählen und an `ComfyConfig` weitergeben
* [ ] Auf Bereitschaft warten über `systemStats` der Fassade, mit Zeitlimit
* [ ] Ausgabekanäle laufend auf eigenem Thread in eine Logdatei mit Rotation schreiben

## Aufgabe 3: Offline-Betrieb und Datenschutz

* [ ] Unterstützten Schalter prüfen: Ausgabe von `--help` der installierten Engine nach `--offline` durchsuchen
* [ ] Mit `--offline` starten, sonst mit dem älteren `--disable-api-nodes`, Ergebnis je Version zwischenspeichern
* [ ] Kennt die Engine keinen der beiden Schalter, Start mit klarem Fehler verweigern, nie mit Bezahl-Knoten starten
* [ ] `GemmaAPITextEncode` in die eingebaute Liste abgeschalteter Knoten von ComfyUI 0.42 eintragen, Weg anhand der Doku klären
* [ ] Umgebung des Kindprozesses: `DO_NOT_TRACK=1` und `HF_HUB_DISABLE_TELEMETRY=1`, nur für den Kindprozess
* [ ] Schalternamen und Liste gegen die ComfyUI-Dokumentation prüfen, in der Planung nicht erreichbar

## Aufgabe 4: Lazy-Start

* [ ] `ensureRunning()` startet den Dienst bei der ersten Kommunikation
* [ ] Sicher gegen gleichzeitige Aufrufer
* [ ] Vorschaltschicht vor der Fassade, `ComfyClient` bleibt prozessunabhängig

## Aufgabe 5: Stop und Neustart

* [ ] Stop zuerst sauber, dann erzwungen, einschließlich Kindprozessen
* [ ] Neustart; bei laufender Erzeugung über die Warteschlange warnen
* [ ] Fremde Prozesse nie beenden

## Aufgabe 6: Absturzsicherung

* [ ] PID-Datei schreiben und verwaisten Dienst beim nächsten Start aufräumen
* [ ] JVM-Shutdown-Hook als Absicherung

## Aufgabe 7: Tests und Doku

* [ ] Tests mit Attrappenprozess: Start, Bereitschaft, Zeitlimit, Stop, Neustart, Verwaisung
* [ ] Tests: Schalterwahl bei beiden Engine-Arten, kein Schalter bekannt, Umgebungsvariablen am Prozess
* [ ] KDoc prüfen
* [ ] README-Abschnitt: ComfyUI läuft offline ohne Bezahl-Knoten, Telemetrie aus, `GemmaAPITextEncode` gesperrt

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
