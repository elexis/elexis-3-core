package ch.elexis.core.types;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;

public enum ArticleSubTyp implements ILocalizedEnum {
	
		UNKNOWN('U'), PHARMA('P'), NONPHARMA('N'), MAGISTERY('M'), COMPLEMENTARY('C'),
		ADDITIVE('A');
	
	final char type;
	
	private ArticleSubTyp(char state){
		this.type = state;
	}
	
	@Override
	public String getLocaleText(){
		try {
			return ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(ArticleSubTyp.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}
	
	public char getTypeChar(){
		return type;
	}
	
	public static ArticleSubTyp byCharSafe(String statusIn){
		if (statusIn != null && statusIn.length() > 0) {
			for (ArticleSubTyp eaTyp : ArticleSubTyp.values()) {
				if (eaTyp.type == statusIn.toUpperCase().charAt(0)) {
					return eaTyp;
				}
			}
		}
		return ArticleSubTyp.UNKNOWN;
	}
	
}
