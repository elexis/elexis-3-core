package ch.elexis.core.findings.util.fhir.accessor;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;

public class FamilyMemberHistoryAccessor extends AbstractFindingsAccessor {
	
	public void setPatientId(DomainResource resource, String patientId){
		org.hl7.fhir.dstu3.model.FamilyMemberHistory fhFamilyMemberHistory =
			(org.hl7.fhir.dstu3.model.FamilyMemberHistory) resource;
		fhFamilyMemberHistory.setPatient(new Reference(new IdDt("Patient", patientId)));
	}
}
