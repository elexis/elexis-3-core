package ch.elexis.hl7;

import java.util.List;

import ch.elexis.core.model.IPatient;

public abstract class HL7PatientResolver {
	public abstract IPatient resolvePatient(String firstname, String lastname, String birthDate);
	
	public abstract boolean matchPatient(IPatient patient, String firstname, String lastname,
		String birthDate);
		
	public abstract IPatient createPatient(String lastName, String firstName, String birthDate,
		String sex);
		
	public abstract List<? extends IPatient> getPatientById(String patid);
	
	public abstract List<? extends IPatient> findPatientByNameAndBirthdate(String lastName, String firstName,
		String birthDate);
}
