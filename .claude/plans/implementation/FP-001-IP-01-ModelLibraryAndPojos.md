# FP-001-IP-01: Modell-Bibliothek und POJOs

## Voraussetzungen

* Keine

## Aufgabe 1: Modul anlegen

* [ ] Skills `fx-model`, `testing`, `project-docs` und `ci-pipeline` laden
* [ ] Modul `lib/model` (`hocal-model`) in `settings.gradle.kts` eintragen
* [ ] `lib/model/build.gradle.kts` wie `app/ui` anlegen, ohne JavaFX und ohne Anwendungs-Plugins
* [ ] Jackson-Abhängigkeiten ergänzen, Lizenz Apache-2.0 ist im `licensee`-Check erlaubt
* [ ] `module-info.java` anlegen, Modell-Pakete für Jackson-databind öffnen
* [ ] Fehlende Lizenz im erlaubten Teil dem Benutzer melden

## Aufgabe 2: POJOs und Konstanten

* [ ] `ProjectData` mit Name, Beschreibung, Projekt-UUID, Autor, Zeiten, Schlagworten, Formatversion, Anwendungsversion
* [ ] Wurzel-POJO mit Projektdaten, UUID-Liste der Referenzen und virtuellem Baum
* [ ] `VirtualNode` für Ordner und Eintrag, Eintrag trägt genau eine Referenz-UUID
* [ ] Alle POJOs mit Getter und Setter, wie `BeanFields` es verlangt
* [ ] Konstanten für Formatversion und feste JSON-Schlüsselnamen

## Aufgabe 3: Tests und Doku

* [ ] Tests je POJO, jede Testmethode mit KDoc-Anwendungsfall
* [ ] KDoc für alle öffentlichen Member
* [ ] Pipeline `ci.yml` und `release.yml` auf das neue Modul prüfen
* [ ] README-Strukturabschnitt anpassen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
