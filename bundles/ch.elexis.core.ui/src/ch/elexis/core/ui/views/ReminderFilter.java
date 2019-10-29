package ch.elexis.core.ui.views;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.data.Patient;
import ch.elexis.data.Reminder;

public class ReminderFilter extends ViewerFilter {

	private String filterText;

	@Override
	public boolean select(final Viewer viewer, final Object parentElement, final Object element) {

		if (element instanceof Reminder) {
			Reminder check = (Reminder) element;
			if (CoreHub.userCfg.get(Preferences.USR_REMINDERSOPEN, false)) {
				int determineDueState = Reminder.determineDueState(check.getDateDue());
				if (determineDueState == 0) {
					return false;
				}
			}
			Patient act = ElexisEventDispatcher.getSelectedPatient();
			String patientId = (act != null) ? act.getId() : "INVALID_ID";
			String[] vals = check.get(true, Reminder.FLD_SUBJECT, Reminder.FLD_MESSAGE, Reminder.FLD_KONTAKT_ID,
					Reminder.FLD_VISIBILITY);
			if (!vals[2].equals(patientId)) {
				Visibility vis = Visibility.byNumericSafe(vals[3]);
				if (vis != Visibility.ALWAYS && vis != Visibility.POPUP_ON_LOGIN) {
					// other (non-selected patient) and not marked always visible
					return false;
				}
			}

			if (filterText != null && filterText.length() > 0) {
				if (!StringUtils.containsIgnoreCase(vals[0], filterText)
						&& !StringUtils.containsIgnoreCase(vals[1], filterText)) {
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
