package ch.elexis.core.model.format;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IMandator;

public class MandatorFormatUtil {

	public static String getMandatorLabel(IMandator mandator) {
		return mandator.getDescription1() + StringUtils.SPACE + mandator.getDescription2() + " ("
				+ mandator.getDescription3() + ")";
	}

}
