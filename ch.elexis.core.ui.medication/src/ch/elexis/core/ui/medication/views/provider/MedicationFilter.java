package ch.elexis.core.ui.medication.views.provider;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.data.Prescription;

public class MedicationFilter extends ViewerFilter {
	private String searchString;
	
	public void setSearchText(String s){
		s = s.replace("*", "");
		this.searchString = ".*" + s.toLowerCase() + ".*";
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element){
		if (searchString == null || searchString.length() == 0) {
			return true;
		}
		Prescription p = (Prescription) element;
		
		// check match of article name
		String mediName = "??";
		if (p.getArtikel() != null) {
			String label = p.getArtikel().getLabel();
			if (label != null) {
				mediName = label.toLowerCase();
			}
		}
		
		if (mediName.matches(searchString)) {
			return true;
		}
		return false;
	}
	
}
