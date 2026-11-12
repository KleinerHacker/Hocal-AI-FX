# FP-009-IP-11: Sprachunterstützung

## Voraussetzungen

* IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `ui-styling`, `translation`, `testing`, `ci-pipeline` und `project-docs` laden

## Aufgabe 2: Registry und Laden

* [ ] `LanguageRegistry`: Code, eigener Name, Schreibrichtung und Rückfall je Sprache
* [ ] Sprachwahl beim Start aus der Einstellung, Systemvorgabe auf beste Übereinstimmung abbilden
* [ ] Dateinamen für Schrift und Region prüfen: `zh_Hans`, `zh_Hant`, `sr_Latn`, `nb`, `he`, `id`
* [ ] Rückfall auf Englisch bei fehlendem Schlüssel, Locale-Daten im Laufzeit-Image (`jdk.localedata`)

## Aufgabe 3: Prüfung im Build

* [ ] Build-Task prüft Schlüsselmenge und Platzhalter aller Bundles gegen die Basis
* [ ] Registry listet nur vollständige Sprachen

## Aufgabe 4: Layout und Schrift

* [ ] Schreibrichtung über `NodeOrientation`, Layout vollständig spiegeln, auch Reihenfolge der Splitter-Teile
* [ ] Richtungs-Icons spiegeln, feste Links/Rechts-Annahmen in FXML und CSS beseitigen
* [ ] Systemschriften für CJK, Arabisch, Hebräisch, Devanagari, Bengali, Thai prüfen, Zeilenhöhe und Abschneiden
* [ ] Eingabemethode (IME) im Prompt-Feld für Chinesisch, Japanisch, Koreanisch prüfen
* [ ] Zahlen-, Datums- und Größenformate folgen der Sprache

## Aufgabe 5: Regeln und Agent

* [ ] Regel gegen Sätze mit Mehrzahlformen in `translation` und `ui-styling` aufnehmen, Beispiel „Wartend: {0}“
* [ ] Skill `translation` und Agent `translator` sprachneutral fassen, bisher auf Deutsch und „Sie“ zugeschnitten
* [ ] Bestätigung neuer Sprachen durch diesen Plan in `ui-styling` vermerken

## Aufgabe 6: Tests

* [ ] Tests: Registry, Sprachauflösung, Schlüsselprüfung
* [ ] TestFX: Ausrichtung bei Rechts-nach-links

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
