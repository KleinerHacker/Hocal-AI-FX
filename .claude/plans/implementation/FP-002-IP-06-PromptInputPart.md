# FP-002-IP-06: Eingabeteil mit Prompt

## Voraussetzungen

* IP-01, IP-02, IP-03

## Aufgabe 1: Vorbereitung

* [ ] Skills `component`, `dialog`, `window`, `icons`, `fx-component-lifecycle`, `ui-styling`, `testing`, `translation`, `project-docs`, `ci-pipeline` laden
* [ ] Agenten `component-explore`/`component-creator`, `dialog-explore`/`dialog-creator` und `icon-creator` verwenden
* [ ] Entschieden: Dialog mit OK und Abbrechen, ESC/X = Abbrechen, kein Dialog-Icon, modal, Besitzer ist das Hauptfenster
* [ ] Entschieden: Chips mit Namen, Vorschaubild und Entfernen-Button; je Dialog genau ein Bild
* [ ] Entschieden: Schaltflächen mit Zeichen bekommen immer Icons; Icons sind Teil dieses Plans
* [ ] Entschieden: Flow-Leiste mit `SegmentedButton` aus ControlsFX, bereits Abhängigkeit; Link `https://controlsfx.github.io/` in `external-docs.md`
* [ ] Entschieden: Absenden-Schaltfläche mit Handler ohne Implementierung; Generierung folgt in FP-006
* [ ] Fortschritt in `.claude/plans/implementation/FP-002-IP-06-PromptInputPart.md` abhaken

## Aufgabe 2: Icons

* [ ] `icon-creator`: `add` (Plus), `remove` (Kreuz) und `send` (Absenden), hell und dunkel, je 16 und 32 px, im Stil der Ordner-Icons
* [ ] `HocalIcons`: Konstanten `ADD`, `REMOVE`, `SEND` und Größenliste `BUTTON_SIZES` ergänzen
* [ ] Icon-Tests in `HocalIconsTest`: Größe, Transparenz, Hell-/Dunkel-Variante
* [ ] Schaltflächen zeigen nur das Icon; Tooltip und Zugänglichkeitstext kommen aus dem Nachrichten-Bundle

## Aufgabe 3: Dialog-Gerüst (erster Dialog des Projekts)

* [ ] Objektklasse `HocalDialog` im Paket `ui` anlegen; alle Dialoge werden dort registriert
* [ ] `app/ui/build.gradle.kts`: Quellsatz `demo` mit Task `runDialogDemo`, nicht von `build`, `check` oder `test` gestartet
* [ ] Quellsatz `demo` wird kompiliert, aber nicht in JAR oder Distribution ausgeliefert
* [ ] `DialogDemo.kt` unter `app/ui/src/demo/kotlin/.../ui/dialog` mit Button für den Galerie-Dialog und Beispieldaten
* [ ] `ci-pipeline`-Skill prüfen: neuer Quellsatz darf die Workflows nicht brechen

## Aufgabe 4: Galerie-Dialog (Paket `ui.dialog`)

* [ ] `GalleryDialog` (Alert), `GalleryDialogView`, `GalleryDialogViewModel` und FXML anlegen; Inhalt in `dialogPane`, Header entfernt
* [ ] Inhalt: `ProjectTree` links, `ProjectFileList` rechts; Liste folgt dem gewählten Ordner des Baums
* [ ] Eingaben: Wurzelordner und `MediaSource`; Ergebnis ist der gewählte `VirtualEntry` oder `null`
* [ ] Ordner sind nicht wählbar; OK ist nur bei gewähltem Eintrag aktiv; ESC und X liefern `null`
* [ ] `HocalDialog.gallery(owner, folder, source)` zeigt den Dialog und liefert das Ergebnis

## Aufgabe 5: Regelwerk

* [ ] `.claude/skills/dialog/SKILL.md`: Abschnitt „Template“ auf `GalleryDialog*.kt` und `HocalDialog` umstellen, Satz „Es gibt noch keinen Dialog“ entfernen
* [ ] Skill `dialog` nach dem Vorbild von `component` und `window`: Component, View, ViewModel als zu übernehmende Struktur nennen
* [ ] Agenten `dialog-explore` und `dialog-creator` auf Verweise zur Vorlage prüfen und anpassen
* [ ] `.claude/rules/external-docs.md`: Abschnitt ControlsFX mit `https://controlsfx.github.io/` ergänzen
* [ ] Feature-Plan FP-002 und IP-06-Plan: „Icons sind nicht Teil eines Plans“ streichen, Toggle-Leiste als Flow-Auswahl, Absenden-Schaltfläche ergänzen

