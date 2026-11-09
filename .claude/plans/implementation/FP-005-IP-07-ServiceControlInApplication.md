# FP-005-IP-07: Dienststeuerung in der Anwendung

## Voraussetzungen

* IP-05
* Menüstruktur aus FP-003 IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `ui-styling`, `icons`, `translation` und `fx-component-lifecycle` laden

## Aufgabe 2: Menü und Anzeige

* [ ] Menüeintrag zum Neustart von ComfyUI mit Tastenkürzel
* [ ] Icon über den Agenten `icon-creator`, Registrierung in `HocalIcons`
* [ ] Zustandsanzeige: gestoppt, startet, läuft, fehlgeschlagen
* [ ] Dienstzustand im View Model als Property abbilden

## Aufgabe 3: Neustart

* [ ] Rückfrage vor dem Neustart, wenn eine Erzeugung läuft
* [ ] Neustart im Hintergrund, Menüeintrag währenddessen gesperrt

## Aufgabe 4: Beenden

* [ ] Dienst beenden bei Beenden, Fensterkreuz und schwerem Fehler
* [ ] Beenden blockiert das Fenster nicht, mit Zeitlimit und danach erzwungen

## Aufgabe 5: Texte, Tests und Doku

* [ ] Nachrichtenschlüssel anlegen, Übersetzung nur über den Agenten `translator`
* [ ] Tests: Zustandsanzeige, Neustart mit Rückfrage, Beenden beim Schließen
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
