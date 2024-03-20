package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IDocumentTemplate;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.builder.IArticleBuilder;
import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.format.PersonFormatUtil;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.internal.dto.CategoryDocumentDTO;
import ch.elexis.core.test.context.TestContext;
import ch.elexis.core.text.ITextPlugin;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IDocumentServiceTest extends AbstractServiceTest {

	private IDocumentService documentService;

	private IDocumentStore documentStore;

	private IDocumentTemplate documentTemplate;

	private IDocumentTemplate directTemplate;

	private ITextPlugin textPlugin;

	private IContext context;

	private IMedicationService medicationService;

	private List<IPrescription> createdPrescriptions;

	private IArticle localArticle;

	private IRecipe recipe;

	@Before
	public void before() throws ElexisException {
		textPlugin = OsgiServiceUtil.getService(ITextPlugin.class)
				.orElseThrow(() -> new IllegalStateException("No text plugin available"));
		documentStore = OsgiServiceUtil.getService(IDocumentStore.class, "(storeid=ch.elexis.data.store.brief)")
				.orElseThrow(() -> new IllegalStateException("No service available"));
		documentService = OsgiServiceUtil.getService(IDocumentService.class)
				.orElseThrow(() -> new IllegalStateException("No service available"));
		medicationService = OsgiServiceUtil.getService(IMedicationService.class)
				.orElseThrow(() -> new IllegalStateException("No service available"));

		IDocumentTemplate document = CoreModelServiceHolder.get().create(IDocumentTemplate.class);
		document.setTitle("TestPlaceholders");
		document.setMimeType("docx");
		document.setCategory(new CategoryDocumentDTO(BriefConstants.TEMPLATE));
		documentTemplate = (IDocumentTemplate) documentStore.saveDocument(document,
				getClass().getResourceAsStream("/rsc/TestPlaceholders.docx"));

		document = CoreModelServiceHolder.get().create(IDocumentTemplate.class);
		document.setTitle("TestDirectTemplates");
		document.setMimeType("docx");
		document.setCategory(new CategoryDocumentDTO(BriefConstants.TEMPLATE));
		directTemplate = (IDocumentTemplate) documentStore.saveDocument(document,
				getClass().getResourceAsStream("/rsc/TestDirectTemplates.docx"));

		createTestMandantPatientFallBehandlung();
		context = ContextServiceHolder.get().createNamedContext("create_document_context");
		((TestContext) context).setParent(null);

		createTestPrescriptions();
	}

	private void createTestPrescriptions() {
		localArticle = new IArticleBuilder(coreModelService, "test medication article", "1234567",
				ArticleTyp.EIGENARTIKEL).build();
		localArticle.setGtin("1111111000000");
		localArticle.setPackageSize(2);
		localArticle.setSellingSize(1);
		coreModelService.save(localArticle);

		createdPrescriptions = new ArrayList<>();
		createdPrescriptions
				.add(new IPrescriptionBuilder(coreModelService, null, localArticle, testPatients.get(0), "0-1-1-0")
						.entryType(EntryType.FIXED_MEDICATION).disposalComment("disposal comment").buildAndSave());
		createdPrescriptions
				.add(new IPrescriptionBuilder(coreModelService, null, localArticle, testPatients.get(0), "1-0-0-1")
						.entryType(EntryType.SYMPTOMATIC_MEDICATION).dosageInstruction("dosage instruction")
						.buildAndSave());
		createdPrescriptions
				.add(new IPrescriptionBuilder(coreModelService, null, localArticle, testPatients.get(0), "1-0-0-0")
						.entryType(EntryType.RESERVE_MEDICATION).buildAndSave());
		createdPrescriptions
				.add(new IPrescriptionBuilder(coreModelService, null, localArticle, testPatients.get(0), "0-0-0-1")
						.entryType(EntryType.FIXED_MEDICATION).buildAndSave());

		recipe = medicationService.createRecipe(testPatients.get(0), createdPrescriptions);
	}

	@After
	public void after() {
		removeTestPrescriptions();

		ContextServiceHolder.get().releaseContext("create_document_context");

		cleanup();

		if (documentTemplate != null) {
			documentStore.removeDocument(documentTemplate);
		}

		OsgiServiceUtil.ungetService(documentService);
		OsgiServiceUtil.ungetService(documentStore);
		OsgiServiceUtil.ungetService(textPlugin);
	}

	private void removeTestPrescriptions() {
		coreModelService.remove(recipe);
		coreModelService.remove(createdPrescriptions);
		coreModelService.remove(localArticle);
	}

	@Test
	public void adressatValidation() throws Exception {
		Map<String, Boolean> validationResult = documentService.validateTemplate(documentTemplate, context);
		assertNotNull(validationResult);
		assertFalse(validationResult.get("[Adressat.Vorname]"));
		context.setNamed("Adressat", testMandators.get(0));
		validationResult = documentService.validateTemplate(documentTemplate, context);
		assertNotNull(validationResult);
		assertTrue(validationResult.get("[Adressat.Vorname]"));
		assertTrue(validationResult.get("[Adressat.Anschrift]"));
	}

	@Test
	public void adressatReplacement() throws Exception {
		context.setNamed("Adressat", testMandators.get(0));

		IDocument createdDocument = documentService.createDocument(documentTemplate, context).getObject();
		assertNotNull(createdDocument);

		int foundCount = textPlugin.findCount("mandator1");
		assertTrue(foundCount > 0);
		foundCount = textPlugin.findCount("Lieber");
		assertTrue(foundCount > 0);
		foundCount = textPlugin.findCount("[Adressat.Anschrift]");
		assertTrue(foundCount == 0);

		saveToTempFileAndDelete(createdDocument);
	}

	@Test
	public void patientReplacement() throws Exception {
		context.setTyped(testPatients.get(0));

		IDocument createdDocument = documentService.createDocument(documentTemplate, context).getObject();
		assertNotNull(createdDocument);
		assertEquals(testPatients.get(0), createdDocument.getPatient());
		assertNotNull(createdDocument.getCreated());
		assertEquals("docx", createdDocument.getMimeType());

		int foundCount = textPlugin.findCount("Armer");
		assertTrue(foundCount > 0);
		foundCount = textPlugin.findCount(PersonFormatUtil.getDateOfBirth(testPatients.get(0)));
		assertTrue(foundCount > 0);

		saveToTempFileAndDelete(createdDocument);
	}

	@Test
	public void patientValidation() throws Exception {
		Map<String, Boolean> validationResult = documentService.validateTemplate(documentTemplate, context);
		assertNotNull(validationResult);
		for (String key : validationResult.keySet()) {
			if (key.startsWith("[Patient.")) {
				assertFalse(validationResult.get(key));
			}
		}
		context.setTyped(testPatients.get(0));
		validationResult = documentService.validateTemplate(documentTemplate, context);
		assertNotNull(validationResult);
		for (String key : validationResult.keySet()) {
			if (key.startsWith("[Patient.")) {
				assertTrue(validationResult.get(key));
			}
		}
	}

	@Test
	public void mandantReplacement() throws Exception {
		context.setTyped(testMandators.get(0));

		IDocument createdDocument = documentService.createDocument(documentTemplate, context).getObject();
		assertNotNull(createdDocument);

		int foundCount = textPlugin.findCount("mandator1");
		assertTrue(foundCount > 0);

		saveToTempFileAndDelete(createdDocument);
	}

	@Test
	public void directRezeptzeilenValidation() throws Exception {
		Map<String, Boolean> validationResult = documentService.validateTemplate(directTemplate, context);
		assertNotNull(validationResult);
		for (String key : validationResult.keySet()) {
			if (key.startsWith("[Rezept")) {
				assertFalse(validationResult.get(key));
			}
		}
		context.setTyped(recipe);
		validationResult = documentService.validateTemplate(directTemplate, context);
		assertNotNull(validationResult);
		for (String key : validationResult.keySet()) {
			if (key.startsWith("[Rezept")) {
				assertTrue(validationResult.get(key));
			}
		}
	}

	@Test
	public void directRezeptzeilenReplacement() throws Exception {
		context.setTyped(recipe);

		IDocument createdDocument = documentService.createDocument(directTemplate, context).getObject();
		assertNotNull(createdDocument);

		saveToTempFileAndDelete(createdDocument);

		int foundCount = textPlugin.findCount("[Rezeptzeilen]");
		assertEquals(0, foundCount);
		foundCount = textPlugin.findCount("test medication article");
		assertTrue(foundCount > 0);
	}

	@Test
	public void directRezeptzeilenExtReplacement() throws Exception {
		context.setTyped(recipe);

		IDocument createdDocument = documentService.createDocument(directTemplate, context).getObject();
		assertNotNull(createdDocument);

		saveToTempFileAndDelete(createdDocument);

		int foundCount = textPlugin.findCount("[RezeptzeilenExt]");
		assertEquals(0, foundCount);
		foundCount = textPlugin.findCount("disposal comment");
		assertTrue(foundCount > 0);
	}

	@Test
	public void directMedikamentenlisteReplacement() throws Exception {
		context.setTyped(recipe);

		IDocument createdDocument = documentService.createDocument(directTemplate, context).getObject();
		assertNotNull(createdDocument);

		saveToTempFileAndDelete(createdDocument);

		int foundCount = textPlugin.findCount("[Medikamentenliste]");
		assertEquals(0, foundCount);
		foundCount = textPlugin.findCount("dosage instruction");
		assertTrue(foundCount > 0);
	}

	@Test
	public void fallValidation() throws Exception {
		Map<String, Boolean> validationResult = documentService.validateTemplate(documentTemplate, context);
		assertNotNull(validationResult);
		for (String key : validationResult.keySet()) {
			if (key.startsWith("[Fall")) {
				assertFalse(validationResult.get(key));
			}
		}
		context.setTyped(AllServiceTests.getCoverage());
		validationResult = documentService.validateTemplate(documentTemplate, context);
		assertNotNull(validationResult);
		for (String key : validationResult.keySet()) {
			if (key.startsWith("[Fall")) {
				assertTrue(validationResult.get(key));
			}
		}
	}

	@Test
	public void fallReplacement() throws Exception {
		context.setTyped(AllServiceTests.getCoverage());

		IDocument createdDocument = documentService.createDocument(documentTemplate, context).getObject();
		assertNotNull(createdDocument);

		saveToTempFileAndDelete(createdDocument);

		int foundCount = textPlugin.findCount("[Fall:-:-:Kostentraeger]");
		assertEquals(0, foundCount);
		foundCount = textPlugin.findCount("1234-5678");
		assertTrue(foundCount > 0);
		foundCount = textPlugin.findCount("Test Organization");
		assertTrue(foundCount > 0);
	}

	private void saveToTempFileAndDelete(IDocument document) throws IOException {
		File tempFile = File.createTempFile(document.getTitle(), ".docx");
		FileUtils.copyInputStreamToFile(document.getContent(), tempFile);
		tempFile.delete();
	}
}
