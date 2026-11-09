# FP-001-IP-02: FX-Property-Modelle

## Voraussetzungen

* IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skill `fx-model` laden
* [ ] Agent `model-explore` ausführen, danach Agent `model-creator` verwenden
* [ ] Modul `lib/fx-model` (`hocal-fx-model`) in `settings.gradle.kts` eintragen
* [ ] Build-Datei nur mit `javafx.base` und Abhängigkeit auf `hocal-model`

## Aufgabe 2: Property-Modelle

* [ ] Je POJO eine `<Pojo>Property` im gespiegelten Paket `org.pcsoft.app.hocal.fx.model`
* [ ] Rekursiven Baum mit verschachtelter Änderungsweitergabe bis zur Wurzel abbilden
* [ ] Referenzliste, Historien-Details je Mediumnummer und Schlagworte in `BeanFields` registrieren
* [ ] `refresh()` je Modell mit dem Rumpf `fields.refresh()`

## Aufgabe 3: Tests und Doku

* [ ] Tests je Property-Modell, auch Weitergabe über mehrere Baum-Ebenen
* [ ] Test für `refresh()` bei Schreibzugriff am POJO vorbei
* [ ] Pipeline und README-Struktur prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
