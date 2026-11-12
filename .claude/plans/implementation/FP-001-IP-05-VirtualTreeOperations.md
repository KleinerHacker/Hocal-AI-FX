# FP-001-IP-05: Operationen auf dem virtuellen Baum

## Voraussetzungen

* IP-01 und IP-04 abgeschlossen, GitHub-Build grün

## Aufgabe 1: Namen und Fehler

* [ ] Neues Paket `org.pcsoft.app.hocal.model.tree` in `lib/model`, in `module-info` exportieren
* [ ] Objekt `TreeNames` mit Namensregeln, per KDoc dokumentiert: nicht leer, keine Leerzeichen am Rand, höchstens 255 Zeichen
* [ ] Verboten: Steuerzeichen, `/ \ : * ? " < > |` sowie die Namen `.` und `..`, der Wurzelname bleibt leer
* [ ] Eindeutigkeit unter Geschwistern ohne Beachtung der Groß-/Kleinschreibung, Ordner und Einträge teilen sich den Namensraum
* [ ] Sealed `TreeException` mit `InvalidNameException`, `DuplicateNameException`, `NodeNotFoundException`
* [ ] Dazu `TreeCycleException`, `RootNodeException`, `UnknownReferenceException`, alle mit Namen oder UUID in der Meldung
* [ ] Verwaiste Referenzen beim Löschen sind normaler Kontrollfluss und kein Fehler, dafür gibt es keine Exception

## Aufgabe 2: Operationen

* [ ] Erweiterungsfunktionen auf `ProjectFile`: `parentOf(node)` sucht per Objektidentität, nicht per Wertgleichheit
* [ ] `createFolder(parent, name)` und `createEntry(parent, name, reference)`, leere Ordner erlaubt, Referenz muss existieren
* [ ] `rename(node, name)`: Wechsel nur der Groß-/Kleinschreibung bleibt erlaubt, Wurzel nicht umbenennbar
* [ ] `move(node, target)`: Zyklus (Ziel gleich Knoten oder darin) verhindern, Namenskollision am Ziel ablehnen, Wurzel nicht verschiebbar
* [ ] Mehrere Einträge dürfen dieselbe Referenz verlinken, auch im selben Ordner unter verschiedenen Namen
* [ ] `orphansOf(node)`: Referenzen, die nach dem Löschen des Knotens kein Eintrag mehr verlinkt, ändert nichts, Grundlage der UI-Warnung
* [ ] `delete(node, store, removeOrphans = false): Boolean`: verlöre eine Referenz ihren letzten Eintrag, ändert es nichts und gibt `false` zurück
* [ ] Mit `removeOrphans = true` werden Knoten, verwaiste Referenzen und deren Medien entfernt, Rückgabe `true`
* [ ] Ohne verwaiste Referenzen löscht `delete` immer und gibt `true` zurück, Wurzel nicht löschbar
* [ ] `removeReference(store, uuid)`: alle Einträge der Referenz, die Referenz und ihre Medien entfernen, ausdrücklicher Aufruf
* [ ] Baum und Referenzliste werden vor den Medien geändert, ein Fehler der Ablage hinterlässt nur unbenutzte Dateien

## Aufgabe 3: Ablage erweitern

* [ ] `MediaStore.deleteReference(reference)` mit Standardimplementierung über `numbers` und `delete`
* [ ] `DirectoryMediaStore` überschreibt es und entfernt zusätzlich den leeren Referenzordner
* [ ] Tests der Ablage: Medien weg, Ordner weg, unbekannte Referenz ohne Fehler

## Aufgabe 4: Tests und Doku

* [ ] Tests Namen: leer, Leerzeichen am Rand, Länge, verbotene Zeichen, `.` und `..`, gültige Namen mit Umlauten
* [ ] Tests Anlegen, Umbenennen, Verschieben: Erfolgsfall, doppelter Name auch bei anderer Groß-/Kleinschreibung, unbekannte Referenz
* [ ] Tests Verschieben: Zyklus auf sich selbst und auf Nachfahren, Wurzel, Knoten außerhalb des Baums
* [ ] Tests Löschen: Eintrag mit weiterem Verweis löscht ohne Rückfrage, gibt `true` zurück, Referenz und Medien bleiben
* [ ] Tests Löschen: letzter Eintrag ohne `removeOrphans` gibt `false` zurück, Baum, Referenzen und Medien unverändert
* [ ] Tests Löschen: mit `removeOrphans = true` verschwinden Referenz und Medien, Rückgabe `true`
* [ ] Tests Löschen: Ordner mit mehreren Einträgen derselben Referenz, `orphansOf` ändert nichts, Wurzel nicht löschbar
* [ ] Test `removeReference` mit mehreren Einträgen in verschiedenen Ordnern
* [ ] Test: nach jeder Operation besteht der Validator, Speichern als ZIP und Lesen funktioniert, Nummern und Medien passen
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Kein lokaler Build möglich, pushen und auf den GitHub-Build warten
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Feature-Plan: Abschnitt „As Built“ ergänzen, Entscheidung zu verwaisten Referenzen und Namensregeln festhalten
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
