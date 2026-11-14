# FP-002-IP-05: Historienliste und Bildansicht

## Voraussetzungen

* IP-04, FP-001 IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `fx-component-lifecycle`, `ui-styling`, `testing`, `project-docs`, `fx-model` laden
* [ ] Agent `component-explore`, danach `component-creator` für beide Komponenten verwenden
* [ ] Fortschritt in `.claude/plans/implementation/FP-002-IP-05-HistoryListAndMediaView.md` abhaken
* [ ] Entschieden: Eintrag der Historie zeigt Vorschau-Icon und Nummer `000` bis `NNN`; die Bildansicht passt das Bild dem Platz an

## Aufgabe 2: Gemeinsame Bausteine

* [ ] `ThumbnailLoader.load(source, reference, number, size, onLoaded)` ergänzen; `loadCurrent` ruft es mit der letzten Nummer
* [ ] `AppearanceObserver` in `ui`: Property `dark`, `start()` und `stop()` für Hell/Dunkel nach dem Systemerscheinungsbild
* [ ] Neue Komponenten nutzen `AppearanceObserver`; Umstellung der bestehenden Komponenten bleibt offen
* [ ] `RefreshableFolderProperty` zu `RefreshableObjectProperty<T>` verallgemeinern; `MainWindowViewModel` und Tests anpassen

## Aufgabe 3: Historienliste (Paket `ui.component`)

* [ ] `HistoryMedium` (Referenz-UUID, Nummer) und `HistoryList` (`ListView`, `fx:root`) mit View, ViewModel, Zelle, FXML und CSS anlegen
* [ ] Eingaben: `fileProperty()` (gewählter `VirtualEntry`) und `mediaSourceProperty()`, vom Aufrufer gebunden; Ausgabe `selectedMediumProperty()`
* [ ] Alle Medien von `000` bis zum letzten in der Reihenfolge der Nummer; Anzahl aus `MediaSource.mediaCount`
* [ ] Das letzte Medium bei jeder Dateiwahl vorwählen und wenn die Historie wächst; sonst Auswahl des Nutzers behalten
* [ ] Zelle: Vorschau-Icon über `ThumbnailLoader`, Nummer dreistellig; Platzhalter-Icon hell/dunkel, solange nichts geladen ist
* [ ] Keine Datei oder Quelle: leere Liste, `selectedMedium` leer; `invalidate(reference)` lädt Vorschauen neu
* [ ] Lebenszyklus: Systemerscheinungsbild nur zwischen `onShow` und `onHide` beobachten; Identität statt Wertgleichheit

## Aufgabe 4: Bildansicht (Paket `ui.component`)

* [ ] `MediaView` mit View, ViewModel, FXML und CSS; Eingaben `mediumProperty()` und `mediaSourceProperty()`
* [ ] `ImageView` passt das Bild mit erhaltenem Seitenverhältnis an den verfügbaren Platz an
* [ ] Bild lazy und außerhalb des FX-Threads laden, höchstens 1024 px Kantenlänge; eigener kleiner Cache für 8 Bilder
* [ ] Nicht darstellbares Medium zeigt das Platzhalter-Icon; keine Auswahl zeigt eine leere Fläche
* [ ] Wechsel des Mediums bricht laufendes Laden ab; verspätete Ergebnisse werden nicht angezeigt
* [ ] `invalidate(reference)` lädt das Bild neu, wenn dessen Referenz betroffen ist

## Aufgabe 5: Fenster

* [ ] `MainWindowView.fxml`: Platzhalter `pnlHistory` und `pnlImage` durch `HistoryList` und `MediaView` ersetzen, Ids bleiben
* [ ] `MainWindowViewModel`: `historyFile` (gewählte Datei, bei Projektänderung erneut gemeldet) und `selectedMedium` ergänzen
* [ ] View bindet `HistoryList.fileProperty` an `historyFile`, `MediaView.mediumProperty` an `selectedMedium`, beide Quellen an `ProjectMediaSource`
* [ ] Bei `MediaChanges` rufen Fenster beide Komponenten mit `invalidate(reference)` auf
* [ ] `selectedMedium` steht für IP-07 (Generierungsdetails) bereit

## Aufgabe 6: Texte und Doku

* [ ] Keine neuen Nachrichtenschlüssel nötig: Nummern und Icons tragen keinen Text; sonst Übersetzung über Agent `translator`
* [ ] README, MkDocs-Hauptfensterseite und KDoc ergänzen; CHANGELOG: Historie und Bild sind sichtbar

## Aufgabe 7: Tests

* [ ] `ThumbnailLoaderTest`: `load` mit Nummer, Cache pro Nummer, Abbruch
* [ ] `HistoryListViewModelTest`: Reihenfolge, Vorauswahl des letzten, Wachsen der Historie, Wechsel der Datei, leere Eingaben
* [ ] `HistoryListTest` (TestFX): Zeilen mit Nummer und Vorschau, Auswahl per Klick, Auswahl bleibt bei unveränderter Historie
* [ ] `MediaViewViewModelTest` und `MediaViewTest`: Laden, Platzhalter, Abbruch bei Wechsel, Anpassung an die Größe
* [ ] `AppearanceObserverTest`: Start, Stopp, doppelter Start; `MainWindowViewModelTest`: `historyFile` meldet Projektänderung
* [ ] `MainWindowIT`: Datei wählen zeigt Historie und Bild; neues Medium wählt das letzte; Medienwechsel lädt das Bild neu

## Abschluss

* [ ] Änderungen pushen, CI von PR #17 abwarten und Ergebnis prüfen; Fehler beheben
* [ ] Skill `project-docs`: KDoc, README, MkDocs, CHANGELOG prüfen
* [ ] Plan-Datei per `git rm` entfernen; Feature-Plan und Feature-Status im selben Änderungssatz abhaken
