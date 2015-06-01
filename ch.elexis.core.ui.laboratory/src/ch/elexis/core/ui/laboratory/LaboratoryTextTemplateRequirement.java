package ch.elexis.core.ui.laboratory;

import ch.elexis.core.ui.text.ITextTemplateRequirement;

public class LaboratoryTextTemplateRequirement implements ITextTemplateRequirement {
	
	public static final String TT_LABPAPER = "Laborblatt";
	public static final String TT_LABPAPER_DESC =
		"Tabelle mit Laborwerten (Ausgabe aus Laborblatt Elexis)";
	
	@Override
	public String[] getNamesOfRequiredTextTemplate(){
		return new String[] {
			TT_LABPAPER
		};
	}
	
	@Override
	public String[] getDescriptionsOfRequiredTextTemplate(){
		return new String[] {
			TT_LABPAPER_DESC
		};
	}
	
}