## Aufgabe 6: Modell-/Flow-Schnittstelle und Vorschau-Laden

* [ ] `ModelOption` und `FlowOption` (Kennung, Name); Schnittstelle `ModelProvider` mit Modellen und den Flows je Modell, in `ui.prompt`
* [ ] `PlaceholderModelProvider` mit drei Platzhalter-Modellen und je zwei oder drei Flows; echte Daten folgen mit FP-009
* [ ] Wechsel des Modells erneuert die Flow-Leiste; gleicher Flow bleibt gewählt, sonst der erste; ein einzelner Flow ist vorgewählt
* [ ] `ThumbnailLoader` in `ui.preview`: Pool, Registry und Cache; lädt ein Medium asynchron, Ergebnis auf dem FX-Thread
* [ ] Chip lädt das letzte Medium der Referenz; Entfernen des Chips bricht das Laden ab
* [ ] Umstellung der Dateiliste auf `ThumbnailLoader` bleibt offen, nicht Teil dieses Plans

## Aufgabe 7: Eingabeteil (Paket `ui.component`)

* [ ] `PromptInput`, `PromptInputView`, `PromptInputViewModel`, FXML und CSS anlegen
* [ ] Oben links Schaltfläche `add`, rechts oben ComboBox für das Modell, darunter die Flow-Leiste als `SegmentedButton`
* [ ] Darunter Chips-Bereich, Platzhalter für Parameter, mehrzeilige Textbox mit Absenden-Schaltfläche `send` dahinter
* [ ] Absenden ist aktiv bei nicht leerem Prompt; Klick ruft einen austauschbaren Handler auf, vorerst ohne Implementierung
* [ ] Properties: `promptProperty`, `selectedModelProperty`, `selectedFlowProperty`, `folderProperty`, `mediaSourceProperty`, Referenzen
* [ ] `add` öffnet den Dialog über eine austauschbare Funktion; Ergebnis wird als Chip gehalten, Doppelte werden ignoriert
* [ ] Chip zeigt Namen, Vorschau des letzten Mediums und `remove`-Icon; Referenz-UUID identifiziert das Bild
* [ ] Hell-/Dunkel-Icons: `dark` aus dem Systemerscheinungsbild, Beobachtung nur zwischen `onShow` und `onHide`
* [ ] Kein Speichern des Prompts, keine Generierung, keine Bilder in der Historie

## Aufgabe 8: Fenster, Texte und Doku

* [ ] `MainWindowView.fxml`: `pnlMedia` wird `BorderPane`, Mitte Platzhalter `pnlImage` für IP-05, unten `PromptInput`
* [ ] Schlüssel in `bundle.properties` für Eingabeteil, Tooltips und Dialog; Übersetzung über Agent `translator`
* [ ] README, MkDocs-Hauptfensterseite, KDoc und CHANGELOG ergänzen; Eingabeteil ist im Fenster sichtbar
* [ ] IP-04-Plan: Bullet für Bindung von `folderProperty`, `mediaSourceProperty` und Aufruf bei Medienänderung ergänzen
* [ ] FP-006-IP-03-Plan: Absenden-Schaltfläche, Icon und Flow-Leiste kommen aus FP-002 IP-06; dort nur Regeln, Kürzel und Daten ergänzen

## Aufgabe 9: Tests

* [ ] Dialog (TestFX): Baum und Liste, nur Einträge wählbar, OK aktiv nur bei Eintrag, Abbrechen und ESC liefern `null`
* [ ] `PromptInputViewModelTest`: Chips (hinzufügen, doppelt, entfernen, Abbruch), Absenden-Aktivierung, Handler, Modell-und-Flow-Synchronisation
* [ ] `PromptInputTest` (TestFX): Bedienelemente, Textbox, Flow-Leiste, Chips mit Vorschau, `add` ruft Funktion mit Ordner und Quelle
* [ ] `ThumbnailLoaderTest`: Laden, defektes Bild, Abbruch, Cache-Treffer
* [ ] Lebenszyklus: drei Wege vom Bildschirm weg; `MainWindowIT`: Eingabeteil unter dem Bild, Skalierung unverändert
* [ ] Nutzer bittet, den Dialog in der Demo hell und dunkel anzusehen; das geht nur am Rechner

## Abschluss

* [ ] Änderungen pushen, CI von PR #17 abwarten und Ergebnis prüfen; Fehler beheben
* [ ] Skill `project-docs`: KDoc, README, MkDocs, CHANGELOG prüfen
* [ ] Plan-Datei per `git rm` entfernen; Feature-Plan und Feature-Status im selben Änderungssatz abhaken
