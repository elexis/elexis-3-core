# Access Control

There are three different entities to control access to:

* System Configuration entries
* System	 Command executions
* Database stored objects


## System configuration entries

bla

## System command executions

See `ch.elexis.core.ac.SystemCommandConstants` for a list of grantable system command ids.

## Database stored objects

Access to these objects is determined both by the class-permissions defined via the roles and a table of specifically
granted access rights.

For objects we have the constraints `AOBO` and `self`

TODO: Is `self` required for objects?


We classify database stored objects into four categories, referring to [IT-Grundschutz M-3.02][it_grundschutz_d3]:

1. Medical Data
2. Patient Data
3. Financial Accounting Data
4. Other Data


[it_grundschutz_d3]: https://www.fmh.ch/files/pdf29/it_grundschutz_d3_2023_de_20230405.pdf "Minimalanforderungen IT-Grundschutz für Praxisärztinnen und Praxisärzte - 11 Empfehlungen"


Patient Data
IContact#isPatient -> IPatient
IRelatedContact
ICoverage
ISticker
IBilled
IInvoice
IAppointment#patientRelated
IReminder
IPayment
AUF

Medical Data
IDocumentLetter
IDocument
IFindings
IEncounter
Perzentilen
ILabResult
IPrescription (Impfungen?, Rezepte?)

Financial Data
Kassenbuch
Archie

Other Data
IOrganization
ILaboratory
IPerson
IArticle




## Concepts

### User

* Connection to Elexis Contact Id
* Definition Acts-On-Behalf-Of

### Role

A user combines a set of roles that enable him to interact with the system with a calculated set of rights (constraints).
These constraints are both meant for security (need-to-know) and safety (misconfiguration-by-accident) purposes.

### Access Control List (ACL)

An ACL exists both per `role` and per `user`.

? The right system, at the moment, is permissive. Everything, that is NOT MENTIONED in a role right file is allowed.


### Access Control Entry (ACE)



### System access

*   `user` A person that logs into the system, with basic system usage capabilities
*	`bot` A machine account, does not log-in

### Business roles

*   `assistant`
* 	`medical-assistant` Extends assistant capabilities with access to medical data (Encounters etc)
*	`medical-practitioner` Has a GLN Number, performs services to be billed in the name of a `mandator`
* 	`mandator` An executive practitioner, in whose name Invoices can be made (? Organization ?)
* 	`laboratory-user` Performs laboratory workflows, allowed to modify labitems
*	`accountant` Has global access to financial and accounting data

### Technical roles

* `sysconfig-editor`	Modifies the global configuration settings
* `task-editor` Modifies the task service settings 


### Real-World Examples

The following list gives samples of occupations and their combined roles:

* MPA:  `user` + `assistant` + `medical-assistant`
* MPA that performs laboratory:  `user` + `assistant` + `medical-assistant` + `laboratory-user`
* Executive MPA: `user` + `Assistant` + `medical-assistant` + `accountant`
* Doctor: `user` + `medical-practitioner`
* Executive Doctor: `user` + `medical-practitioner` + `mandator`
* IT Administrator: `user` + `sysconfig-editor` + `task-editor`

## Rights

### Denominator

*	`c` CREATE 
*	`r` READ 
*	`u` UPDATE
* 	`d` DELETE
*	`v` VIEW
*	`x` EXECUTE (for command executions, in stored objects means "elevated rights" on an object - EXECUTIVE)
*	`e` EXPORT

### Constraints

Are appended to the Denominator using an `:`.

Object constraints

* 	`*` No constraint (default if none set)
*	`aobo` Acts on behalf of, this is only supported by specific objects (Encounter, ...)
*	`self` Objects that are owned by the current user

### Examples

* `"ch.elexis.core.model.IEncounter" : "cruv:aobo",` Create, read, update and view rights on encounters that are owned by someone we act on behalf of (or self)
* `"ch.elexis.core.model.IEncounter" : "cruvx:aobo"` same, but with elevated rights - in this case - allowed to edit consultation even if it is billed

