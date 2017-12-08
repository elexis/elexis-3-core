package ch.elexis.core.ui.laboratory;

import ch.elexis.core.ui.text.ITextTemplateRequirement;

public class LaboratoryTextTemplateRequirement implements ITextTemplateRequirement {
	
	public static final String TT_LABPAPER = "Laborblatt";
	public static final String TT_LABPAPER_DESC =
		"Tabelle mit Laborwerten (Ausgabe aus Laborblatt Elexis)";
	
	public static final String TT_LABORDERS = "Verordnungen";
	public static final String TT_LABORDERS_DESC =
		"Tabelle mit Verordnungen (Ausf\u00fcllbare Felder f\u00fcr Datum/Uhrzeit und Laborwert";
	
	@Override
	public String[] getNamesOfRequiredTextTemplate(){
		return new String[] {
			TT_LABPAPER, TT_LABORDERS
		};
	}
	
	@Override
	public String[] getDescriptionsOfRequiredTextTemplate(){
		return new String[] {
			TT_LABPAPER_DESC, TT_LABORDERS_DESC
		};
	}
	
}
