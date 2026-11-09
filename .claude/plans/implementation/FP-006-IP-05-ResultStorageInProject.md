# FP-006-IP-05: Ergebnis im Projekt speichern

## Voraussetzungen

* IP-02, FP-001 IP-07, FP-002 IP-05

## Aufgabe 1: Vorbereitung

* [ ] Skills `fx-model`, `fx-component-lifecycle` und `testing` laden

## Aufgabe 2: Speichern

* [ ] Bei Auftragsende Ergebnis als neuestes Medium der beim Senden festgehaltenen Referenz anhängen
* [ ] Über den `ProjectManager`, mit Historiendetails Prompt, Modell und Flow
* [ ] Zusätzliche Bilder nicht speichern, Dateiendung folgt dem Ergebnis von ComfyUI
* [ ] Projekt wird `dirty`
* [ ] Überlauf über `999` als typisierten Fehler dem Benutzer anzeigen

## Aufgabe 3: Auswahl

* [ ] Historienliste springt auf das neue Medium, nur wenn dieselbe Referenz noch angezeigt wird
* [ ] Dateiliste zeigt das neue Medium als aktuelles Vorschaubild

## Aufgabe 4: Projektwechsel

* [ ] Schließen, Ersetzen oder Öffnen eines Projekts bei laufenden oder wartenden Aufträgen: einmal für alle fragen
* [ ] Antwort warten oder verwerfen, Verwerfen bricht alle Aufträge ab
* [ ] Ergebnis für eine nicht mehr vorhandene Referenz verwerfen und melden

## Aufgabe 5: Texte, Tests und Doku

* [ ] Nachrichtenschlüssel anlegen, Übersetzung nur über den Agenten `translator`
* [ ] Tests: Anhängen, Historiendetails, Auswahlsprung, Projektwechsel, Überlauf, fehlende Referenz
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
