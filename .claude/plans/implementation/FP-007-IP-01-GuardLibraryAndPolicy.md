# FP-007-IP-01: Schutz-Bibliothek und Regelwerk

## Voraussetzungen

* keine; Jackson und SLF4J werden mit FP-001 und FP-004 geteilt

## Aufgabe 1: Vorbereitung

* [ ] Skills `ci-pipeline`, `project-docs` und `testing` laden
* [ ] Prüfen, dass keine neue Abhängigkeit nötig ist, sonst Freigabe des Benutzers einholen

## Aufgabe 2: Modul

* [ ] Modul `lib/guard` (`hocal-guard`) in `settings.gradle.kts` eintragen
* [ ] `build.gradle.kts` ohne JavaFX und ohne Anwendungs-Plugins anlegen
* [ ] `module-info.java` anlegen, nur das Paket `api` exportieren
* [ ] Paket `org.pcsoft.app.hocal.guard` mit `api`, `policy`, `config`, `engine`, `audit` anlegen

## Aufgabe 3: Datentypen

* [ ] Signale M und S mit den Stufen `NONE`, `POSSIBLE`, `LIKELY` anlegen
* [ ] `RequestSnapshot` mit Prompt-Texten (positiv, negativ, sonstige) und Eingabebildern anlegen
* [ ] `Verdict` mit `ALLOWED`, `BLOCKED` samt Regel-Id und `UNDECIDABLE` samt Grund anlegen
* [ ] Regel-Ids `R1` bis `R4` und Fehlerhierarchie `GuardException` anlegen

## Aufgabe 4: Regelwerk

* [ ] Konstanten Schutzalter 18 und Sicherheitsalter 20 im Paket `policy` anlegen
* [ ] Blockstufe: M `LIKELY` mit S ab `POSSIBLE`, oder M ab `POSSIBLE` mit S `LIKELY`
* [ ] R1: M und S aus den Prompt-Texten, einzeln und zusammen
* [ ] R2: M und S im selben Eingabebild
* [ ] R3: M und S vereinigt über alle Eingaben, auch Bild gegen Bild
* [ ] R4: M und S im erzeugten Bild, für sich allein
* [ ] S ohne M und M ohne S ergeben immer `ALLOWED`
* [ ] Regelwerk als reine Funktionen ohne Ein- und Ausgabe umsetzen

## Aufgabe 5: Konfiguration

* [ ] `guard.properties` im Ressourcen-Ordner mit Schlüsselschema für Modelle und Prompts anlegen
* [ ] `GuardConfig` als typisierten Leser anlegen, einmal laden, beim Laden prüfen
* [ ] Platzhalter für Schutzalter und Sicherheitsalter in den Prompts aus den Konstanten füllen
* [ ] Fehlender oder leerer Schlüssel führt zu `UNDECIDABLE`
* [ ] Paket der Datei weder exportieren noch öffnen, kein Überschreiben zur Laufzeit

## Aufgabe 6: Fassade

* [ ] Schnittstelle `ClassifierEngine` für Text- und Bildklassifikation anlegen
* [ ] Fassade `ContentGuard` mit `checkRequest`, `checkResult` und `checkPreview` anlegen
* [ ] Fehler, Zeitüberschreitung oder fehlende Engine ergeben `UNDECIDABLE`, nie `ALLOWED`
* [ ] Inhaltsfreies Protokoll: nur Prüfung, Regel-Id, Stufen und Dauer

## Aufgabe 7: Tests und Doku

* [ ] Attrappen für `ClassifierEngine` als Test-Hilfen anlegen
* [ ] Tests: jede Kombination von R1 bis R4, beide Grenzen der Blockstufe
* [ ] Tests: S allein erlaubt, M allein erlaubt, Bild gegen Bild blockiert
* [ ] Tests: `GuardConfig` meldet fehlende und leere Schlüssel
* [ ] Test: Protokoll enthält keinen Prompt-Text und keine Bilddaten
* [ ] CI-Pipeline, Lizenzprüfung und README-Strukturabschnitt prüfen und anpassen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
