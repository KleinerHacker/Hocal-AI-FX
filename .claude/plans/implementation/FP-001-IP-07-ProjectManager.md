# FP-001-IP-07: Projekt-Manager (Singleton)

## Voraussetzungen

* IP-02 bis IP-06 abgeschlossen, GitHub-Build grün

## Aufgabe 1: Grundgerüst

* [ ] Skills `fx-model`, `fx-component-lifecycle` und `testing` laden
* [ ] Neues Paket `org.pcsoft.app.hocal.fx.model.manager` in `lib/fx-model`, in `module-info` exportieren, nur `javafx.base`
* [ ] Kotlin-`object` `ProjectManager`, genau ein Projekt gleichzeitig, gehalten als `ProjectFileProperty` in `val project`
* [ ] Schreibgeschützte Properties: `loadedProperty`, `pathProperty` (`Path?`), `dirtyProperty`, `encryptedProperty`
* [ ] Medien liegen während der Sitzung in einem `DirectoryMediaStore` in einem temporären Ordner
* [ ] Temporärer Ordner pro Sitzung, optional vom Aufrufer vorgegebenes Wurzelverzeichnis, beim Schließen vollständig gelöscht
* [ ] Das Passwort aus `open` oder `saveAs` bleibt als Kopie im Speicher, damit `save` nicht erneut fragt, wird beim Schließen überschrieben
* [ ] Konfigurierbare `applicationVersion` und `threadCheck`, weil `Platform` in `javafx.base` fehlt, `app/ui` setzt beides beim Start

## Aufgabe 2: Lebenszyklus

* [ ] `createNew(name)`: leeres Projekt mit neuer UUID und Erstellzeit, ohne Pfad, `dirty = true`
* [ ] `open(path, password?)`: liest Projekt und Medien in die Ablage, bei Fehler bleibt der Manager geschlossen und der Ordner weg
* [ ] `createNew` und `open` bei geladenem Projekt: `IllegalStateException`, vorher schließen
* [ ] `close(discardChanges = false): Boolean`: bei `dirty` ohne Freigabe `false` und keine Änderung, sonst `true`
* [ ] `save()`: schreibt über Pfad und gespeichertes Passwort, ohne Pfad `IllegalStateException`, setzt Änderungszeit und Anwendungsversion
* [ ] `saveAs(path, password?)`: neuer Pfad und neues Passwort, ohne Passwort unverschlüsselt, danach `dirty = false`
* [ ] Das Passwort ändert der Nutzer nur über `saveAs`, der Manager hat keine eigene Methode zum Ändern des Passworts
* [ ] Während Laden und Speichern löst die interne Änderungsmeldung `dirty` nicht aus
* [ ] Datei-Ein- und Ausgabe blockiert den Aufrufer, in der KDoc vermerkt

## Aufgabe 3: Änderungen

* [ ] Alle Änderungen laufen über den Manager, jede prüft zuerst `threadCheck` und ein geladenes Projekt
* [ ] Baum: `createFolder`, `createEntry`, `rename`, `move`, `delete(node, removeOrphans = false): Boolean`, `orphansOf`, `removeReference`
* [ ] Medien: `addReference(medium, extension, content)` mit neuer UUID, `appendMedium`, `removeMedium`, Lesezugriff `openMedium` und `mediumExtension`
* [ ] Nach jeder Änderung `refresh()` des Property-Modells und `dirty = true`, nur wenn die Operation gelungen ist
* [ ] Änderung eines Feldes über das Property-Modell setzt `dirty` ebenfalls, über die Ungültig-Meldung des Wurzel-Modells
* [ ] Kein Zugriff von außen auf Speicher und Baum-Operationen über den Manager hinaus, per KDoc und Skill festgehalten
* [ ] Skill `fx-model`: Hinweis zum Halter des geladenen Projekts auf `ProjectManager` ändern

## Aufgabe 4: Tests und Doku

* [ ] Singleton-Zustand pro Test zurücksetzen über eine `internal`-Hilfsmethode, Arbeitsordner über `@TempDir`
* [ ] Tests Lebenszyklus: neu, Speichern unter, schließen, öffnen, Pfad und `dirty` je Schritt, Meldungen der Properties
* [ ] Tests: nur ein Projekt, `createNew` und `open` bei geladenem Projekt, `close` mit `dirty` ohne und mit Freigabe
* [ ] Tests: Fehler beim Öffnen (falsches Passwort, beschädigte Datei, fehlende Datei) lassen den Manager geschlossen, temporärer Ordner weg
* [ ] Tests Änderungen: jede Baum- und Medien-Operation setzt `dirty`, ein abgelehntes `delete` nicht, Property-Modelle zeigen den neuen Stand
* [ ] Test: neu, Referenz, Einträge, Speichern, Schließen, Öffnen liefert gleiches Projekt, Nummern und Medien passen
* [ ] Tests verschlüsselt: `saveAs` mit Passwort, `save` behält es, `saveAs` ohne Passwort wechselt zurück, Datei beginnt mit `HOCALENC`
* [ ] Tests: Änderungszeit und Anwendungsversion nach Speichern, Erstellzeit bleibt, `threadCheck` liefert `false` lässt Operationen scheitern
* [ ] KDoc, README und MkDocs prüfen

## Abschluss

* [ ] Kein lokaler Build möglich, pushen und auf den GitHub-Build warten
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Feature-Plan: Abschnitt „As Built“ ergänzen, Feature auf `COMPLETED` setzen
* [ ] Plan-Datei und `FP-001-Overview.md` mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
