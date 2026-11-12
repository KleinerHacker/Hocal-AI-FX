# FP-001-IP-07: Projekt-Manager (Singleton)

## Voraussetzungen

* IP-02, IP-03, IP-04, IP-05, IP-06

## Aufgabe 1: Grundgerüst

* [ ] Skills `fx-model`, `fx-component-lifecycle` und `testing` laden
* [ ] Kotlin-`object` `ProjectManager` in `lib/fx-model`, nur `javafx.base`
* [ ] Genau ein Projekt gleichzeitig geladen, abgebildet als `ProjectProperty`
* [ ] Schreibgeschützte Property `project` (nullable) für Bindings der Oberfläche
* [ ] Properties `dirty` und `path` des geladenen Projekts

## Aufgabe 2: Lebenszyklus

* [ ] Operationen: neu, öffnen mit optionalem Passwort, speichern, speichern unter, schließen
* [ ] Neu oder Öffnen bei geladenem Projekt nur nach Schließen
* [ ] Bei `dirty` Fehler, solange das Verwerfen nicht freigegeben ist
* [ ] Speichern nutzt Storage und Verschlüsselung, setzt Änderungszeitpunkt und Anwendungsversion

## Aufgabe 3: Änderungen

* [ ] Alle Änderungen laufen ausschließlich über den Manager
* [ ] Baum-Operationen aus IP-05 und Medien-Operationen aus IP-04 über den Manager anbieten
* [ ] Änderung am POJO, danach `refresh()` des Property-Modells und `dirty` setzen
* [ ] Kein Zugriff von außen auf Storage und Baum-Operationen
* [ ] Änderungen nur auf dem FX-Thread, Datei-Ein-/Ausgabe blockiert den Aufrufer

## Aufgabe 4: Tests und Doku

* [ ] Tests: nur ein Projekt zur Zeit, `dirty`-Verhalten, Änderungen über den Manager, Fehlerfälle
* [ ] Singleton-Zustand pro Test zurücksetzen, Hilfsmethode nur für Tests
* [ ] KDoc, README und MkDocs prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
