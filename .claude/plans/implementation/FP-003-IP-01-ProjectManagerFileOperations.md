# FP-003-IP-01: Projekt-Manager: Dateioperationen

## Voraussetzungen

* FP-001 IP-06 und FP-001 IP-07 sind abgeschlossen (Verschlüsselung, `ProjectManager`)

## Aufgabe 1: Vorbereitung

* [ ] Skills `fx-model`, `fx-component-lifecycle` und `testing` laden
* [ ] Synchrone Methoden `createNew`, `open`, `save`, `saveAs`, `close` bleiben unverändert bestehen
* [ ] Neue asynchrone Methoden daneben ergänzen, damit vorhandene Tests und Aufrufer unberührt bleiben
* [ ] `isPasswordRequired`, `encryptedProperty` und Passwort-Überschreiben in `close` existieren bereits, nicht neu bauen

## Aufgabe 2: Temporäre Medien verschlüsseln

* [ ] Gilt nur für verschlüsselte Projekte; Medien unverschlüsselter Projekte bleiben im Temp-Ordner im Klartext
* [ ] `DirectoryMediaStore` in `lib/model` erhält optionalen Sitzungsschlüssel als `CharArray`, Standard `null` (unverändert)
* [ ] `accept` schreibt über `BlockStreamCipher.openOutput` mit `iterations = 1`, `open` liest über `openInput`
* [ ] Manager erzeugt den Sitzungsschlüssel mit `SecureRandom` nur beim Öffnen oder Speichern eines verschlüsselten Projekts
* [ ] Sitzungsschlüssel unabhängig vom Projektpasswort: Passwortwechsel braucht kein Neuverschlüsseln der Medien
* [ ] Speichern unter von unverschlüsselt auf verschlüsselt verschlüsselt vorhandene Klartextmedien nachträglich, wiederholbar und abbruchsicher
* [ ] Verschlüsselt-Zustand je Datei erkennbar, Verwechslung mit Klartextmedien ausgeschlossen
* [ ] Wechsel auf unverschlüsselt lässt den Speicher bis zum Schließen verschlüsselt
* [ ] Sitzungsschlüssel überschreiben bei Schließen, Ersetzen und fehlgeschlagenem Öffnen
* [ ] Temporäres Verzeichnis nach Fehler im Hintergrund vollständig löschen, auch teilweise verschlüsseltes

## Aufgabe 3: Ergebnistypen

* [ ] Sealed Klasse `FileOperationResult` im Paket `manager`: `Success`, `Failure`, `Busy`
* [ ] Enum `FileError`: ungültig, beschädigt, neuere Version, falsches Passwort, Passwort nötig
* [ ] Weitere `FileError`-Werte: manipuliert, Verschlüsselung nicht unterstützt, Lesefehler, Schreibfehler, unbekannt
* [ ] Abbildung der `ProjectFileException`-Unterklassen und von `IOException` auf `FileError` in einer Funktion
* [ ] `Failure` trägt `FileError` und die ursächliche Exception, aber niemals Passwort oder Dateiinhalt

## Aufgabe 4: Hintergrundausführung

* [ ] Interne Abstraktion für Hintergrundlauf und FX-Rückmeldung, Standard über `javafx.concurrent.Task`
* [ ] Austauschbar wie `threadCheck`, damit Tests in `lib/fx-model` ohne Toolkit synchron laufen
* [ ] Schreibgeschützte Property `busyProperty` samt `isBusy`, nur auf dem FX-Thread gesetzt
* [ ] Zweite Operation während `busy` ergibt `Busy`, ohne etwas zu verändern
* [ ] Änderungsmethoden (Baum, Medien) und `close` während `busy` mit `IllegalStateException` abweisen
* [ ] Ergebnis-Callback wird immer auf dem FX-Thread aufgerufen, auch bei Ausnahmen im Hintergrund

## Aufgabe 5: Öffnen, Speichern, Speichern unter

* [ ] `openAsync(path, password, onDone)`: liest im Hintergrund in ein neues temporäres Verzeichnis
* [ ] Bei Erfolg auf dem FX-Thread: altes Projekt freigeben, neues anhängen, Passwort übernehmen
* [ ] Bei Fehler: temporäres Verzeichnis löschen, Projekt, Pfad und Passwort bleiben unverändert
* [ ] Übergebenes Passwort sofort kopieren, die Kopie nach dem Lauf überschreiben
* [ ] `saveAsync(onDone)`: Zeitstempel und Version auf dem FX-Thread setzen, schreiben im Hintergrund
* [ ] `saveAsAsync(path, password, onDone)`: Pfad, Passwort und Thumbnail-Speicher erst nach Erfolg übernehmen
* [ ] Leeres Passwort (`CharArray` der Länge 0) mit `IllegalArgumentException` ablehnen, `null` heißt unverschlüsselt
* [ ] `dirty` erst nach erfolgreichem Schreiben zurücksetzen

## Aufgabe 6: Tests Verschlüsselung der temporären Daten

* [ ] `DirectoryMediaStoreTest` in `lib/model`: mit Schlüssel enthält die Datei kein Klartextmuster, Rundlauf byte-genau
* [ ] Falscher Schlüssel und manipulierte Mediendatei schlagen beim Lesen fehl
* [ ] Ohne Schlüssel bleibt das Verhalten unverändert, vorhandene Tests laufen unverändert
* [ ] `ProjectManagerTest`: geöffnetes verschlüsseltes Projekt hat keine Klartextmedien im Temp-Ordner, auch nach `addReference` und `appendMedium`
* [ ] Test: unverschlüsseltes Projekt hält Medien im Klartext, ohne Schlüssel und ohne Verschlüsselungsaufwand
* [ ] Test: Speichern unter auf verschlüsselt verschlüsselt vorhandene Medien nach, Inhalte danach lesbar
* [ ] Test: Speichern unter auf unverschlüsselt, Medien bleiben lesbar und speicherbar
* [ ] Test: fehlgeschlagenes Öffnen und Abbruch im Hintergrund lassen keine Klartextdatei und kein Verzeichnis zurück
* [ ] Test: Sitzungsschlüssel nach Schließen überschrieben, nächste Sitzung nutzt neuen Schlüssel

## Aufgabe 7: Tests Dateioperationen und Doku

* [ ] Tests: Erfolg von Öffnen, Speichern, Speichern unter, jeweils verschlüsselt und unverschlüsselt
* [ ] Tests: jeder `FileError` aus echten defekten Dateien, Projekt, Pfad und Passwort unverändert danach
* [ ] Tests: `Busy`-Abweisung, Abweisung von Änderungen während `busy`, Callback auf dem FX-Thread-Ersatz
* [ ] Tests: Passwort überschrieben bei `close`, Ersetzen und fehlgeschlagenem Öffnen
* [ ] Tests: Ersetzen eines geladenen Projekts beim Öffnen, leeres Passwort abgelehnt
* [ ] Testdateien in einem temporären Verzeichnis, jede Testmethode mit KDoc
* [ ] KDoc von `ProjectManager` und `DirectoryMediaStore` aktualisieren, „blockiert den Aufrufer“ auf synchrone Methoden begrenzen
* [ ] Feature-Plan FP-003 (Englisch): Scope, Affected Areas und Decisions von IP-01 um die Medienverschlüsselung ergänzen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
