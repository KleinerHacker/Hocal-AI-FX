# FP-002-IP-02: Verzeichnisbaum-Komponente

## Voraussetzungen

* IP-01, FP-001 IP-02, FP-001 IP-07

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `fx-component-lifecycle`, `ui-styling` und `fx-model` laden
* [ ] Agent `component-explore`, danach Agent `component-creator` verwenden

## Aufgabe 2: Komponente

* [ ] Komponente `ProjectTree` mit View und View-Model in `ui/component` anlegen
* [ ] Baum nur aus Ordnern aufbauen, niemals Dateien anzeigen
* [ ] Baum aus den Property-Modellen der Knoten aufbauen und bei Modelländerungen nachführen
* [ ] Hinter dem Ordnernamen grau und in Klammern die Anzahl der direkten Unterelemente, Unterordner mitgezählt
* [ ] Gewählten Ordner als schreibgeschützte Property bereitstellen
* [ ] Leeren Baum für ein neues Projekt ohne Fehler anzeigen
* [ ] Registrierungen an Modellen beim Verlassen des Bildschirms lösen

## Aufgabe 3: Regelwerk

* [ ] `.claude/skills/component/SKILL.md` auf `ProjectTree` als Beispielkomponente statt `FlowList` umstellen
* [ ] Agenten unter `.claude/agents` auf Verweise auf `FlowList` prüfen und anpassen

## Aufgabe 4: Tests und Doku

* [ ] Nachrichtenschlüssel in `bundle.properties` ergänzen, Übersetzung über Agent `translator`
* [ ] `module-info.java` öffnen, Tests: Ordner, Zähler, Änderungen im Modell, leerer Baum
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
