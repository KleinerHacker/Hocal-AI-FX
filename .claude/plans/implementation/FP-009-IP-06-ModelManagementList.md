# FP-009-IP-06: Modell-Verwaltungsliste

## Voraussetzungen

* IP-04, IP-05, FP-003 IP-02, FP-003 IP-04

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `dialog`, `ui-styling`, `fx-component-lifecycle` und `translation` laden
* [ ] Agenten `component-explore` und `dialog-explore` verwenden

## Aufgabe 2: Liste

* [ ] Eigenständige Komponente, später in einen Einstellungsdialog verschiebbar
* [ ] Menüeintrag im Hauptfenster öffnet sie, ohne Icon
* [ ] Zeile je Modell: Name, Größe, Lizenz- und Hugging-Face-Link, Zustand
* [ ] Zustände: nicht installiert, lädt mit Fortschritt, installiert
* [ ] Download-Taste bei „nicht installiert“, Löschen-Taste bei „installiert“

## Aufgabe 3: Download und Löschen

* [ ] Vor dem Download Dialog mit Name, Größe und Lizenz, der Benutzer stimmt zu
* [ ] Fortschrittsbalken mit Prozent und „Abbrechen“ in der Zeile, mehrere Downloads gleichzeitig
* [ ] Löschen mit Rückfrage, abgelehnt solange eine Erzeugung das Modell nutzt
* [ ] Nichts wird automatisch oder beim Start geladen

## Aufgabe 4: Anzeiger und Hinweise

* [ ] Anzeiger oben rechts am Ende der Menüleiste: Kreis mit Prozentzahl und Anzahl der Downloads, ausgeblendet ohne Download
* [ ] Gesamtfortschritt aus Summe der Bytes über Summe der Größen, Klick öffnet die Liste
* [ ] Nicht installiertes Modell in der ComboBox mit Hinweis, Senden dafür gesperrt
* [ ] Beim Beenden mit laufenden Downloads fragen: abbrechen und beenden oder warten

## Aufgabe 5: Texte, Tests und Doku

* [ ] Nachrichtenschlüssel anlegen, Übersetzung nur über den Agenten `translator`
* [ ] Tests: Zustände, Download und Löschen, Anzeiger, Beenden mit Downloads, Fehlerdialog
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
