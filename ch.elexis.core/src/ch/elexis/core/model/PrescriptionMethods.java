package ch.elexis.core.model;

import java.util.Arrays;

public class PrescriptionMethods {
	/**
	 * 
	 * @return the signature split into a string array with 4 elements; will always return an array
	 *         of 4 elements, where empty entries are of type String ""
	 * @since 3.1.0
	 * @since 3.2.0 relocated from Prescription
	 */
	public static String[] getSignatureAsStringArray(String signature){
		String[] daytimeSignature = new String[4];
		Arrays.fill(daytimeSignature, "");
		if (signature != null) {
			// Match stuff like '1/2', '7/8'
			//			if (signature.matches("^[0-9]/[0-9]$")) {
			if (signature.matches("[0-9½¼]+([xX][0-9]+(/[0-9]+)?|)")) { //$NON-NLS-1$
				String[] split = signature.split("[xX]");//$NON-NLS-1$
				int length = (split.length <= 4) ? split.length : 4;
				System.arraycopy(split, 0, daytimeSignature, 0, length);
			} else if (signature.indexOf('-') != -1) {
				String[] split = signature.split("[-]"); //$NON-NLS-1$
				int length = (split.length <= 4) ? split.length : 4;
				System.arraycopy(split, 0, daytimeSignature, 0, length);
			} else if (signature.indexOf("/") != -1) {
				String[] split = signature.split("[/]"); //$NON-NLS-1$
				int length = (split.length <= 4) ? split.length : 4;
				System.arraycopy(split, 0, daytimeSignature, 0, length);
			} else {
				daytimeSignature[0] = signature;
			}
		}
		return getDayTimeOrFreetextSignatureArray(daytimeSignature);
	}
	
	/**
	 * Only specific numeric dosage values are allowed as day time dosage<br>
	 * Accepted: 1, 1.5, 1/2 or 1,5<br>
	 * <br>
	 * Regex Explanation: <br>
	 * [0-9]([,.]{1}[0-9]+)? one or multiple digits that might be splitted by one , or .<br>
	 * ([/]{1}[0-9]*([,.]{1}[0-9]+)?)? zero or one occurrence of a slash followed by a numeric
	 * expression like described above
	 * 
	 * @param morn
	 * @param noon
	 * @param eve
	 * @param night
	 * @return 4 field array in case of a dayTime signatue. 1 field array if freetext
	 * @since 3.2.0 relocated from Prescription
	 */
	private static String[] getDayTimeOrFreetextSignatureArray(String[] signature){
		String[] values = new String[4];
		Arrays.fill(values, "");
		
		String morn = signature[0];
		String noon = signature[1];
		String eve = signature[2];
		String night = signature[3];
		String doseExpr = "[0-9]*([,.]{1}[0-9]+)?([/]{1}[0-9]+([,.]{1}[0-9]+)?)?";
		
		// valid day time dosage was subscribed so 4 field array can be populated
		if (morn.matches(doseExpr) && noon.matches(doseExpr) && eve.matches(doseExpr)
			&& night.matches(doseExpr)) {
			if (morn.isEmpty() && noon.isEmpty() && eve.isEmpty() && night.isEmpty()) {
				return values;
			}
			values[0] = morn.isEmpty() ? "0" : morn;
			values[1] = noon.isEmpty() ? "0" : noon;
			values[2] = eve.isEmpty() ? "0" : eve;
			values[3] = night.isEmpty() ? "0" : night;
			return values;
		}
		
		// build up freetext field
		String freetext = createFreetextString(morn, noon, eve, night);
		freetext.trim();
		values[0] = freetext;
		return values;
	}
	
	/**
	 * 
	 * @param values
	 * @return
	 * @since 3.2.0 relocated from Prescription
	 */
	private static String createFreetextString(String... values){
		StringBuilder sb = new StringBuilder();
		for (String val : values) {
			if (!val.isEmpty() && !sb.toString().isEmpty()) {
				sb.append("-");
			}
			sb.append(val);
		}
		return sb.toString();
	}
	
}
