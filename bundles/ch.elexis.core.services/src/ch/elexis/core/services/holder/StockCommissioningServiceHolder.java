package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IStockCommissioningSystemService;

@Component
public class StockCommissioningServiceHolder {
	
	private static IStockCommissioningSystemService stockService;
	
	@Reference
	public void setStockCommissioningSystemService(IStockCommissioningSystemService stockService){
		StockCommissioningServiceHolder.stockService = stockService;
	}
	
	public static IStockCommissioningSystemService get(){
		if (stockService == null) {
			throw new IllegalStateException("No IStockCommissioningSystemService available");
		}
		return stockService;
	}
}
