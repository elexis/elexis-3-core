package ch.elexis.core.ui.util;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.ui.constants.OrderConstants;

public final class OrderEntryComparators {

	private static final Collator COLLATOR;
	static {
		COLLATOR = Collator.getInstance(Locale.getDefault());
		COLLATOR.setStrength(Collator.PRIMARY);
	}

	private OrderEntryComparators() {
	}

	public static Comparator<IOrderEntry> forColumn(int columnIndex, int direction) {
		Comparator<IOrderEntry> base = switch (columnIndex) {
		case OrderConstants.OrderTable.STATUS -> byStatus();
		case OrderConstants.OrderTable.ORDERED -> Comparator.comparingInt(IOrderEntry::getAmount);
		case OrderConstants.OrderTable.DELIVERED -> Comparator.comparingInt(IOrderEntry::getDelivered);
		case OrderConstants.OrderTable.ADD -> (a, b) -> 0;
		case OrderConstants.OrderTable.ARTICLE -> comparingString(e -> {
			IArticle a = e.getArticle();
			return a != null ? nvl(a.getLabel()) : StringUtils.EMPTY;
		});
		case OrderConstants.OrderTable.SUPPLIER -> comparingString(e -> {
			IContact c = e.getProvider();
			return c != null ? nvl(c.getLabel()) : StringUtils.EMPTY;
		});
		case OrderConstants.OrderTable.STOCK -> comparingString(e -> {
			IStock s = e.getStock();
			return s != null ? nvl(s.getLabel()) : StringUtils.EMPTY;
		});
		default -> (a, b) -> 0;
		};
		return (direction == SWT.DOWN) ? base.reversed() : base;
	}

	private static Comparator<IOrderEntry> byStatus() {
		return Comparator.comparingInt(e -> {
			OrderEntryState s = e.getState();
			if (s == null)
				return 99;
			return switch (s) {
			case OPEN -> 0;
			case ORDERED -> 1;
			case PARTIAL_DELIVER -> 2;
			case DONE -> 3;
			default -> 99;
			};
		});
	}

	private static Comparator<IOrderEntry> comparingString(java.util.function.Function<IOrderEntry, String> f) {
		return (a, b) -> COLLATOR.compare(nvl(f.apply(a)), nvl(f.apply(b)));
	}

	private static String nvl(String s) {
		return s == null ? StringUtils.EMPTY : s;
	}
}
