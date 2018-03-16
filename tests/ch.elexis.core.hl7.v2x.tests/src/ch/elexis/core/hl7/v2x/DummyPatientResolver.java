package ch.elexis.core.hl7.v2x;

import java.util.Collections;
import java.util.List;

import ch.elexis.core.data.beans.ContactBean;
import ch.elexis.core.model.IPatient;
import ch.elexis.data.Patient;
import ch.elexis.hl7.HL7PatientResolver;

public class DummyPatientResolver extends HL7PatientResolver {
	
	private Patient patient;
	
	public Patient getPatient(){
		return patient;
	}
	
	public DummyPatientResolver(Patient dummyPatient){
		this.patient = dummyPatient;
	}

	@Override
	public boolean matchPatient(IPatient patient, String firstname, String lastname,
		String birthDate){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IPatient createPatient(String lastName, String firstName, String birthDate,
		String sex){
		return new ContactBean(new Patient(lastName, firstName, birthDate, sex));
	}

	@Override
	public List<IPatient> getPatientById(String patid){
		return Collections.singletonList(new ContactBean(patient));
	}

	@Override
	public List<IPatient> findPatientByNameAndBirthdate(String lastName, String firstName,
		String birthDate){
		return Collections.singletonList(new ContactBean(patient));
	}

	@Override
	public IPatient resolvePatient(String firstname, String lastname, String birthDate){
		return new ContactBean(patient);
	}
}