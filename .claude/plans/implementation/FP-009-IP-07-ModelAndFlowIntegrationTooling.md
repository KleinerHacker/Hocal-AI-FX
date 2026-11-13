# FP-009-IP-07: Werkzeuge zur Modell- und Flow-Integration

## Voraussetzungen

* IP-02
* IP-03 (Beispiel für die Anleitung)

## Aufgabe 1: Skill `ai-model`

* [ ] Skill `project-docs` laden, vorhandene Skills als Vorlage für Aufbau ansehen
* [ ] `.claude/skills/ai-model/SKILL.md` anlegen, Auslöser: Änderung unter `models` oder `flows` in `lib/ai-model`
* [ ] Format nur per Verweis auf KDoc und Docs-Seite aus IP-01 beschreiben, keine Regel doppelt
* [ ] Regeln für Ids und Dateinamen festhalten, Flow-Id ist der Dateiname
* [ ] Liste der Angaben, die von Hand geprüft werden müssen: URL, Revision, Prüfsumme, Lizenz, Login

## Aufgabe 2: Ablauf neues Modell

* [ ] Schritte festlegen: Quelle und Lizenz prüfen, gesperrte Modelle mit Login ablehnen und den Benutzer fragen
* [ ] Repository, festgeschriebene Revision und Dateien mit Zielordner bestimmen
* [ ] Größe und SHA-256 aus der echten Datei ermitteln, sonst beim Benutzer erfragen
* [ ] Modell-Datei anlegen, Eintrag in `models/index.json`, mindestens ein Flow
* [ ] Katalog-Test ausführen, README-Lizenzvermerk ergänzen

## Aufgabe 3: Ablauf neuer Flow zu bestehendem Modell

* [ ] Flow als unveränderten ComfyUI-API-Export in `flows/<flow>.json` ablegen, nichts aus Beispielen kopieren
* [ ] Flow-Eintrag in der Modell-Datei ergänzen: Standardfelder, Ziele, Parameter mit `mode`
* [ ] Ziele `"<Knoten-Id>.<Eingabe>"` gegen den Flow prüfen, Datei-Ids des Modells binden
* [ ] Flow auf gesperrte Knotentypen prüfen, etwa `GemmaAPITextEncode`, solche Flows nicht aufnehmen
* [ ] Flow als ungeprüft markieren, bis er gegen ein echtes ComfyUI läuft
* [ ] Katalog-Test ausführen

## Aufgabe 4: Agent `ai-model-integrator`

* [ ] `.claude/agents/ai-model-integrator.md` anlegen, Skill `ai-model` als Pflichtlektüre
* [ ] Eingabe: Modell oder Flow, Quelle, Ziel; fehlende Angaben beim Benutzer erfragen
* [ ] Keine URL, Revision, Prüfsumme oder Knoten-Bindung erfinden
* [ ] Änderungen nur an Ressourcen und Tests von `lib/ai-model` sowie Dokumentation
* [ ] Bericht: was geprüft ist, was ungeprüft bleibt, Ergebnis des Katalog-Tests
* [ ] Der Agent startet selbst keine weiteren Agenten

## Aufgabe 5: Befehle

* [ ] `.claude/commands/add-model.md` anlegen: Argument Modellname, lädt den Skill, startet den Agenten
* [ ] `.claude/commands/add-flow.md` anlegen: Argumente Modell-Id und Flow-Name, gleicher Ablauf
* [ ] Fehlende Argumente beim Benutzer erfragen, Konsole auf Deutsch

## Aufgabe 6: Einbindung und Erprobung

* [ ] Skill in der Liste in `.claude/CLAUDE.md` mit Auslöser eintragen
* [ ] Ablauf an einem Testmodell und einem Testflow durchspielen, Ergebnis danach verwerfen
* [ ] Katalog-Test muss bei beiden Abläufen grün sein

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
