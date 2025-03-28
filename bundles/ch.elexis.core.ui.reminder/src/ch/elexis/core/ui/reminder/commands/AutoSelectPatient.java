 
package ch.elexis.core.ui.reminder.commands;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.views.Messages;

public class AutoSelectPatient {

	@Execute
	public void execute(MItem item) {
		if (ConfigServiceHolder.get().getActiveUserContact(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, false)) {
			ConfigServiceHolder.get().setActiveUserContact(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, false);
			item.setSelected(false);
		} else {
			ConfigServiceHolder.get().setActiveUserContact(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, true);
			item.setSelected(true);
		}
	}

	@CanExecute
	public boolean canExecute(MItem item) {
		item.setTooltip(Messages.ReminderView_toggleSelectPatientActionTooltip);
		item.setSelected(
				ConfigServiceHolder.get().getActiveUserContact(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, false));
		return true;
	}
}