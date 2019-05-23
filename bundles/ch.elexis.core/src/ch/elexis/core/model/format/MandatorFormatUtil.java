package ch.elexis.core.model.format;

import ch.elexis.core.model.IMandator;

public class MandatorFormatUtil {
	
	public static String getMandatorLabel(IMandator mandator){
		return mandator.getDescription1() + " " + mandator.getDescription2() + " ("
			+ mandator.getDescription3() + ")";
	}
	
}
