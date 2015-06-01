package ch.elexis.core.ui.data;

import org.eclipse.swt.graphics.Color;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.Mandant;

public class UiMandant {
	
	public static Color getColorForMandator(Mandant m){
		return UiDesk.getColorFromRGB(CoreHub.globalCfg.get(Preferences.USR_MANDATOR_COLORS_PREFIX
			+ m.getLabel(), UiDesk.COL_GREY60));
	}
}
