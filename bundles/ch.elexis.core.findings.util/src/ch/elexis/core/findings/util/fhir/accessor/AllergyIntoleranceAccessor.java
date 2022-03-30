package ch.elexis.core.findings.util.fhir.accessor;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;

public class AllergyIntoleranceAccessor extends AbstractFindingsAccessor {

	public void setPatientId(DomainResource resource, String patientId) {
		AllergyIntolerance fhAllergyIntolerance = (AllergyIntolerance) resource;
		fhAllergyIntolerance.setPatient(new Reference(new IdDt("Patient", patientId)));
	}
}
