# FP-005: Übersicht

Feature-Plan: `.claude/plans/features/FP-005-ComfyUiRuntime.md`

## Teilpläne

* IP-01 Runtime-Bibliotheks-Grundlage: `FP-005-IP-01-RuntimeLibraryFoundation.md`
* IP-02 Download und Archivbehandlung: `FP-005-IP-02-DownloadAndArchiveHandling.md`
* IP-03 Python-Umgebung: `FP-005-IP-03-PythonEnvironment.md`
* IP-04 ComfyUI-Runtime-Installation: `FP-005-IP-04-ComfyUiRuntimeInstallation.md`
* IP-05 Dienst-Prozessverwaltung: `FP-005-IP-05-ServiceProcessManagement.md`
* IP-06 Erststart-Ablauf: `FP-005-IP-06-FirstStartSetupFlow.md`
* IP-07 Dienststeuerung in der Anwendung: `FP-005-IP-07-ServiceControlInApplication.md`

## Reihenfolge

* IP-01: Voraussetzung FP-004 IP-01
* IP-02: Voraussetzung IP-01
* IP-03: Voraussetzung IP-02
* IP-04: Voraussetzung IP-03
* IP-05: Voraussetzung IP-01, FP-004 IP-03; parallel zu IP-02 bis IP-04 möglich
* IP-06: Voraussetzung IP-04, IP-05
* IP-07: Voraussetzung IP-05, FP-003 IP-02
