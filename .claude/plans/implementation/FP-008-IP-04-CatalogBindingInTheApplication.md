# FP-008-IP-04: Katalog-Anbindung in der Anwendung

## Voraussetzungen

* IP-02, FP-002 IP-06, FP-006 IP-01

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `ui-styling`, `fx-component-lifecycle` und `translation` laden
* [ ] Mit FP-002 IP-06 und FP-006 IP-01 abgleichen, bevor umgesetzt wird

## Aufgabe 2: Auswahl

* [ ] Schnittstelle der Modellliste aus FP-002 IP-06 mit dem Katalog umsetzen
* [ ] Toggle-Leiste zeigt die Flows des gewählten Modells und wechselt mit der Auswahl
* [ ] Link zur Hugging-Face-Seite des Modells in der Liste

## Aufgabe 3: Parameterbereich

* [ ] Ausklappbarer Parameterbereich im Eingabeteil, aufgebaut aus den Parametern des Flows
* [ ] Eingabe nach Typ: Textfeld, Zahlenfeld oder Regler, Auswahlliste, Schalter, mit Vorgabe vorbelegt
* [ ] `EDITABLE` als Eingabe, `READ_ONLY` deaktiviert angezeigt, `CONSTANT` nicht angezeigt

## Aufgabe 4: Standardfelder

* [ ] `prompt` ist die Textbox, `negativePrompt` ein zweites Textfeld, wenn vorhanden
* [ ] `seed` als Zahl mit Schalter „zufällig“, standardmäßig an
* [ ] `inputImage` entscheidet, ob das angezeigte Medium gesendet wird, `extraImages` zeigt die Galerie-Taste

## Aufgabe 5: Werte und Historie

* [ ] Gesammelte Werte an den Builder aus FP-006 IP-01 und den `GenerationService` übergeben
* [ ] Historie speichert Katalog-Ids für Modell und Flow, den Seed und die Werte der `EDITABLE`-Parameter

## Aufgabe 6: Texte, Tests und Doku

* [ ] Nachrichtenschlüssel anlegen, Übersetzung nur über den Agenten `translator`
* [ ] Tests: Flow-Wechsel, Darstellung je Typ und `mode`, Seed-Schalter, Bildfelder
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
