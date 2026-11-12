# FP-001-IP-06: Verschlüsselung

## Voraussetzungen

* IP-03

## Aufgabe 1: Header und Schlüssel

* [ ] Header mit Magic, Version, PBKDF2-Parametern, Salt und IV
* [ ] Schlüssel per `PBKDF2WithHmacSHA256` mit 600.000 Iterationen, 256 Bit
* [ ] Zufälliges Salt und IV bei jedem Speichern
* [ ] Passwort als `CharArray`, danach überschreiben, nie loggen

## Aufgabe 2: Schreiben und Lesen

* [ ] AES-256-GCM (`AES/GCM/NoPadding`), Schreiben über `CipherOutputStream`
* [ ] Lesen über `CipherInputStream` direkt in den ZIP-Reader, keine temporäre Datei
* [ ] Verschlüsselung am Header, unverschlüsseltes ZIP an der Signatur erkennen, derselbe Lesepfad
* [ ] Ergebnis erst nach vollständig gelesenem Stream und geprüftem Tag verwenden
* [ ] Wechsel zwischen verschlüsselt und unverschlüsselt beim Speichern
* [ ] Typisierte Fehler: falsches Passwort, manipulierte Datei, unbekannte Version

## Aufgabe 3: Tests und Doku

* [ ] Tests: Round-Trip, falsches Passwort, gekipptes Byte, abgeschnittene Datei
* [ ] Tests: unbekannte Version, unverschlüsselte Datei ohne Passwort, Wechsel der Form
* [ ] KDoc, README und MkDocs prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
