package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.builder.IArticleBuilder;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.TimeTool;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IStockServiceTest extends AbstractServiceTest {

	static IModelService coreModelService = AllServiceTests.getModelService();
	private static IStockService service;
	private static IPatient patient;
	private static IArticle article;
	private static IPerson person;

	@BeforeClass
	public static void beforeClass() {
		TimeTool timeTool = new TimeTool();
		service = OsgiServiceUtil.getService(IStockService.class).get();
		patient = new IContactBuilder.PatientBuilder(coreModelService, "test", "patient", LocalDate.of(2000, 1, 1),
				Gender.MALE).buildAndSave();
		article = new IArticleBuilder(coreModelService, "test medication article", "1234567", ArticleTyp.ARTIKELSTAMM)
				.buildAndSave();
		person = new IContactBuilder.PersonBuilder(coreModelService, "mandator1 " + timeTool.toString(),
				"Anton" + timeTool.toString(), timeTool.toLocalDate(), Gender.MALE).mandator().buildAndSave();
	}

	@Test
	public void a_createPatientStock() {
		service.setEnablePatientStock(patient, true);

		IStock stock = service.getPatientStock(patient).get();
		assertEquals(0, stock.getPriority());
		assertEquals("PatientStock-" + patient.getPatientNr(), stock.getId());
		assertEquals("P" + patient.getPatientNr(), stock.getCode());
		assertEquals("patient test", stock.getDescription());
		assertEquals("Patient", stock.getLocation());
		assertFalse(stock.isDeleted());

		IStockEntry stockEntry = service.storeArticleInStock(stock,
				StoreToStringServiceHolder.getStoreToString(article));
		assertEquals(article, stockEntry.getArticle());
		assertEquals(1, stockEntry.getCurrentStock());
	}

	@Test
	public void b_deletePatientStock() {
		IStock patientStock = service.getPatientStock(patient).get();
		List<IStockEntry> stockEntries = service.findAllStockEntriesForStock(patientStock);
		assertEquals(article, stockEntries.get(0).getArticle());
		assertEquals(1, stockEntries.get(0).getCurrentStock());
		assertEquals(patientStock, stockEntries.get(0).getStock());

		service.setEnablePatientStock(patient, false);

		assertFalse(service.getPatientStock(patient).isPresent());
		List<IStockEntry> entries = service.findAllStockEntriesForStock(patientStock);
		assertTrue(entries.isEmpty());
	}

	@Test
	public void performSingleDisposalWithProvidedStock() {
		IStock stock = coreModelService.create(IStock.class);
		stock.setOwner(person);
		IStockEntry stockEntry = service.storeArticleInStock(stock, article);
		stockEntry.setCurrentStock(2);
		coreModelService.save(stockEntry);

		service.performSingleDisposal(article, 1, person.getId(), null);
		assertEquals(1, service.findStockEntryForArticleInStock(stock, article).getCurrentStock());
	}

	@Test
	public void performSingleReturn() {
		IStock stock = coreModelService.create(IStock.class);
		IStockEntry stockEntry = service.storeArticleInStock(stock, article);
		stockEntry.setCurrentStock(1);
		coreModelService.save(stockEntry);

		service.performSingleReturn(article, 1, person.getId(), stock);
		assertEquals(2, service.findStockEntryForArticleInStock(stock, article).getCurrentStock());

	}
}
