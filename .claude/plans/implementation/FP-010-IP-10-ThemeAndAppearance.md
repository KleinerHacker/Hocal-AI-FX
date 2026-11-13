# FP-010-IP-10: Theme und Erscheinungsbild

## Voraussetzungen

* FP-002 IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `ui-styling`, `icons`, `testing` und `project-docs` laden
* [ ] Design-Vorlage `docs/docs/assets/images/file.png` vom Benutzer anfordern, sonst neutrale Palette

## Aufgabe 2: Theme

* [ ] Basis-Stylesheet mit benannten Farben, Schriften und Größen, auf den Standard-Skins aufgebaut
* [ ] Farbschemata hell und dunkel
* [ ] Alle verwendeten Controls, Menüs, Dialoge, Splitter, Listen, Modellliste, Kachel und Splash gestalten
* [ ] Kontrast von Text und Icons in beiden Schemata prüfen

## Aufgabe 3: Umschaltung

* [ ] `AppearanceManager`: wendet das Schema auf alle Szenen an, über eine zentrale Stelle
* [ ] Systemvorgabe folgt dem Farbschema des Betriebssystems, auch bei Änderung zur Laufzeit
* [ ] Prüfen, wie die JavaFX-Version das Schema liefert (Plattform-Einstellungen)
* [ ] Neue Fenster nehmen automatisch teil

## Aufgabe 4: Icons und Regeln

* [ ] Icon-Varianten `<name>-dark@32.png` folgen dem Schema, fehlende über `icon-creator` erstellen
* [ ] `HocalIcons` wählt die Variante
* [ ] Skill `ui-styling` um die Theme-Regeln erweitern

## Aufgabe 5: Tests

* [ ] Auswahllogik testen
* [ ] TestFX: Szene trägt das Stylesheet, Wechsel wirkt sofort

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
