# FP-007-IP-02: Textanalyse

## Voraussetzungen

* IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skill `testing` laden
* [ ] Format der Lexikon-Dateien festlegen und in KDoc beschreiben

## Aufgabe 2: Normalisierung

* [ ] Paket `text` in `lib/guard` anlegen
* [ ] Abstände, Leetspeak, Homoglyphen, Null-Breite-Zeichen und Schreibweise vereinheitlichen
* [ ] Normalisierung vor jeder weiteren Prüfung anwenden

## Aufgabe 3: Lexika

* [ ] Lexika für M und S als Ressourcen je Sprache anlegen, zuerst Englisch
* [ ] Sprachen der Welle 1 aus FP-010 ergänzen
* [ ] Lexika allein durch eine weitere Datei erweiterbar halten
* [ ] Lexikon-Inhalte nie in Oberfläche oder Protokoll ausgeben

## Aufgabe 4: Altersangaben

* [ ] Parser für Zahlen, Bereiche, „-jährig“, Schulstufen und Verwandtschaftsangaben anlegen
* [ ] Genanntes Alter unter 20 ergibt M `LIKELY`
* [ ] Nur genanntes Alter ab 20 gilt als erwachsen
* [ ] Vage Jugendbeschreibung ohne Alter ergibt M `POSSIBLE`
* [ ] Erwachsenen-Angabe senkt M nur, wenn sie im ganzen Text widerspruchsfrei ist

## Aufgabe 5: Auswertung

* [ ] Jeden Text einzeln und alle Texte zusammen auswerten
* [ ] Negativen Prompt, der einen Begriff ausschließt, nicht als Absicht werten
* [ ] Negativer Prompt hebt keinen Treffer aus einem anderen Text auf
* [ ] Semantisches Ergebnis der `ClassifierEngine` einbeziehen, es hebt eine Stufe nur an
* [ ] Regelschicht entscheidet klare Fälle ohne Engine

## Aufgabe 6: Tests und Doku

* [ ] Tests mit neutralen Platzhaltern statt expliziter Texte
* [ ] Tests: Altersgrenzen 17, 18, 19 und 20 mit und ohne S
* [ ] Tests: Verschleierung, mehrere Prompts zusammen, negativer Prompt
* [ ] Tests: bekannte Fehlalarme als dokumentierte Fälle aufnehmen
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
