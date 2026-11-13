# FP-007-IP-03: Klassifikations-Prozess

## Voraussetzungen

* IP-01, FP-005 IP-02, FP-005 IP-03, FP-005 IP-05

## Aufgabe 1: Vorbereitung

* [ ] Skills `testing`, `startup` und `project-docs` laden
* [ ] Dokumentation der Kandidaten-Pakete lesen, keine fremde Klasse dekompilieren

## Aufgabe 2: Entscheidung zur Engine

* [ ] Engine-Ansatz wählen: lokaler Prozess auf der Python-Umgebung oder ONNX in der JVM
* [ ] Kandidaten-Modelle für Text und Bild mit Lizenz, Größe und Hardware-Bedarf zusammenstellen
* [ ] Festlegen, ob die Engine die GPU nutzen darf
* [ ] Freigabe des Benutzers für jede neue Abhängigkeit (JVM und Python) einholen
* [ ] Entscheidung im Feature-Plan festhalten

## Aufgabe 3: Prozess

* [ ] Prozess-Implementierung der `ClassifierEngine` im Paket `engine` anlegen
* [ ] Python-Arbeitsskript als Ressource anlegen, ohne Modellnamen und ohne Prompt-Text
* [ ] Protokoll über Standard-Ein- und -Ausgabe als JSON-Zeilen, kein Netzwerk-Port
* [ ] Start bei Bedarf, Bereitschaftsprüfung, Neustart und Zeitlimit je Anfrage
* [ ] Prozess beim Ende der Anwendung stoppen, verwaiste Prozesse beim Start beenden
* [ ] Absturz oder Hänger ergibt `UNDECIDABLE`

## Aufgabe 4: Modelle und Prompts

* [ ] Modelle mit Quelle, Datei, Revision und Prüfsumme in `guard.properties` eintragen
* [ ] Prompts für Textprüfung, Bildprüfung und Vorschauprüfung in `guard.properties` eintragen
* [ ] Prompts ohne explizite Formulierungen und mit Platzhaltern für die Altersgrenzen schreiben
* [ ] Modell und Prompt von der Kotlin-Seite an das Arbeitsskript übergeben
* [ ] Modelle über den geprüften Download aus FP-005 IP-02 ins Datenverzeichnis laden
* [ ] Fehlende Modelle ergeben `UNDECIDABLE` mit Reparatur-Grund

## Aufgabe 5: Klassifikation

* [ ] Textklassifikation liefert M- und S-Stufe
* [ ] Bildklassifikation liefert M- und S-Stufe, auch bei mehreren Personen
* [ ] Antworten der Engine prüfen, ungültige Antwort ergibt `UNDECIDABLE`

## Aufgabe 6: Tests und Doku

* [ ] Tests mit Stub-Arbeitsskript: Start, Bereitschaft, Zeitlimit, Absturz, ungültige Antwort
* [ ] Test: kein Modellname und kein Prompt-Text als Literal im Code
* [ ] Test: jeder benötigte Schlüssel in `guard.properties` ist vorhanden und nicht leer
* [ ] Lizenzen der Engine-Bestandteile in der Lizenzprüfung ergänzen
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
