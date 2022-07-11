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
		String text = ea.getText();
		if (!ea.isProduct()) {
			StringBuilder label = new StringBuilder();
			Availability availability = StockServiceHolder.get().getCumulatedAvailabilityForArticle(ea);
			if (availability != null) {
				label.append(" (" + availability.toString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return text + StringUtils.SPACE + label.toString();
		}
		return text;
	}
}
