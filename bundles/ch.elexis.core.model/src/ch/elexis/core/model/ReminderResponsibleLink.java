package ch.elexis.core.model;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;

public class ReminderResponsibleLink
		extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.ReminderResponsibleLink>
		implements IdentifiableWithXid, IReminderResponsibleLink {

	public ReminderResponsibleLink(ch.elexis.core.jpa.entities.ReminderResponsibleLink entity) {
		super(entity);
	}

	@Override
	public IReminder getReminder() {
		return ModelUtil.load(getEntity().getReminderid(), IReminder.class);
	}

	@Override
	public IContact getResponsible() {
		return ModelUtil.getAdapter(getEntity().getResponsible(), IContact.class);
	}
}
