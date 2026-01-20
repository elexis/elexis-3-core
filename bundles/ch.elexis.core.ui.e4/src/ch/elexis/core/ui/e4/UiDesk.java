package ch.elexis.core.ui.e4;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class UiDesk {

	public static final String COL_RED = "rot"; //$NON-NLS-1$
	public static final String COL_GREEN = "gruen"; //$NON-NLS-1$
	public static final String COL_DARKGREEN = "dunkelgruen"; //$NON-NLS-1$
	public static final String COL_BLUE = "blau"; //$NON-NLS-1$
	public static final String COL_SKYBLUE = "himmelblau"; //$NON-NLS-1$
	public static final String COL_LIGHTBLUE = "hellblau"; //$NON-NLS-1$
	public static final String COL_BLACK = "schwarz"; //$NON-NLS-1$
	public static final String COL_GREY = "grau"; //$NON-NLS-1$
	public static final String COL_WHITE = "weiss"; //$NON-NLS-1$
	public static final String COL_DARKGREY = "dunkelgrau"; //$NON-NLS-1$
	public static final String COL_LIGHTGREY = "hellgrau"; //$NON-NLS-1$
	public static final String COL_GREY60 = "grau60"; //$NON-NLS-1$
	public static final String COL_GREY20 = "grau20"; //$NON-NLS-1$

	{
		UiDesk.getColorRegistry().put(UiDesk.COL_RED, new RGB(255, 0, 0));
		UiDesk.getColorRegistry().put(UiDesk.COL_GREEN, new RGB(0, 255, 0));
		UiDesk.getColorRegistry().put(UiDesk.COL_DARKGREEN, new RGB(0, 88, 0));
		UiDesk.getColorRegistry().put(UiDesk.COL_BLUE, new RGB(0, 0, 255));
		UiDesk.getColorRegistry().put(UiDesk.COL_SKYBLUE, new RGB(135, 206, 250));
		UiDesk.getColorRegistry().put(UiDesk.COL_LIGHTBLUE, new RGB(0, 191, 255));
		UiDesk.getColorRegistry().put(UiDesk.COL_BLACK, new RGB(0, 0, 0));
		UiDesk.getColorRegistry().put(UiDesk.COL_GREY, new RGB(0x60, 0x60, 0x60));
		UiDesk.getColorRegistry().put(UiDesk.COL_WHITE, new RGB(255, 255, 255));
		UiDesk.getColorRegistry().put(UiDesk.COL_DARKGREY, new RGB(50, 50, 50));
		UiDesk.getColorRegistry().put(UiDesk.COL_LIGHTGREY, new RGB(180, 180, 180));
		UiDesk.getColorRegistry().put(UiDesk.COL_GREY60, new RGB(153, 153, 153));
		UiDesk.getColorRegistry().put(UiDesk.COL_GREY20, new RGB(51, 51, 51));
	}

	public static Color getColor(String desc) {
		return getColorRegistry().get(desc);
	}

	public static ColorRegistry getColorRegistry() {
		return JFaceResources.getColorRegistry();
	}

	/**
	 * Eine Color aus einer RGB-Beschreibung als Hex-String herstellen
	 *
	 * @param coldesc Die Farbe als Beschreibung in Hex-Form
	 * @return die Farbe als Color, ist in Regisry gespeichert
	 */
	public static Color getColorFromRGB(final String coldesc) {
		String col = StringTool.pad(StringTool.LEFT, '0', coldesc, 6);
		if (!getColorRegistry().hasValueFor(col)) {
			RGB rgb;
			try {
				rgb = new RGB(Integer.parseInt(col.substring(0, 2), 16), Integer.parseInt(col.substring(2, 4), 16),
						Integer.parseInt(col.substring(4, 6), 16));
			} catch (NumberFormatException nex) {
				ExHandler.handle(nex);
				rgb = new RGB(100, 100, 100);
			}
			getColorRegistry().put(col, rgb);
		}
		return getColorRegistry().get(col);
	}

}
