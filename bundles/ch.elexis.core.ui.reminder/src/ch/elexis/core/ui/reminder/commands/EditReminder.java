 
package ch.elexis.core.ui.reminder.commands;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.reminder.dialogs.ReminderDetailDialog;
import jakarta.inject.Named;

public class EditReminder {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		IReminder reminder = ContextServiceHolder.get().getTyped(IReminder.class).get();
		ReminderDetailDialog dialog = new ReminderDetailDialog(reminder, shell);
		if (dialog.open() != Dialog.OK) {
			// refresh if cancelled to reset changes
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, reminder);
		}
	}
	
	
	@CanExecute
	public boolean canExecute() {
		return ContextServiceHolder.get().getTyped(IReminder.class).isPresent();
	}
}