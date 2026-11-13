# FP-002-IP-04: Projekt-Anbindung und Fenster-Verdrahtung

## Voraussetzungen

* IP-02, IP-03, FP-001 IP-07

## Aufgabe 1: Anbindung

* [ ] `MainWindowViewModel` folgt dem Projekt des `ProjectManager`
* [ ] Neues Projekt entsteht über den `ProjectManager`, das Fenster legt es nicht selbst an
* [ ] Anfangszustand: neues Projekt, leerer Baum, leere Dateiliste, leerer Inhaltsbereich

## Aufgabe 2: Verdrahtung

* [ ] Gewählter Ordner des Baums steuert die Dateiliste
* [ ] Gewählte Datei der Liste wird an den Inhaltsbereich weitergereicht
* [ ] `MediaSource` über den `ProjectManager` bereitstellen (`mediaCount`, `extension`, `openMedium`) und an die Dateiliste binden
* [ ] Bei Medienänderung (anhängen, entfernen) `thumbnails.invalidate(reference)` der Dateiliste aufrufen, sonst bleibt bei gleicher Medienanzahl ein veraltetes Bild
* [ ] Dateiliste muss Änderungen im gewählten Ordner bemerken; `selectedFolder` des Baums meldet keine inneren Änderungen
* [ ] `PromptInput`: `folderProperty` und `mediaSourceProperty` an Projekt und Medienquelle binden; Chips bei Medienänderung neu laden
* [ ] Registrierungen am `ProjectManager` beim Schließen des Fensters lösen, Skill `fx-component-lifecycle`

## Aufgabe 3: Tests und Doku

* [ ] `MainWindowIT`: neues Projekt zeigt leeren Baum und leere Liste ohne Fehler
* [ ] `MainWindowIT`: gefülltes Modell zeigt Ordner, Dateien und Icons
* [ ] README, MkDocs-Seite des Hauptfensters und KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
