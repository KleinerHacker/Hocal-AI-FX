# FP-010-IP-04: Pipeline-Integration

## Voraussetzungen

* FP-010 IP-02, FP-010 IP-03

## Aufgabe 1: Vorbereitung

* [ ] Skill `ci-pipeline` laden, Struktur von `ci.yml` und `release.yml` einhalten

## Aufgabe 2: Release-Pipeline

* [ ] Build-Job je Betriebssystem ruft `jlink` statt `packageDist` auf
* [ ] `verifyImage` je Betriebssystem ausführen, ohne Java im `PATH`
* [ ] Artefaktpfad auf das Image-Archiv ändern
* [ ] Release-Job hängt die drei Image-Archive an

## Aufgabe 3: CI-Pipeline

* [ ] Image-Build und `verifyImage` im Build-Zweig von `ci.yml` aufnehmen
* [ ] Reihenfolge und Parallelität nach Skill `ci-pipeline` prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
