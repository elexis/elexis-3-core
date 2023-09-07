package ch.elexis.core.ui.medication.views.provider;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;

public class MedicationFilter extends ViewerFilter {
	private String searchString = StringUtils.EMPTY;
	private Viewer viewer;

	public MedicationFilter(Viewer viewer) {
		this.viewer = viewer;
	}

	public void setSearchText(@NonNull String s) {
		if (s.equalsIgnoreCase(searchString))
			return;

		s = s.replace("*", StringUtils.EMPTY); //$NON-NLS-1$
		searchString = ".*" + s.toLowerCase() + ".*"; //$NON-NLS-1$ //$NON-NLS-2$

		viewer.getControl().setRedraw(false);
		viewer.refresh();
		viewer.getControl().setRedraw(true);
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.isEmpty()) {
			return true;
		}
		MedicationTableViewerItem p = (MedicationTableViewerItem) element;

		// check match of article name
		String mediName = "??"; //$NON-NLS-1$

		String label = p.getArtikelLabel();
		if (label != null) {
			mediName = label.toLowerCase();
		}

		if (mediName.matches(searchString)) {
			return true;
		}
		return false;
	}

	public void clearSearchText() {
		this.searchString = StringUtils.EMPTY;
	}

}
