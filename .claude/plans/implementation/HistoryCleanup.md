# HistoryCleanup: Historie vor der Veröffentlichung bereinigen

## Voraussetzungen

* Ausdrücklicher Auftrag des Nutzers: Historie im bestehenden Repo umschreiben, Force-Push auf `master` und den PR-Branch
* `.claude` bleibt im Repo, es gibt keine Tags, nur die Branches `master` und `ccr-f7b0ecbe-wryow1`

## Aufgabe 1: Vorbereitung

* [ ] Sicherung aller Refs als Git-Bundle im Scratchpad-Verzeichnis, nicht im Repo
* [ ] Baum-SHA des aktuellen Stands notieren, Vergleichswert für die spätere Prüfung
* [ ] Werkzeug: `git filter-branch` aus Git 2.43, `git filter-repo` ist nicht installiert und wird nicht nachinstalliert
* [ ] Identitäten aller 52 Commits auflisten, Autor und Committer, Zuordnung festlegen
* [ ] Treffer des früheren Projektnamens in Dateien und Commit-Texten der ganzen Historie suchen, nur maskiert ausgeben
* [ ] Die Schreibweisen des Namens aus den maskierten Treffern ableiten, der Name selbst steht nie in Befehlen oder Ausgaben
* [ ] Treffer der beiden E-Mail-Adressen und des Klarnamens in Dateiinhalten suchen

## Aufgabe 2: Umschreiben

* [ ] Autor und Committer beider E-Mail-Adressen auf `KleinerHacker <9840159+KleinerHacker@users.noreply.github.com>` setzen
* [ ] Die Identitäten `Claude <noreply@anthropic.com>` und `GitHub <noreply@github.com>` bleiben unverändert
* [ ] Zeilen `Claude-Session:` aus allen Commit-Texten entfernen, `Co-Authored-By` bleibt
* [ ] Treffer des Projektnamens in Dateien durch neutralen Text ersetzen, in Commit-Texten ebenso
* [ ] E-Mail-Adressen und Klarname in Dateiinhalten ersetzen, falls gefunden
* [ ] Alle Refs neu schreiben, Sicherungs-Refs von `filter-branch` entfernen, Reflog leeren, `git gc --prune=now`

## Aufgabe 3: Prüfen vor dem Push

* [ ] Alle Commits: nur noch die Noreply-Adresse, `Claude` und `GitHub` als Autor und Committer
* [ ] Kein `Claude-Session` mehr in einem Commit-Text
* [ ] Kein Treffer von Projektname, E-Mail-Adressen und Klarname in Commit-Texten und allen Dateiständen der Historie
* [ ] Baum-SHA des neuen Stands identisch mit dem notierten, Dateiinhalt unverändert
* [ ] Anzahl der Commits unverändert, `git fsck` ohne Fehler

## Aufgabe 4: Force-Push

* [ ] `git push --force-with-lease` zuerst für `ccr-f7b0ecbe-wryow1`, danach für `master`, Ergebnis je Branch prüfen
* [ ] Wird `master` abgelehnt (Schutzregel, fehlende Berechtigung), nicht umgehen, Befehl und Sicherung dem Nutzer nennen
* [ ] Remote-Stand lesen: Hashes und Autoren der beiden Branches stimmen mit dem lokalen Stand überein

## Aufgabe 5: Hinweise an den Nutzer

* [ ] PR #7 ist nach dem Umschreiben von `master` nicht mehr mergebar und muss neu erstellt werden
* [ ] Der PR-Text und der Verlauf von PR #7 enthalten weiter einen Session-Link, vor dem Veröffentlichen den PR schließen und bereinigen
* [ ] Alte Commits bleiben bei GitHub über PR-Referenzen und die SHA-URL erreichbar, bei Bedarf den GitHub-Support um Bereinigung bitten
* [ ] Das Sicherungs-Bundle enthält die alte Historie, nach der Prüfung löschen, es endet mit der Session

## Abschluss

* [ ] Kein Build nötig, der Dateiinhalt bleibt gleich, der Force-Push startet einen neuen CI-Lauf
* [ ] Plan-Datei in `.claude/plans/implementation` ablegen und nach dem Abschluss mit `git rm` entfernen
