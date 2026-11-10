# FP-006-IP-03: Sende-Taste und Prompt-Absenden

## Voraussetzungen

* IP-02, FP-002 IP-06

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `ui-styling`, `fx-component-lifecycle`, `icons` und `translation` laden
* [ ] Agent `component-explore` verwenden

## Aufgabe 2: Sende-Taste

* [ ] Sende-Taste hinter der Textbox im `PromptInput`, Tastenkürzel `Ctrl+Enter`
* [ ] Aktiv nur bei nicht leerem Prompt, gewählter Referenz und geöffnetem Projekt
* [ ] Sperre nicht während laufender Aufträge, Senden bleibt möglich
* [ ] Icon über den Agenten `icon-creator`, Registrierung in `HocalIcons`

## Aufgabe 3: Auswahl

* [ ] ComboBox zeigt die echten Modelle aus IP-01
* [ ] Toggle-Button-Leiste aus FP-002 IP-06 trägt die echten Flows, der Platzhalter entfällt

## Aufgabe 4: Absenden

* [ ] Daten sammeln: Prompt, Modell, Flow, Seed, Parameterwerte, im Hauptfenster angezeigtes Medium, weitere Galerie-Bilder
* [ ] Daten an den `GenerationService` übergeben, Textbox bleibt gefüllt
* [ ] Fehlerdialoge: Dienst nicht verfügbar, Workflow abgelehnt, keine Verbindung

## Aufgabe 5: Texte, Tests und Doku

* [ ] Nachrichtenschlüssel anlegen, Übersetzung nur über den Agenten `translator`
* [ ] Tests: Aktivierungsregeln, Datensammlung, Tastenkürzel, Fehlerdialog
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
