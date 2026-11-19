# Testübersicht Core Mail UI

Stand: 13.11.2026, Ticket 28645, Auftrag B.

Das Fragment `ch.elexis.core.mail.ui.test` enthält 46 mit `@Test` annotierte Methoden in 2 Testklassen. `AllMailTests` umfasst `SendMailDialogTest` (1 SWT-Test) und den aus Medelexis verschobenen `MailAccountFormTest` (45 Tests). Der bisherige Importtest entfällt zusammen mit dem manuellen Import.

`MailUiAllTests.launch` wählt nur das Testfragment und die Persistenz-Bundles aus `MailAllTests.launch`, die übrigen Abhängigkeiten ergänzt PDE selbst (`automaticIncludeRequirements`). Die frühere feste Bundle-Liste startete nach dem Umzug des Mail-Kerns nicht mehr. Am 13.11.2026 mit dieser Launch: 46 von 46 grün. `SendMailDialogTest.testConfidentialSend` deckte auf, dass seit Ticket 27226 das Leerzeichen vor `(Vertraulich)` im Betreff fehlte; `SendMailDialog` setzt es wieder. Die Suite läuft nicht im CI. Dynamische OSGi-Beiträge, Mandantenauswahl, Standardkonto, Warnhinweise und Anbieterwahl mit und ohne Secrets müssen im Produkt geprüft werden.

## MailAccountFormTest

45 reine JUnit-4-Tests ohne SWT-Instanz prüfen Formularlogik, Vorgaben, ACL-Felder, berechtigte Scope-Wechsel, Gruppenhinweise, Signaturfilter, Validierung und Besitzerübergabe.

