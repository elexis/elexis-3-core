package ch.elexis.core.ui.importer.div.importers;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.importer.div.importers.AbstractHL7PatientResolver;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.exchange.KontaktMatcher;
import ch.elexis.core.ui.exchange.KontaktMatcher.CreateMode;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class ImporterPatientResolver extends AbstractHL7PatientResolver {
	private TimeTool convertTool = new TimeTool();

	@Override
	public IPatient resolvePatient(String firstname, String lastname, String birthDate, String sender, String patid) {

		// resolve with full data
		Patient pat = KontaktMatcher.findPatient(lastname, firstname, birthDate, StringUtils.EMPTY, StringUtils.EMPTY,
				StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, CreateMode.FAIL);
		// try to resolve with only the beginning of the name
		if (pat == null) {
			String shortLastname = lastname;
			String shortFirstname = firstname;
			if (lastname.length() > 3) {
				shortLastname = lastname.substring(0, 3);
			}
			if (firstname.length() > 3) {
				shortFirstname = firstname.substring(0, 3);
			}
			pat = KontaktMatcher.findPatient(shortLastname, shortFirstname, birthDate, StringUtils.EMPTY,
					StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, CreateMode.FAIL);
		}
		// user decides
		if (pat == null) {

			convertTool.set(birthDate);
			String birthStr = convertTool.toString(TimeTool.DATE_GER);
			if (sender != null) {
				pat = (Patient) KontaktSelektor.showInSync(Patient.class, Messages.Core_Select_Patient,
						Messages.HL7_WhoIs + lastname + StringUtils.SPACE + firstname + " ," + birthStr + "\t\t PID: " //$NON-NLS-1$ //$NON-NLS-2$
								+ patid + " ?\n" + Messages.HL7_Lab + StringUtils.SPACE + sender);
			} else {
				pat = (Patient) KontaktSelektor.showInSync(Patient.class, Messages.Core_Select_Patient,
						Messages.HL7_WhoIs + lastname + StringUtils.SPACE + firstname + " ," + birthStr + "?"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (pat != null) {
			return CoreModelServiceHolder.get().load(pat.getId(), IPatient.class).orElse(null);
		}
		return null;
	}

}
