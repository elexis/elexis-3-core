package ch.elexis.core.ui.views.provider;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.ui.constants.OrderConstants;
import ch.elexis.core.ui.util.OrderManagementUtil;
import ch.elexis.core.ui.views.OrderManagementView;

public class EntryTableLabelProvider extends ColumnLabelProvider {

	private final boolean showDeliveredColumn;
	private final int columnIndex;
	private OrderManagementView orderManagementView;

	public EntryTableLabelProvider(int columnIndex, boolean showDeliveredColumn,
			OrderManagementView orderManagementView) {
		this.columnIndex = columnIndex;
		this.showDeliveredColumn = showDeliveredColumn;
		this.orderManagementView = orderManagementView;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IOrderEntry entry) {
			int ordered = entry.getAmount();
			int delivered = entry.getDelivered();
			int missing = Math.max(0, ordered - delivered);
			String deliveredText = showDeliveredColumn ? String.valueOf(delivered) : StringUtils.EMPTY;
			String missingText = showDeliveredColumn ? String.valueOf(missing) : StringUtils.EMPTY;

			String articleName = (entry.getArticle() != null) ? entry.getArticle().getName() : StringUtils.EMPTY;
			String providerLabel = (entry.getProvider() != null) ? entry.getProvider().getLabel() : "Unknown";
			String stockCode = (entry.getStock() != null) ? entry.getStock().getCode() : "N/A";

			switch (columnIndex) {
			case OrderConstants.OrderTable.STATUS:
				return String.format("%12s", missingText);
			case OrderConstants.OrderTable.ORDERED:
				return String.valueOf(ordered);
			case OrderConstants.OrderTable.DELIVERED:
				return deliveredText;
			case OrderConstants.OrderTable.ADD:
				if (orderManagementView != null) {
					Integer pendingValue = orderManagementView.getPendingDeliveredValues().get(entry);
					return pendingValue != null ? String.valueOf(pendingValue) : StringUtils.EMPTY;
				}
				return StringUtils.EMPTY;
			case OrderConstants.OrderTable.ARTICLE:
				return articleName;
			case OrderConstants.OrderTable.SUPPLIER:
				return providerLabel;
			case OrderConstants.OrderTable.STOCK:
				return stockCode;
			default:
				return StringUtils.EMPTY;
			}
		}
		return StringUtils.EMPTY;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IOrderEntry entry) {
			if (columnIndex == OrderConstants.OrderTable.STATUS && entry.getState() != OrderEntryState.OPEN) {
				Image icon = OrderManagementUtil.getEntryStatusIcon(entry);
				return icon;
			}
		}
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		if (element instanceof IOrderEntry entry) {
			if (columnIndex == OrderConstants.OrderTable.ADD && orderManagementView.isDeliveryEditMode()) {
				if (entry.getState() == OrderEntryState.ORDERED
						|| entry.getState() == OrderEntryState.PARTIAL_DELIVER) {
					return Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
				}
			}
		}
		return null;
	}
}
