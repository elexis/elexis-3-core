package ch.elexis.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.elexis.core.types.LabItemTyp;

public class LabResultConstants {
	public static final int PATHOLOGIC = 1 << 0;

	public static final String SMALLER = "<";
	public static final String BIGGER = ">";

	public static final String EXTINFO_HL7_SUBID = "Hl7SubId";

	public static Pattern refValuesPattern = Pattern.compile("\\((.*?)\\)"); //$NON-NLS-1$

	public static String[] VALID_ABS_VALUES = new String[] { "positiv", "negativ", "pos.", "neg.", "pos", "neg", ">0",
			"<0" };

	/**
	 * Determine whether the provided LabItem and its result are pathologic
	 *
	 * @param item
	 * @param result
	 * @param referenceValues
	 * @return
	 */
	public static boolean isPathologic(final ILabItem item, final String result, final String referenceValues) {
		if (item.getTyp().equals(LabItemTyp.ABSOLUTE)) {
			if (result.toLowerCase().startsWith("pos")) { //$NON-NLS-1$
				return true;
			}
			if (result.trim().startsWith("+")) { //$NON-NLS-1$
				return true;
			}
		} else /* if(item.getTyp().equals(LabItem.typ.NUMERIC)) */ {
			List<String> refStrings = LabResultConstants.parseRefString(referenceValues);
			// only test first string as range is defined in one string
			if (!refStrings.isEmpty() && result != null) {
				return LabResultConstants.testRef(refStrings.get(0), result);
			}
		}
		return false;
	}

	public static List<String> parseRefString(String ref) {
		List<String> result = new ArrayList<>();

		Matcher m = refValuesPattern.matcher(ref);

		while (m.find()) {
			result.add(m.group(1).trim());
		}

		// add the whole string if nothing found
		if (result.isEmpty()) {
			result.add(ref.trim());
		}

		return result;
	}

	public static boolean testRef(String ref, String result) {
		try {
			if (ref.trim().startsWith(SMALLER) || ref.trim().startsWith(BIGGER)) {
				String resultSign = null;
				double refVal = Double.parseDouble(ref.substring(1).trim());

				if (result.trim().startsWith(SMALLER) || result.trim().startsWith(BIGGER)) {
					resultSign = result.substring(0, 1).trim();
					result = result.substring(1).trim();
				}
				double val = Double.parseDouble(result);
				if (ref.trim().startsWith(SMALLER)) {
					if (val >= refVal && !(val == refVal && SMALLER.equals(resultSign))) {
						return true;
					}
				} else {
					if (val <= refVal && !(val == refVal && BIGGER.equals(resultSign))) {
						return true;
					}
				}
			} else {
				String[] range = ref.split("\\s*-\\s*"); //$NON-NLS-1$
				if (range.length == 2) {
					double lower = Double.parseDouble(range[0]);
					double upper = Double.parseDouble(range[1]);
					double val = Double.parseDouble(result);
					if ((val < lower) || (val > upper)) {
						return true;
					}
				}
			}
		} catch (NumberFormatException nfe) {
			// don't mind
		}
		return false;
	}

	public static boolean isValidNumericRefValue(String value) {
		List<String> refs = parseRefString(value);
		for (String string : refs) {
			try {
				if (string.trim().startsWith("<") || string.trim().startsWith(">")) { //$NON-NLS-1$ //$NON-NLS-2$
					Double.parseDouble(string.substring(1).trim());
				} else {
					String[] range = string.split("\\s*-\\s*"); //$NON-NLS-1$
					if (range.length == 2) {
						Double.parseDouble(range[0]);
						Double.parseDouble(range[1]);
					} else {
						return false;
					}
				}
			} catch (NumberFormatException nfe) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidAbsoluteRefValue(String value) {
		for (String string : VALID_ABS_VALUES) {
			if (value.trim().equals(string)) {
				return true;
			}
		}
		return false;
	}
}
