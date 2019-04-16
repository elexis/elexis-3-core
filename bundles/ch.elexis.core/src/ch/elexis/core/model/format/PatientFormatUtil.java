package ch.elexis.core.model.format;

import ch.elexis.core.model.IPatient;

public class PatientFormatUtil {
	
	public static String getNarrativeText(IPatient patient){
		return ((patient.getTitel() != null) ? patient.getTitel() + " " : "")
			+ patient.getFirstName() + " " + patient.getLastName() + " ("
			+ PersonFormatUtil.getGenderCharLocalized(patient) + "), "
			+ PersonFormatUtil.getDateOfBirth(patient) + " (" + patient.getAgeInYears()
			+ ") - [" + patient.getPatientNr() + "]";
	}
}
