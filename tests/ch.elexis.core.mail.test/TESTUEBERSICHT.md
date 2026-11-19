# Testuebersicht Mail-Kern, Auftrag A

Stand: 13.11.2026, Ticket 28645, Branch `28645_Mail_Core`.

Das Fragment enthaelt **30 mit `@Test` annotierte Methoden in 6 Testklassen**. `AllTests` enthaelt alle sechs Klassen. Die vier vorhandenen Klassen `MailAccountTest`, `MailClientTest`, `MailClientImapTest` und `MailTextTemplateTest` bleiben unveraendert.

| Klasse | Methoden | Pruefinhalt |
|---|---:|---|
| `MailLegacyMigrationTest` | 7 | Protokoll-/TLS-Abbildung, Vorgabewerte, gepaddetes Base64, verschluesselte Passwoerter, Mandanten, Erhalt der Alt-Schluessel, bestehende Konten, lokale Konten, Fehlerfortsetzung, vorhandener Marker und zwei konkurrierende Migrationsstarts |
| `MailClientFacadeTest` | 11 | gemeinsame Konten in Konfigurationsreihenfolge, persoenliche Konten im Systempfad abweisen, passwortfreie Sicht, lokaler Vorrang, lokales Speichern/Loeschen, Entfernen neuer Schluessel, 80-Zeichen-Grenze, SMTP ohne Benutzer, lokale Zugangsdaten, Fehlerabbildung, Dateianhang, CLIENT-/SERVER-Gesendet-Kopie, IMAP trotz SMTP-Sicht und Flag-Filter |

`MailLegacyTestSupport` verdrahtet die Produktionsklassen mit isolierten Konfigurations- und Model-Service-Fakes. Der Migrationstest simuliert einen eindeutigen Datenbank-Insert atomar; er ersetzt keinen Test gegen die echte Praxisdatenbank mit zwei Stationen. Die Fassade verwendet echte GreenMail-Server mit dynamischen SMTP-/IMAP-Ports.

Die neuen Klassen lassen sich ohne OSGi als eigenstaendige JUnit-Tests ausfuehren. Dafuer liegt ein lokaler Hilfsaufruf in `_codex/check_a.py`; Details, tatsaechliche Ergebnisse und offene Pruefungen stehen in `_codex/REPORT-A.md`. Die alten dienstabhaengigen Tests sowie die vollstaendige Plug-in-Suite muessen in Eclipse ausgefuehrt werden.
