# FP-007-IP-05: Einbau in die Generierung

## Voraussetzungen

* IP-02, IP-04, FP-006 IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skill `testing` laden
* [ ] Abhängigkeit von `lib/comfy` auf die API von `lib/guard` eintragen
* [ ] Prüfen, ob die Fassade aus FP-004 das Löschen eines History-Eintrags anbietet

## Aufgabe 2: Übergabe der Eingaben

* [ ] `WorkflowBuilder` liefert alle wirklich geschriebenen Texte mit ihrer Rolle
* [ ] `WorkflowBuilder` liefert alle Eingabebilder, auch das Medium aus dem Hauptfenster
* [ ] `RequestSnapshot` aus diesen Daten beim Senden bilden und am Auftrag halten

## Aufgabe 3: Vorprüfung

* [ ] `GenerationService` erhält `ContentGuard` als Pflicht-Parameter im Konstruktor
* [ ] `checkRequest` vor dem Hochladen und vor dem Absenden aufrufen
* [ ] Prüfung 1 (Prompt), Prüfung 2 (Eingabebilder) und Prüfung 3 (zusammen) auswerten
* [ ] Bei `BLOCKED` oder `UNDECIDABLE` nichts hochladen und nichts absenden
* [ ] Unabhängige Prüfungen parallel und nie auf dem FX-Thread ausführen

## Aufgabe 4: Nachprüfung

* [ ] Ergebnis nach dem Endereignis in den Speicher laden
* [ ] `checkResult` für Prüfung 4 aufrufen, das Ausgabebild für sich allein beurteilen
* [ ] Nur ein `ALLOWED`-Ergebnis an Kachel und `ProjectManager` weitergeben
* [ ] Stichprobe der Vorschaubilder an `checkPreview` geben, Treffer bricht den Lauf ab

## Aufgabe 5: Zustand und Aufräumen

* [ ] Auftragszustand `BLOCKED` mit Art `REQUEST`, `RESULT` oder `UNDECIDABLE` ergänzen
* [ ] Dateien eines blockierten Laufs in Ausgabe, Temp und Eingabe von ComfyUI löschen
* [ ] History-Eintrag des Servers entfernen, bei Bedarf die Fassade dafür erweitern
* [ ] Fehlgeschlagenes Aufräumen inhaltsfrei protokollieren
* [ ] Wartende Aufträge behalten ihren Schnappschuss, ihr Ergebnis durchläuft Prüfung 4

## Aufgabe 6: Tests und Doku

* [ ] Tests mit Engine-Attrappen: je ein Block für R1, R2, R3 und R4
* [ ] Test: Körpertausch über zwei Eingabebilder wird in Prüfung 3 blockiert
* [ ] Test: blockierte Anfrage ruft weder Upload noch `submit` auf
* [ ] Test: blockiertes Ergebnis erreicht weder Kachel noch `ProjectManager`
* [ ] Test: fehlende Engine verhindert jede Generierung
* [ ] Architekturtest: keine Produktionsklasse baut einen `GenerationService` ohne `ContentGuard`
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
