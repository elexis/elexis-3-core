package ch.elexis.core.ui.stock.propertytester;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.Stock;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {
	
	// cache only
	private Boolean isScsAvailable;
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue){
		if ("isSCSAvailable".equals(property)) {
			if (isScsAvailable == null) {
				List<Stock> allStocks = CoreHub.getStockService().getAllStocks(true);
				Optional<Stock> scs =
					allStocks.stream().filter(s -> s.isCommissioningSystem()).findFirst();
				isScsAvailable = scs.isPresent();
			}
			return isScsAvailable;
			
		}
		return false;
	}
	
}
