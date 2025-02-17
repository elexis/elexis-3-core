package ch.elexis.core.findings.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter.EncounterStatus;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.Test;

import ch.elexis.core.findings.util.test.AllTests;

public class FindingsFormatUtilTest {

	@Test
	public void isCurrentFindingsFormat() throws IOException {
		String content = AllTests.getResourceAsString("/rsc/json/ConditionFormat20.json");
		assertFalse(FindingsFormatUtil.isCurrentFindingsFormat(content));

		content = AllTests.getResourceAsString("/rsc/json/EncounterFormat20.json");
		assertFalse(FindingsFormatUtil.isCurrentFindingsFormat(content));

		content = AllTests.getResourceAsString("/rsc/json/ProcedureRequestFormat20.json");
		assertFalse(FindingsFormatUtil.isCurrentFindingsFormat(content));
	}

	@Test
	public void convertCondition24() throws IOException {
		// condition format of HAPI FHIR 3.0
		String oldContent = AllTests.getResourceAsString("/rsc/json/ConditionFormat24.json");
		assertFalse(FindingsFormatUtil.isCurrentFindingsFormat(oldContent));

		Optional<String> newContent = FindingsFormatUtil.convertToCurrentFindingsFormat(oldContent);
		assertTrue(newContent.isPresent());

		IBaseResource resource = AllTests.getJsonParser4().parseResource(newContent.get());
		assertTrue(resource instanceof org.hl7.fhir.r4.model.Condition);
		org.hl7.fhir.r4.model.Condition condition = (org.hl7.fhir.r4.model.Condition) resource;

		// category changed from primitive to codeable concept
		org.hl7.fhir.r4.model.CodeableConcept clinicalStatus = condition.getClinicalStatus();
		assertFalse(clinicalStatus.isEmpty());
		List<org.hl7.fhir.r4.model.Coding> coding = clinicalStatus.getCoding();
		assertFalse(coding.isEmpty());
		assertTrue(coding.get(0).getCode().equals("active"));
		// dateRecorded changed to assertedDate
		Date assertedDate = condition.getRecordedDate();
		assertNotNull(assertedDate);
	}

	@Test
	public void convertEncounter24() throws IOException {
		// encounter format of HAPI FHIR 2.4
		String oldContent = AllTests.getResourceAsString("/rsc/json/EncounterFormat24.json");
		assertFalse(FindingsFormatUtil.isCurrentFindingsFormat(oldContent));

		Optional<String> newContent = FindingsFormatUtil.convertToCurrentFindingsFormat(oldContent);
		assertTrue(newContent.isPresent());

		IBaseResource resource = AllTests.getJsonParser4().parseResource(newContent.get());
		assertTrue(resource instanceof org.hl7.fhir.r4.model.Encounter);
		org.hl7.fhir.r4.model.Encounter encounter = (org.hl7.fhir.r4.model.Encounter) resource;

		// add required status
		EncounterStatus encounterStatus = encounter.getStatus();
		assertNotNull(encounterStatus);
		// diagnosis did not change
		List<org.hl7.fhir.r4.model.Encounter.DiagnosisComponent> diagnosis = encounter.getDiagnosis();
		assertNotNull(diagnosis);
		assertFalse(diagnosis.isEmpty());
		org.hl7.fhir.r4.model.Encounter.DiagnosisComponent component = diagnosis.get(0);
		org.hl7.fhir.r4.model.Reference conditionRef = component.getCondition();
		assertNotNull(conditionRef);
		assertTrue(conditionRef.getReference().contains("b32e7576f35aeed5c071273"));
		// subject did not change
		org.hl7.fhir.r4.model.Reference subjectRef = encounter.getSubject();
		assertNotNull(subjectRef);
		assertTrue(subjectRef.getReference().contains("p40991d40ab93bd6c071259"));
	}

	@Test
	public void convertProdcedureRequest24() throws IOException {
		// encounter format of HAPI FHIR 2.0
		String oldContent = AllTests.getResourceAsString("/rsc/json/ProcedureRequestFormat24.json");
		assertFalse(FindingsFormatUtil.isCurrentFindingsFormat(oldContent));

		Optional<String> newContent = FindingsFormatUtil.convertToCurrentFindingsFormat(oldContent);
		assertTrue(newContent.isPresent());

		IBaseResource resource = AllTests.getJsonParser4().parseResource(newContent.get());
		assertTrue(resource instanceof ServiceRequest);
		ServiceRequest serviceRequest = (ServiceRequest) resource;

		// context changed to encounter
		org.hl7.fhir.r4.model.Reference encounterRef = serviceRequest.getEncounter();
		assertNotNull(encounterRef);
		assertTrue(encounterRef.getReference().contains("bccd5b535f6c4334aa9ca5cc0"));
	}
}
