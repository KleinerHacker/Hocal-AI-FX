# FP-002-IP-08: Verschlüsselung in eigenem Lib `lib/crypto`

## Aufgabe 1: Vorbereitung

* Skills `fx-model`, `testing`, `project-docs`, `ci-pipeline` laden
* Entschieden: Beide Pläne gehören zu FP-002, gleicher Branch und PR; Plan-Dateien `FP-002-IP-08-CryptoLib.md` und `FP-002-IP-09-DiskThumbnailCache.md`
* Entschieden: Neuer Lib `lib/crypto` (`hocal-crypto`), JavaFX-frei, ohne Abhängigkeit zu anderen Modulen
* Entschieden: Die öffentlichen Fehlertypen von `lib/model` bleiben gleich; die Oberfläche merkt vom Umbau nichts
* Feature-Plan und Status um IP-08 und IP-09 ergänzen; IP-09 hängt an IP-08 und IP-05

## Aufgabe 2: Modul und Regelwerk

* `architecture.md`: `lib/crypto` als Verschlüsselungs-Lib nennen, JavaFX-frei
* `settings.gradle.kts`: `:lib:crypto` einbinden und `hocal-crypto` nennen
* `lib/crypto/build.gradle.kts` und `module-info.java` anlegen; Paket `org.pcsoft.app.hocal.crypto` wird exportiert
* `lib/model` hängt per `implementation` an `hocal-crypto`, `module-info.java` ergänzt `requires`
* Skill `ci-pipeline` prüfen, README-Strukturabschnitt und `fx-model`-Skill ergänzen

## Aufgabe 3: Verschieben nach `lib/crypto`

* Per `git mv`: `ProjectCipher`, `EncryptionHeader`, `BlockCipherStreams` aus `lib/model` in den neuen Lib; `ProjectCipher` heißt dort `BlockStreamCipher`
* Eigene Fehler im Lib: `CryptoException` mit Passwort fehlt, falsches Passwort, manipuliert, unbekannte Verschlüsselung, ungültiger Header
* `ProjectStorage` übersetzt die Fehler des Libs in die bisherigen Projektfehler; Verhalten und öffentliche Fehlertypen bleiben
* Verfahren bleiben unverändert: AES-256-GCM blockweise, PBKDF2-HMAC-SHA256 mit 600.000 Runden; Dateiformat bleibt kompatibel
* `lib/model` hat danach keine eigene Krypto mehr; Pakete und Exporte in `module-info.java` bereinigen

## Aufgabe 4: Neu im Lib

* `KeyDerivation`: Schlüssel einmal aus Passwort und Salt ableiten; Standardrunden als gemeinsame Konstante
* `RecordCipher`: einzelne Datensätze mit abgeleitetem Schlüssel versiegeln und öffnen, zufällige IV je Datensatz, zusätzliche authentifizierte Daten
* `RecordCipher.close()` überschreibt die Schlüsselbytes mit Nullen; das übergebene Passwort wird nicht verändert

## Aufgabe 5: Tests

* Bestehende Krypto-Tests aus `lib/model` in `lib/crypto` verschieben; Speicher-Tests in `lib/model` laufen unverändert weiter
* Neue Tests für `KeyDerivation` und `RecordCipher`: Rundlauf, falscher Schlüssel, Manipulation, andere zusätzliche Daten, Schließen
* Regressionstest (`RT`) in `lib/model`: eine vorhandene verschlüsselte Projektdatei wird weiter gelesen

## Abschluss

* Änderungen pushen, CI von PR #17 abwarten und Ergebnis prüfen; Fehler beheben
* Skill `project-docs`: KDoc, README, CHANGELOG prüfen; CHANGELOG bleibt, Nutzer sieht nichts
* Plan-Datei per `git rm` entfernen; Feature-Plan und Feature-Status im selben Änderungssatz abhaken
