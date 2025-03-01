 
package ch.elexis.core.ui.reminder.commands;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.builder.IReminderBuilder;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.reminder.dialogs.ReminderDetailDialog;
import jakarta.inject.Named;

public class CreateReminder {
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		
		IReminder reminder = new IReminderBuilder(CoreModelServiceHolder.get(), ContextServiceHolder.get(),
				Visibility.ALWAYS, ProcessStatus.OPEN, StringUtils.EMPTY).build();
		reminder.setType(Type.COMMON);

		ContextServiceHolder.get().getActiveUserContact().ifPresent(c -> {
			reminder.addResponsible(c);
		});

		ContextServiceHolder.get().getActivePatient().ifPresent(p -> {
			reminder.setContact(p);
		});
		
		ReminderDetailDialog dialog = new ReminderDetailDialog(reminder, shell);
		dialog.open();
	}
		
}