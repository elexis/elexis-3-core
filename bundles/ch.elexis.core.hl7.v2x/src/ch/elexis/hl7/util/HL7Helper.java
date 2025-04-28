package ch.elexis.hl7.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

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

	/**
	 * Detects encoding from MSH line. Defaults to UTF-8.
	 *
	 * @param message HL7 message as string
	 * @return charset name (e.g. ISO-8859-1 or UTF-8)
	 */
	public static String getEncoding(String message) {
		try (BufferedReader reader = new BufferedReader(new StringReader(message))) {
			String firstLine = reader.readLine();
			if (firstLine != null && firstLine.startsWith("MSH") //$NON-NLS-1$
					&& (firstLine.contains("8859-1") || firstLine.contains("8859/1"))) { //$NON-NLS-1$ //$NON-NLS-2$
				return StandardCharsets.ISO_8859_1.name();
			}
		} catch (IOException ignored) {
			// fallback below
		}
		return StandardCharsets.UTF_8.name();
	}

	/**
	 * Parses a raw HL7 string into a HAPI Message object.
	 *
	 * @param raw HL7 message
	 * @return parsed HL7 Message
	 * @throws Exception if parsing fails
	 */
	public static Message parseMessage(String raw) throws Exception {
		PipeParser parser = new PipeParser();
		parser.setValidationContext(new NoValidation());
		return parser.parse(raw);
	}
}
