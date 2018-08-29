package ch.elexis.core.data.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IStockService;

@Component
public class StockServiceHolder {
	
	private static IStockService stockService;
	
	@Reference
	public void setStockService(IStockService stockService){
		StockServiceHolder.stockService = stockService;
	}
	
	public static IStockService get(){
		if (stockService == null) {
			throw new IllegalStateException("No IStockService available");
		}
		return stockService;
	}
}
