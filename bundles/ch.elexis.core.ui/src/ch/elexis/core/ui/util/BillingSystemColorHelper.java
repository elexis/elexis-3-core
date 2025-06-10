package ch.elexis.core.ui.util;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.preferences.UserCasePreferences;

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

	public static void handleColorCellClick(TableViewer viewer, Event event) {
		Table table = viewer.getTable();
		Point pt = new Point(event.x, event.y);
		TableItem item = table.getItem(pt);
		if (item != null) {
			String name = item.getText(0);
			if (!UserCasePreferences.MENUSEPARATOR.equals(name)) {
				ColorDialog dlg = new ColorDialog(table.getShell());
				org.eclipse.swt.graphics.RGB rgb = dlg.open();
				if (rgb != null) {
					String value = rgb.red + "," + rgb.green + "," + rgb.blue;
					ConfigServiceHolder.get().set("billingSystemColor_" + name, value);
					viewer.refresh();
				}
			}
		}
	}
}
