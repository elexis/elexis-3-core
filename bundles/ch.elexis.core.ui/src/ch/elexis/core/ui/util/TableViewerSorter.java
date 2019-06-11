package ch.elexis.core.ui.util;

import java.util.Arrays;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.jdt.NonNull;

/**
 * Sorter for every row of a table viewer; requires a content provider that implements the
 * {@link IColumnContentProvider} interface. Strings are compared case insensitive.
 * 
 * @see https://dzone.com/articles/javaswt-click-table-column
 * @since 3.7
 */
public class TableViewerSorter {
	private final TableViewer tableViewer;
	
	public TableViewerSorter(TableViewer tableViewer){
		this.tableViewer = tableViewer;
		addColumnSelectionListeners(tableViewer);
		tableViewer.setComparator(new ViewerComparator() {
			public int compare(Viewer viewer, Object e1, Object e2){
				return compareElements(e1, e2);
			}
		});
	}
	
	private void addColumnSelectionListeners(TableViewer tableViewer){
		TableColumn[] columns = tableViewer.getTable().getColumns();
		for (int i = 0; i < columns.length; i++) {
			addColumnSelectionListener(columns[i]);
		}
	}
	
	private void addColumnSelectionListener(TableColumn column){
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				tableColumnClicked((TableColumn) e.widget);
			}
		});
		
	}
	
	private void tableColumnClicked(TableColumn column){
		Table table = column.getParent();
		if (column.equals(table.getSortColumn())) {
			table.setSortDirection(table.getSortDirection() == SWT.UP ? SWT.DOWN : SWT.UP);
		} else {
			table.setSortColumn(column);
			table.setSortDirection(SWT.UP);
		}
		
		tableViewer.refresh();
		
	}
	
	@SuppressWarnings({
		"rawtypes", "unchecked"
	})
	private int compareElements(Object e1, Object e2){
		IColumnContentProvider columnValueProvider =
			(IColumnContentProvider) tableViewer.getContentProvider();
		
		Table table = tableViewer.getTable();
		
		int index = Arrays.asList(table.getColumns()).indexOf(table.getSortColumn());
		int result = 0;
		if (index != -1) {
			Comparable c1 = columnValueProvider.getValue(e1, index);
			Comparable c2 = columnValueProvider.getValue(e2, index);
			if (c1 instanceof String && c2 instanceof String) {
				String _c1 = (String) c1;
				String _c2 = (String) c2;
				result = _c1.compareToIgnoreCase(_c2);
			} else {
				result = c1.compareTo(c2);
			}
		}
		
		return table.getSortDirection() == SWT.UP ? result : -result;
	}
	
	public interface IColumnContentProvider {
		@NonNull
		Comparable<?> getValue(Object element, int columnIndex);
	}
	
}
