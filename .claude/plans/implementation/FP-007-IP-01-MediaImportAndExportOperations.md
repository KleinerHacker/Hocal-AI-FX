# FP-007-IP-01: Medien-Import- und Export-Operationen

## Voraussetzungen

* FP-001 IP-04, FP-001 IP-05, FP-001 IP-07, FP-002 IP-03

## Aufgabe 1: Vorbereitung

* [ ] Skills `fx-model`, `fx-component-lifecycle` und `testing` laden
* [ ] Mit dem Benutzer klären: Größenlimit für sehr große Dateien

## Aufgabe 2: Import

* [ ] Import-Operation im `ProjectManager`: eine oder mehrere Dateien in einen Ordner
* [ ] Typprüfung über die Medientyp-Registry aus FP-002 IP-03
* [ ] Je Datei neue Referenz mit Medium `000` und Baumeintrag als eine Änderung
* [ ] Name nach dem Dateinamen, im Ordner eindeutig, bei Konflikt eine Nummer anhängen
* [ ] Datei vollständig lesen, bevor das Modell geändert wird, unlesbare Datei überspringen

## Aufgabe 3: Export

* [ ] Export eines Mediums in eine Datei mit der eigenen Endung
* [ ] Export einer Referenz: nur das aktuelle Medium in eine Datei, ohne Historie und Historiendetails
* [ ] Export eines Ordners in ein Verzeichnis mit Unterverzeichnissen und Dateien nach Eintragsnamen
* [ ] Dateinamen für das Dateisystem gültig machen
* [ ] Vorhandene Zieldatei nur mit ausdrücklicher Freigabe des Aufrufers überschreiben, sonst Konfliktergebnis melden
* [ ] Aus dem Projekt als Stream lesen und schreiben, kein Medium doppelt im Speicher
* [ ] Export ändert das Projekt nicht und setzt `dirty` nicht

## Aufgabe 4: Kopie in die Galerie

* [ ] Medium der Historie als neue Referenz in einen Ordner kopieren, Kopie ist Medium `000`
* [ ] Neue UUID, Historiendetails Prompt, Modell und Flow werden mitkopiert
* [ ] Name aus dem Referenznamen und der Mediennummer, im Ordner eindeutig
* [ ] Änderung als eine Einheit, Projekt wird `dirty`, `refresh()` des Property-Modells

## Aufgabe 5: Ergebnis und Hintergrund

* [ ] Typisiertes Ergebnis mit verarbeiteten und übersprungenen Objekten samt Grund
* [ ] Fehler bei einem Objekt stoppt die anderen nicht
* [ ] Dateizugriff im Hintergrund über `javafx.concurrent`, Änderung am Modell auf dem FX-Thread

## Aufgabe 6: Tests und Doku

* [ ] Tests Import: Erfolg, mehrere Dateien, nicht unterstützter Typ, Namenskonflikt, unlesbare Datei, Teilerfolg
* [ ] Tests Export: Medium, Referenz, Ordner, vorhandene Zieldatei, ungültige Zeichen, Projekt unverändert
* [ ] Tests Kopie: neue UUID, Historiendetails, eindeutiger Name
* [ ] Testdateien in einem temporären Verzeichnis, KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
