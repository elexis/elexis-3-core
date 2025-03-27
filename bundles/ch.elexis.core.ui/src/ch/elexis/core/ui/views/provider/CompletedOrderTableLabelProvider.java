package ch.elexis.core.ui.views.provider;

import java.time.format.DateTimeFormatter;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.model.IOrder;

public class CompletedOrderTableLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IOrder order) {
			switch (columnIndex) {
			case 0:
				return order.getTimestamp().format(formatter); // Abbreviation (empty)
			case 1:
				return order.getName();
			}
		}
		return "";
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}
