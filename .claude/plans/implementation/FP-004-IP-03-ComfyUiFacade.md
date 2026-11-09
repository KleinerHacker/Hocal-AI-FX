# FP-004-IP-03: ComfyUI-Fassade

## Voraussetzungen

* IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing` und `project-docs` laden

## Aufgabe 2: Schnittstelle

* [ ] Interface `ComfyClient` mit Implementierung, damit Oberfläche und Tests sie ersetzen können
* [ ] Workflow bleibt ein undurchsichtiger JSON-Baum, kein Workflow-Aufbau

## Aufgabe 3: Operationen

* [ ] `submit`: `POST /prompt` mit `client_id`, Ergebnis `prompt_id` und Warteschlangennummer
* [ ] Abgelehnten Workflow als Ausnahme mit Knotenfehlerliste melden
* [ ] `history` für eine Ausführung und als Liste
* [ ] `view` lädt eine Ausgabedatei, `upload` lädt ein Eingabebild hoch
* [ ] `objectInfo` liefert den Knotenkatalog als JSON-Baum, groß, nicht tief parsen
* [ ] `queue` lesen und wartende Einträge löschen, `interrupt` ausführen
* [ ] `systemStats` und Verbindungsprüfung auf dieser Basis

## Aufgabe 4: Tests und Doku

* [ ] WireMock-Tests je Operation, Erfolg und Fehlerfall
* [ ] KDoc mit Anwendungsfall, README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
