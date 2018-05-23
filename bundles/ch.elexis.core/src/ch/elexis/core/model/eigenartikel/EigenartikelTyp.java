package ch.elexis.core.model.eigenartikel;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;

public enum EigenartikelTyp implements ILocalizedEnum {
	
		UNKNOWN('U'), PHARMA('P'), NONPHARMA('N'), MAGISTERY('M'), COMPLEMENTARY('C'),
		ADDITIVE('A');
	
	final char type;
	
	private EigenartikelTyp(char state){
		this.type = state;
	}
	
	@Override
	public String getLocaleText(){
		try {
			return ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(EigenartikelTyp.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}
	
	public char getTypeChar(){
		return type;
	}
	
	public static EigenartikelTyp byCharSafe(String statusIn){
		if (statusIn != null && statusIn.length() > 0) {
			for (EigenartikelTyp eaTyp : EigenartikelTyp.values()) {
				if (eaTyp.type == statusIn.toUpperCase().charAt(0)) {
					return eaTyp;
				}
			}
		}
		return EigenartikelTyp.UNKNOWN;
	}
	
}
