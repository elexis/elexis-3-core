package ch.elexis.core.ui.util;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.data.util.SortedList;
import ch.elexis.core.model.IOrderEntry;

public class TableSortController {

	private final TableViewer viewer;
	private int currentColumn = -1;
	private int currentDirection = SWT.UP;

	public TableSortController(TableViewer viewer) {
		this.viewer = viewer;
		viewer.getTable().setSortColumn(null);
		viewer.getTable().setSortDirection(SWT.NONE);
		hookColumns();
	}

	public void setDefaultSort(int columnIndex, int direction) {
		currentColumn = columnIndex;
		currentDirection = direction;
		applySort();
	}

	private void hookColumns() {
		Table table = viewer.getTable();
		for (int i = 0; i < table.getColumnCount(); i++) {
			final int colIndex = i;
			TableColumn col = table.getColumn(i);
			col.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (currentColumn == colIndex) {
						currentDirection = (currentDirection == SWT.UP) ? SWT.DOWN : SWT.UP;
					} else {
						currentColumn = colIndex;
						currentDirection = SWT.UP;
					}
					applySort();
				}
			});
		}
	}

	private void applySort() {
		if (currentColumn < 0)
			return;

		Object input = viewer.getInput();
		if (input instanceof java.util.List<?> list) {
			@SuppressWarnings("unchecked")
			java.util.List<IOrderEntry> src = (java.util.List<IOrderEntry>) list;

			SortedList<IOrderEntry> sorted = new SortedList<>(src,
					OrderEntryComparators.forColumn(currentColumn, currentDirection));
			viewer.setInput(sorted);
		}
		Table table = viewer.getTable();
		if (currentColumn >= 0 && currentColumn < table.getColumnCount()) {
			table.setSortColumn(table.getColumn(currentColumn));
		}

		viewer.refresh();
	}

	public int getCurrentColumn() {
		return currentColumn;
	}

	public int getCurrentDirection() {
		return currentDirection;
	}
}
