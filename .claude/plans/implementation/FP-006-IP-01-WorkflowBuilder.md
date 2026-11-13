# FP-006-IP-01: Workflow-Builder

## Voraussetzungen

* FP-004 IP-03, FP-009 IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing` und `project-docs` laden
* [ ] Katalogformat aus FP-009 IP-01 prüfen: Standardfelder, Parameter, Ziele `<Knoten>.<Eingabe>`

## Aufgabe 2: Builder

* [ ] `WorkflowBuilder` arbeitet auf einer Kopie des Workflows, die Vorlage des Katalogs bleibt unverändert
* [ ] Werte schreiben: Prompt, Dateinamen des Modells, Seed, Eingabebild, weitere Bilder und Parameter
* [ ] Neuer zufälliger Seed bei jedem Senden, solange der Benutzer ihn nicht festgelegt hat
* [ ] Konstanten (`CONSTANT`) aus dem Katalog in den Workflow schreiben
* [ ] Nicht vorhandenes Ziel als typisierten Fehler melden

## Aufgabe 3: Modelle und Prüfung

* [ ] Installierte Checkpoint-Namen über `objectInfo` der Fassade lesen und mit dem Katalog vergleichen
* [ ] Ziele gegen die dem Server bekannten Knoten prüfen

## Aufgabe 4: Tests und Doku

* [ ] Tests: jedes Ziel, Seed zufällig und fest, Konstanten, fehlendes Ziel, Modellvergleich mit WireMock
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
