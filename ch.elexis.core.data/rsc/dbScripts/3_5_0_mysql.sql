###
# Tables of elexis core 
# same order as in ch.elexis.core.data -> rsc/createDB.script  
# except zusatzadresse which needs adaptation of its foreignt key constraint.
###

alter table zusatzadresse drop foreign key FK_ZUSATZADRESSE_KONTAKT_ID;
alter table kontakt modify id varchar(36) charset latin1;
alter table zusatzadresse modify Kontakt_ID varchar(36) charset latin1;
ALTER TABLE zusatzadresse add CONSTRAINT FK_ZUSATZADRESSE_KONTAKT_ID FOREIGN KEY (Kontakt_ID) REFERENCES kontakt(id);


alter table kontakt_adress_joint modify id varchar(36),
	modify myid varchar(36),
	modify otherid varchar(36),
	modify bezug varchar(255);

alter table faelle modify id varchar(36),
	modify patientid varchar(36), 
	modify garantid varchar(36),
	modify kostentrid varchar(36),
	modify grund varchar(255),
	modify diagnosen varchar(255);


alter table behandlungen modify id varchar(36),
	modify fallid varchar(36),
	modify mandantid varchar(36),
	modify rechnungsid varchar(36);

alter table artikel modify id varchar(36),
	modify lieferantid varchar(36),
  modify extid varchar(36);

alter table patient_artikel_joint modify id varchar(36),
  modify patientid varchar(36),
  modify artikelid varchar(36),
  modify rezeptid varchar(36),
  modify prescriptor varchar(36);

alter table artikel_details modify ARTICLE_ID varchar(36);

alter table konto modify id varchar(36),
	modify patientid varchar(36),
	modify RechnungsID varchar(36),
	modify ZahlungsID varchar(36);

alter table leistungen modify id varchar(36),
  modify behandlung varchar(36),
  modify userID varchar(36);

alter table ek_preise modify id varchar(36);

alter table vk_preise modify id varchar(36);


alter table diagnosen modify id varchar(36);

alter table behdl_dg_joint modify ID varchar(36),
	modify BehandlungsID varchar(36),
	modify DiagnoseID varchar(36);

alter table briefe modify id varchar(36),
	modify absenderid varchar(36),
	modify destid varchar(36),
	modify behandlungsid varchar(36),
	modify patientid varchar(36);


alter table output_log modify id varchar(36),
  modify ObjectID varchar(36);


alter table rechnungen modify id varchar(36),
  modify fallid varchar(36),
  modify mandantid varchar(36);

alter table zahlungen modify id varchar(36),
  modify rechnungsid varchar(36);


alter table reminders modify id varchar(36),
  modify identid varchar(36),
  modify originid varchar(36),
  modify responsible varchar(36);

alter table reminders_responsible_link modify id varchar(36),
  modify ReminderID varchar(36),
  modify ResponsibleID varchar(36);

alter table bbs modify id varchar(36),
  modify reference varchar(36),
  modify authorID varchar(36);

alter table laboritems modify id varchar(36),
  modify laborID varchar(36), 
  modify gruppe varchar(36);

alter table laborwerte modify id varchar(36),
  modify patientid varchar(36),
  modify itemid varchar(36),
  modify Origin varchar(36),
  modify OriginID varchar(36); 

alter table labgroups modify id varchar(36);

alter table labgroup_item_joint modify GroupID varchar(36),
  modify ItemID varchar(36);

alter table laborder modify id varchar(36);

alter table rezepte modify id varchar(36),
  modify patientid varchar(36),
  modify mandantid varchar(36),
  modify BriefID varchar(36);

alter table auf modify id varchar(36),
	modify patientid varchar(36),
	modify fallid varchar(36),
	modify briefid varchar(36);

alter table eigenleistungen modify ID varchar(36);

alter table logs modify id varchar(36),
  modify userID varchar(36);

alter table userconfig modify UserID varchar(36);

alter table xid modify ID varchar(36),
  modify `object` varchar(36);

alter table etiketten modify ID varchar(36),
	modify Image varchar(36);

alter table etiketten_object_link modify obj varchar(36),
	modify etikette varchar(36);

alter table etiketten_objclass_link modify sticker varchar(36);

alter table dbimage modify ID varchar(36);

alter table stock modify id varchar(36),
  modify owner varchar(36),
  modify responsible varchar(36);

alter table stock_entry modify id varchar(36),
  modify stock varchar(36),
  modify article_id varchar(36),
  modify provider varchar(36);


alter table bestellung_entry modify ID varchar(36),
	modify ARTICLE_ID varchar(36),
	modify stock varchar(36),
	modify PROVIDER varchar(36);


alter table role modify id varchar(36);

alter table user_role_joint modify id varchar(36),
  modify user_id varchar(36);

alter table right_ modify id varchar(36),
  modify parentid varchar(36);

 alter table role_right_joint modify id varchar(36),
  modify role_id varchar(36); 

 alter table user_ modify id varchar(36);   


#################
# Tables from kown plugins. alphabetically
# Note: These may throw errors, since not all tables exist in all
# Elexis installations.
################

alter table artikelstamm_ch modify ID varchar(36),
	modify LieferantID varchar(36);

alter table at_medevit_elexis_gdt_protokoll modify ID varchar(36),
	modify PatientID varchar(36);

