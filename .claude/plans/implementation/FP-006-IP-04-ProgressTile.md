# FP-006-IP-04: Fortschrittskachel

## Voraussetzungen

* IP-02, FP-002 IP-01, FP-002 IP-04

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `ui-styling`, `fx-component-lifecycle`, `icons` und `translation` laden
* [ ] Agenten `component-explore` und `component-creator` verwenden

## Aufgabe 2: Kachel

* [ ] `GenerationTile`: Vorschaubild links, daneben Prozentanzeige, am unteren Rand ein Fortschrittsbalken
* [ ] Platzhalter bis zum ersten Vorschaubild, am Ende das fertige Bild
* [ ] Zustände: startet, läuft, fertig, fehlgeschlagen, abgebrochen
* [ ] Fertige Kachel schließt nach einer Verzögerung, fehlgeschlagene bleibt bis zum Schließen

## Aufgabe 3: Bedienung

* [ ] Schaltfläche „x“: bricht einen laufenden Auftrag ab, sonst schließt sie nur die Kachel
* [ ] Klick auf die Kachel wählt Ordner, Datei und neues Medium in Baum, Dateiliste und Historienliste
* [ ] Kachel nimmt keinen Fokus und blockiert das Fenster nicht

## Aufgabe 4: Overlay und Warteschlange

* [ ] Overlay-Ebene im Hauptfenster unten rechts als Kachel-Host
* [ ] Genau ein wartender Auftrag als kompakte Zeile: Flow, Modell, Prompt-Anfang, „x“
* [ ] Ab zwei wartenden Aufträgen eine eingeklappte Zeile „N wartend“, Klick klappt die Liste auf und zu
* [ ] „x“ in einer Zeile entfernt den wartenden Auftrag aus der Warteschlange

## Aufgabe 5: Aktualisierung, Tests und Doku

* [ ] Vorschau- und Fortschrittsupdates bündeln, Übernahme auf den FX-Thread über `Platform.runLater`
* [ ] Nachrichtenschlüssel anlegen, Übersetzung nur über den Agenten `translator`
* [ ] Tests: Zustände, „x“, Klick-Auswahl, Einklappen ab zwei, Verzögerung
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