| Testmethode | Was wird geprüft | Bereich |
|---|---|---|
| `imapFillsPreset` | Standard-IMAP erhält leere Hosts, 993/SSL, 587/STARTTLS, PASSWORD und AUTO. | Kontenformular |
| `hinFillsPreset` | HIN erhält die vorgegebenen Hosts, Ports, TLS-Verfahren, PASSWORD und CLIENT. | Kontenformular |
| `m365FillsPreset` | Microsoft 365 erhält Outlook-Hosts, 993/SSL, 587/STARTTLS, OAUTH2 und SERVER. | Kontenformular |
| `googleFillsPreset` | Google erhält Gmail-Hosts, 993/SSL, 465/SSL, OAUTH2 und SERVER. | Kontenformular |
| `providerSwitchKeepsExplicitFields` | Anbieterwechsel erhält ausdrücklich bearbeitete Serverfelder und ersetzt unberührte Vorgaben. | Kontenformular |
| `explicitValueMatchingPresetIsStillKept` | Auch ein ausdrücklich eingegebener Wert, der der alten Vorgabe entspricht, bleibt erhalten. | Kontenformular |
| `googleWithoutSignInUsesPassword` | Google ohne konfigurierte Browseranmeldung verwendet PASSWORD. | Kontenformular |
| `personalAccountIncludesOwnerAndDelegates` | Persönliches Konto enthält Eigentümer und Stellvertretung, jedoch keine Praxis-Sichtbarkeitslisten. | Kontenformular |
| `sharedAccountIncludesVisibility` | Praxiskonto enthält Benutzer- und Gruppensichtbarkeit, ohne persönlichen Eigentümer und Stellvertretung. | Kontenformular |
| `existingScopeAndOwnerStayWithoutRight` | Ohne gemeinsames Verwaltungsrecht bleiben bestehender Geltungsbereich und Eigentümer unverändert; Scope-Wechsel werden nicht angeboten. | Berechtigung |
| `savedNewScopeStaysWithoutRight` | Ohne gemeinsames Verwaltungsrecht bleibt der Geltungsbereich auch nach dem ersten Speichern für OAuth gesperrt. | Berechtigung |
| `emptyAddressHasValidationKey` | Leere Adresse liefert MailSettings_InvalidAddress. | Kontenformular |
| `invalidAddressHasValidationKey` | Adresse ohne @ liefert MailSettings_InvalidAddress. | Kontenformular |
| `emptyImapHostHasValidationKey` | Leerer IMAP-Host liefert MailSettings_InvalidHost. | Kontenformular |
| `emptySmtpHostHasValidationKey` | Leerer SMTP-Host liefert MailSettings_InvalidHost. | Kontenformular |
| `nonNumericPortHasValidationKey` | Nicht numerischer Port liefert MailSettings_InvalidPort. | Kontenformular |
| `negativePortHasValidationKey` | Negativer Port liefert MailSettings_InvalidPort. | Kontenformular |
| `zeroPortHasValidationKey` | Port 0 liefert MailSettings_InvalidPort. | Kontenformular |
| `tooLargePortHasValidationKey` | Port über 65535 liefert MailSettings_InvalidPort. | Kontenformular |
| `invalidBudgetHasValidationKey` | Mehr als drei Verbindungen liefert MailSettings_InvalidLimits. | Kontenformular |
| `invalidPollHasValidationKey` | Abfrageintervall unter 60 Sekunden liefert MailSettings_InvalidLimits. | Kontenformular |
| `signatureIsFiltered` | OutgoingHtmlFilter entfernt Skriptinhalt und erhält erlaubte Signaturformatierung. | Kontenformular |
| `personalMicrosoftUsesConsumerSmtp` | Persönliche Outlook-Adresse aktualisiert den unberührten SMTP-Host auf smtp-mail.outlook.com. | Kontenformular |
| `validFormHasNoValidationKey` | Gültige HIN-Konfiguration hat keinen Validierungsfehler. | Kontenformular |
| `personalToSharedConvertsLists` | Eigentümer und Stellvertretung ergeben eindeutige Benutzersichtbarkeit; Eigentümer und Stellvertretung entfallen, Gruppen bleiben erhalten. | Scope-Wechsel |
| `sharedToPersonalConvertsListsAndShowsHint` | Der aktive Benutzer wird Eigentümer; sichtbare Benutzer ergeben eindeutige Stellvertretung ohne ihn. Gemeinsame Listen entfallen; der Gruppenhinweis bleibt auch beim anschliessenden Erfassen gesetzt. | Scope-Wechsel, Hinweis |
| `switchingWithoutGroupsHasNoHint` | Ohne vorherige Gruppen wird kein Gruppenhinweis gesetzt. | Hinweis |
| `savedNewScopeCanChangeWithRight` | Ein für die Browseranmeldung neu gespeichertes Konto kann mit Verwaltungsrecht den Geltungsbereich wechseln. | Berechtigung |
| `explicitVisibilityIsKeptWhenSwitching` | Explizite Ziel-Sichtbarkeit überschreibt die automatische Übernahme. | Listenübernahme |
| `explicitDelegatesAreKeptWhenSwitching` | Explizite Ziel-Stellvertretung überschreibt die automatische Übernahme. | Listenübernahme |
| `existingSharedScopeStaysWithoutRight` | Ohne Verwaltungsrecht bleibt ein bestehendes gemeinsames Konto gesperrt; Eigentümer, Scope und Gruppen werden nicht umgewandelt. | Berechtigung |
| `newAccountPreselectsOwnerWithoutEnablingTransfer` | Aktiver Benutzer ist vorbelegt; bei ungespeichertem neuen Konto ist die Auswahl gesperrt. | Besitzerauswahl |
| `existingOwnerCanSelectTransfer` | Bestehender Besitzer kann ohne Praxis-Recht wechseln; Auswahl entfernt den neuen Besitzer aus der Stellvertretung, bleibt beim Erfassen erhalten und meldet Änderungen einschließlich Rückwahl. | Übergabe |
| `nonOwnerCannotSelectTransfer` | Ein anderer aktiver Benutzer kann den Besitzer nicht ändern. | Berechtigung |
| `sharedToPersonalEnablesOwnerSelection` | Bei Praxis-Konten ist die Auswahl gesperrt; nach Wechsel zu persönlich kann ein Zielbesitzer gewählt werden, ohne ihn als Stellvertreter zu behalten. Rückwechsel sperrt die Auswahl. | Scope-Wechsel |
| `savedNewAccountEnablesOwnerSelection` | Nach erstem Speichern ist die Besitzerwahl möglich; Speichern aktualisiert den Vergleichswert für Folgeänderungen. | Speicherstatus |
| `administratorCanSelectForeignOwner` | Administrator kann den Besitzer eines fremden persönlichen Kontos auswählen. | Besitzerwechsel |
| `foreignAuthenticationStaysDisabled` | Fremde persönliche Konten bieten auch nach ungespeichertem Scope-Wechsel keine Authentifizierungsaktionen an. | OAuth |
| `ownAndSharedAuthenticationIsAllowed` | Eigene persönliche und gemeinsame Konten behalten Authentifizierungsaktionen. | OAuth |
| `personalOwnerLabelUsesUserLabel` | Persönlicher Besitzer erscheint mit dem Benutzernamen wie in der Besitzerauswahl. | Besitzer-Spalte |
| `personalOwnerLabelFallsBackToId` | Fehlende Benutzerbeschriftung fällt auf die gespeicherte Benutzer-ID zurück. | Besitzer-Spalte |
| `sharedOwnerLabelUsesSharedText` | Praxiskonto zeigt den lokalisierten gemeinsamen Text ohne Benutzerauflösung. | Besitzer-Spalte |


Auftrag B ergänzt `loadsSingleEndpointAndPreservesIt`, `singleEndpointSurvivesAddressEdit` und `removingBothEndpointsIsInvalid`: Ein-Protokoll-Konten behalten beim Speichern und bei Adressänderungen ihren fehlenden Endpunkt; dessen Port wird nicht validiert. Konten ohne Endpunkt bleiben ungültig. Neue Konten benötigen weiterhin beide Endpunkte.
