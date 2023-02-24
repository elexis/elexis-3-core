package ch.elexis.core.ui.dbcheck.contributions;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.LocalLockServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;

public class SetMobileOfContact extends ExternalMaintenance {
	private static final Pattern SWISS = Pattern.compile("^(07[5-9])(\\d{3})(\\d{2})(\\d{2})$");
	private static final Pattern PREFIXSWISS = Pattern.compile("^((\\+|00)41)(7[5-9])(\\d{3})(\\d{2})(\\d{2})$");
	int count;
	String newPhone;

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {

		IQuery<IContact> contactQuery = CoreModelServiceHolder.get().getQuery(IContact.class);
		contactQuery.and(ModelPackage.Literals.ICONTACT__ORGANIZATION, COMPARATOR.EQUALS, false);
		contactQuery.startGroup();
		contactQuery.or(ModelPackage.Literals.ICONTACT__MOBILE, COMPARATOR.EQUALS, "");
		contactQuery.or(ModelPackage.Literals.ICONTACT__MOBILE, COMPARATOR.EQUALS, null);

		count = 0;
		try (IQueryCursor<IContact> cursor = contactQuery.executeAsCursor()) {
			pm.beginTask("Kontaktdaten bearbeiten", cursor.size());
			while (cursor.hasNext()) {

				IContact patient = cursor.next();
				if (StringUtils.isNotBlank(patient.getPhone1())) {
					checkPhone(patient, patient.getPhone1());
				} else if (StringUtils.isNotBlank(patient.getPhone2())) {
					checkPhone(patient, patient.getPhone2());
				}
				cursor.clear();
				pm.worked(1);
			}
		}
		pm.done();
		return count + " Kontake wurden erfolgreich bearbeitet";
	}

	@Override
	public String getMaintenanceDescription() {
		return "Setzt Mobilnummer des Kontaktes, falls in anderem Feld vorhanden";
	}

	private void checkPhone(IContact patient, String phone) {
		newPhone = phone.replaceAll("[^0-9+]", "");
		if (SWISS.matcher(newPhone).find() || PREFIXSWISS.matcher(newPhone).find()) {
			patient.setMobile(newPhone);
			LocalLockServiceHolder.get().acquireLock(patient);
			CoreModelServiceHolder.get().save(patient);
			LocalLockServiceHolder.get().releaseLock(patient);
			count++;
		}
	}
}
