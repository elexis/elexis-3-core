package ch.elexis.core.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentTemplate;
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
	}

	@After
	public void after() {
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
		IContext context = ContextServiceHolder.get().createNamedContext("create_document_context");
		context.setNamed("Adressat", testMandators.get(0));

		IDocument createdDocument = documentService.createDocument(documentTemplate, context);
		assertNotNull(createdDocument);

		int foundCount = getFindTextCount("mandator1");
		assertTrue(foundCount > 0);
	}

	private int getFindTextCount(String text) throws Exception {
		Method method = textPlugin.getClass().getMethod("findTextCount", new Class[] { String.class });
		return (int) method.invoke(textPlugin, text);
	}
}
