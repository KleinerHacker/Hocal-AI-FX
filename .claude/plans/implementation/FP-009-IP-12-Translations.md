# FP-009-IP-12: Übersetzungen

## Voraussetzungen

* IP-11

## Aufgabe 1: Vorbereitung

* [ ] Skill `translation` laden, Agent `translator` verwenden, immer je Schlüssel mit Kontext und englischem Text
* [ ] Glossar der Fachbegriffe je Sprache führen: Modell, Flow, Prompt, Historie, Galerie, Projekt
* [ ] Deutsche Datei dient als Referenz für Begriffe

## Aufgabe 2: Welle 1

* [ ] de, fr, es, it, pt (brasilianisch), nl, pl, ru, uk, tr
* [ ] Je Sprache eine Datei, Schlüsselprüfung aus IP-11 nach jeder Sprache

## Aufgabe 3: Welle 2

* [ ] sv, da, nb, fi, cs, sk, hu, ro, bg, el, hr, sl, lt, lv, et, ca
* [ ] sr (kyrillisch) und sr_Latn (lateinisch) als zwei Dateien

## Aufgabe 4: Welle 3

* [ ] zh-Hans, zh-Hant, ja, ko, hi, bn, th, vi, id, ms
* [ ] Rechts-nach-links: ar, he, fa, erst nach Layout- und Schriftprüfung aus IP-11

## Aufgabe 5: Abschluss der Wellen

* [ ] Registry-Einträge je fertige Sprache, jede Welle einzeln auslieferbar
* [ ] README-Hinweis: maschinelle Übersetzung, Prüfung durch Muttersprachler willkommen
* [ ] Spätere Schlüssel anderer Pläne gehen an alle vorhandenen Sprachen, Build verlangt Vollständigkeit

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
