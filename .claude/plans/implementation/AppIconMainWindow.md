# Anwendungs-Icon im Hauptfenster

## Aufgabe 1: Referenz ablegen

* Hochgeladenes Bild als `docs/docs/assets/images/icon.png` ablegen (1319x1192, transparent)
* Datei mit `git add` hinzufügen

## Aufgabe 2: Icons erzeugen (Agent `icon-creator`)

* Agent `icon-creator` aus der Hauptsession starten, Referenz `icon.png` und Skill `icons` übergeben
* Größen `app@16`, `app@24`, `app@32`, `app@48`, `app@64`, `app@128`, `app@256` erzeugen
* Große Größen (256, 128, 64, 48) per hochwertiger Verkleinerung (Lanczos) aus dem Original
* Kleine Größen (32, 24, 16) neu zeichnen: Helm, Visier, "AI"-Schriftzug, ohne Glow und Swoosh
* Alle Dateien quadratisch, transparent, Motiv zentriert mit kleinem Rand
* Ablage flach in `app/ui/src/main/resources/icons`, danach `git add`
* Ergebnis jeder Größe visuell prüfen, bei Unschärfe neu zeichnen lassen

## Aufgabe 3: HocalIcons anlegen

* Skill `ui-styling` und `icons` vor der Änderung unter `app/ui` laden
* Objekt `HocalIcons` im Paket `org.pcsoft.app.hocal.app` anlegen, mit KDoc
* Dateiname aus Name und Größe bauen: `<name>@<size>.png`, Schema nur hier
* Funktion `image(name, size)` mit Cache laden, Ressource unter `/icons/`
* Funktion `appIcons()` liefert alle `app`-Größen als Liste von `Image`
* Fehlende Ressource führt zu aussagekräftiger Exception mit Dateiname

## Aufgabe 4: Hauptfenster

* In `MainWindow.kt` `icons.setAll(HocalIcons.appIcons())` im `init`-Block setzen
* KDoc-Hinweis "carries no icon yet" entfernen und Verweis auf `HocalIcons` anpassen
* Prüfen, ob das Ressourcenverzeichnis `icons` im Modul `module-info.java` Zugriff braucht

## Aufgabe 5: Tests

* Skill `testing` vor dem Anlegen der Testklasse laden
* Test `HocalIconsTest`: jede Größe ladbar, Kantenlänge entspricht Größe, Transparenz vorhanden
* Test: unbekannter Name wirft Exception mit Dateinamen
* Test (TestFX): `MainWindow` hat nach Erzeugung sieben Icons
* Jede Testmethode mit ausführlichem KDoc

## Aufgabe 6: Dokumentation und Abschluss

* Skill `project-docs` laden; README-Featureliste und MkDocs auf Anwendungs-Icon prüfen
* `CHANGELOG.md` unter `[UNRELEASED]` ergänzen: Hauptfenster zeigt Anwendungs-Icon
* Gradle-Target `build` per Agent ausführen
* Änderungen committen und auf den Session-Branch pushen
* Plandatei nach Abschluss mit `git rm` entfernen
