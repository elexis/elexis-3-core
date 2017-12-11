package ch.elexis.core.findings.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.DiagnosisComponent;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Test;

import ch.elexis.core.findings.ICondition.ConditionCategory;
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
	public void convertCondition20() throws IOException {
		// condition format of HAPI FHIR 2.0
		String oldContent = AllTests.getResourceAsString("/rsc/json/ConditionFormat20.json");
		assertFalse(FindingsFormatUtil.isCurrentFindingsFormat(oldContent));

		Optional<String> newContent = FindingsFormatUtil.convertToCurrentFindingsFormat(oldContent);
		assertTrue(newContent.isPresent());

		IBaseResource resource = AllTests.getJsonParser().parseResource(newContent.get());
		assertTrue(resource instanceof Condition);
		Condition condition = (Condition) resource;

		// category changed from diagnosis to problem-list-item
		List<CodeableConcept> category = condition.getCategory();
		assertFalse(category.isEmpty());
		CodeableConcept code = category.get(0);
		List<Coding> coding = code.getCoding();
		assertFalse(coding.isEmpty());
		assertTrue(coding.get(0).getCode().equals(ConditionCategory.PROBLEMLISTITEM.getCode()));
		// dateRecorded changed to assertedDate
		Date assertedDate = condition.getAssertedDate();
		assertNotNull(assertedDate);
	}

	@Test
	public void convertEncounter20() throws IOException {
		// encounter format of HAPI FHIR 2.0
		String oldContent = AllTests.getResourceAsString("/rsc/json/EncounterFormat20.json");
		assertFalse(FindingsFormatUtil.isCurrentFindingsFormat(oldContent));

		Optional<String> newContent = FindingsFormatUtil.convertToCurrentFindingsFormat(oldContent);
		assertTrue(newContent.isPresent());

		IBaseResource resource = AllTests.getJsonParser().parseResource(newContent.get());
		assertTrue(resource instanceof Encounter);
		Encounter encounter = (Encounter) resource;

		// indication changed to diagnosis
		List<DiagnosisComponent> diagnosis = encounter.getDiagnosis();
		assertNotNull(diagnosis);
		assertFalse(diagnosis.isEmpty());
		DiagnosisComponent component = diagnosis.get(0);
		Reference conditionRef = component.getCondition();
		assertNotNull(conditionRef);
		assertTrue(conditionRef.getReference().contains("CONDITIONA"));
		// patient changed to subject
		Reference subjectRef = encounter.getSubject();
		assertNotNull(subjectRef);
		assertTrue(subjectRef.getReference().contains("PATIENTID"));
	}

	@Test
	public void convertProdcedureRequest20() throws IOException {
		// encounter format of HAPI FHIR 2.0
		String oldContent = AllTests.getResourceAsString("/rsc/json/ProcedureRequestFormat20.json");
		assertFalse(FindingsFormatUtil.isCurrentFindingsFormat(oldContent));

		Optional<String> newContent = FindingsFormatUtil.convertToCurrentFindingsFormat(oldContent);
		assertTrue(newContent.isPresent());

		IBaseResource resource = AllTests.getJsonParser().parseResource(newContent.get());
		assertTrue(resource instanceof ProcedureRequest);
		ProcedureRequest procedureRequest = (ProcedureRequest) resource;

		// encounter changed to context
		Reference encounterRef = procedureRequest.getContext();
		assertNotNull(encounterRef);
		assertTrue(encounterRef.getReference().contains("ENCOUNTERID"));
	}
}
