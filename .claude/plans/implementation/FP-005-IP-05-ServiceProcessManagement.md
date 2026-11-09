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

## Aufgabe 3: Lazy-Start

* [ ] `ensureRunning()` startet den Dienst bei der ersten Kommunikation
* [ ] Sicher gegen gleichzeitige Aufrufer
* [ ] Vorschaltschicht vor der Fassade, `ComfyClient` bleibt prozessunabhängig

## Aufgabe 4: Stop und Neustart

* [ ] Stop zuerst sauber, dann erzwungen, einschließlich Kindprozessen
* [ ] Neustart; bei laufender Erzeugung über die Warteschlange warnen
* [ ] Fremde Prozesse nie beenden

## Aufgabe 5: Absturzsicherung

* [ ] PID-Datei schreiben und verwaisten Dienst beim nächsten Start aufräumen
* [ ] JVM-Shutdown-Hook als Absicherung

## Aufgabe 6: Tests und Doku

* [ ] Tests mit Attrappenprozess: Start, Bereitschaft, Zeitlimit, Stop, Neustart, Verwaisung
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
