# Release 3.12

We document here very briefly the changes in the elexis-3-core and elexis-3-base repositories which have a visual impact for the user.
Bug fixes and minor changes are not mentioned.

The following new features were added. For some you find some documentation under the corresponding feature/bundle

* at.medevit.elexis.hin.sign.core
* ch.elexis.global_inbox.es
* ch.elexis.mednet.webapi
* ch.framsteg.elexis.administration.covercard
* ch.framsteg.elexis.docbox
* ch.framsteg.elexis.finance.analytics
* ch.framsteg.elexis.hin.auth
* ch.framsteg.elexis.labor.teamw

## Rollen und Rechte: Überarbeitung

Die Rollen und Rechte wurden laut den Vorgaben des BAG überarbeitet. You find more information in this [Readme](./bundles/ch.elexis.core/src/ch/elexis/core/ac/README.md)

If you are starting a test database where the first practioner ist created by passing the options like

    -Dch.elexis.firstMandantName=Mustermann -Dch.elexis.firstMandantPassword=elexisTest -Dch.elexis.firstMandantEmail=mmustermann@elexis.info \
    -Dch.elexis.username=Mustermann -Dch.elexis.password=elexisTest

then you have to enter in the SQL command line (when connected) to the database the following commands to give him/her the needed grants to add a tarmed,etc

    insert into user_role_joint (id, deleted, user_id)  values ('mandator', 0, 'Mustermann');
    insert into user_role_joint (id, deleted, user_id)  values ('poweruser', 0, 'Mustermann');
    insert into user_role_joint (id, deleted, user_id)  values ('ict-administrator', 0, 'Mustermann');
    insert into user_role_joint (id, deleted, user_id)  values ('medical-assistant', 0, 'Mustermann');
    insert into user_role_joint (id, deleted, user_id)  values ('medical-user', 0, 'Mustermann');
    insert into user_role_joint (id, deleted, user_id)  values ('bot', 0, 'Mustermann');
    insert into user_role_joint (id, deleted, user_id)  values ('mpk', 0, 'Mustermann');
    insert into user_role_joint (id, deleted, user_id)  values ('mpa', 0, 'Mustermann');


Die folgenden Info wurden von der [Medelexis](https://support.medelexis.ch/docs/version-3-12) übernommen, wo auch ein paar Screenshots dazu zu finden sind.

## View Leistungen: Tarmed-Gültigkeit prüfen

Ab Version 3.12 kann die Gültigkeit einer Tarmed-Leistung direkt in der View Leistungen überprüft werden. Dafür muss ein Rechtsklick auf die gewünschte Position getätigt werden und Leistungen Details ausgewählt werden.

## View Fälle: Pop-Up Meldung beim Schliessen eines Falles mit offenen Konsultationen

Falls Sie einen Fall schliessen wollen und dieser Konsultationen beinhaltet, die nicht abgerechnet wurden, so erhalten Sie ein Pop-Up mit der Aufschrift:

Der Fall (Name) hat noch nicht verrechnete Konsultationen. Soll der Fall dennoch geschlossen werden?

Falls Sie dies mit Ja bestätigen, wird der Fall geschlossen, ansonsten bleibt der Fall unverändert.

## View Rechnungsausgabe: Ausgabeziele angepasst

Die Ziele für die Rechnungsausgabe wurden umbenannt oder ausgeblendet. Folgende Änderungen wurden vorgenommen:
Änderungen Rechnungsausgabe

## View Labor: Patient-ID anzeigen bei Import von HL7

Wird ein Laborbefund in Elexis importiert und der Patient ist unbekannt, so wird die Patienten ID angezeigt.

## View Agenda: Terminkombinationen

In Elexis 3.12 ist es möglich Terminkombinationen zu setzen. Wird ein Termin mit dem konfigurierten Termintyp erstellt, so wird automatisch ein zweiter Termin zum gewünschten Zeitpunkt im gewünschten Agendabereich erstellt.
 
## Export Icon: Standardisierung

Die Export Icons in der View Rechnungsliste und Output View wurden überarbeitet. Neu finden Sie ein Dropdown um die Liste entweder als CSV oder PDF zu exportieren.

