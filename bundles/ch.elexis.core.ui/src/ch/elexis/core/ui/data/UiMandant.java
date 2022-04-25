package ch.elexis.core.ui.data;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Mandant;

public class UiMandant {

	public static Color getColorForMandator(Mandant m) {
		return UiDesk.getColorFromRGB(ConfigServiceHolder
				.getGlobal(Preferences.USR_MANDATOR_COLORS_PREFIX + m.getLabel(), UiDesk.COL_GREY60));
	}

	public static void setColorForMandator(Mandant m, RGB rgb) {
		String rgbString = UiDesk.createColor(rgb);
		ConfigServiceHolder.setGlobal(Preferences.USR_MANDATOR_COLORS_PREFIX + m.getLabel(), rgbString);
	}
}
