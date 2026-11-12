# FP-001-IP-06: Verschlüsselung

## Voraussetzungen

* IP-03 abgeschlossen, GitHub-Build grün

## Aufgabe 1: Fehler und Header

* [ ] Neue Fehler in `ProjectFileException`: `PasswordRequired`, `WrongPassword`, `Manipulated`, `UnsupportedEncryptionVersion`
* [ ] Internes `EncryptionHeader`: Magic `HOCALENC`, Version, KDF-Kennung, Iterationen, Salt (16), Basis-IV (12)
* [ ] Header zusätzlich: Blockgröße (4 Byte) und Prüfmerkmal (32 Byte), Gesamtlänge fest 78 Byte
* [ ] Header lesen mit Obergrenzen für Iterationen und Blockgröße, sonst `Invalid`
* [ ] Unbekannte Version oder KDF-Kennung ergibt `UnsupportedEncryptionVersion`
* [ ] Header endet im Stream zu früh: `Damaged`

## Aufgabe 2: Schlüssel

* [ ] Internes `ProjectCipher`: Schlüssel per `PBKDF2WithHmacSHA256`, 256 Bit, Iterationen aus dem Header
* [ ] Standard 600.000 Iterationen, zufälliges Salt und Basis-IV bei jedem Speichern (`SecureRandom`)
* [ ] Prüfmerkmal als HMAC-SHA256 über eine feste Zeichenfolge, Vergleich zeitkonstant
* [ ] Passwort als `CharArray`, Aufrufer-Array unverändert, Schlüsselkopien nach Gebrauch überschreiben
* [ ] Passwort und Schlüssel erscheinen nie in Meldungen oder Logs

## Aufgabe 3: Blockweises GCM

* [ ] Schreib-Strom: Blöcke zu 64 KiB, AES-256-GCM, jeder Block mit eigenem Tag (16 Byte)
* [ ] Block-IV: Basis-IV mit Blockzähler (8 Byte, Big Endian) verknüpft, nie doppelt
* [ ] Zusätzliche Daten je Block: Header plus Kennzeichen „letzter Block“, schützt vor Abschneiden und Umordnen
* [ ] Letzter Block wird erst beim Schließen geschrieben, auch wenn er voll ist
* [ ] Lese-Strom: ein Byte Vorausschau erkennt den letzten Block, jeder Block wird vor Weitergabe geprüft
* [ ] Fehlgeschlagenes Tag, auch durch Abschneiden, ergibt `Manipulated`
* [ ] Beide Ströme schließen den umschlossenen Strom nicht

## Aufgabe 4: Anbindung an `ProjectStorage`

* [ ] Parameter `password: CharArray? = null` jeweils als letzter Parameter bei `read` und `write`
* [ ] Schreiben mit Passwort: Header schreiben, ZIP durch den Schreib-Strom leiten, Datei-Variante bleibt atomar
* [ ] Schreiben ohne Passwort: unverschlüsseltes Standard-ZIP wie bisher, damit Wechsel beim Speichern möglich
* [ ] Lesen erkennt Magic oder ZIP-Signatur `PK\u0003\u0004`, beides sonst `Invalid`
* [ ] Verschlüsselte Datei ohne Passwort: `PasswordRequired`, falsches Passwort: `WrongPassword` vor dem ersten Block
* [ ] Unverschlüsselte Datei mit übergebenem Passwort wird normal gelesen
* [ ] Nach dem ZIP-Reader den Reststrom bis zum Ende lesen, damit der letzte Block geprüft wird
* [ ] Ergebnis zählt erst nach vollständig gelesenem und geprüftem Strom
* [ ] Intern überladbare Iterationszahl, damit Tests schnell laufen

## Aufgabe 5: Tests und Doku

* [ ] Tests: Round-Trip mit Passwort, Medieninhalt bleibt gleich, mehrere Blöcke durch große Medien
* [ ] Tests: falsches Passwort, fehlendes Passwort, gekipptes Byte in Header, Daten und Tag
* [ ] Tests: abgeschnittene Datei (im Block und an der Blockgrenze), vertauschte Blöcke
* [ ] Tests: unbekannte Version, unbekannte KDF-Kennung, überhöhte Iterationen oder Blockgröße
* [ ] Tests: Wechsel verschlüsselt zu unverschlüsselt und zurück, Datei-Variante atomar, Aufrufer-Passwort unverändert
* [ ] Regressionstest `…RT`: feste verschlüsselte Datei im Testverzeichnis lesen, mit Python (`cryptography`) unabhängig erzeugt
* [ ] KDoc, README und MkDocs prüfen

## Abschluss

* [ ] Kein lokaler Build möglich, pushen und auf den GitHub-Build warten
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Feature-Plan: Technische Hinweise zu `CipherInputStream` auf blockweises GCM korrigieren, Abschnitt „As Built“ ergänzen
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
