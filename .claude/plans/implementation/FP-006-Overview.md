# FP-006: Übersicht

Feature-Plan: `.claude/plans/features/FP-006-GenerationIntegration.md`

## Teilpläne

* IP-01 Workflow-Builder: `FP-006-IP-01-WorkflowBuilder.md`
* IP-02 Generierungsdienst: `FP-006-IP-02-GenerationService.md`
* IP-03 Sende-Taste und Prompt-Absenden: `FP-006-IP-03-SendButtonAndPromptSubmission.md`
* IP-04 Fortschrittskachel: `FP-006-IP-04-ProgressTile.md`
* IP-05 Ergebnis im Projekt speichern: `FP-006-IP-05-ResultStorageInProject.md`

## Reihenfolge

* IP-01: Voraussetzung FP-004 IP-03, FP-009 IP-02
* IP-02: Voraussetzung IP-01, FP-004 IP-04, FP-005 IP-05
* IP-03: Voraussetzung IP-02, FP-002 IP-06; parallel zu IP-04 und IP-05 möglich
* IP-04: Voraussetzung IP-02, FP-002 IP-01, FP-002 IP-04; parallel zu IP-03 und IP-05 möglich
* IP-05: Voraussetzung IP-02, FP-001 IP-07, FP-002 IP-05; parallel zu IP-03 und IP-04 möglich
