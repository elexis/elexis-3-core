package ch.elexis.core.fhir.model.dto;

import java.util.List;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.prescription.EntryType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IPatientDto extends IPersonDto implements IPatient {

	IContact familyDoctor;
	String diagnosen;
	String risk;
	String familyAnamnese;
	String personalAnamnese;
	String allergies;
	String patientNr;

	public IPatientDto() {
		setPerson(true);
		setPatient(true);
	}

	@Override
	public List<ICoverage> getCoverages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IPrescription> getMedication(List<EntryType> filterType) {
		// TODO Auto-generated method stub
		return null;
	}

}
