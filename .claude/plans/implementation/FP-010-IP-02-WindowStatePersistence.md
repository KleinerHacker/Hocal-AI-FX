# FP-010-IP-02: Fensterzustand

## Voraussetzungen

* IP-01, FP-002 IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `window`, `fx-component-lifecycle`, `ui-styling` und `testing` laden

## Aufgabe 2: Beobachten und Speichern

* [ ] `WindowStateKeeper` beobachtet Position, Größe und Maximiert-Zustand des Hauptfensters
* [ ] Teilerpositionen der drei Splitter beobachten: außen, innen vertikal, zwei im Inhaltsbereich
* [ ] Beim Maximieren die normalen Maße speichern, damit Wiederherstellen stimmt
* [ ] Schreiben verzögert und beim Schließen des Fensters
* [ ] Listener beim Schließen wieder abmelden

## Aufgabe 3: Wiederherstellen

* [ ] Beim Start vor dem Anzeigen anwenden, Teilerpositionen erst setzen, wenn die Szene eine Größe hat
* [ ] Position außerhalb aller Bildschirme auf den Hauptbildschirm korrigieren
* [ ] Größe auf den Bildschirm begrenzen, mehrere Monitore und gemischte Skalierung beachten
* [ ] Ohne gespeicherte Werte gelten die Vorgaben aus FP-002

## Aufgabe 4: Tests und Doku

* [ ] Korrektur von Positionen außerhalb als reine Logik testen
* [ ] TestFX: Teilerpositionen werden wiederhergestellt
* [ ] KDoc prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
