package ch.elexis.core.types;

import ch.elexis.core.model.IArticle;

/**
 * Elexis has different types of {@link IArticle}. Maximum code system name is 15 char.
 * 
 * @author thomas
 *
 */
public enum ArticleTyp {
		ARTIKEL("Artikel"), EIGENARTIKEL("Eigenartikel"), MIGEL("MiGeL"), MEDICAL("Medical"),
		MEDIKAMENT("Medikament");
	
	private String codeSystemName;
	
	private ArticleTyp(String codeSystemName){
		this.codeSystemName = codeSystemName;
	}
	
	public String getCodeSystemName(){
		return codeSystemName;
	}
	
	public static ArticleTyp fromCodeSystemName(String name){
		for (ArticleTyp value : ArticleTyp.values()) {
			if (value.getCodeSystemName().equals(name)) {
				return value;
			}
		}
		return null;
	}
}
