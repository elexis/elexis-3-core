package ch.elexis.core.fhir.model.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.MaritalStatus;
import ch.elexis.core.types.Gender;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IPersonDto extends IContactDto implements IPerson {

	Gender gender;
	String titel;
	String titelSuffix;
	LocalDateTime dateOfBirth;
	LocalDateTime dateOfDeath;
	MaritalStatus maritalStatus;
	IContact legalGuardian;

	public IPersonDto() {
		setPerson(true);
	}

	@Override
	public int getAgeInYears() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getFirstName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFirstName(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getLastName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLastName(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getAgeAtIn(LocalDateTime reference, ChronoUnit chronoUnit) {
		// TODO Auto-generated method stub
		return 0;
	}

}
