# FP-008-IP-03: Demonstrationsmodell

## Voraussetzungen

* IP-02

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing` und `project-docs` laden
* [ ] Bedingungen von Krea 2 prüfen: Lizenz, kommerzielle Nutzung, gesperrt oder Login nötig
* [ ] Bei Login-Pflicht den Benutzer fragen, die Festlegung „keine Modelle mit Login“ wäre neu zu entscheiden

## Aufgabe 2: Modell

* [ ] Hugging-Face-Repository, festgeschriebene Revision und Liste der benötigten Dateien bestimmen
* [ ] Zielordner, Größe und SHA-256 je Datei aus den echten Dateien ermitteln
* [ ] Modell-Datei mit Name, Hugging-Face-Verweis, Lizenz und Hardware-Hinweis anlegen
* [ ] Eintrag in `models/index.json`

## Aufgabe 3: Flow

* [ ] Workflow selbst in ComfyUI bauen und im API-Format exportieren, nichts aus Beispielen kopieren
* [ ] Flow-Eintrag mit Standardfeldern und Ziele für Prompt, Modell-Dateien und Seed
* [ ] Parameter Schritte, Guidance und Größe mit Vorgaben, Grenzen und Ziel
* [ ] Konstanten (`CONSTANT`) für feste Werte wie Anzahl der Bilder

## Aufgabe 4: Tests und Doku

* [ ] Test: der echte Katalog lädt und besteht die Validierung
* [ ] Flow bleibt als ungeprüft markiert, bis FP-006 ihn gegen ein echtes ComfyUI ausführt
* [ ] Lizenz des Modells in der README vermerken, KDoc prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
