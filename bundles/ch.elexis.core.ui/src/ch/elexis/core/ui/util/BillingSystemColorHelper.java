package ch.elexis.core.ui.util;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.core.services.holder.ConfigServiceHolder;

public class BillingSystemColorHelper {
	private static final String COLOR_KEY_PREFIX = "billingSystemColor_";
	public static final int COLOR_COLUMN_INDEX = 1;

	public static Color getBaseColor(String billingSystemName, Display display) {
		String key = COLOR_KEY_PREFIX + billingSystemName;
		String value = ConfigServiceHolder.get().get(key, null);
		if (value == null) {
			return display.getSystemColor(SWT.COLOR_WHITE);
		}
		String[] rgb = value.split(",");
		try {
			int r = Integer.parseInt(rgb[0]);
			int g = Integer.parseInt(rgb[1]);
			int b = Integer.parseInt(rgb[2]);
			return new Color(display, r, g, b);
		} catch (Exception ex) {
			return display.getSystemColor(SWT.COLOR_WHITE);
		}
	}

	public static Color mixWithWhite(Color color, int percent, Display display) {
		int r = (color.getRed() * (100 - percent) + 255 * percent) / 100;
		int g = (color.getGreen() * (100 - percent) + 255 * percent) / 100;
		int b = (color.getBlue() * (100 - percent) + 255 * percent) / 100;
		return new Color(display, r, g, b);
	}

	public static Color getBlendedBillingSystemColor(String billingSystemName, int blendPercent, Display display) {
		Color baseColor = getBaseColor(billingSystemName, display);
		Color mixed = mixWithWhite(baseColor, blendPercent, display);
		if (baseColor != display.getSystemColor(SWT.COLOR_WHITE)) {
			baseColor.dispose();
		}
		return mixed;
	}

	public static Color loadColor(String name, Display display) {
		String value = ConfigServiceHolder.get().get(COLOR_KEY_PREFIX + name, null);
		if (value == null)
			return display.getSystemColor(SWT.COLOR_WHITE);
		String[] rgb = value.split(",");
		try {
			int r = Integer.parseInt(rgb[0]);
			int g = Integer.parseInt(rgb[1]);
			int b = Integer.parseInt(rgb[2]);
			return new Color(display, r, g, b);
		} catch (Exception ex) {
			return display.getSystemColor(SWT.COLOR_WHITE);
		}
	}

	public static void saveColor(String name, Color color) {
		if (name == null || color == null)
			return;
		String value = color.getRed() + "," + color.getGreen() + "," + color.getBlue();
		ConfigServiceHolder.get().set(COLOR_KEY_PREFIX + name, value);
	}

	public static void paintColorCell(Event event, Map<String, Color> colorMap, Table table) {
		if (event.index == COLOR_COLUMN_INDEX) {
			TableItem item = (TableItem) event.item;
			String name = item.getText(0);
			Color color = colorMap.getOrDefault(name, table.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			int size = Math.min(event.height - 6, 18);
			int x = event.x + 8;
			int y = event.y + (event.height - size) / 2;
			event.gc.setBackground(color);
			event.gc.fillRectangle(x, y, size, size);
			event.gc.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_BLACK));
			event.gc.drawRectangle(x, y, size, size);
			event.detail &= ~SWT.FOREGROUND;
		}
	}

	public static void handleColorCellClick(Event event, Table table, Map<String, Color> colorMap) {
		Point pt = new Point(event.x, event.y);
		TableItem item = table.getItem(pt);
		if (item == null)
			return;

		int colIndex = -1;
		int x = 0;
		for (int i = 0; i < table.getColumnCount(); i++) {
			int width = table.getColumn(i).getWidth();
			if (pt.x >= x && pt.x < x + width) {
				colIndex = i;
				break;
			}
			x += width;
		}
		if (colIndex == COLOR_COLUMN_INDEX) {
			ColorDialog dlg = new ColorDialog(table.getShell());
			org.eclipse.swt.graphics.RGB rgb = dlg.open();
			if (rgb != null) {
				Color newColor = new Color(table.getDisplay(), rgb);
				String name = item.getText(0);

				Color oldColor = colorMap.get(name);
				if (oldColor != null && !oldColor.isDisposed()
						&& oldColor != table.getDisplay().getSystemColor(SWT.COLOR_WHITE)) {
					oldColor.dispose();
				}
				colorMap.put(name, newColor);
				BillingSystemColorHelper.saveColor(name, newColor);
				table.redraw();
			}
		}
	}

}
