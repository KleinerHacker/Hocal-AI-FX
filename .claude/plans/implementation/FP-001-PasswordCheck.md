# FP-001-PasswordCheck: Passwort-Bedarf vor dem Öffnen abfragen

## Voraussetzungen

* FP-001 abgeschlossen, GitHub-Lauf von PR #14 abwarten, damit Umbau und neue Methode nicht in einem Lauf vermischt werden

## Aufgabe 1: Storage

* [ ] `ProjectStorage.isEncrypted(file: Path): Boolean` in `storage.internal`, liest nur die ersten acht Byte der Datei
* [ ] Ergebnis `true` genau dann, wenn die Datei mit dem Magic `HOCALENC` beginnt, sonst `false`
* [ ] Datei kürzer als acht Byte, leer oder kein Projekt: `false`, die Fehlermeldung kommt dann erst beim Öffnen
* [ ] Fehlende oder nicht lesbare Datei: `java.io.IOException` wie bei `read`, keine eigene Ausnahme
* [ ] Prüfung des Magic an einer Stelle, `read(input, …)` nutzt dieselbe interne Funktion statt eines eigenen Vergleichs
* [ ] Es wird weder gelesen noch entschlüsselt, kein Passwort nötig, der Header wird nicht geprüft

## Aufgabe 2: Projekt-Manager

* [ ] `ProjectManager.isPasswordRequired(path: Path): Boolean` ruft die Storage-Prüfung auf
* [ ] Prüft `threadCheck` wie die anderen Operationen, braucht kein geladenes Projekt und ändert keinen Zustand
* [ ] KDoc: Ablauf für die Oberfläche, erst `isPasswordRequired`, bei `true` Dialog, dann `open(path, password)`
* [ ] KDoc: ein falsches Passwort meldet `open` weiter mit `WrongPasswordException`, ein fehlendes mit `PasswordRequiredException`
* [ ] KDoc von `open` verweist auf die neue Methode

## Aufgabe 3: Tests

* [ ] Storage: verschlüsselte Datei `true`, normales ZIP `false`, leere Datei `false`, Datei unter acht Byte `false`, Zufallsbytes `false`
* [ ] Storage: fehlende Datei wirft `NoSuchFileException`, die Datei bleibt unverändert
* [ ] Storage: Antwort passt zum Verhalten von `read`, verschlüsselt ohne Passwort liefert `PasswordRequiredException`
* [ ] Manager: nach `saveAs` mit Passwort `true`, nach `saveAs` ohne Passwort `false`
* [ ] Manager: funktioniert ohne geladenes Projekt und bei geladenem Projekt, ändert weder `dirty` noch Pfad noch geladenen Zustand
* [ ] Manager: `threadCheck` liefert `false`, die Methode wirft `IllegalStateException`
* [ ] Manager: fehlende Datei wirft `NoSuchFileException`

## Aufgabe 4: Doku

* [ ] Feature-Plan FP-001: Eintrag im Abschnitt „As Built“ von IP-07 zur neuen Methode
* [ ] README und MkDocs prüfen, CHANGELOG unverändert

## Abschluss

* [ ] Änderungen lokal committen, Push erst nach dem Ergebnis des laufenden Laufs von PR #14, dann ein Lauf für beides
* [ ] Auf den GitHub-Lauf warten
* [ ] Plan-Datei in `.claude/plans/implementation` ablegen und nach dem Abschluss mit `git rm` entfernen
