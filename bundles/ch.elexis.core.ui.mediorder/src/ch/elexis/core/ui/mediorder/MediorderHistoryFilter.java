package ch.elexis.core.ui.mediorder;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.core.model.IOrderEntry;

public class MediorderHistoryFilter extends ViewerFilter {

	private String searchTerm;

	public void setSearchTerm(String term) {
		this.searchTerm = ".*" + term.toLowerCase() + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {

		if (searchTerm == null || searchTerm.length() == 0) {
			return true;
		}
		IOrderEntry orderEntry = (IOrderEntry) element;
		String articleName = orderEntry.getArticle().getName().toLowerCase();
		if (articleName.matches(searchTerm)) {
			return true;
		}
		return false;
	}

}
