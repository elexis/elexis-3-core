package ch.elexis.core.ui.util;

import ch.elexis.core.services.holder.ConfigServiceHolder;

public class BillingSystemColorHelper {
	private static final String COLOR_KEY_PREFIX = "billingSystemColor_";
	public static final int COLOR_COLUMN_INDEX = 1;
	private static final String DEFAULT_COLOR_HEX = "FFFFFF";


	public static String getMixedHexColorForBillingSystem(String billingSystemName, int percentWhite) {
		String key = COLOR_KEY_PREFIX + billingSystemName;
		String value = ConfigServiceHolder.get().get(key, null);
		if (value == null) {
			return DEFAULT_COLOR_HEX;
		}
		String[] rgb = value.split(",");
		try {
			int r = Integer.parseInt(rgb[0].trim());
			int g = Integer.parseInt(rgb[1].trim());
			int b = Integer.parseInt(rgb[2].trim());
			r = (r * (100 - percentWhite) + 255 * percentWhite) / 100;
			g = (g * (100 - percentWhite) + 255 * percentWhite) / 100;
			b = (b * (100 - percentWhite) + 255 * percentWhite) / 100;
			return String.format("%02X%02X%02X", r, g, b);
		} catch (Exception ex) {
			return DEFAULT_COLOR_HEX;
		}
	}
}
