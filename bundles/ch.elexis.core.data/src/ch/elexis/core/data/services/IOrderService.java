package ch.elexis.core.data.services;

import ch.elexis.core.data.interfaces.IOrder;
import ch.elexis.core.data.interfaces.IOrderEntry;
import ch.elexis.core.data.interfaces.IStockEntry;

public interface IOrderService {
	public IOrderEntry findOpenOrderEntryForStockEntry(IStockEntry ise);
	
	public IOrderEntry addRefillForStockEntryToOrder(IStockEntry ise, IOrder order);
}
