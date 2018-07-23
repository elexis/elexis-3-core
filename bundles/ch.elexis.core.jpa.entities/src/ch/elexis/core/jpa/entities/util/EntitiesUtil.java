package ch.elexis.core.jpa.entities.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntitiesUtil {
	private static Logger log = LoggerFactory.getLogger(EntitiesUtil.class);

	/**
	 * return a numeric field making sure the call will not fail on illegal values
	 * 
	 * @param in
	 *            name of the field
	 * @return the value of the field as integer or 0 if it was null or not numeric.
	 */
	public static int checkZero(final String in){
		if (StringUtils.isEmpty(in)) {
			return 0;
		}
		try {
			return Integer.parseInt((in).trim());
		} catch (NumberFormatException ex) {
			log.warn("Error parsing number [{}], returning 0. ", in);
			return 0;
		}
	}

	/**
	 * return a numeric field making sure the call will not fail on illegal values
	 * 
	 * @param in
	 *            name of the field
	 * @return the value of the field as double or 0.0 if it was null or not a
	 *         Double.
	 */
	public static double checkZeroDouble(final String in) {
		if (StringUtils.isEmpty(in)) {
			return 0.0;
		}
		try {
			return Double.parseDouble(in.trim());
		} catch (NumberFormatException ex) {
			log.warn("Error parsing number [{}], returning 0.0. ", in);
			return 0.0;
		}
	}
}
