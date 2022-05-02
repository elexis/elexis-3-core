package ch.elexis.core.ui.eigenartikel;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.LabelProvider;

import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.services.IStockService.Availability;

public class EigenartikelTreeLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		IArticle ea = (IArticle) element;
		String name = ea.getName();
		if (!ea.isProduct()) {
			String label = StringUtils.EMPTY;
			label += ea.getPackageSize() + StringUtils.SPACE
					+ (ea.getPackageUnit() != null ? ea.getPackageUnit() : StringUtils.EMPTY);
			Availability availability = StockServiceHolder.get().getCumulatedAvailabilityForArticle(ea);
			if (availability != null) {
				label += " (" + availability.toString() + ")";
			}
			return name + StringUtils.SPACE + label;
		}
		return name;
	}
}
