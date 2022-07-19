package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IStockCommissioningSystemService;

@Component
public class StockCommissioningServiceHolder {

	private static IStockCommissioningSystemService stockService;

	// Please see
	// info.elexis.server.core.connector.elexis.internal.services.scs.StockCommissioningSystemService
	// for explanation
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY)
	public void setStockCommissioningSystemService(IStockCommissioningSystemService stockService) {
		LoggerFactory.getLogger(getClass()).info("Setting "+stockService);
		StockCommissioningServiceHolder.stockService = stockService;
	}

	public void unsetStockCommissioningSystemService(IStockCommissioningSystemService stockService) {
		LoggerFactory.getLogger(getClass()).info("Unsetting "+stockService);
		StockCommissioningServiceHolder.stockService = null;
	}

	public static IStockCommissioningSystemService get() {
		if (stockService == null) {
			throw new IllegalStateException("No IStockCommissioningSystemService available");
		}
		return stockService;
	}
}
