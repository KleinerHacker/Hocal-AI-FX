# FP-001-IP-05: Operationen auf dem virtuellen Baum

## Voraussetzungen

* IP-01

## Aufgabe 1: Operationen

* [ ] Ordner und Einträge anlegen, umbenennen, verschieben und löschen
* [ ] Leere Ordner erlauben, ein Eintrag verlinkt immer eine vorhandene Referenz
* [ ] Eine UUID darf von mehreren Einträgen verlinkt werden
* [ ] Eindeutige Geschwisternamen, Zyklus beim Verschieben verhindern
* [ ] Namensregeln festlegen und per KDoc dokumentieren
* [ ] Löschen eines Eintrags löscht nie Medien
* [ ] Referenz entfernen als eigene Aktion, entfernt Ordner und UUID
* [ ] Typisierte Fehler für jeden ungültigen Vorgang

## Aufgabe 2: Tests und Doku

* [ ] Tests je Operation und je Fehlerfall
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
