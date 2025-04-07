 
package ch.elexis.core.ui.reminder.commands;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUserGroup;
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
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell,
			@Optional @Named("createReminder.responsible") String responsible,
			@Optional @Named("createReminder.responsiblegroup") String group,
			@Optional @Named("createReminder.patient") String patient,
			@Optional @Named("createReminder.popup") String popup) {
		
		IReminder reminder = new IReminderBuilder(CoreModelServiceHolder.get(), ContextServiceHolder.get(),
				Visibility.ALWAYS, ProcessStatus.OPEN, StringUtils.EMPTY).build();
		reminder.setType(Type.COMMON);

		ContextServiceHolder.get().getActiveUserContact().ifPresent(c -> {
			reminder.addResponsible(c);
		});

		ContextServiceHolder.get().getActivePatient().ifPresent(p -> {
			reminder.setContact(p);
		});
		
		if (responsible instanceof String) {
			if (responsible.length() > 4) {
				reminder.getResponsible().forEach(c -> {
					reminder.removeResponsible(c);
				});
				reminder.addResponsible(CoreModelServiceHolder.get().load(responsible, IContact.class).orElse(null));
			} else {
				reminder.getResponsible().forEach(c -> {
					reminder.removeResponsible(c);
				});
				reminder.setResponsibleAll(true);
			}
		}
		if (group instanceof String) {
			if (group.length() > 1) {
				reminder.getResponsible().forEach(c -> {
					reminder.removeResponsible(c);
				});
				reminder.setGroup(CoreModelServiceHolder.get().load(group, IUserGroup.class).orElse(null));
			}
		}
		if (patient instanceof String) {
			if (patient.length() > 4) {
				reminder.setContact(CoreModelServiceHolder.get().load(patient, IContact.class).orElse(null));
			}
		}
		if (popup instanceof String) {
			if (Boolean.valueOf(popup)) {
				if (reminder.getContact() != null && reminder.getContact().isPatient()) {
					reminder.setVisibility(Visibility.POPUP_ON_PATIENT_SELECTION);
				} else {
					reminder.setVisibility(Visibility.POPUP_ON_LOGIN);
				}
			}
		}

		ReminderDetailDialog dialog = new ReminderDetailDialog(reminder, shell);
		dialog.open();
	}
		
}