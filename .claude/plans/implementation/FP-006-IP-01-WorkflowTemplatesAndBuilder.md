# FP-006-IP-01: Workflow-Vorlagen und Builder

## Voraussetzungen

* FP-004 IP-03

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing` und `project-docs` laden
* [ ] Mit dem Benutzer klären: welche Flows und Modelle zuerst geliefert werden und welche Modelldateien die Runtime installiert

## Aufgabe 2: Vorlagen

* [ ] Workflow-Vorlagen im ComfyUI-API-Format als Ressourcen der Anwendung ablegen
* [ ] Benannte Platzhalter für Prompt, Seed, Modell, Eingabebild und weitere Bilder festlegen
* [ ] `WorkflowRegistry` liest die Vorlagen, der Vorlagenname ist der Flow-Name der Historie

## Aufgabe 3: Builder

* [ ] `WorkflowBuilder` füllt die Platzhalter und erzeugt pro Lauf einen neuen Seed
* [ ] Seed wird nicht in der Historie gespeichert
* [ ] Fehlende oder überzählige Platzhalter als typisierten Fehler melden

## Aufgabe 4: Modelle und Prüfung

* [ ] Modellliste (Checkpoint-Namen) über `objectInfo` der Fassade lesen
* [ ] Vorlage prüfen: Platzhalter vorhanden, Knoten dem Server bekannt, Modell vorhanden

## Aufgabe 5: Tests und Doku

* [ ] Tests: Platzhalter füllen, neuer Seed, Fehlerfälle, Modellliste mit WireMock
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
