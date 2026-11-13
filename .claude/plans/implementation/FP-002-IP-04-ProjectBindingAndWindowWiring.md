# FP-002-IP-04: Projekt-Anbindung und Fenster-Verdrahtung

## Voraussetzungen

* IP-02, IP-03, IP-06, FP-001 IP-07

## Aufgabe 1: Vorbereitung

* [ ] Skills `window`, `component`, `fx-component-lifecycle`, `ui-styling`, `testing`, `translation`, `project-docs` laden
* [ ] Fortschritt in `.claude/plans/implementation/FP-002-IP-04-ProjectBindingAndWindowWiring.md` abhaken
* [ ] Entschieden: Das neue Projekt entsteht beim Anwendungsstart über den `ProjectManager`, nicht im Fenster
* [ ] Hinweis: IP-05 und IP-07 hängen an IP-04; bisher war IP-05 irrtümlich als frei genannt

## Aufgabe 2: Projektstart (`HocalApplication`)

* [ ] `ProjectManager.threadCheck` auf `Platform.isFxApplicationThread()` setzen
* [ ] `ProjectManager.applicationVersion` aus der Version des Pakets setzen, sonst `dev`
* [ ] Vor dem Hauptfenster `ProjectManager.createNew(Messages["project.new.name"])` aufrufen
* [ ] `stop()` ruft `ProjectManager.close(discardChanges = true)`; das temporäre Verzeichnis wird entfernt

## Aufgabe 3: Medienquelle und Medienänderungen

* [ ] `ProjectMediaSource` (`MediaSource`) über `ProjectManager`: `mediaCount`, `extension`, `open`; `ui.preview` oder `ui`
* [ ] Alle Zugriffe auf den `ProjectManager` laufen im FX-Thread; Aufrufe aus Ladethreads werden dorthin übergeben und abgewartet
* [ ] Unbekannte Referenz liefert Anzahl 0; `extension` und `open` werfen wie die Quelle, die Registry fängt es ab
* [ ] `MediaChanges` (Objekt): `changed(reference)` und `subscribe(listener)`; Aufrufer nach `appendMedium` und `removeMedium` kommt mit FP-006
* [ ] Beschreibung in KDoc: Wer Medien ändert, ruft `MediaChanges.changed(reference)` auf

## Aufgabe 4: Verdrahtung im Hauptfenster

* [ ] `MainWindowView.fxml`: Platzhalter `pnlTree` und `pnlFiles` durch `ProjectTree` und `ProjectFileList` ersetzen, Ids bleiben
* [ ] `MainWindowViewModel`: Property für den Wurzelordner `ProjectManager.project.treeProperty`, `selectedFolder`, `listFolder`, `selectedFile`
* [ ] `ProjectTree.folderProperty` und `PromptInput.folderProperty` an den Wurzelordner binden
* [ ] `selectedFolder` folgt dem Baum; `listFolder` folgt `selectedFolder` und meldet zusätzlich jede Änderung des Projekts
* [ ] `ProjectFileList.folderProperty` an `listFolder` binden; `selectedFile` folgt der Liste, IP-05 liest sie
* [ ] `mediaSourceProperty` von Dateiliste und `PromptInput` auf `ProjectMediaSource` setzen
* [ ] `ProjectFileList.invalidate(reference)` ergänzen; Fenster ruft es und `PromptInput.invalidate` bei `MediaChanges` auf
* [ ] Registrierungen am `ProjectManager` und an `MediaChanges` nur zwischen `onShow` und `onHide`, per `showingBinding()`

## Aufgabe 5: Texte und Doku

* [ ] Schlüssel `project.new.name` in `bundle.properties`; Übersetzung über Agent `translator`
* [ ] README, MkDocs-Hauptfensterseite, KDoc ergänzen; CHANGELOG: Baum und Liste zeigen das neue Projekt
* [ ] Plan-Datei IP-04 aus diesem Plan aktualisieren; IP-05 und IP-07 bleiben abhängig

## Aufgabe 6: Tests

* [ ] `ProjectMediaSourceTest`: Anzahl, Endung, Inhalt, unbekannte Referenz, Aufruf aus Fremdthread bei FX-Thread-Prüfung
* [ ] `MediaChangesTest`: Benachrichtigung, Abmelden, mehrere Abonnenten
* [ ] `MainWindowViewModelTest`: `listFolder` meldet Projektänderung, folgt der Auswahl, Abmelden bei `onHide`
* [ ] `MainWindowIT`: neues Projekt zeigt leeren Baum und leere Liste ohne Fehler
* [ ] `MainWindowIT`: gefülltes Modell zeigt Ordner, Dateien und Vorschau-Icons; Ordnerwahl ändert die Liste
* [ ] `MainWindowIT`: Änderung im gewählten Ordner erscheint in der Liste; Eingabeteil erhält Ordner und Quelle
* [ ] Testzustand des `ProjectManager` nach jedem Test mit `close(discardChanges = true)` zurücksetzen

## Abschluss

* [ ] Änderungen pushen, CI von PR #17 abwarten und Ergebnis prüfen; Fehler beheben
* [ ] Skill `project-docs`: KDoc, README, MkDocs, CHANGELOG prüfen
* [ ] Plan-Datei per `git rm` entfernen; Feature-Plan und Feature-Status im selben Änderungssatz abhaken
