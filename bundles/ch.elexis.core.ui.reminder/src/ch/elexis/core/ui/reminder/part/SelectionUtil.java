package ch.elexis.core.ui.reminder.part;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

import ch.elexis.core.ui.reminder.part.nattable.ReminderSpanningBodyDataProvider;

public class SelectionUtil {

	public static Object getData(NatTable natTable, ReminderSpanningBodyDataProvider dataProvider, int x, int y) {
		int columnPosition = natTable.getColumnPositionByX(x);
		int rowPosition = natTable.getRowPositionByY(y);

		return dataProvider.getData(natTable.getColumnIndexByPosition(columnPosition), natTable.getRowIndexByPosition(rowPosition));
	}

	public static Integer getSpanningRowPosition(NatTable natTable,
			ReminderSpanningBodyDataProvider dataProvider, int x, int y) {
		int columnPosition = natTable.getColumnPositionByX(x);
		int rowPosition = natTable.getRowPositionByY(y);
		int columnIndex = natTable.getColumnIndexByPosition(columnPosition);
		int rowIndex = natTable.getRowIndexByPosition(rowPosition);

		List<Integer> rowIndexs = dataProvider.getDataSpanningRowPositions(columnIndex, rowIndex);

		return (rowIndexs.get(0) - rowIndex) + rowPosition;
	}

	public static ILayerCell getCell(NatTable natTable, ReminderSpanningBodyDataProvider dataProvider, int x, int y) {
		int columnPosition = natTable.getColumnPositionByX(x);
		int rowPosition = natTable.getRowPositionByY(y);

		// use the row position of the row with data not the spanned afterwards
		rowPosition = SelectionUtil.getSpanningRowPosition(natTable, dataProvider, x, y);
		return natTable.getCellByPosition(columnPosition, rowPosition);
	}

	public static boolean isHoverCell(NatTable natTable, ReminderSpanningBodyDataProvider dataProvider, ILayerCell cell,
			int x, int y) {
		int columnPosition = natTable.getColumnPositionByX(x);
		int rowPosition = natTable.getRowPositionByY(y);

		// use the row position of the row with data not the spanned afterwards
		rowPosition = SelectionUtil.getSpanningRowPosition(natTable, dataProvider, x, y);
		int startX = cell.getLayer().getStartXOfColumnPosition(columnPosition);
		int startY = cell.getLayer().getStartYOfRowPosition(rowPosition);

		return x > startX && y > startY;
	}

	public static boolean isHoverCheck(NatTable natTable, ReminderSpanningBodyDataProvider dataProvider,
			ILayerCell cell, int x, int y) {
		if (cell != null) {
			int columnPosition = natTable.getColumnPositionByX(x);
			int rowPosition = natTable.getRowPositionByY(y);

			// use the row position of the row with data not the spanned afterwards
			rowPosition = SelectionUtil.getSpanningRowPosition(natTable, dataProvider, x, y);
			int startX = cell.getLayer().getStartXOfColumnPosition(columnPosition);
			int startY = cell.getLayer().getStartYOfRowPosition(rowPosition);

			if (x > startX && y > startY) {
				int cellx = x - startX;
				int celly = y - startY;

				int middle = (cell.getBounds().height / 2);
				return cellx < 16 && (celly < middle + 8 && celly > middle - 8);
			}
		}
		return false;
	}
}
