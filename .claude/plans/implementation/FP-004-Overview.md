# FP-004: Übersicht

Feature-Plan: `.claude/plans/features/FP-004-ComfyUiConnection.md`

## Teilpläne

* IP-01 Bibliotheks-Grundlage: `FP-004-IP-01-LibraryFoundation.md`
* IP-02 HTTP-Transport: `FP-004-IP-02-HttpTransport.md`
* IP-03 ComfyUI-Fassade: `FP-004-IP-03-ComfyUiFacade.md`
* IP-04 Ausführungs-Ereignis-Hook: `FP-004-IP-04-ExecutionEventHook.md`
* IP-05 Stub-Server und Integrationstests: `FP-004-IP-05-StubServerAndIntegrationTests.md`

## Reihenfolge

* IP-01: keine Voraussetzung
* IP-02: Voraussetzung IP-01
* IP-03: Voraussetzung IP-02
* IP-04: Voraussetzung IP-01, parallel zu IP-02 und IP-03 möglich
* IP-05: Voraussetzung IP-03, IP-04
