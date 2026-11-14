# FP-002-IP-09: Thumbnail-Cache auf der Festplatte und eigener Lib

## Aufgabe 1: Vorbereitung

* Skills `fx-model`, `testing`, `project-docs`, `ci-pipeline`, `fx-component-lifecycle` laden
* Entschieden: Der neue Lib `lib/thumbnail` (`hocal-thumbnail`) nutzt aus `javafx.graphics` nur das Paket `javafx.scene.image`
* Entschieden: Der `ProjectManager` besitzt die Cache-Sitzung; das Passwort verlässt ihn nicht
* Entschieden: Cache nur für die Sitzung; Schlüssel aus dem Projektpasswort über `hocal-crypto`; Datei wird beim Schließen gelöscht

## Aufgabe 2: Modul und Regelwerk

* `architecture.md`: `lib/thumbnail` nennen; aus `javafx.graphics` nur `javafx.scene.image`
* `settings.gradle.kts` und `lib/thumbnail/build.gradle.kts` mit `module-info.java`; Tests laufen auf dem Klassenpfad
* `lib/thumbnail` hängt an `hocal-crypto`; `lib/fx-model` hängt per `api` an `hocal-thumbnail`; `app/ui` ergänzt `requires`
* Skill `ci-pipeline` prüfen, README-Strukturabschnitt ergänzen

## Aufgabe 3: Lib, Cache-Grundlagen (Paket `org.pcsoft.app.hocal.thumbnail`)

* `ThumbnailKey`, `ThumbnailCache`, `MemoryThumbnailCache`, `SizeClassCache` per `git mv` aus `app/ui` verschieben
* Alle Caches generisch über den Werttyp `V` machen; Lib-Tests nutzen Textwerte statt `Image`
* `TieredThumbnailCache<V>`: Speicher zuerst, dann Platte; Treffer von der Platte wandert in den Speicher
* Tests aus `app/ui` in den Lib verschieben und anpassen

## Aufgabe 4: Lib, Platten-Speicher

* `ThumbnailPixels`: Breite, Höhe und Pixelbytes (BGRA, vormultipliziert), einfache Datenklasse ohne JavaFX
* `ZipThumbnailStore` implementiert `ThumbnailCache<ThumbnailPixels>`: unkomprimierte (STORED) ZIP-Datei im Temp-Verzeichnis
* Einträge werden angehängt, der Index liegt im Speicher, gelesen wird direkt per `FileChannel` ohne Entpacken
* Beim Schließen wird das Zentralverzeichnis geschrieben, die Datei ist dann ein gültiges ZIP; danach wird sie gelöscht
* Medienanzahl steht je Eintrag im Inhalt; Invalidieren und Leeren wirken über den Index; ab 64 MB wird zurückgesetzt
* Thread-sicher: Schreiben unter Sperre, Lesen parallel; Dateifehler führen zu Fehltreffer statt Absturz
* Verschlüsselt über `RecordCipher`: Eintragsname als zusätzliche authentifizierte Daten, Eintragsname per HMAC verschleiert
* Passwortkopie und Schlüssel werden beim Schließen mit Nullen überschrieben

## Aufgabe 5: Lib, Bildumwandlung

* `ImageThumbnails` (`javafx.scene.image`): `Image` in `ThumbnailPixels` und zurück, farbgenau, nur für kleine Bilder
* Große Bilder der Bildansicht werden nicht auf die Platte geschrieben

## Aufgabe 6: ProjectManager (`lib/fx-model`)

* Der Manager legt `thumbnails.zip` im Sitzungsverzeichnis des Projekts an, bei Neu und Öffnen
* Verschlüsselt genau dann, wenn das Projekt ein Passwort hat; das Passwort wird als Kopie übergeben
* `ProjectManager.thumbnailCache`: stabile Fassade, die an den aktuellen Speicher delegiert; ohne Projekt Fehltreffer
* `saveAs` mit anderem Passwort oder Wechsel zwischen verschlüsselt und unverschlüsselt erneuert den Cache leer
* `close` und `release` schließen den Speicher und löschen die Datei; kein Rest im Temp-Verzeichnis

## Aufgabe 7: Anwendung (`app/ui`)

* `ThumbnailService.cache`: kleine Bilder zweistufig (Speicher 500, Platte über `ProjectManager.thumbnailCache`), große nur im Speicher
* Der Listener beim Projektwechsel leert weiter die Speicherstufe; die Platte leert der Manager
* `ThumbnailLoader`, Komponenten und Tests auf `ThumbnailCache<Image>` umstellen
* README, KDoc und Skill `fx-model` ergänzen; CHANGELOG nur, wenn Nutzer etwas sehen

## Aufgabe 8: Tests

* Lib: `ZipThumbnailStoreTest` für Speichern, Lesen, Medienanzahl, Invalidieren, Leeren, Grenze, Parallelität, gelöschte Datei
* Lib: Nach dem Schließen öffnet `java.util.zip.ZipFile` die Datei; Einträge sind STORED mit richtiger Prüfsumme
* Lib: verschlüsselter Speicher: Rundlauf, falscher Schlüssel, Manipulation, Namen verraten die Referenz nicht
* Lib: `TieredThumbnailCacheTest`; `fx-model`: `ProjectManagerTest` für Anlegen, Verschlüsselung, `saveAs`, Löschen beim Schließen
* `app/ui`: `ImageThumbnailsTest` für farbgenauen Rundlauf; `ThumbnailServiceTest` und `MainWindowIT` für den Platten-Treffer

## Abschluss

* Änderungen pushen, CI von PR #17 abwarten und Ergebnis prüfen; Fehler beheben
* Skill `project-docs`: KDoc, README, MkDocs, CHANGELOG prüfen
* Plan-Datei per `git rm` entfernen; Feature-Plan und Feature-Status im selben Änderungssatz abhaken
