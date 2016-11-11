package ch.elexis.core.stock;

public interface IOrder {
	
	IOrderEntry addEntry(Object article, IStock stock, Object provider, int toOrder);
	
}
