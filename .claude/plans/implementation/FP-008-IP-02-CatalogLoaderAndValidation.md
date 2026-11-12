# FP-008-IP-02: Katalog-Lader und Validierung

## Voraussetzungen

* IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing` und `project-docs` laden
* [ ] Entscheiden: unbekannte JSON-Felder ablehnen oder ignorieren

## Aufgabe 2: Lader

* [ ] `CatalogLoader` liest `models/index.json`, die Modell-Dateien und die Flow-Dateien aus den Ressourcen
* [ ] Lesen über den Class Loader des Moduls, Ordner `models` und `flows` sind keine Pakete
* [ ] Flow-Verweise auflösen, jeder Flow-Eintrag verweist auf eine Flow-Datei
* [ ] Katalog einmal laden und zwischenspeichern, thread-sicher

## Aufgabe 3: Validierung

* [ ] Schema-Version bekannt, Modell-Ids und Parameter-Ids eindeutig
* [ ] Hugging-Face-URL ist `https` auf dem Host `huggingface.co`
* [ ] Jede Datei hat SHA-256, positive Größe und bekannten Zielordner
* [ ] Pflicht-Standardfelder vorhanden, Ziele zeigen auf vorhandene Knoten und Eingaben des Workflows
* [ ] Vorgabe passt zu Typ und Grenzen, `mode` ist einer der drei Werte
* [ ] Flow-Datei mit gesperrtem Knotentyp wie `GemmaAPITextEncode` wird abgelehnt
* [ ] Lader bekommt die gesperrten Typen als Parameter, keine Abhängigkeit auf `lib/comfy`
* [ ] Fehler nennen Datei und Feld

## Aufgabe 4: API und Tests

* [ ] `ModelCatalog`: Modelle auflisten, Modell nach Id, Flow-Einträge eines Modells, Workflow eines Flows
* [ ] `CatalogException`-Hierarchie für jeden Fehlerfall
* [ ] Tests mit Testressourcen: gültiger Katalog und jeder Fehlerfall, auch Flow mit gesperrtem Knoten
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
