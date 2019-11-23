package ch.elexis.core.model.format;

import ch.elexis.core.model.IMandator;

import ch.rgw.tools.StringTool;
public class MandatorFormatUtil {
	
	public static String getMandatorLabel(IMandator mandator){
		return mandator.getDescription1() + StringTool.space + mandator.getDescription2() + " ("
			+ mandator.getDescription3() + ")";
	}
	
}
