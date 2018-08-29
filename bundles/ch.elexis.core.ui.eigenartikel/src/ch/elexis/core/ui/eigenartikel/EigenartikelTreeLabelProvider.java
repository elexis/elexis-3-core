package ch.elexis.core.ui.eigenartikel;

import org.eclipse.jface.viewers.LabelProvider;

import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.model.ITypedArticle;
import ch.elexis.core.services.IStockService.Availability;

public class EigenartikelTreeLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element){
		ITypedArticle ea = (ITypedArticle) element;
		String name = ea.getName();
		if (!ea.isProduct()) {
			String label = "";
			label += ea.getPackageSize() + " " + ea.getPackageUnit();
			Availability availability =
				StockServiceHolder.get().getCumulatedAvailabilityForArticle(ea);
			if (availability != null) {
				label += " (" + availability.toString() + ")";
			}
			return name +" " +label;
		}
		return name;
	}
}
