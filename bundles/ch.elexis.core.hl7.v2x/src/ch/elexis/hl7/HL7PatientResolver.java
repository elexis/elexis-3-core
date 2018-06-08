package ch.elexis.hl7;

import java.util.List;

import ch.elexis.core.data.interfaces.IPatient;

public abstract class HL7PatientResolver {
	/**
	 * Search for an existing {@link IPatient} with the provided properties. If no {@link IPatient}
	 * is found, the User is presented with a UI to select the {@link IPatient}.
	 * 
	 * @param firstname
	 * @param lastname
	 * @param birthDate
	 * @return
	 */
	public abstract IPatient resolvePatient(String firstname, String lastname, String birthDate);
	
	/**
	 * Search for an existing {@link IPatient} with the provided properties.
	 * 
	 * @param patient
	 * @param firstname
	 * @param lastname
	 * @param birthDate
	 * @return
	 */
	public abstract boolean matchPatient(IPatient patient, String firstname, String lastname,
		String birthDate);
	
	/**
	 * Create a new {@link IPatient} with the provided properties.
	 * 
	 * @param lastName
	 * @param firstName
	 * @param birthDate
	 * @param sex
	 * @return
	 */
	public abstract IPatient createPatient(String lastName, String firstName, String birthDate,
		String sex);
	
	/**
	 * Search for an existing {@link IPatient} with the provided patient number.
	 * 
	 * @param patient
	 *            number
	 * @return
	 */
	public abstract List<? extends IPatient> getPatientById(String patid);
	
	/**
	 * Search for existing list of {@link IPatient}s with the provided properties.
	 * 
	 * @param lastName
	 * @param firstName
	 * @param birthDate
	 * @return
	 */
	public abstract List<? extends IPatient> findPatientByNameAndBirthdate(String lastName, String firstName,
		String birthDate);
}
