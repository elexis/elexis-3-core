package ch.elexis.core.data.interfaces;

public interface IOrder {
	
	IOrderEntry addEntry(Object article, IStock stock, Object provider, int toOrder);
	
}
