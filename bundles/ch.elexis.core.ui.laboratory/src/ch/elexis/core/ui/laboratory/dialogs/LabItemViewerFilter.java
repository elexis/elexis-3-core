package ch.elexis.core.ui.laboratory.dialogs;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class LabItemViewerFilter extends ViewerFilter {
	protected String searchString;
	protected LabItemLabelProvider labelProvider;
	
	public LabItemViewerFilter(LabItemLabelProvider labelProvider){
		this.labelProvider = labelProvider;
	}
	
	public void setSearchText(String s){
		// Search must be a substring of the existing value
		this.searchString = ".*" + s + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element){
		if (searchString == null || searchString.length() == 0) {
			return true;
		}
		String label = labelProvider.getText(element);
		if (label != null && label.matches(searchString)) {
			return true;
		}
		return false;
	}
}