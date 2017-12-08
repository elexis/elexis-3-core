package ch.elexis.core.findings.util.fhir.accessor;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;

public class AllergyIntoleranceAccessor extends AbstractFindingsAccessor {
	
	public void setPatientId(DomainResource resource, String patientId){
		org.hl7.fhir.dstu3.model.AllergyIntolerance fhAllergyIntolerance =
			(org.hl7.fhir.dstu3.model.AllergyIntolerance) resource;
		fhAllergyIntolerance.setPatient(new Reference(new IdDt("Patient", patientId)));
	}
}
