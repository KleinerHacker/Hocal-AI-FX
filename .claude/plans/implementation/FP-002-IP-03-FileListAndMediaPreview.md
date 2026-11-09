# FP-002-IP-03: Dateiliste und Medien-Vorschau

## Voraussetzungen

* IP-01, FP-001 IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `fx-component-lifecycle`, `ui-styling` und `fx-model` laden
* [ ] Agent `component-explore`, danach Agent `component-creator` verwenden

## Aufgabe 2: Medien-Vorschau

* [ ] Schnittstelle `MediaPreview` anlegen, erste Umsetzung `ImageMediaPreview` mit Standard-JavaFX-`Image`
* [ ] Nur Bildformate von Standard-JavaFX unterstützen, kein weiterer Decoder
* [ ] Registry im Code wählt den Provider nach Medientyp, neutrales Platzhalter-Icon als Rückfall
* [ ] Medienquelle als Schnittstelle: je Eintrag Original und aktuelles Medium, kein Dateisystem
* [ ] Vorschau lazy und außerhalb des FX-Threads laden, Laden bei Ordnerwechsel abbrechen
* [ ] Fehlerhaftes Bild darf die Liste nie stören

## Aufgabe 3: Dateiliste

* [ ] Komponente `ProjectFileList` mit View und View-Model anlegen
* [ ] Nur Dateien (Einträge) des gewählten Ordners anzeigen, niemals Ordner
* [ ] Eine Zeile pro Eintrag: Name und zwei Vorschaubilder, zuerst Original, direkt dahinter das aktuelle Medium
* [ ] Bei nur einem Medium dasselbe Icon doppelt, Tooltip nennt Original und aktuelles Medium
* [ ] OFFEN: Größe des Vorschaubilds in der Zeile, vor der Umsetzung festlegen
* [ ] Gewählte Datei als schreibgeschützte Property bereitstellen

## Aufgabe 4: Tests und Doku

* [ ] Nachrichtenschlüssel ergänzen, Übersetzung über Agent `translator`
* [ ] Tests: Provider-Auswahl, Platzhalter, defektes Bild, Zeilenaufbau, einzelnes Medium
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
