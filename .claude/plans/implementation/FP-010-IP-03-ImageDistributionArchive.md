# FP-010-IP-03: Image-Distributionsarchiv

## Voraussetzungen

* FP-010 IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `project-docs` und `ci-pipeline` laden

## Aufgabe 2: Archiv

* [ ] `jlink.imageZip` auf `hocal-ui-<Version>-<Plattform>.zip` in `build/distributions` setzen
* [ ] Ausführungsrechte des Launchers im Archiv erhalten
* [ ] `assemble` und `build` erzeugen das Image-Archiv

## Aufgabe 3: Entfernen des alten Archivs

* [ ] Task `packageDist` aus `app/ui/build.gradle.kts` entfernen
* [ ] Deaktivierung von `distZip` und `distTar` entfernen, Kommentar anpassen
* [ ] Dateien `src/dist/hocal-ui.sh` und `src/dist/hocal-ui.bat` mit `git rm` löschen
* [ ] Verbleibende Verweise auf `packageDist` suchen und anpassen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
