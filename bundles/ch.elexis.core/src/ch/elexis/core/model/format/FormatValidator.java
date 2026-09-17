package ch.elexis.core.model.format;

import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Hlfsklasse zum AHV und Email validieren und formattieren.
 *
 */
public class FormatValidator {

	private static final int AHV_NUM_LEN_WITHOUT_SEP = 13;
	private static final String AHV_SEP = ".";
	private static final int MAX_DATE_SEGMENTS = 3;

	/**
	 * Validates a AHV (swiss social number)
	 * 
	 * @param ahvNum
	 * @return true if argument is a valid AHV number
	 */
	public static boolean isValidAHVNum(final String ahvNum) {
		if (StringUtils.isBlank(ahvNum)) {
			return false;
		}

		return isValidFormattedAHVNum(ahvNum) || isValidUnformattedAHVNum(ahvNum);
	}

	public static boolean isValidFormattedAHVNum(final String ahvToValidate) {
		return hasAHVNumFormat(ahvToValidate) && isControlDigitValid(ahvToValidate);
	}

	public static boolean isValidUnformattedAHVNum(final String ahvToValidate) {

		if (hasNumsWithLength(ahvToValidate, AHV_NUM_LEN_WITHOUT_SEP)) {
			return isControlDigitValid(getFormattedAHVNum(ahvToValidate));
		}

		return false;
	}

	private static boolean isControlDigitValid(final String ahvNum) {
		final String unformattedAHVNum = getUnformattedAHVNum(ahvNum);

		// validate CH AHV
		if (unformattedAHVNum.startsWith("756")) {
			int factor = 3;
			int total = 0;
			int nextToLastIndex = AHV_NUM_LEN_WITHOUT_SEP - 2;

			for (int i = nextToLastIndex; i >= 0; i--) {
				int value = Character.getNumericValue(unformattedAHVNum.charAt(i));
				total += (value * factor);
				factor = (factor == 3) ? 1 : 3;
			}

			int nextMultipleOfTen = (int) Math.ceil((double) total / 10) * 10;
			int expectedControlDigit = nextMultipleOfTen - total;

			return getControlDigit(ahvNum) == expectedControlDigit;
		} else {
			// LI AHV not validated
			return unformattedAHVNum.matches("438[0-9]{10}");
		}

	}

	public static boolean hasAHVNumFormat(final String ahvNum) {
		return Pattern.compile("[0-9]{3}\\.[0-9]{4}\\.[0-9]{4}\\.[0-9]{2}").matcher(ahvNum).matches();
	}

	public static boolean hasNumsWithLength(final String value, final int length) {
		return Pattern.compile("[0-9]{" + length + "}").matcher(value).matches();
	}

	public static boolean isValidMailAddress(final String mailAddress) {
		return Pattern.compile("^[A-ZÄÖÜÀÉÈèéàäöüß0-9._%+-]+@[A-ZÄÖÜÀÉÈèéàäöüß0-9.-]+\\.[A-Z]{2,6}$",
				Pattern.CASE_INSENSITIVE).matcher(mailAddress).matches();
	}

	public static String getUnformattedAHVNum(final String str) {
		return str.replaceAll("[^0-9]", "");
	}

	public static int getControlDigit(final String ahvNum) {
		return Character.getNumericValue(ahvNum.charAt(ahvNum.length() - 1));
	}

	public static String getFormattedAHVNum(final String unformattedAhvNum) {
		if (hasAHVNumFormat(unformattedAhvNum)) {
			return unformattedAhvNum;
		}

		final var ahv1 = unformattedAhvNum.substring(0, 3);
		final var ahv2 = unformattedAhvNum.substring(3, 7);
		final var ahv3 = unformattedAhvNum.substring(7, 11);
		final var ahv4 = unformattedAhvNum.substring(11);

		return new StringBuilder(ahv1).append(AHV_SEP).append(ahv2).append(AHV_SEP).append(ahv3).append(AHV_SEP)
				.append(ahv4).toString();
	}

	public static String getFormattedBirthdate(final String input) {
		if (StringUtils.isBlank(input)) {
			return StringUtils.EMPTY;
		}
		StringBuilder digits = new StringBuilder();
		String[] segments = input.replaceAll("[^0-9.]", StringUtils.EMPTY).split("\\.", -1);
		for (int i = 0; i < segments.length; i++) {
			String segment = segments[i];
			boolean closedByUser = i < segments.length - 1;
			if (closedByUser && i < 2 && segment.length() == 1) {
				digits.append('0');
			}
			digits.append(segment);
		}
		String value = digits.length() > 8 ? digits.substring(0, 8) : digits.toString();
		int length = value.length();
		StringBuilder sb = new StringBuilder();
		sb.append(value, 0, Math.min(2, length));
		if (length >= 2) {
			sb.append('.');
			sb.append(value, 2, Math.min(4, length));
		}
		if (length >= 4) {
			sb.append('.');
			sb.append(value, 4, length);
		}
		return sb.toString();
	}

	public static String getFormattedBirthdateFilter(final String input) {
		if (StringUtils.isBlank(input)) {
			return StringUtils.EMPTY;
		}
		String cleaned = input.replaceAll("[^0-9.]", StringUtils.EMPTY).replaceAll("\\.{2,}", ".")
				.replaceAll("^\\.+", StringUtils.EMPTY);
		if (cleaned.indexOf('.') < 0) {
			String digits = cleaned.length() > 8 ? cleaned.substring(0, 8) : cleaned;
			if (digits.length() <= 4) {
				// might still be a year
				return digits;
			}
			StringBuilder sb = new StringBuilder();
			sb.append(digits, 0, 2).append('.');
			sb.append(digits, 2, 4).append('.');
			sb.append(digits, 4, digits.length());
			return sb.toString();
		}
		StringJoiner sj = new StringJoiner(".");
		String overflow = StringUtils.EMPTY;
		int segmentCount = 0;
		for (String segment : cleaned.split("\\.", -1)) {
			if (segmentCount >= MAX_DATE_SEGMENTS) {
				break;
			}
			overflow = addDateSegment(sj, segmentCount++, overflow + segment);
		}
		while (!overflow.isEmpty() && segmentCount < MAX_DATE_SEGMENTS) {
			overflow = addDateSegment(sj, segmentCount++, overflow);
		}
		return sj.toString();
	}

	private static String addDateSegment(final StringJoiner sj, final int index, final String segment) {
		int maxLength = index < 2 ? 2 : 4;
		if (segment.length() > maxLength) {
			sj.add(segment.substring(0, maxLength));
			return segment.substring(maxLength);
		}
		sj.add(segment);
		return StringUtils.EMPTY;
	}
}