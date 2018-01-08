package ch.elexis.core.ui.eigenartikel;

import org.eclipse.jface.viewers.LabelProvider;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.services.IStockService.Availability;

public class EigenartikelTreeLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element){
		Eigenartikel ea = (Eigenartikel) element;
		String name = ea.getName();
		if (!ea.isProduct()) {
			String label = "";
			String packageSizeString = ea.getPackageSizeString();
			if (packageSizeString != null && packageSizeString.length() > 0) {
				label += packageSizeString + " ";
			} else {
				label += ea.getPackungsGroesse() + " " + ea.getMeasurementUnit();
			}
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
