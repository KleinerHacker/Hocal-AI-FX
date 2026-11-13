# FP-007-IP-07: Auswertungswerkzeug und Dokumentation

## Voraussetzungen

* IP-05

## Aufgabe 1: Vorbereitung

* [ ] Skills `project-docs`, `ci-pipeline` und `testing` laden
* [ ] Format der Kennzeichnung für lokale Beispiele festlegen

## Aufgabe 2: Auswertungs-Task

* [ ] Gradle-Task anlegen, der den Schutz über einen lokalen Ordner gekennzeichneter Beispiele laufen lässt
* [ ] Bericht nur mit Zählwerten schreiben: blockiert, erlaubt, nicht entscheidbar, je Regel
* [ ] Fehlalarme und Fehlschläge gegen die Kennzeichnung zählen
* [ ] Bericht enthält keinen Text und kein Bild der Beispiele
* [ ] Beispiel-Ordner per `.gitignore` ausschließen, nichts davon einchecken

## Aufgabe 3: Kalibrierung

* [ ] Hinweise zur Kalibrierung der Schwellen und des Sicherheitsalters 20 schreiben
* [ ] Ablauf nach einer Änderung von `guard.properties` beschreiben: Auswertung erneut ausführen
* [ ] Festhalten, dass der Eigentümer das Material außerhalb des Repositories bereitstellt

## Aufgabe 4: Dokumentation

* [ ] MkDocs-Seite: was geschützt wird, die vier Prüfungen, die Grenzen des Schutzes
* [ ] README und KDoc ergänzen, `CHANGELOG.md` ergänzen
* [ ] Lizenzen der Engine-Bestandteile und Modelle in die Lizenzliste aus FP-010 IP-09 aufnehmen

## Aufgabe 5: Pipeline

* [ ] Modul `lib/guard` in der Modulliste der Workflows prüfen
* [ ] Sicherstellen, dass die CI keine Beispieldaten und keine Schutz-Modelle lädt
* [ ] Modul in die Modulliste des Laufzeit-Abbilds aus FP-011 IP-01 aufnehmen

## Aufgabe 6: Tests

* [ ] Test des Tasks mit neutralen Beispielen und Engine-Attrappe
* [ ] Test: Bericht enthält nur Zählwerte

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG ergänzen
* [ ] Feature-Abschluss nach `.claude/commands/feature-plan.md`: Feature-Plan verdichten, Status- und Plan-Dateien mit `git rm` entfernen
