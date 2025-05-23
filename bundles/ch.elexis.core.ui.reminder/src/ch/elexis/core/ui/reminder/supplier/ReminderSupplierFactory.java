package ch.elexis.core.ui.reminder.supplier;

import java.util.List;
import java.util.function.Supplier;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.ui.reminder.part.nattable.ReminderColumn.Type;

public class ReminderSupplierFactory {

	public static Supplier<List<IReminder>> get(Type type, String search, boolean showCompleted, int limit) {
		if (type == Type.ALL) {
			if (FhirModelServiceHolder.isAvailable()) {
				return new FhirAllSupplier(search, showCompleted, limit);
			} else {
				return new AllSupplier(search, showCompleted, limit);
			}
		} else if (type == Type.POPUP) {
			if (FhirModelServiceHolder.isAvailable()) {
				return new FhirPopupSupplier(search, showCompleted, limit);
			} else {
				return new PopupSupplier(search, showCompleted, limit);
			}
		}
		throw new IllegalArgumentException("Unknown type " + type);
	}

	public static Supplier<List<IReminder>> get(IPatient patient, String search, boolean showCompleted, int limit) {
		if (FhirModelServiceHolder.isAvailable()) {
			return new FhirPatientSupplier(patient, search, showCompleted, limit);
		} else {
			return new PatientSupplier(patient, search, showCompleted, limit);
		}
	}

	public static Supplier<List<IReminder>> get(IContact contact, String search, boolean showCompleted, int limit) {
		if (FhirModelServiceHolder.isAvailable()) {
			return new FhirContactSupplier(contact, search, showCompleted, limit);
		} else {
			return new ContactSupplier(contact, search, showCompleted, limit);
		}
	}

	public static Supplier<List<IReminder>> get(IUserGroup group, String search, boolean showCompleted, int limit) {
		if (FhirModelServiceHolder.isAvailable()) {
			return new FhirGroupSupplier(group, search, showCompleted, limit);
		} else {
			return new GroupSupplier(group, search, showCompleted, limit);
		}
	}
}
