package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.codings.ValueSetServiceComponent;
import ch.elexis.core.findings.test.AllTests;
import ch.elexis.core.model.IDocument;

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
	
	@Test
	public void setDocument() throws ElexisException{
		IDocumentReference reference =
			FindingsServiceComponent.getService().create(IDocumentReference.class);
		assertNotNull(reference);
		reference.setPatientId(AllTests.PATIENT_ID);
		
		IDocument document =
			DocumentStoreComponent.getService().createDocument(AllTests.PATIENT_ID, "test", "test");
		DocumentStoreComponent.getService().saveDocument(document);
		reference.setDocument(document);
		FindingsServiceComponent.getService().saveFinding(reference);
		
		IDocumentReference reloaded = FindingsServiceComponent.getService()
			.findById(reference.getId(), IDocumentReference.class)
			.orElseThrow(() -> new IllegalStateException("IDocumentReference not found"));
		
		IDocument reloadedDocument = reloaded.getDocument();
		assertNotNull(reloadedDocument);
		assertEquals(document.getId(), reloadedDocument.getId());
	}
}
