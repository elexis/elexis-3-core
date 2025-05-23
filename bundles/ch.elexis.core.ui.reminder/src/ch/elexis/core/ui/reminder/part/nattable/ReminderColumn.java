package ch.elexis.core.ui.reminder.part.nattable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.Color;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.data.UiMandant;
import ch.elexis.core.ui.reminder.supplier.ReminderSupplierFactory;

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

	private boolean showCompleted;

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
		Supplier<List<IReminder>> supplier = ReminderSupplierFactory.get(Type.POPUP, search, showCompleted, 500);
		return supplier.get();
	}

	private List<IReminder> loadAll() {
		Supplier<List<IReminder>> supplier = ReminderSupplierFactory.get(Type.ALL, search, showCompleted, 500);
		return supplier.get();
	}

	private List<IReminder> loadGroup() {
		Supplier<List<IReminder>> supplier = ReminderSupplierFactory.get(group, search, showCompleted, 500);
		return supplier.get();
	}

	private List<IReminder> loadContact(IContact contact) {
		Supplier<List<IReminder>> supplier = ReminderSupplierFactory.get(contact, search, showCompleted, 500);
		return supplier.get();
	}

	private List<IReminder> loadPatient(IPatient patient) {
		Supplier<List<IReminder>> supplier = ReminderSupplierFactory.get(patient, search, showCompleted, 500);
		return supplier.get();
	}

	public String getId() {
		return name + "::" + getType().name();
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		if (type == Type.USER) {
			IContact responsible = getResponsible();
			return getName() + " (" + responsible.getDescription1() + StringUtils.SPACE + responsible.getDescription2()
					+ ")";
		}
		return getName();
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

	public void setShowCompleted(Boolean value) {
		this.showCompleted = value;
	}
}
