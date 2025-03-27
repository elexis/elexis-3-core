package ch.elexis.core.ui.reminder.part;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

import ch.elexis.core.ui.reminder.part.nattable.ReminderBodyDataProvider;

public class SelectionUtil {

	public static Object getData(NatTable natTable, ReminderBodyDataProvider dataProvider, int x, int y) {
		int columnPosition = natTable.getColumnPositionByX(x);
		int rowPosition = natTable.getRowPositionByY(y);

		return dataProvider.getData(natTable.getColumnIndexByPosition(columnPosition), natTable.getRowIndexByPosition(rowPosition));
	}

	public static ILayerCell getCell(NatTable natTable, ReminderBodyDataProvider dataProvider, int x, int y) {
		int columnPosition = natTable.getColumnPositionByX(x);
		int rowPosition = natTable.getRowPositionByY(y);

		return natTable.getCellByPosition(columnPosition, rowPosition);
	}

	public static boolean isHoverCell(NatTable natTable, ReminderBodyDataProvider dataProvider, ILayerCell cell,
			int x, int y) {
		int columnPosition = natTable.getColumnPositionByX(x);
		int rowPosition = natTable.getRowPositionByY(y);

		int startX = cell.getLayer().getStartXOfColumnPosition(columnPosition);
		int startY = cell.getLayer().getStartYOfRowPosition(rowPosition);

		return x > startX && y > startY;
	}

	public static boolean isHoverCheck(NatTable natTable, ReminderBodyDataProvider dataProvider,
			ILayerCell cell, int x, int y) {
		if (cell != null) {
			int columnPosition = natTable.getColumnPositionByX(x);
			int rowPosition = natTable.getRowPositionByY(y);

			int startX = cell.getLayer().getStartXOfColumnPosition(columnPosition);
			int startY = cell.getLayer().getStartYOfRowPosition(rowPosition);

			if (x > startX && y > startY) {
				int cellx = x - startX;
				int celly = y - startY;
				
				return cell.getBounds().width - cellx < 16 && (celly < 16);
			}
		}
		return false;
	}

	public static boolean isHoverLink(NatTable natTable, ReminderBodyDataProvider dataProvider, ILayerCell cell,
			int x, int y) {
		if (cell != null) {
			int columnPosition = natTable.getColumnPositionByX(x);
			int rowPosition = natTable.getRowPositionByY(y);

			int startX = cell.getLayer().getStartXOfColumnPosition(columnPosition);
			int startY = cell.getLayer().getStartYOfRowPosition(rowPosition);

			if (x > startX && y > startY) {
				int cellx = x - startX;
				int celly = y - startY;

				return cell.getBounds().width - cellx < 16 && (celly > 18 && celly < 34);
			}
		}
		return false;
	}
}
