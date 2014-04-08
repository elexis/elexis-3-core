package ch.elexis.hl7;

import ch.elexis.data.Patient;

public abstract class HL7PatientResolver {
	public abstract Patient resolvePatient(String firstname, String lastname, String birthDate);
	
	public abstract boolean matchPatient(Patient patient, String firstname, String lastname,
		String birthDate);
}
