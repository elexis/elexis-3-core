package ch.elexis.core.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.model.format.PersonFormatUtil;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.internal.dto.CategoryDocumentDTO;
import ch.elexis.core.text.ITextPlugin;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IDocumentServiceTest extends AbstractServiceTest {

	private IDocumentService documentService;

	private IDocumentStore documentStore;

	private IDocumentTemplate documentTemplate;

	private ITextPlugin textPlugin;

	private IContext context;

	@Before
	public void before() throws ElexisException {
		textPlugin = OsgiServiceUtil.getService(ITextPlugin.class)
				.orElseThrow(() -> new IllegalStateException("No text plugin available"));
		documentStore = OsgiServiceUtil.getService(IDocumentStore.class, "(storeid=ch.elexis.data.store.brief)")
				.orElseThrow(() -> new IllegalStateException("No service available"));
		documentService = OsgiServiceUtil.getService(IDocumentService.class)
				.orElseThrow(() -> new IllegalStateException("No service available"));

		IDocumentTemplate document = CoreModelServiceHolder.get().create(IDocumentTemplate.class);
		document.setTitle("TestPlaceholders");
		document.setMimeType("docx");
		document.setCategory(new CategoryDocumentDTO(BriefConstants.TEMPLATE));
		documentTemplate = (IDocumentTemplate) documentStore.saveDocument(document,
				getClass().getResourceAsStream("/rsc/TestPlaceholders.docx"));

		createTestMandantPatientFallBehandlung();
		context = ContextServiceHolder.get().createNamedContext("create_document_context");
	}

	@After
	public void after() {
		ContextServiceHolder.get().releaseContext("create_document_context");

		cleanup();

		if (documentTemplate != null) {
			documentStore.removeDocument(documentTemplate);
		}

		OsgiServiceUtil.ungetService(documentService);
		OsgiServiceUtil.ungetService(documentStore);
		OsgiServiceUtil.ungetService(textPlugin);
	}

	@Test
	public void adressatReplacement() throws Exception {
		context.setNamed("Adressat", testMandators.get(0));

		IDocument createdDocument = documentService.createDocument(documentTemplate, context);
		assertNotNull(createdDocument);

		int foundCount = getFindTextCount("mandator1");
		assertTrue(foundCount > 0);
		foundCount = getFindTextCount("Lieber");
		assertTrue(foundCount > 0);

		saveToTempFileAndDelete(createdDocument);
	}

	@Test
	public void patientReplacement() throws Exception {
		context.setTyped(testPatients.get(0));

		IDocument createdDocument = documentService.createDocument(documentTemplate, context);
		assertNotNull(createdDocument);

		int foundCount = getFindTextCount("Armer");
		assertTrue(foundCount > 0);
		foundCount = getFindTextCount(PersonFormatUtil.getDateOfBirth(testPatients.get(0)));
		assertTrue(foundCount > 0);

		saveToTempFileAndDelete(createdDocument);
	}

	@Test
	public void mandantReplacement() throws Exception {
		context.setTyped(testMandators.get(0));

		IDocument createdDocument = documentService.createDocument(documentTemplate, context);
		assertNotNull(createdDocument);

		int foundCount = getFindTextCount("mandator1");
		assertTrue(foundCount > 0);

		saveToTempFileAndDelete(createdDocument);
	}

	private int getFindTextCount(String text) throws Exception {
		Method method = textPlugin.getClass().getMethod("findTextCount", new Class[] { String.class });
		return (int) method.invoke(textPlugin, text);
	}

	private void saveToTempFileAndDelete(IDocument document) throws IOException {
		File tempFile = File.createTempFile(document.getTitle(), ".docx");
		FileUtils.copyInputStreamToFile(document.getContent(), tempFile);
		tempFile.delete();
	}
}
