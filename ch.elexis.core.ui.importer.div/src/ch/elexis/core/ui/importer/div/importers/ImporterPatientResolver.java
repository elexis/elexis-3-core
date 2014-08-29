package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.exchange.KontaktMatcher;
import ch.elexis.core.ui.exchange.KontaktMatcher.CreateMode;
import ch.elexis.data.Patient;
import ch.elexis.hl7.HL7PatientResolver;
import ch.rgw.tools.TimeTool;

public class ImporterPatientResolver extends HL7PatientResolver {
	
	private TimeTool convertTool = new TimeTool();
	
	@Override
	public Patient resolvePatient(String firstname, String lastname, String birthDate){
		// resolve with full data
		Patient pat =
			KontaktMatcher.findPatient(lastname, firstname, birthDate, "", "", "", "", "",
				CreateMode.FAIL);
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
			pat =
				KontaktMatcher.findPatient(shortLastname, shortFirstname, birthDate, "", "", "",
					"", "", CreateMode.FAIL);
		}
		// user decides
		if (pat == null) {
			convertTool.set(birthDate);
			String birthStr = convertTool.toString(TimeTool.DATE_GER);
			pat =
				(Patient) KontaktSelektor.showInSync(Patient.class, Messages.HL7_SelectPatient,
					Messages.HL7_WhoIs + lastname + " " + firstname + " ," + birthStr + "?");
		}
		
		return pat;
	}
	
	@Override
	public boolean matchPatient(Patient patient, String firstname, String lastname, String birthDate){
		return KontaktMatcher.isSame(patient, lastname, firstname, birthDate);
	}
}
