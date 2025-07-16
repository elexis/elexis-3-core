package ch.elexis.core.data.events;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.IAccountTransaction;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.model.IUser;
import ch.elexis.data.AUF;
import ch.elexis.data.AccountTransaction;
import ch.elexis.data.Anwender;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;
import ch.elexis.data.Labor;
import ch.elexis.data.Mandant;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.elexis.data.Prescription;
import ch.elexis.data.Rechnung;
import ch.elexis.data.Reminder;
import ch.elexis.data.User;

@Component
public class CoreElexisClassToModelInterfaceContribution implements ElexisClassToModelInterfaceContribution {

	@Override
	public Optional<Class<?>> getCoreModelInterfaceForElexisClass(Class<?> elexisClazz) {
		if (elexisClazz == User.class) {
			return Optional.of(IUser.class);
		} else if (elexisClazz == Anwender.class) {
			return Optional.of(IContact.class);
		} else if (elexisClazz == Mandant.class) {
			return Optional.of(IMandator.class);
		} else if (elexisClazz == Patient.class) {
			return Optional.of(IPatient.class);
		} else if (elexisClazz == Konsultation.class) {
			return Optional.of(IEncounter.class);
		} else if (elexisClazz == Fall.class) {
			return Optional.of(ICoverage.class);
		} else if (elexisClazz == Prescription.class) {
			return Optional.of(IPrescription.class);
		} else if (elexisClazz == Brief.class) {
			return Optional.of(IDocumentLetter.class);
		} else if (elexisClazz == Reminder.class) {
			return Optional.of(IReminder.class);
		} else if (elexisClazz == Rechnung.class) {
			return Optional.of(IInvoice.class);
		} else if (elexisClazz == AccountTransaction.class) {
			return Optional.of(IAccountTransaction.class);
		} else if (elexisClazz == Kontakt.class) {
			return Optional.of(IContact.class);
		} else if (elexisClazz == LabItem.class) {
			return Optional.of(ILabItem.class);
		} else if (elexisClazz == LabResult.class) {
			return Optional.of(ILabResult.class);
		} else if (elexisClazz == LabOrder.class) {
			return Optional.of(ILabOrder.class);
		} else if (elexisClazz == Person.class) {
			return Optional.of(IPerson.class);
		} else if (elexisClazz == Organisation.class) {
			return Optional.of(IOrganization.class);
		} else if (elexisClazz == Labor.class) {
			return Optional.of(ILaboratory.class);
		} else if (elexisClazz == AUF.class) {
			return Optional.of(ISickCertificate.class);
		}
		return Optional.empty();
	}
}

