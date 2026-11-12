# FP-009-IP-09: Hilfe-Menü und Lizenzdialog

## Voraussetzungen

* FP-002 IP-01, FP-008 IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `dialog`, `ui-styling`, `translation`, `ci-pipeline`, `testing` laden
* [ ] Agents `dialog-explore` und `dialog-creator` verwenden

## Aufgabe 2: Menü und Dialoge

* [ ] Hilfe-Menü mit „Über“ und „Komponenten“, ohne Icons
* [ ] Über-Dialog: Name, Version aus dem Build, Kurzbeschreibung, eigene Lizenz
* [ ] Komponenten-Dialog: Liste mit Name, Version, Lizenz und Link

## Aufgabe 3: Liste

* [ ] Gradle-Task erzeugt die Liste aus den Daten der vorhandenen Lizenzprüfung als Ressource
* [ ] Feste Einträge für ComfyUI (GPL-3.0, nur heruntergeladen), Python, `uv` und PyTorch
* [ ] Lizenzen der Modelle des Katalogs zur Laufzeit aus dem Katalog lesen
* [ ] Build prüft, dass die Liste aktuell ist, neue Abhängigkeit nur nach Rückfrage

## Aufgabe 4: Doku und Tests

* [ ] README-Hinweis zu Lizenzen ergänzen
* [ ] Tests des Dialogs und der Listenerzeugung

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
