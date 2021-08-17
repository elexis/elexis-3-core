package ch.elexis.core.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.internal.dto.CategoryDocumentDTO;
import ch.elexis.core.types.DocumentStatus;
import ch.elexis.core.utils.OsgiServiceUtil;

public class BriefDocumentStoreTest extends AbstractServiceTest {
	
	static IDocumentStore iDocumentStore;
	
	@Before
	public void before(){
		Optional<IDocumentStore> documentService = OsgiServiceUtil.getService(IDocumentStore.class,
			"(storeid=ch.elexis.data.store.brief)");
		iDocumentStore =
			documentService.orElseThrow(() -> new IllegalStateException("No service available"));
		
		createTestMandantPatientFallBehandlung();
	}
	
	@After
	public void after(){
		cleanup();
		OsgiServiceUtil.ungetService(iDocumentStore);
	}
	
	@Test
	public void testCrudAndSearchDocuments() throws IOException, ElexisException{
		IPatient iPatient = testPatients.get(0);
		
		// persist metadata of documents
		IDocumentLetter letter = coreModelService.create(IDocumentLetter.class);
		letter.setPatient(iPatient);
		letter.setDescription("Test desc");
		letter.setMimeType("docx");
		letter.setTitle("Test Brief");
		letter.setCategory(new CategoryDocumentDTO(BriefConstants.RECHNUNG));
		letter.setEncounter(testEncounters.get(0));
		letter.setContent(getClass().getResourceAsStream("/rsc/Testdocument.docx"));
		iDocumentStore.saveDocument(letter);
		
		// search documents
		Assert.assertEquals(0, iDocumentStore.getDocuments("WRONG ID", null, null, null).size());
		List<IDocument> documents = iDocumentStore.getDocuments(iPatient.getId(), null, null, null);
		Assert.assertEquals(1, documents.size());
		
		IDocumentLetter persistedDocument = (IDocumentLetter) documents.get(0);
		Assert.assertTrue(persistedDocument.getId() != null);
		Assert.assertEquals("Test Brief", persistedDocument.getTitle());
		Assert.assertEquals("docx", persistedDocument.getMimeType());
		Assert.assertEquals("Test desc", persistedDocument.getDescription());
		Assert.assertEquals(BriefConstants.RECHNUNG, persistedDocument.getCategory().getName());
		Assert.assertEquals(Collections.singletonList(DocumentStatus.NEW),
			persistedDocument.getStatus());
		Assert.assertEquals(5554, persistedDocument.getContentLength());
		Assert.assertEquals(testEncounters.get(0), persistedDocument.getEncounter());
		
		// save content
		iDocumentStore.saveDocument(persistedDocument, IOUtils.toInputStream("test"));
		
		// search with wrong category
		documents = iDocumentStore.getDocuments(iPatient.getId(), null,
			new CategoryDocumentDTO(BriefConstants.BESTELLUNG), null);
		Assert.assertEquals(0, documents.size());
		
		// search with true category
		documents = iDocumentStore.getDocuments(iPatient.getId(), null,
			new CategoryDocumentDTO(BriefConstants.RECHNUNG), null);
		Assert.assertEquals(1, documents.size());
		
		// verify content
		persistedDocument = (IDocumentLetter) documents.get(0);
		Assert.assertTrue(persistedDocument.getId() != null);
		Assert.assertEquals("Test Brief", persistedDocument.getTitle());
		Optional<InputStream> in = iDocumentStore.loadContent(persistedDocument);
		Assert.assertTrue(in.isPresent());
		String s = IOUtils.toString(in.get());
		Assert.assertEquals("test", s);
		
		// remove document
		Assert.assertEquals(1,
			iDocumentStore.getDocuments(iPatient.getId(), null, null, null).size());
		iDocumentStore.removeDocument(persistedDocument);
		Assert.assertEquals(0,
			iDocumentStore.getDocuments(iPatient.getId(), null, null, null).size());
	}
}
