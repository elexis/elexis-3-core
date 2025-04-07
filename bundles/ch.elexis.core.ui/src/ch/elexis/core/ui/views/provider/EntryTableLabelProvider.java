package ch.elexis.core.ui.views.provider;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.ui.util.OrderManagementUtil;

public class EntryTableLabelProvider extends ColumnLabelProvider {

	private final boolean showDeliveredColumn;
	private final int columnIndex;

	public EntryTableLabelProvider(int columnIndex, boolean showDeliveredColumn) {
		this.columnIndex = columnIndex;
		this.showDeliveredColumn = showDeliveredColumn;
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
			case 0:
				return String.format("%12s", missingText);
			case 1:
				return String.valueOf(ordered);
			case 2:
				return deliveredText;
			case 3:
				return articleName;
			case 4:
				return providerLabel;
			case 5:
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
			if (columnIndex == 0 && entry.getState() != OrderEntryState.OPEN) {
				Image icon = OrderManagementUtil.getEntryStatusIcon(entry);
				return icon;
			}
		}
		return null;
	}

	@Override
	public Color getBackground(Object element) {
		return null;
	}
}
