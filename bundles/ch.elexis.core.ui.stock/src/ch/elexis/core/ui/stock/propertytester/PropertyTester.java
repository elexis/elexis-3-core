package ch.elexis.core.ui.stock.propertytester;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.model.IStock;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	// cache only
	private Boolean isScsAvailable;

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("isSCSAvailable".equals(property)) { //$NON-NLS-1$
			if (isScsAvailable == null) {
				List<IStock> allStocks = StockServiceHolder.get().getAllStocks(true, false);
				Optional<IStock> scs = allStocks.stream().filter(s -> s.isCommissioningSystem()).findFirst();
				isScsAvailable = scs.isPresent();
			}
			return isScsAvailable;

		}
		return false;
	}

}
