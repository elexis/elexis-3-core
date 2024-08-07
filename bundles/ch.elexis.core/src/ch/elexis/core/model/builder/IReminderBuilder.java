package ch.elexis.core.model.builder;

import java.time.LocalDate;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;

public class IReminderBuilder extends AbstractBuilder<IReminder> {

	public IReminderBuilder(IModelService modelService, IContextService context, Visibility visibility,
			ProcessStatus status, String message) {
		super(modelService);

		object = modelService.create(IReminder.class);
		object.setStatus(status);
		object.setDue(LocalDate.now());
		object.setVisibility(visibility);
		object.setMessage(message);
		if (context != null) {
			object.setCreator(context.getActiveUserContact().orElse(null));
			object.setContact(context.getActiveUserContact().orElse(null));
		}
	}

	public IReminderBuilder addResponsible(IContact responsible) {
		object.addResponsible(responsible);
		return this;
	}

	public IReminderBuilder contact(IContact contact) {
		object.setContact(contact);
		return this;
	}

	public IReminderBuilder subject(String subject) {
		object.setSubject(subject);
		return this;
	}

	public IReminderBuilder visibility(Visibility visibility) {
		object.setVisibility(visibility);
		return this;
	}

	public IReminderBuilder status(ProcessStatus status) {
		object.setStatus(status);
		return this;
	}
}
