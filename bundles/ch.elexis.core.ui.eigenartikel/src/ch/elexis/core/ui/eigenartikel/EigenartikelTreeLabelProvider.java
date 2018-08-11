package ch.elexis.core.ui.eigenartikel;

import org.eclipse.jface.viewers.LabelProvider;

import ch.elexis.core.data.activator.CoreHub;
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
				CoreHub.getStockService().getCumulatedAvailabilityForArticle(ea);
			if (availability != null) {
				label += " (" + availability.toString() + ")";
			}
			return name +" " +label;
		}
		
		return name;
	}
}