A request to `IEncounter:cruv:*` would fail, in both examples, as no ID is given, and the general right is limited to `aobo`


#### Objects supporting AOBO


AOBO includes self

`ACEOwner` Annotation auf Model oder JPA Entities??

IEncounter
IInvoice
(required)
Diagnosen, Medikation, Labor, Befunde


Configuration constraints




# Preference Page

ONE PreferencePage - ONE ConfigurationScope!!

* Original Einstellungen Seite soll nur local und user(:self) beinhalten. (TODO Wie filtern?) ` org.eclipse.ui.internal.handlers.ShowPreferencePageHandler` or GlobalActions.OpenPreferencesAction
* Separate Preference Page für Global (welches Mandator:* beinhaltet) (E4PreferencesHandler mit Filter auf `system-configuration.global.*` etc. ) 
* Filter über preferencePage.id `system-configuration.$ConfigurationScope.`
* Assert usage of ConfigServicePreferenceStore with correct scope in Preference Page
* Contributions only via e4 (?)

https://stackoverflow.com/questions/1460761/howto-hide-a-preference-page-in-an-eclipse-rcp

# PROBLEMS

What if code assumes that if null returned it does not exist and create it? We need to
introduce the fact that it exists, but is not accessible to the user!!


# Right Replacements

See `ch.elexis.core.model.ac.EvACEs` for replacement

* `AccessControlDefaults.ACCOUNTING_BILLMODIFY` -> `IInvoice:u`
* `AccessControlDefaults.ACCOUNTING_STATS` -> `` (Rechnungen Statistiken)
* `AccessControlDefaults.ACCOUNTING_GLOBAL` -> `IInvoice:rv:*` explicit global, no aobo
* `AccessControlDefaults.CASE_MODIFY` ->  `ICoverage:u` (Fall ändern)
* `AccessControlDefaults.CASE_MODIFY_SPECIALS` ->  `ICoverage:ux` (Ändern von verborgenen Fall-Daten in der Fall-Anzeige)
* `AccessControlDefaults.CASE_DEFINE_SPECIALS` -> `ICoverage:ux` (Definieren von Spezialfeldern im Abrechnungssystem)
* `AccessControlDefaults.ADMIN_KONS_EDIT_IF_BILLED` -> `IEncounter:ux`
* `AccessControlDefaults.LSTG_CHARGE_FOR_ALL` -> ``
* `AccessControlDefaults.CASE_REOPEN` -> `ICoverage:ux`
* `AccessControlDefaults.DELETE_FORCED` -> `type:dx`
* `AccessControlDefaults.ADMIN_CHANGE_BILLSTATUS_MANUALLY` -> `IInvoice:u`
* `AccessControlDefaults.AC_IMPORT` -> replaced through fine granular rights per type?
* `AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS` -> `IReminder:rv:*`
* `AccessControlDefaults.AC_CHANGEMANDANT` -> entfernt
* `AccessControlDefaults.LSTG_VERRECHNEN` ->
* `AccessControlDefaults.LABITEM_MERGE` -> `ILabItem:u`
* `AccessControlDefaults.LAB_SEEN` -> 
* `AccessControlDefaults.LSTG_VERRECHNEN` -> `IBilled:cu`
* `AccessControlDefaults.ACL_USERS` -> `IUser:cud:*`
* `AccessControlDefaults.KONS_REASSIGN` -> 
* `AccessControlDefaults.SCRIPT_EDIT` -> `Script:u`
* `AccessControlDefaults.SCRIPT_EXECUTE` -> `Script:x`
* `AccessControlDefaults.DOCUMENT_SYSTEMPLATE` -> `IDocumentLetter:cux` (Systemvorlagen ändern) 
* `AccessControlDefaults.DOCUMENT_TEMPLATE` -> `IDocumentLetter:cu` (Vorlagen ändern)