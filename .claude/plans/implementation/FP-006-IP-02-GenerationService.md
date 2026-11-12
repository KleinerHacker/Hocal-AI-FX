# FP-006-IP-02: Generierungsdienst

## Voraussetzungen

* IP-01, FP-004 IP-04, FP-005 IP-05

## Aufgabe 1: Vorbereitung

* [ ] Skills `fx-component-lifecycle` und `testing` laden
* [ ] Prüfen, dass FP-005 IP-05 ComfyUI mit `--preview-method auto` startet, sonst dort ergänzen

## Aufgabe 2: Aufträge

* [ ] `GenerationService` und `GenerationJob` in `lib/comfy`, Paket `generation`, ohne JavaFX
* [ ] Zustände: vorbereitend, Dienst startet, wartend mit Position, laufend mit Prozent, fertig, fehlgeschlagen, abgebrochen
* [ ] Mehrere Aufträge gleichzeitig, beobachtbare Liste in der Reihenfolge der ComfyUI-Warteschlange

## Aufgabe 3: Ablauf eines Auftrags

* [ ] `ensureRunning()` aus FP-005 aufrufen
* [ ] Eingabebild und weitere Bilder beim Senden festhalten und hochladen
* [ ] Workflow bauen und absenden, `prompt_id` identifiziert den Auftrag
* [ ] Ereignisse vor der Rückkehr von `submit` puffern und danach zuordnen

## Aufgabe 4: Fortschritt

* [ ] Prozent aus Sampler-Schritten und Anteil fertiger Knoten berechnen, Cache-Knoten als erledigt zählen
* [ ] Neuestes Vorschaubild je Auftrag bereitstellen, Bündelung übernimmt die Oberfläche
* [ ] Bei verlorenem WebSocket das Ende über die History ermitteln

## Aufgabe 5: Ende und Abbruch

* [ ] Nach dem Endereignis History lesen und Ergebnis herunterladen
* [ ] Gesperrten Knoten aus der Fassade als Zustand „fehlgeschlagen“ mit Grund und Knotentyp melden
* [ ] Abbruch über `interrupt`, wartende Aufträge über das Löschen in der Warteschlange entfernen
* [ ] Neustart des Dienstes markiert alle Aufträge als abgebrochen

## Aufgabe 6: Tests und Doku

* [ ] Tests mit Attrappen für `ComfyClient` und `ComfyEventHook`: Ablauf, Warteschlange, Fehler, Abbruch, Verlust
* [ ] Test: gesperrter Knoten ergibt „fehlgeschlagen“ mit Grund, ohne Aufruf am Server
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
