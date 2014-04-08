package ch.elexis.core.ui.importer.div.importers;

import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.exchange.KontaktMatcher;
import ch.elexis.data.Patient;
import ch.elexis.hl7.HL7PatientResolver;

public class ImporterPatientResolver extends HL7PatientResolver {
	
	@Override
	public Patient resolvePatient(String firstname, String lastname, String birthDate){
		return (Patient) KontaktSelektor.showInSync(Patient.class, Messages.HL7_SelectPatient,
			Messages.HL7_WhoIs + lastname + " " + firstname + " ," + birthDate + "?");
	}
	
	@Override
	public boolean matchPatient(Patient patient, String firstname, String lastname, String birthDate){
		return KontaktMatcher.isSame(patient, lastname, firstname, birthDate);
	}
}
