 
package ch.elexis.core.ui.reminder.commands;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.model.IReminder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.reminder.dialogs.ReminderDetailDialog;

public class EditReminder {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		ReminderDetailDialog dialog = new ReminderDetailDialog(
				ContextServiceHolder.get().getTyped(IReminder.class).get(), shell);
		dialog.open();
	}
	
	
	@CanExecute
	public boolean canExecute() {
		return ContextServiceHolder.get().getTyped(IReminder.class).isPresent();
	}
}