package ch.elexis.core.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.UserGroup;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;

public class Reminder extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Reminder>
		implements IdentifiableWithXid, IReminder {

	public Reminder(ch.elexis.core.jpa.entities.Reminder entity) {
		super(entity);
	}

	@Override
	public Object getExtInfo(Object key) {
		return extInfoHandler.getExtInfo(key);
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		extInfoHandler.setExtInfo(key, value);
	}

	@Override
	public Map<Object, Object> getMap() {
		return extInfoHandler.getMap();
	}

	@Override
	public IContact getCreator() {
		return ModelUtil.getAdapter(getEntity().getCreator(), IContact.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCreator(IContact value) {
		if (value != null) {
			getEntityMarkDirty().setCreator(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntityMarkDirty().setCreator(null);
		}
	}

	@Override
	public List<IContact> getResponsible() {
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getResponsible().parallelStream().filter(r -> !r.isDeleted())
				.map(r -> ModelUtil.getAdapter(r, IContact.class, true)).collect(Collectors.toList());
	}

	@Override
	public IContact getContact() {
		return ModelUtil.getAdapter(getEntity().getKontakt(), IContact.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setContact(IContact value) {
		if (value != null) {
			getEntityMarkDirty().setKontakt(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntityMarkDirty().setKontakt(null);
		}
	}

	@Override
	public IUserGroup getGroup() {
		return ModelUtil.getAdapter(getEntity().getUserGroup(), IUserGroup.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setGroup(IUserGroup value) {
		if (value != null) {
			getEntityMarkDirty().setUserGroup(((AbstractIdModelAdapter<UserGroup>) value).getEntity());
		} else {
			getEntityMarkDirty().setUserGroup(null);
		}
	}

	@Override
	public LocalDate getDue() {
		return getEntity().getDateDue();
	}

	@Override
	public void setDue(LocalDate value) {
		getEntityMarkDirty().setDateDue(value);
	}

	@Override
	public ProcessStatus getStatus() {
		return getEntity().getStatus();
	}

	@Override
	public void setStatus(ProcessStatus value) {
		getEntityMarkDirty().setStatus(value);
	}

	@Override
	public Visibility getVisibility() {
		return getEntity().getVisibility();
	}

	@Override
	public void setVisibility(Visibility value) {
		getEntityMarkDirty().setVisibility(value);
	}

	@Override
	public String getSubject() {
		return getEntity().getSubject();
	}

	@Override
	public void setSubject(String value) {
		getEntityMarkDirty().setSubject(value);
	}

	@Override
	public String getMessage() {
		return getEntity().getMessage();
	}

	@Override
	public void setMessage(String value) {
		getEntityMarkDirty().setMessage(value);
	}

	@Override
	public Priority getPriority() {
		return getEntity().getPriority();
	}

	@Override
	public void setPriority(Priority value) {
		getEntityMarkDirty().setPriority(value);
	}

	@Override
	public Type getType() {
		return getEntity().getActionType();
	}

	@Override
	public void setType(Type value) {
		getEntityMarkDirty().setActionType(value);
	}

	@Override
	public void addResponsible(IContact responsible) {
		@SuppressWarnings("unchecked")
		Kontakt contact = ((AbstractIdModelAdapter<Kontakt>) responsible).getEntity();
		if (!getEntity().getResponsible().contains(contact)) {
			getEntityMarkDirty().getResponsible().add(contact);
		}
	}

	@Override
	public void removeResponsible(IContact responsible) {
		@SuppressWarnings("unchecked")
		Kontakt contact = ((AbstractIdModelAdapter<Kontakt>) responsible).getEntity();
		if (getEntity().getResponsible().contains(contact)) {
			getEntityMarkDirty().getResponsible().remove(contact);
		}
	}

	@Override
	public boolean isResponsibleAll() {
		return ch.elexis.core.jpa.entities.Reminder.ALL_RESPONSIBLE.equals(getEntity().getResponsibleValue());
	}

	@Override
	public void setResponsibleAll(boolean value) {
		if (value) {
			getEntityMarkDirty().setResponsibleValue(ch.elexis.core.jpa.entities.Reminder.ALL_RESPONSIBLE);
		} else {
			getEntityMarkDirty().setResponsibleValue(null);
		}
	}
}
