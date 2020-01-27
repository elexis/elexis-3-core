package ch.elexis.core.findings;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.codings.ValueSetServiceComponent;
import ch.elexis.core.findings.test.AllTests;

public class DocumentReferenceTest {
	
	@Before
	public void beforeTest(){
		AllTests.deleteAllFindings();
		List<IFinding> findings = FindingsServiceComponent.getService()
			.getPatientsFindings(AllTests.PATIENT_ID, IFinding.class);
		assertTrue(findings.isEmpty());
	}
	
	@Test
	public void getProperties(){
		IDocumentReference reference =
			FindingsServiceComponent.getService().create(IDocumentReference.class);
		assertNotNull(reference);
		reference.setPatientId(AllTests.PATIENT_ID);
		Map<String, ICoding> practiceSettingValueSet =
			ValueSetServiceComponent.getService().asMap(ValueSetServiceComponent.getService()
				.getValueSetByName("EprDocumentPracticeSettingCode"));
		// General medicine
		reference.setPracticeSetting(practiceSettingValueSet.get("394802001"));
		
		Map<String, ICoding> documentClassValueSet = ValueSetServiceComponent.getService()
			.asMap(ValueSetServiceComponent.getService().getValueSetByName("EprDocumentClassCode"));
		// Note on Procedure
		reference.setDocumentClass(documentClassValueSet.get("1241000195103"));
		
		Map<String, ICoding> facilityTypeValueSet =
			ValueSetServiceComponent.getService().asMap(ValueSetServiceComponent.getService()
				.getValueSetByName("EprHealthcareFacilityTypeCode"));
		// Ambulatory care site 
		reference.setFacilityType(facilityTypeValueSet.get("35971002"));
		FindingsServiceComponent.getService().saveFinding(reference);
		
		IDocumentReference reloaded = FindingsServiceComponent.getService().findById(reference.getId(), IDocumentReference.class).orElseThrow(() -> new IllegalStateException("IDocumentReference not found"));
		
		assertTrue(practiceSettingValueSet.containsKey(reloaded.getPracticeSetting().getCode()));
		assertTrue(documentClassValueSet.containsKey(reloaded.getDocumentClass().getCode()));
		assertTrue(facilityTypeValueSet.containsKey(reloaded.getFacilityType().getCode()));
	}
}
