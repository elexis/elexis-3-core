package ch.elexis.core.ui.eigenartikel;

import org.eclipse.jface.viewers.LabelProvider;

import ch.elexis.core.eigenartikel.Eigenartikel;

public class EigenartikelTreeLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element){
		Eigenartikel ea = (Eigenartikel) element;
		if (!ea.isProduct()) {
			String label = "";
			String packageSizeString = ea.getPackageSizeString();
			if (packageSizeString != null && packageSizeString.length() > 0) {
				label += packageSizeString + " ";
			} else {
				label += ea.getPackungsGroesse() + " " + ea.getMeasurementUnit();
			}
			if (ea.isLagerartikel()) {
				label += " (" + Integer.toString(ea.getTotalCount()) + ")";
			}
			return label;
		}
		
		return ea.getName();
	}
}
