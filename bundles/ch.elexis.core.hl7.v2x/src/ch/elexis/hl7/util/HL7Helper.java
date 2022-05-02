package ch.elexis.hl7.util;

import org.apache.commons.lang3.StringUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class HL7Helper {

	private static final SimpleDateFormat SDF_DATE_TIME_PATTERN;
	private static final String DTM_DATE_TIME_PATTERN = "yyyyMMddHHmmss"; //$NON-NLS-1$

	static {
		SDF_DATE_TIME_PATTERN = new SimpleDateFormat(DTM_DATE_TIME_PATTERN);
	}

	/**
	 * Transformiert einen HL7 Date/Time String in ein java.util.Date
	 *
	 * @param dateTimeStr
	 * @return java.util.Date
	 */
	public static Date stringToDate(final String dateTimeStr) throws ParseException {
		if (dateTimeStr == null || dateTimeStr.length() == 0) {
			return null;
		}

		if (dateTimeStr.length() >= 14) {
			return SDF_DATE_TIME_PATTERN.parse(dateTimeStr);
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat(DTM_DATE_TIME_PATTERN.substring(0, dateTimeStr.length()));
			return sdf.parse(dateTimeStr);
		}
	}

	/**
	 * Transformiert java.util.Date in ein HL7 String
	 *
	 * @param date
	 * @return
	 */
	public static String dateToString(final Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return SDF_DATE_TIME_PATTERN.format(cal.getTime());
	}

	public static String dateToString(LocalDateTime localDateTime) {
		return dateToString(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
	}

	public static String determineName(List<String> possibleNames) {
		String ret = StringUtils.EMPTY;
		for (String possibleName : possibleNames) {
			if (possibleName != null && !"null".equals(possibleName)) {
				int possibleNonDigitCount = getNonDigitCharacters(possibleName);
				int retNonDigitCount = getNonDigitCharacters(ret);
				if (possibleNonDigitCount > retNonDigitCount) {
					ret = possibleName;
				}
			}
		}
		return ret;
	}

	private static int getNonDigitCharacters(String possibleName) {
		int ret = possibleName.length();
		for (int i = 0, len = possibleName.length(); i < len; i++) {
			if (Character.isDigit(possibleName.charAt(i))) {
				ret--;
			}
		}
		return ret;
	}
}
