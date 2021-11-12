package ch.elexis.core.findings.util.fhir.accessor;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.FamilyMemberHistory;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;

public class FamilyMemberHistoryAccessor extends AbstractFindingsAccessor {
	
	public void setPatientId(DomainResource resource, String patientId){
		FamilyMemberHistory fhFamilyMemberHistory = (FamilyMemberHistory) resource;
		fhFamilyMemberHistory.setPatient(new Reference(new IdDt("Patient", patientId)));
	}
}
