# FP-011-IP-02: Image-Verifikation

## Voraussetzungen

* FP-011 IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing`, `project-docs` und `ci-pipeline` laden

## Aufgabe 2: Prüfaufgabe

* [ ] Gradle-Task `verifyImage` in `app/ui/build.gradle.kts` anlegen, abhängig von `jlink`
* [ ] Modulliste des Images gegen eine feste Liste erwarteter Module prüfen
* [ ] Launcher des Images headless starten, Exit-Code und Ausgabe auswerten
* [ ] Prüfung ohne `JAVA_HOME` und ohne `java` im `PATH` ausführen
* [ ] Auf Linux, Windows und macOS lauffähig halten, nur volle Pfade verwenden

## Aufgabe 3: Tests

* [ ] Integrationstest `...IT` nach Skill `testing`, falls er zum Muster passt
* [ ] Negativfall prüfen: Image ohne Log4j-Provider oder Locale-Daten schlägt fehl

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
