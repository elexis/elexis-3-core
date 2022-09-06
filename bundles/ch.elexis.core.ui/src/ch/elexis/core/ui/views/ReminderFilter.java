package ch.elexis.core.ui.views;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.core.model.IReminder;

public class ReminderFilter extends ViewerFilter {

	private String filterText;

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {

		if (element instanceof IReminder) {
			IReminder check = (IReminder) element;
			if (filterText != null && filterText.length() > 0) {
				if (!StringUtils.containsIgnoreCase(check.getSubject(), filterText)
						&& !StringUtils.containsIgnoreCase(check.getMessage(), filterText)) {
					return false;
				}
			}
		}
		return true;
	}

	public void setFilterText(String text) {
		filterText = text;
	}
}
