package ch.elexis.core.model;

public interface IOrder {
	
	IOrderEntry addEntry(Object article, IStock stock, Object provider, int toOrder);
	
}
