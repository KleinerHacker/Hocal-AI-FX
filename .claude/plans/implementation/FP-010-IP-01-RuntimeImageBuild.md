# FP-010-IP-01: Runtime-Image bauen

## Voraussetzungen

* Keine

## Aufgabe 1: Vorbereitung

* [ ] Skills `ui-styling`, `testing`, `project-docs` und `ci-pipeline` laden
* [ ] Gradle-Task `jlink` über einen Agenten mit vollem Pfad ausführen, Ausgangszustand festhalten

## Aufgabe 2: Image-Konfiguration

* [ ] Merged-Modul für `mvvmfx` und `typetools` mit JDK 25 und JavaFX 25.0.1 lauffähig machen
* [ ] Log4j-2-Provider und SLF4J-Bindung explizit in das Image aufnehmen
* [ ] Modul `jdk.localedata` für Deutsch und weitere Sprachen aufnehmen
* [ ] Launcher mit JVM-Option `--enable-native-access=javafx.graphics` versehen
* [ ] Bei fehlenden `uses`, `provides` oder `opens` die `module-info.java` ergänzen

## Aufgabe 3: Start aus dem Image

* [ ] Launcher des Images ohne `JAVA_HOME` und ohne `java` im `PATH` starten
* [ ] FXML, Resource-Bundles, Logging, Deutsch und Englisch, Theme im Image prüfen
* [ ] Optionen `--strip-debug` und `--compress` beibehalten, Stacktraces im Log prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
