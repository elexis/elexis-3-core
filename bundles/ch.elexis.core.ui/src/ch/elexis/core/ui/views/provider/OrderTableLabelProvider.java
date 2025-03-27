package ch.elexis.core.ui.views.provider;

import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.model.IOrder;
import ch.elexis.core.ui.util.OrderManagementUtil;

public class OrderTableLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IOrder order) {
			switch (columnIndex) {
			case 0:
				return StringUtils.EMPTY; // Abbreviation (empty)
			case 1:
				return order.getTimestamp().format(formatter);
			case 2:
				return order.getName();
			}
		}
		return StringUtils.EMPTY;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof IOrder order && columnIndex == 0) {
			return OrderManagementUtil.getStatusIcon(order);
		}
		return null;
	}
}
