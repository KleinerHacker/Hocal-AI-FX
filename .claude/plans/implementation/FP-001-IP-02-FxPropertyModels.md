# FP-001-IP-02: FX-Property-Modelle

## Voraussetzungen

* IP-01 abgeschlossen, GitHub-Build grün

## Aufgabe 1: Modul und Infrastruktur

* [ ] Skills `fx-model`, `testing`, `project-docs`, `ci-pipeline` laden
* [ ] Agent `model-explore` ausführen, Referenz `/home/user/kleinerhacker/a reference project/lib/fx-model` im Prompt nennen
* [ ] Modul `lib/fx-model` (`hocal-fx-model`) in `settings.gradle.kts` eintragen
* [ ] `build.gradle.kts` wie Referenz: `java-library`, JavaFX-Plugin nur `javafx.base`, `api` auf `hocal-model`
* [ ] `module-info.java`: `requires transitive` für `javafx.base` und `org.pcsoft.app.hocal.model`, Pakete exportieren
* [ ] Kotlin und `module-info` in dasselbe Ausgabeverzeichnis kompilieren lassen, wie in der Referenz
* [ ] `BeanFields` aus der Referenz nach `org.pcsoft.app.hocal.fx.model.internal` übernehmen, Paket anpassen

## Aufgabe 2: Anpassung in lib/model

* [ ] `VirtualFolder` und `VirtualEntry`: `equals`, `hashCode`, `toString` nach Werten von Hand ergänzen
* [ ] Keine Data Class für die Knoten, Hierarchie bleibt `sealed`
* [ ] Tests für `equals`, `hashCode` und `toString` der Knoten ergänzen

## Aufgabe 3: Property-Modelle (Agent `model-creator`)

* [ ] `ProjectFileProperty` als Wurzel mit optionalem Start-Objekt: `project`, `tree` über `model`, `references` über `list`
* [ ] `ProjectDataProperty`: Texte über `string`, `formatVersion` über `integer`, `tags` über `list`
* [ ] `ProjectDataProperty`: `created` und `modified` als `Instant?` über `reference`
* [ ] `ReferenceProperty`: `uuid` über `string`, `media` als einfache Objekte über `list`
* [ ] `MediumProperty`: `prompt`, `model`, `flow` über `string`, `seed` als `Long?` über `reference`
* [ ] `MediumProperty`: `parameters` als `Map<String, String>` über `reference`
* [ ] `VirtualFolderProperty`: `name` über `string`, `children` als einfache Knoten über `list`
* [ ] `VirtualEntryProperty`: `name` und `reference` über `string`
* [ ] Fabrik `of(...)` für herausgegriffene Listeneinträge wie `ChapterProperty.of` der Referenz
* [ ] `refresh()` je Modell mit dem Rumpf `fields.refresh()`, KDoc und Lizenzkopf überall

## Aufgabe 4: Tests

* [ ] `ChangeRecorder` aus der Referenz in den Test-Quellen übernehmen
* [ ] Je Modell alle Pflichtfälle des Skills `fx-model` für alle Felder
* [ ] Änderungsweitergabe bis zur Wurzel: `ProjectFileProperty`, `ProjectDataProperty`, `VirtualFolderProperty`
* [ ] Test `refresh()` bei Schreibzugriff am POJO vorbei
* [ ] Test Austausch des umschlossenen Objekts, mit gleichen und mit anderen Werten
* [ ] Test der `of`-Fabriken und der Listen-Operationen Hinzufügen, Entfernen, Austauschen

## Aufgabe 5: Doku und Pipeline

* [ ] README-Strukturabschnitt um `lib/fx-model` ergänzen
* [ ] Pipeline `ci.yml` und `release.yml` auf das neue Modul prüfen
* [ ] Feature-Plan: Abschnitt „As Built“ zu IP-02 ergänzen

## Abschluss

* [ ] Kein lokaler Build möglich (kein JDK 25), stattdessen pushen und auf den GitHub-Build warten
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG unverändert
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
