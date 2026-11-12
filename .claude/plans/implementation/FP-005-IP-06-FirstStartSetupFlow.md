# FP-005-IP-06: Erststart-Ablauf

## Voraussetzungen

* IP-04, IP-05

## Aufgabe 1: Vorbereitung

* [ ] Skills `startup`, `dialog`, `ui-styling`, `fx-component-lifecycle` und `translation` laden
* [ ] Skill `startup` um eine Ausnahme für langlaufende Schritte mit Fortschrittsmeldung ergänzen

## Aufgabe 2: Startbereich

* [ ] Startbereich laut Skill anlegen, falls noch nicht vorhanden: `SplashStage`, `Startup`, `StartupStep`
* [ ] Splash-Screen zeigt Schritt, Downloadgröße, Fortschrittsbalken und Abbrechen
* [ ] Hauptfenster erst nach erfolgreich geprüfter Installation öffnen

## Aufgabe 3: Schritte

* [ ] Startschritt für die Umgebungsprüfung, schnell bei intakter Installation
* [ ] Startschritt für die Installation, Arbeit auf dem FX-Thread nur über `onFxThread`
* [ ] Download mit Zeitlimits und Abbruch, kein endloses Warten

## Aufgabe 4: Dialoge

* [ ] Zustimmungsdialog vor dem ersten Download: Inhalt, Quellen, Größen, Zielverzeichnis
* [ ] Ablehnen oder Abbrechen beendet das Programm, nichts Halbfertiges bleibt
* [ ] Fehlerdialog mit verständlichem Text und Logpfad, Wiederholen oder Beenden
* [ ] Dialoge erscheinen über dem Splash-Screen

## Aufgabe 5: Texte, Tests und Doku

* [ ] Nachrichtenschlüssel anlegen, Übersetzung nur über den Agenten `translator`
* [ ] Tests: Schritt ohne Argumente, Reihenfolge, Ablehnen, Fehlerpfad, Marshalling
* [ ] KDoc und README prüfen

## Abschluss

* [ ] Gradle `build` über einen Agenten mit vollem Pfad ausführen
* [ ] Skill `project-docs`: README, MkDocs, KDoc prüfen, CHANGELOG nur bei sichtbarer Änderung
* [ ] Plan-Datei mit `git rm` entfernen, Feature-Plan und Feature-Status im selben Änderungssatz abhaken
