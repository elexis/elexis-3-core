package ch.elexis.data.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.types.DocumentStatus;
import ch.elexis.data.dto.BriefDocumentDTO;
import ch.elexis.data.dto.CategoryDocumentDTO;

public class BriefDocumentStoreTest {
	
	static IDocumentStore iDocumentStore;
	
	@BeforeClass
	public static void beforeClass(){
		BundleContext context =
			FrameworkUtil.getBundle(BriefDocumentStoreTest.class).getBundleContext();
		ServiceReference<?> reference = context.getServiceReference(IDocumentStore.class);
		iDocumentStore = (IDocumentStore) context.getService(reference);
	}
	
	@Test
	public void testCrudAndSearchDocuments() throws IOException, ElexisException{
		
		// persist metadata of documents
		BriefDocumentDTO briefDocumentDTO = new BriefDocumentDTO("ch.elexis.data.store.brief");
		briefDocumentDTO.setPatientId("-1");
		briefDocumentDTO.setStatus(DocumentStatus.RECIVED); // invalid state for a new document
		briefDocumentDTO.setDescription("Test desc");
		briefDocumentDTO.setMimeType("docx");
		briefDocumentDTO.setTitle("Test Brief");
		briefDocumentDTO.setCategory(new CategoryDocumentDTO(BriefConstants.RECHNUNG));
		iDocumentStore.saveDocument(briefDocumentDTO);
		
		// search documents
		Assert.assertEquals(0, iDocumentStore.getDocuments("WRONG ID", null, null, null).size());
		List<IDocument> documents = iDocumentStore.getDocuments("-1", null, null, null);
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
		documents = iDocumentStore.getDocuments("-1", null,
			new CategoryDocumentDTO(BriefConstants.BESTELLUNG), null);
		Assert.assertEquals(0, documents.size());
		
		// search with true category
		documents = iDocumentStore.getDocuments("-1", null,
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
		Assert.assertEquals(1, iDocumentStore.getDocuments("-1", null, null, null).size());
		iDocumentStore.removeDocument(persistedDocument);
		Assert.assertEquals(0, iDocumentStore.getDocuments("-1", null, null, null).size());
	}
}