alter table at_medevit_elexis_impfplan modify ID varchar(36),
	modify Patient_ID varchar(36);

alter table at_medevit_elexis_inbox modify ID varchar(36);
alter table at_medevit_elexis_labmap modify ID varchar(36);
alter table at_medevit_elexis_loinc modify ID varchar(36);
alter table at_medevit_elexis_medindex_article modify ID varchar(36);
alter table at_medevit_elexis_medindex_product modify ID varchar(36);
alter table at_medevit_medelexis_vat_ch modify ID varchar(36);

alter table bildanzeige modify ID varchar(36),
	modify PatID varchar(36);

alter table ch_elexis_arzttarif_ch_rfe modify id varchar(36),
  modify konsID varchar(36);

alter table ch_elexis_arzttarife_ch_complementary modify id varchar(36);
alter table ch_elexis_arzttarife_ch_physio modify id varchar(36);
alter table ch_elexis_core_findings_condition modify id varchar(25);
alter table ch_elexis_core_findings_encounter modify id varchar(36);
alter table ch_elexis_core_findings_observation modify id varchar(36);
alter table  ch_elexis_core_findings_procedurerequest modify id varchar(36);
alter table ch_elexis_developer_resources_sampletable modify id varchar(36),
  modify PatientID varchar(36);

alter table ch_elexis_eigendiagnosen modify id varchar(36);  

alter table ch_elexis_icpc_encounter modify id varchar(36),
  modify kons varchar(36),
  modify episode varchar(36);

alter table ch_elexis_icpc_episodes modify id varchar(36),
  modify patientid varchar(36);  

alter table ch_elexis_icpc_episodes_diagnoses_link modify id varchar(36),
  modify episode varchar(36);

alter table ch_elexis_impfplan_vaccination_types modify id varchar(36);
alter table ch_elexis_impfplan_vaccinations modify id varchar(36),
  modify patientID varchar(36);

alter table ch_elexis_kassenbuch modify id varchar(36);

alter table ch_elexis_medikamente_bag_ext modify id varchar(36);
alter table ch_elexis_medikamente_bag_interactions modify id varchar(36),
  modify subst1 varchar(36),
  modify subst2 varchar(36),
  modify Contributor varchar(36);
alter table ch_elexis_medikamente_bag_joint modify id varchar(36),
  modify product varchar(36),
  modify substance varchar(36);
alter table ch_elexis_medikamente_bag_substance modify id varchar(36);

alter table ch_elexis_messages modify ID varchar(36),
	modify origin varchar(36),
	modify destination varchar(36);

alter table ch_elexis_messwerte_messwerte modify id varchar(36),
  modify messungID varchar(36);

alter table ch_elexis_molemax modify id varchar(36),
  modify patientID varchar(36),
  modify parentID varchar(36);

alter table ch_elexis_notes modify id varchar(36),
  modify parent varchar(36);

alter table ch_elexis_privatrechnung modify id varchar(36),
  modify subsystem varchar(36);

alter table ch_elexis_stickynotes modify id varchar(36),
  modify patientid varchar(36);

alter table  ch_medelexis_therapieplan_dispenses modify id varchar(36),
  modify medicationID varchar(36);

alter table ch_medelexis_therapieplan_medication modify id varchar(36),
  modify patientID varchar(36),
  modify problemid varchar(36),
  modify article varchar(255);

alter table ch_medelexis_labortarif2009 modify id varchar(36);

alter table com_hilotec_elexis_messwerte_messungen modify id varchar(36),
  modify PatientID varchar(36);

alter table com_hilotec_elexis_messwerte_messwerte modify id varchar(36),
  modify MessungID varchar(36);

alter table default_signatures modify id varchar(36);

alter table elexisbefunde modify id varchar(36),
	modify patientid varchar(36);

alter table esrrecords modify ID varchar(36),
	modify RECHNUNGSID varchar(36),
	modify PATIENTID varchar(36),
	modify MANDANTID varchar(36);


alter table heap modify ID varchar(80);

alter table iatrix_problem modify id varchar(36),
  modify PatientID varchar(36);

alter table iatrix_problem_behdl_joint modify id varchar(36),
  modify ProblemID varchar(36),
  modify BehandlungsID varchar(36);

alter table iatrix_problem_dauermedikation_joint modify id varchar(36),
  modify ProblemID varchar(36),
  modify DauermedikationID varchar(36);

alter table iatrix_problem_dg_joint modify id varchar(36),
  modify ProblemID varchar(36),
  modify DiagnoseID varchar(36);

alter table icd10 modify id varchar(36), modify parent varchar(36);

alter table kontakt_order_management modify ID varchar(36),
	modify KONTAKT_ID varchar(36);

alter table leistungsblock modify id varchar(36),
  modify mandantid varchar(36);

alter table net_medshare_percentile_data modify id varchar(36),
  modify patient_id varchar(36);

alter table net_medshare_percentile_patient modify id varchar(36),
  modify patient_id varchar(36);

alter table net_medshare_percentile_refdata modify id varchar(36);    

alter table verrechnetcopy  modify id varchar(36),
  modify RechnungId varchar(36),
  modify BehandlungId varchar(36),
  modify Leistg_code varchar(36),
  modify UserID varchar(36);

