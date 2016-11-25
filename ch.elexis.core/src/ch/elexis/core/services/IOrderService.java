package ch.elexis.core.services;

import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStockEntry;

public interface IOrderService {
	public IOrderEntry findOpenOrderEntryForStockEntry(IStockEntry ise);
	
	public IOrderEntry addRefillForStockEntryToOrder(IStockEntry ise, IOrder order);
}
