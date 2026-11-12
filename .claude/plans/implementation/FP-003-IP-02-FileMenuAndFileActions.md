# FP-003-IP-02: Dateimenü und Datei-Aktionen

## Voraussetzungen

* IP-01, FP-002 IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `ui-styling`, `window`, `dialog`, `fx-component-lifecycle` und `icons` laden
* [ ] Design-Vorlage `docs/docs/assets/images/file.png` vom Benutzer einholen, bevor ein Icon gezeichnet wird

## Aufgabe 2: Menü

* [ ] Einträge Neu (`Shortcut+N`), Öffnen (`Shortcut+O`), Speichern (`Shortcut+S`), Speichern unter (`Shortcut+Shift+S`), Beenden (`Shortcut+Q`)
* [ ] Kein Eintrag Schließen, kein eigener Eintrag für die Verschlüsselung
* [ ] Speichern deaktiviert, solange ein Pfad bekannt ist und keine Änderungen vorliegen
* [ ] Aktivierung und Fenstertitel an die Properties des `ProjectManager` binden

## Aufgabe 3: Aktionen

* [ ] Dateiauswahl mit Filter `.hai`, Endung bei Speichern unter anhängen, ohne Standardnamen und Startordner
* [ ] Rückfrage-Dialog bei Neu, Öffnen, Beenden und Schließen über das Fensterkreuz: Speichern, Verwerfen, Abbrechen
* [ ] Speichern in der Rückfrage startet den Speichern-Ablauf, Abbruch oder Fehler bricht die ursprüngliche Aktion ab
* [ ] Neu setzt über den Manager auf ein leeres Projekt zurück
* [ ] Öffnen ersetzt das Projekt erst nach erfolgreichem Laden
* [ ] Speichern ohne Pfad verhält sich wie Speichern unter
* [ ] Fenstertitel `Hocal AI - <Dateiname>`, bei Änderungen ein angehängtes `*`, ohne Datei „Unbenannt“

## Aufgabe 4: Icons

* [ ] `HocalIcons` im Wurzelpaket von `app/ui` mit dem ersten Icon anlegen
* [ ] Icons `new`, `open`, `save`, `save-as`, `exit` als `<name>@32.png` mit Agent `icon-creator` zeichnen lassen
* [ ] Wo der Kontrast es verlangt, dunkle Variante `<name>-dark@32.png`
* [ ] Variantenwahl über das Farbschema des Betriebssystems, Vorschlag laut offener Frage bestätigen
* [ ] Alle Menüeinträge über `HocalIcons` mit Icon versehen

## Aufgabe 5: Tests und Doku

* [ ] Nachrichtenschlüssel ergänzen, Übersetzung über Agent `translator`
* [ ] `MainWindowIT`: Menüeinträge, Aktivierung, Titel, Rückfrage beim Schließen
* [ ] README, MkDocs-Seite zum Dateimenü und KDoc prüfen, CHANGELOG-Eintrag ergänzen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
