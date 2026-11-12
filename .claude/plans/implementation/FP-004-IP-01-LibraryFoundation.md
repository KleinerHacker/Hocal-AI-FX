# FP-004-IP-01: Bibliotheks-Grundlage

## Voraussetzungen

* keine; Jackson wird mit FP-001 IP-01 geteilt, wer zuerst kommt, ergänzt die Abhängigkeit

## Aufgabe 1: Vorbereitung

* [ ] Skills `ci-pipeline`, `project-docs` und `testing` laden
* [ ] Freigabe des Benutzers für transitive Artefakte und neue Lizenzen von `httpclient5` und WireMock einholen

## Aufgabe 2: Modul

* [ ] Modul `lib/comfy` (`hocal-comfy`) in `settings.gradle.kts` eintragen
* [ ] `build.gradle.kts` ohne JavaFX und ohne Anwendungs-Plugins anlegen
* [ ] Abhängigkeiten `httpclient5`, Jackson, SLF4J API und Testabhängigkeit WireMock ergänzen
* [ ] `module-info.java` anlegen, Modul `java.net.http` einbinden
* [ ] Prüfen, ob Apache-Artefakte als automatische Module in Modulpfad und jlink laufen, sonst verschmelzen

## Aufgabe 3: Konfiguration und Fehler

* [ ] `ComfyConfig`: Host, Port, Zeitlimits, Standard `http://127.0.0.1:8188`, nur lokal, Validierung
* [ ] `clientId` als UUID pro Anwendungslauf erzeugen
* [ ] Fehlerhierarchie `ComfyException`: Verbindung, Zeitüberschreitung, Serverfehler, ungültige Antwort, Knotenfehler
* [ ] Datentypen der Anfragen und Antworten als Kotlin-Klassen anlegen

## Aufgabe 4: Tests und Doku

* [ ] Tests für Konfiguration, Validierung und Fehlertypen mit KDoc-Anwendungsfall
* [ ] CI-Pipeline, Lizenzprüfung und README-Strukturabschnitt prüfen und anpassen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
