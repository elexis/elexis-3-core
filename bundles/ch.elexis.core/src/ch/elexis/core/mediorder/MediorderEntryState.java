package ch.elexis.core.mediorder;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStockEntry;

public enum MediorderEntryState implements ILocalizedEnum {

	INVALID, AWAITING_REQUEST, REQUESTED, PARTIALLY_REQUESTED, ORDERED, PARTIALLY_ORDERED, IN_STOCK, PARTIALLY_IN_STOCK;

	private IOrderEntry orderEntry;
	private IStockEntry stockEntry;

	public IOrderEntry getOrderEntry() {
		return orderEntry;
	}

	public void setOrderEntry(IOrderEntry orderEntry) {
		this.orderEntry = orderEntry;
	}

	public IStockEntry getStockEntry() {
		return stockEntry;
	}

	public void setStockEntry(IStockEntry stockEntry) {
		this.stockEntry = stockEntry;
	}

	@Override
	public String getLocaleText() {
		return switch (this) {
		case INVALID -> "Ungültig";
		case AWAITING_REQUEST -> "Erwarte Anforderung";
		case REQUESTED -> "Angefordert";
		case PARTIALLY_REQUESTED -> "Teilanforderung";
		case ORDERED -> "Bestellt";
		case PARTIALLY_ORDERED -> orderEntry.getAmount() + " Stk. bestellt";
		case IN_STOCK -> "Auf Lager";
		case PARTIALLY_IN_STOCK -> stockEntry.getCurrentStock() + " Stk. auf Lager";
		default -> throw new IllegalArgumentException("Unexpected value: " + this);
		};
	}
}
