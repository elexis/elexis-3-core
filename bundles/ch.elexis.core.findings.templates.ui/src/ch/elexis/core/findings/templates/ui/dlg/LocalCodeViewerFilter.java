package ch.elexis.core.findings.templates.ui.dlg;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class LocalCodeViewerFilter extends ViewerFilter {
	protected String searchString;
	protected ILabelProvider labelProvider;

	public LocalCodeViewerFilter(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	public void setSearchText(String s) {
		// Search must be a substring of the existing value
		this.searchString = ".*" + s + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.isEmpty()) {
			return true;
		}
		String label = labelProvider.getText(element);
		if (label != null && label.toLowerCase().matches(searchString.toLowerCase())) {
			return true;
		}
		return false;
	}
}