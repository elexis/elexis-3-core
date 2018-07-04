package ch.elexis.data.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.DocumentStatus;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.data.dto.CategoryDocumentDTO;

public class BriefDocumentStoreTest {
	
	static IDocumentStore iDocumentStore;
	
	static IModelService iModelService;
	
	static IPatient iPatient;
	
	@BeforeClass
	public static void beforeClass(){
		Optional<IModelService> modelService = OsgiServiceUtil.getService(IModelService.class);
		iModelService =
			modelService.orElseThrow(() -> new IllegalStateException("No service available"));
		
		Optional<IDocumentStore> documentService = OsgiServiceUtil.getService(IDocumentStore.class);
		iDocumentStore =
			documentService.orElseThrow(() -> new IllegalStateException("No service available"));
		
		iPatient = iModelService.create(IPatient.class);
		iPatient.setFirstName("test");
		iPatient.setLastName("patient");
		iModelService.save(iPatient);
	}
	
	@AfterClass
	public static void afterClass(){
		OsgiServiceUtil.ungetService(iDocumentStore);
		OsgiServiceUtil.ungetService(iModelService);
	}
	
	@Test
	public void testCrudAndSearchDocuments() throws IOException, ElexisException{
		// persist metadata of documents
		IDocumentLetter letter = iModelService.create(IDocumentLetter.class);
		letter.setPatient(iPatient);
		letter.setStatus(DocumentStatus.RECIVED); // invalid state for a new document
		letter.setDescription("Test desc");
		letter.setMimeType("docx");
		letter.setTitle("Test Brief");
		letter.setCategory(new CategoryDocumentDTO(BriefConstants.RECHNUNG));
		iDocumentStore.saveDocument(letter);
		
		// search documents
		Assert.assertEquals(0, iDocumentStore.getDocuments("WRONG ID", null, null, null).size());
		List<IDocument> documents = iDocumentStore.getDocuments(iPatient.getId(), null, null, null);
		Assert.assertEquals(1, documents.size());
		
		IDocument persistedDocument = documents.get(0);
		Assert.assertTrue(persistedDocument.getId() != null);
		Assert.assertEquals("Test Brief", persistedDocument.getTitle());
		Assert.assertEquals( "docx", persistedDocument.getMimeType());
		Assert.assertEquals("Test desc", persistedDocument.getDescription() );
		Assert.assertEquals(BriefConstants.RECHNUNG, persistedDocument.getCategory().getName());
		Assert.assertEquals( DocumentStatus.NEW, persistedDocument.getStatus());
		
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
		persistedDocument = documents.get(0);
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
