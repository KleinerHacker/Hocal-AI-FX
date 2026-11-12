# FP-003-IP-03: Passwort- und Verschlüsselungsdialoge

## Voraussetzungen

* IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `dialog`, `ui-styling` und `fx-component-lifecycle` laden
* [ ] Agent `dialog-explore`, danach Agent `dialog-creator` verwenden

## Aufgabe 2: Passwortdialog

* [ ] `PasswordDialog` beim Öffnen einer verschlüsselten Datei, ein Passwortfeld
* [ ] Falsches Passwort: Dialog bleibt mit Hinweis offen, erneuter Versuch oder Abbrechen möglich
* [ ] Abbrechen lässt das aktuelle Projekt unverändert
* [ ] Feld sofort nach dem Lesen leeren, Passwort als `CharArray` weitergeben

## Aufgabe 3: Verschlüsselungsdialog

* [ ] `EncryptionOptionsDialog` nach der Pfadwahl bei Speichern unter
* [ ] Schalter „Verschlüsseln“, Passwort und Wiederholung, beide müssen übereinstimmen
* [ ] Bei aktivem Schalter leeres Passwort ablehnen, keine weiteren Regeln
* [ ] Voreinstellung aus dem aktuellen Zustand des Projekts
* [ ] Hinzufügen, Ändern und Entfernen des Passworts nur hier, kein eigener Menüpunkt

## Aufgabe 4: Tests und Doku

* [ ] Nachrichtenschlüssel ergänzen, Übersetzung über Agent `translator`
* [ ] Tests: Wiederholung, leeres Passwort, Voreinstellung, falsches Passwort mit erneutem Versuch
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
