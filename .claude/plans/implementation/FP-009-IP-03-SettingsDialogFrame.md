# FP-009-IP-03: Rahmen des Einstellungsdialogs

## Voraussetzungen

* IP-01, FP-003 IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `dialog`, `ui-styling`, `icons`, `translation` und `testing` laden
* [ ] Agents `dialog-explore` und `dialog-creator` verwenden

## Aufgabe 2: Menü

* [ ] Eintrag `Datei -> Einstellungen` mit Tastenkürzel `Ctrl+,`
* [ ] Icon `settings@32.png` samt Variante über den Agenten `icon-creator`, Design-Vorlage `file.png` beachten

## Aufgabe 3: Dialog

* [ ] Modaler Dialog: Kategorieliste links, Seite rechts, Kategorien aus einer Liste von Seiten
* [ ] Buttons OK, Abbrechen, Übernehmen und „Standard wiederherstellen“ je Kategorie mit Rückfrage
* [ ] Bearbeitung auf einer Kopie der Einstellungen, OK und Übernehmen schreiben sie als eine Änderung zurück
* [ ] Seitenvertrag: Felder, Gültigkeit, Markierung „Neustart nötig“, Dialog-Größe wird nicht gespeichert
* [ ] Ungültige Eingabe am Feld anzeigen, OK und Übernehmen sperren
* [ ] Dialog auf einer bestimmten Kategorie öffnen, genutzt vom Download-Anzeiger

## Aufgabe 4: Texte und Tests

* [ ] Bundle-Schlüssel auf Englisch anlegen, Deutsch über den Agenten `translator`
* [ ] TestFX: Öffnen, Abbrechen, Übernehmen, Sperren bei ungültiger Eingabe

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
