# FP-002-IP-01: Hauptfenster-Splitter-Layout

## Voraussetzungen

* Keine

## Aufgabe 1: Vorbereitung

* [ ] Skills `ui-styling`, `window` und `component` laden
* [ ] `MainWindowView.fxml`, `MainWindowView` und `MainWindowViewModel` lesen

## Aufgabe 2: Layout

* [ ] Äußeren horizontalen `SplitPane` mit Teiler 0,3 anlegen
* [ ] Linke Seite als vertikalen `SplitPane` mit Teiler 0,5 anlegen, oben Baum, unten Dateiliste
* [ ] Inhaltsbereich als horizontalen `SplitPane` mit drei Teilen und den Teilern 0,2 und 0,8 anlegen
* [ ] Links 0,2 Generierungsdaten, Mitte 0,6 Bild mit Eingabeteil, rechts 0,2 Historienliste, zunächst Platzhalter
* [ ] Menüleiste und Fenstergröße 1280 x 800 beibehalten
* [ ] Teilerpositionen erst setzen, wenn die Scene eine Größe hat

## Aufgabe 3: Demo entfernen

* [ ] `FlowList` mit View, View-Model und FXML entfernen
* [ ] Beispiel-Flows, ausgewählten Flow und Auswahl-Label aus dem Hauptfenster entfernen
* [ ] Zugehörige Nachrichtenschlüssel in `bundle.properties` und `bundle_de.properties` entfernen, Skill `translation` laden
* [ ] `module-info.java` und zugehörige Tests anpassen

## Aufgabe 4: Tests und Doku

* [ ] `MainWindowIT`: Teilerpositionen und Größenverhalten beim Skalieren prüfen
* [ ] KDoc, README und MkDocs-Seite des Hauptfensters prüfen, CHANGELOG nur bei sichtbarer Änderung

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
