package ch.elexis.core.model.format;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Hlfsklasse zum AHV und Email validieren und formattieren.
 *
 */
public class FormatValidator {

	private static final int AHV_NUM_LEN_WITHOUT_SEP = 13;
	private static final String AHV_SEP = ".";

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

}
