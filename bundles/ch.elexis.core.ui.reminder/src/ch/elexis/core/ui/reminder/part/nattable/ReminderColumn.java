package ch.elexis.core.ui.reminder.part.nattable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IReminderResponsibleLink;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.ISubQuery;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.data.UiMandant;

public class ReminderColumn {

	public enum Type {
		USER, PATIENT, ALL, POPUP, GROUP
	}

	private String name;
	private String color;
	private Type type;

	private IContact contact;

	private IUserGroup group;
	private String search;

	public static List<ReminderColumn> getAllAvailable() {
		List<ReminderColumn> available = new ArrayList<ReminderColumn>();
		available.add(new ReminderColumn("Alle", "cecece", ReminderColumn.Type.ALL));
		available.add(new ReminderColumn("Meine", "acbfad", ReminderColumn.Type.USER));
		available.add(new ReminderColumn("Patient", "bfacac", ReminderColumn.Type.PATIENT));
		available.add(new ReminderColumn("Popup", "67fccd", ReminderColumn.Type.POPUP));

		CoreModelServiceHolder.get().getQuery(IUserGroup.class).execute()
				.forEach(ug -> available.add(new ReminderColumn(ug, "84d6ed", Type.GROUP)));

		CoreModelServiceHolder.get().getQuery(IUser.class)
				.and(ModelPackage.Literals.IUSER__ACTIVE, COMPARATOR.EQUALS, Boolean.TRUE)
				.and(ModelPackage.Literals.IUSER__ASSIGNED_CONTACT, COMPARATOR.NOT_EQUALS, null).execute()
				.forEach(u -> available.add(new ReminderColumn(u, "e9ed84", Type.USER)));

		return available;
	}

	public ReminderColumn(String name, String color, Type type) {
		this.name = name;
		this.color = color;
		this.type = type;
	}

	public ReminderColumn(IUserGroup userGroup, String color, Type type) {
		this.name = userGroup.getId();
		this.group = userGroup;
		this.color = color;
		this.type = type;
	}

	public ReminderColumn(IUser user, String color, Type type) {
		this.name = user.getId();
		this.contact = user.getAssignedContact();
		if (this.contact.isMandator()) {
			Color mandatorColor = UiMandant.getColorForIMandator(
					CoreModelServiceHolder.get().load(this.contact.getId(), IMandator.class).get());
			this.color = Integer.toHexString(mandatorColor.getRed()) + Integer.toHexString(mandatorColor.getGreen())
					+ Integer.toHexString(mandatorColor.getBlue());
		} else {
			this.color = color;
		}
		this.type = type;
	}

	public List<IReminder> loadReminders() {
		if (type == Type.USER) {
			if (contact == null) {
				Optional<IUser> user = ContextServiceHolder.get().getActiveUser();
				if (user.isPresent() && user.get().getAssignedContact() != null) {
					return loadContact(user.get().getAssignedContact());
				}
			} else {
				return loadContact(contact);
			}
		} else if (type == Type.PATIENT) {
			Optional<IPatient> patient = ContextServiceHolder.get().getActivePatient();
			if (patient.isPresent()) {
				return loadPatient(patient.get());
			}
		} else if (type == Type.ALL) {
			return loadAll();
		} else if (type == Type.POPUP) {
			return loadPopup();
		} else if (type == Type.GROUP) {
			return loadGroup();
		}
		return Collections.emptyList();
	}

	private List<IReminder> loadPopup() {
		IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
		query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);
		query.startGroup();
		query.and("visibility", COMPARATOR.EQUALS, Visibility.POPUP_ON_PATIENT_SELECTION);
		query.or("visibility", COMPARATOR.EQUALS, Visibility.POPUP_ON_LOGIN);
		query.andJoinGroups();

		if (hasSearch()) {
			addSearchToQuery(query);
		}

		query.orderBy(ModelPackage.Literals.IREMINDER__DUE, ORDER.DESC);
		query.limit(500);
		return query.execute();
	}

	private List<IReminder> loadAll() {
		IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
		query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);
		query.and("responsibleValue", COMPARATOR.EQUALS, "ALL");
		query.and("visibility", COMPARATOR.NOT_EQUALS, Visibility.POPUP_ON_PATIENT_SELECTION);
		query.and("visibility", COMPARATOR.NOT_EQUALS, Visibility.POPUP_ON_LOGIN);

		if (hasSearch()) {
			addSearchToQuery(query);
		}

		query.orderBy(ModelPackage.Literals.IREMINDER__DUE, ORDER.DESC);
		query.limit(500);
		return query.execute();
	}

	private List<IReminder> loadGroup() {
		IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
		query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);

		query.and("userGroup", COMPARATOR.EQUALS, group);

		if (hasSearch()) {
			addSearchToQuery(query);
		}

		query.orderBy(ModelPackage.Literals.IREMINDER__DUE, ORDER.DESC);
		query.limit(500);
		return query.execute();
	}

	private List<IReminder> loadContact(IContact contact) {
		IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
		query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);

		ISubQuery<IReminderResponsibleLink> subQuery = query.createSubQuery(IReminderResponsibleLink.class,
				CoreModelServiceHolder.get());
		subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
		subQuery.and("responsible", COMPARATOR.EQUALS, contact);
		query.exists(subQuery);

		if (hasSearch()) {
			addSearchToQuery(query);
		}

		query.orderBy(ModelPackage.Literals.IREMINDER__DUE, ORDER.DESC);
		query.limit(500);
		return query.execute();
	}

	private List<IReminder> loadPatient(IPatient patient) {
		IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
		query.and(ModelPackage.Literals.IREMINDER__CONTACT, COMPARATOR.EQUALS, patient);
		query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);

		if (hasSearch()) {
			addSearchToQuery(query);
		}

		query.orderBy(ModelPackage.Literals.IREMINDER__DUE, ORDER.DESC);
		query.limit(500);
		return query.execute();
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

	public Type getType() {
		return type;
	}

	public IContact getResponsible() {
		if (contact == null) {
			Optional<IUser> user = ContextServiceHolder.get().getActiveUser();
			if (user.isPresent() && user.get().getAssignedContact() != null) {
				return user.get().getAssignedContact();
			}
		}
		return contact;
	}

	public IContact getPatient() {
		if (type == Type.PATIENT) {
			Optional<IPatient> patient = ContextServiceHolder.get().getActivePatient();
			if (patient.isPresent()) {
				return patient.get();
			}
		}
		return null;
	}

	public IUserGroup getGroup() {
		return group;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReminderColumn other = (ReminderColumn) obj;
		return Objects.equals(name, other.name) && type == other.type;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public boolean hasSearch() {
		return StringUtils.isNotBlank(this.search);
	}

	private void addSearchToQuery(IQuery<IReminder> query) {
		String likeSearch = "%" + search + "%";
		query.and(ModelPackage.Literals.IREMINDER__SUBJECT, COMPARATOR.LIKE, likeSearch, true);
	}
}
