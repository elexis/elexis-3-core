package ch.elexis.core.hl7.v2x;

import java.util.Collections;
import java.util.List;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.hl7.HL7PatientResolver;
import ch.rgw.tools.TimeTool;

public class DummyPatientResolver extends HL7PatientResolver {
	
	private IPatient patient;
	
	public IPatient getPatient(){
		return patient;
	}
	
	public DummyPatientResolver(IPatient dummyPatient){
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
		TimeTool birthDateTimeTool = new TimeTool(birthDate);
		Gender gender = Gender.fromValue(sex);
		return new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), firstName, lastName,
			birthDateTimeTool.toLocalDate(), gender).buildAndSave();
	}

	@Override
	public List<IPatient> getPatientById(String patid){
		return Collections.singletonList(patient);
	}

	@Override
	public List<IPatient> findPatientByNameAndBirthdate(String lastName, String firstName,
		String birthDate){
		return Collections.singletonList(patient);
	}

	@Override
	public IPatient resolvePatient(String firstname, String lastname, String birthDate){
		return patient;
	}
}