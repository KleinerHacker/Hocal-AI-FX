# FP-001-ManagerOnlyAccess: Zugriff nur über den Projekt-Manager

## Voraussetzungen

* FP-001 abgeschlossen, GitHub-Build grün

## Aufgabe 1: Pakete festlegen

* [ ] Öffentlich bleiben: `model.project` (POJOs), `model.tree` (`TreeNames`, `TreeException` samt Unterklassen), `model.storage` (nur Ausnahmen)
* [ ] `MediaHistoryException` samt Unterklassen aus `MediaHistory.kt` in eine eigene Datei im Paket `model.storage` auslagern
* [ ] Neues geschütztes Paket `model.tree.internal` für `TreeOperations.kt`
* [ ] Neues geschütztes Paket `model.storage.internal` für `ProjectStorage`, Verschlüsselung, Ströme, `ProjectValidator` und `StorageLimits`
* [ ] Ebenfalls dorthin: `MediaStore`, `MediaSource`, `MediaSink`, `DirectoryMediaStore` und die Erweiterungsfunktionen aus `MediaHistory.kt`
* [ ] Kotlin-Sichtbarkeit bleibt `public`, weil `internal` nicht über Gradle-Module reicht, der Schutz kommt vom Java-Modul

## Aufgabe 2: Dateien verschieben

* [ ] Dateien per `git mv` in die geschützten Pakete, Paketzeilen anpassen, genau eine Paketzeile je Datei prüfen
* [ ] Importe der öffentlichen Ausnahmen und von `TreeNames` in den verschobenen Dateien ergänzen
* [ ] Jackson-Hilfsklassen `FormatProbe` und `ProjectProbe` liegen danach in `storage.internal`
* [ ] KDoc-Verweise auf verschobene Klassen nachziehen
* [ ] Reihenfolge je Datei: alle Änderungen sammeln, Datei genau einmal schreiben

## Aufgabe 3: Modul-Descriptor

* [ ] `module-info.java` von `lib/model`: `exports …tree.internal to org.pcsoft.app.hocal.fx.model`
* [ ] Ebenso `exports …storage.internal to org.pcsoft.app.hocal.fx.model`
* [ ] `exports` von `project`, `tree` und `storage` bleiben ohne Ziel
* [ ] `opens …storage to tools.jackson.databind` ersetzen durch `opens …storage.internal to tools.jackson.databind`
* [ ] Kommentar im Descriptor: nur der Projekt-Manager darf die geschützten Pakete benutzen
* [ ] Kein `-Werror` im Projekt, eine Warnung wegen des noch unbekannten Zielmoduls beim Kompilieren von `lib/model` ist unkritisch

## Aufgabe 4: Projekt-Manager

* [ ] Importe in `ProjectManager` auf `storage.internal` und `tree.internal` umstellen
* [ ] Öffentliche Signaturen des Managers dürfen keinen Typ aus einem geschützten Paket enthalten, prüfen
* [ ] KDoc des Managers: Operationen sind nur über ihn erreichbar, Verweis auf den Modul-Schutz
* [ ] `ProjectManagerTest`: Importe der Ausnahmen aus den öffentlichen Paketen unverändert, restliche Importe anpassen

## Aufgabe 5: Tests

* [ ] Tests spiegeln die Pakete: Tests der verschobenen Klassen nach `storage.internal` und `tree.internal`, `TreeNamesTest` bleibt in `tree`
* [ ] Hilfsklassen `StorageFixtures` und `MemoryMedia` ziehen mit, Ressourcen der festen Dateien bleiben, Ressourcenpfade unverändert
* [ ] Neuer Test `ModuleEncapsulationTest`: liest `module-info.class` von `hocal-model` über den Klassenlader und wertet `ModuleDescriptor` aus
* [ ] Der Test prüft: `tree.internal` und `storage.internal` nur an `org.pcsoft.app.hocal.fx.model` exportiert
* [ ] Der Test prüft außerdem: `project`, `tree` und `storage` sind für alle exportiert
* [ ] Tests laufen weiter auf dem Klassenpfad, der Schutz selbst wirkt nur auf dem Modulpfad, in der KDoc des Tests vermerkt

## Aufgabe 6: Regeln und Doku

* [ ] `architecture.md`: Regel, dass Operationen in `…internal`-Paketen nur an `hocal-fx-model` exportiert werden
* [ ] Skill `fx-model`: Abschnitt „Visibility“ um die geschützten Pakete und den Weg über den `ProjectManager` ergänzen
* [ ] Feature-Plan FP-001: Paketnamen in „As Built“ von IP-03, IP-04, IP-05 und IP-07 nachziehen
* [ ] README und MkDocs prüfen, CHANGELOG unverändert

## Abschluss

* [ ] Kein lokaler Build möglich, pushen und auf den GitHub-Build warten
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan im selben Änderungssatz anpassen
