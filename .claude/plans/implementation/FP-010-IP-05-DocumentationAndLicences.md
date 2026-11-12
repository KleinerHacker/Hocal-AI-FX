# FP-010-IP-05: Doku und Lizenzen

## Voraussetzungen

* FP-010 IP-03

## Aufgabe 1: Vorbereitung

* [ ] Skill `project-docs` laden

## Aufgabe 2: Dokumentation

* [ ] README: Abschnitt „Consume the artifacts“ auf das Image-Archiv umstellen
* [ ] README: Hinweis „Java 25 runtime has to be installed“ entfernen
* [ ] README: Tabelle „State of implementation“ anpassen
* [ ] `docs/docs/index.md` und Installationsseite in MkDocs anpassen
* [ ] CHANGELOG `[Unreleased]`: Eintrag zur mitgelieferten Java-Laufzeit

## Aufgabe 3: Lizenzen

* [ ] Mitgelieferte Laufzeit mit Lizenz GPL-2.0 mit Classpath Exception benennen
* [ ] Text für den Lizenzdialog aus FP-009 IP-09 bereitstellen
* [ ] MkDocs mit `--strict` über einen Agenten bauen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
