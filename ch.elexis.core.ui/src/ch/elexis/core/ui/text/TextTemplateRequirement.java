package ch.elexis.core.ui.text;

public class TextTemplateRequirement implements ITextTemplateRequirement {
	public static final String TT_AUF_CERT = "AUF-Zeugnis";
	public static final String TT_AUF_CERT_DESC = "AUF-Zeugnis für einfache AUF in %";
	
	public static final String TT_KG_COVER_SHEET = "KG-Deckblatt";
	public static final String TT_KG_COVER_SHEET_DESC =
		"Deckblatt für die KG mit den Informationen aus Patientendetails";
	
	public static final String TT_KG = "KG";
	public static final String TT_KG_DESC = "Krankengeschichte";
	
	public static final String TT_ADDRESS_LABEL = "AdressEtikette";
	public static final String TT_ADDRESS_LABEL_DESC = "Adressetikette für angewählten Kontakt";
	
	public static final String TT_PATIENT_LABEL = "PatientEtikette";
	public static final String TT_PATIENT_LABEL_DESC =
		"Patientenetikette mit Adresse, Geschlecht und Pat.-ID";
	
	public static final String TT_PATIENT_LABEL_ORDER = "PatientEtiketteAuftrag";
	public static final String TT_PATIENT_LABEL_ORDER_DESC = "Patientenetikette mit Auftragsnummer";
	
	public static final String TT_MEDI_LABEL = "Medietikette";
	public static final String TT_MEDI_LABEL_DESC = "Vorlage für MediEtikette";
	
	public static final String TT_LIST = "Liste";
	public static final String TT_LIST_DESC = "Liste für Abrechnungliste des Plugins KonsNachDatum";
	
	public static final String TT_BILLING_LIST = "Abrechnungsliste";
	public static final String TT_BILLING_LIST_DESC =
		"Vorlage für die Ausgabe einer Abrechnungsliste";
	
	public static final String TT_ORDER = "Bestellung";
	public static final String TT_ORDER_DESC = "Bestellliste Artikel in Bestellvorschlag";
	
	public static final String TT_XRAY = "Roentgen-Blatt";
	public static final String TT_XRAY_DESC = "Vorlage für Röntgenblatt";
	
	public static final String TT_INTAKE_LIST = "Einnahmeliste";
	public static final String TT_INTAKE_LIST_DESC =
		"Einnahmeliste Medikamente für angewählten Patient";
	
	public static final String TT_PRESCRIPTION = "Rezept";
	public static final String TT_PRESCRIPTION_DESC = "Rezeptvorlage";
	
	@Override
	public String[] getNamesOfRequiredTextTemplate(){
		return new String[] {
			TT_AUF_CERT, TT_KG_COVER_SHEET, TT_KG, TT_ADDRESS_LABEL, TT_PATIENT_LABEL,
			TT_PATIENT_LABEL_ORDER, TT_MEDI_LABEL, TT_LIST, TT_BILLING_LIST, TT_ORDER, TT_XRAY,
			TT_INTAKE_LIST, TT_PRESCRIPTION
		};
	}
	
	@Override
	public String[] getDescriptionsOfRequiredTextTemplate(){
		return new String[] {
			TT_AUF_CERT_DESC, TT_KG_COVER_SHEET_DESC, TT_KG_DESC, TT_ADDRESS_LABEL_DESC,
			TT_PATIENT_LABEL_DESC, TT_PATIENT_LABEL_ORDER_DESC, TT_MEDI_LABEL_DESC, TT_LIST_DESC,
			TT_BILLING_LIST_DESC, TT_ORDER_DESC, TT_XRAY_DESC, TT_INTAKE_LIST_DESC,
			TT_PRESCRIPTION_DESC
		};
	}
	
}
