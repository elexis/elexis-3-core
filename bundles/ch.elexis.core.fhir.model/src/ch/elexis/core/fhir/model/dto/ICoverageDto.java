package ch.elexis.core.fhir.model.dto;

import java.time.LocalDate;
import java.util.List;

import ch.elexis.core.model.IBillingSystem;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import lombok.Data;

@Data
public class ICoverageDto extends IdentifiableDeletableDto implements ICoverage {

	String insuranceNumber;
	IBillingSystem billingSystem;
	IContact guarantor;
	LocalDate dateTo;
	LocalDate dateFrom;
	String reason;
	LocalDate billingProposalDate;
	String description;
	IContact costBearer;
	IPatient patient;
	boolean open;

	@Override
	public List<IEncounter> getEncounters() {
		// TODO Auto-generated method stub
		return null;
	}

}
