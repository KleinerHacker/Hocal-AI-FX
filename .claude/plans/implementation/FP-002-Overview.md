# FP-002: Übersicht

Feature-Plan: `.claude/plans/features/FP-002-MainWindowLayout.md`

## Teilpläne

* IP-01 Hauptfenster-Splitter-Layout: `FP-002-IP-01-MainWindowSplitterLayout.md`
* IP-02 Verzeichnisbaum-Komponente: `FP-002-IP-02-DirectoryTreeComponent.md`
* IP-03 Dateiliste und Medien-Vorschau: `FP-002-IP-03-FileListAndMediaPreview.md`
* IP-04 Projekt-Anbindung und Fenster-Verdrahtung: `FP-002-IP-04-ProjectBindingAndWindowWiring.md`
* IP-05 Historienliste und Bildansicht: `FP-002-IP-05-HistoryListAndMediaView.md`
* IP-06 Eingabeteil mit Prompt: `FP-002-IP-06-PromptInputPart.md`
* IP-07 Panel mit Generierungsdaten: `FP-002-IP-07-GenerationDetailsPanel.md`

## Reihenfolge

* IP-01: Voraussetzung keine
* IP-02: Voraussetzung IP-01, FP-001 IP-02, FP-001 IP-07
* IP-03: Voraussetzung IP-01, FP-001 IP-02
* IP-04: Voraussetzung IP-02, IP-03, FP-001 IP-07
* IP-05: Voraussetzung IP-04, FP-001 IP-02
* IP-06: Voraussetzung IP-01
* IP-07: Voraussetzung IP-05, FP-001 IP-02
