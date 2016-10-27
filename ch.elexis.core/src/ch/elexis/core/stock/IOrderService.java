package ch.elexis.core.stock;

public interface IOrderService {
	public IOrderEntry findOpenOrderEntryForStockEntry(IStockEntry ise);
	
	public IOrderEntry addRefillForStockEntryToOrder(IStockEntry ise, IOrder order);
}
