package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.builder.IArticleBuilder;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IStockServiceTest extends AbstractServiceTest {

	static IModelService coreModelService = AllServiceTests.getModelService();
	private static IStockService service;
	private static IPatient patient;
	private static IArticle article;

	@BeforeClass
	public static void beforeClass() {
		service = OsgiServiceUtil.getService(IStockService.class).get();
		patient = new IContactBuilder.PatientBuilder(coreModelService, "test", "patient", LocalDate.of(2000, 1, 1),
				Gender.MALE).buildAndSave();
		article = new IArticleBuilder(coreModelService, "test medication article", "1234567", ArticleTyp.ARTIKELSTAMM)
				.buildAndSave();
	}

	@Test
	public void a_createStock() {
		service.setEnablePatientStock(patient, true);

		IStock stock = service.getPatientStock(patient).get();
		assertEquals(0, stock.getPriority());
		assertEquals("P" + patient.getPatientNr(), stock.getCode());
		assertEquals("patient test", stock.getDescription());
		assertFalse(stock.isDeleted());

		IStockEntry stockEntry = service.storeArticleInStock(stock,
				StoreToStringServiceHolder.getStoreToString(article));
		assertEquals(article, stockEntry.getArticle());
		assertEquals(1, stockEntry.getCurrentStock());
	}

	@Test
	public void b_deleteStock() {
		IStock stock = service.getPatientStock(patient).get();
		List<IStockEntry> stockEntries = service.findAllStockEntriesForStock(stock);
		assertEquals(article, stockEntries.get(0).getArticle());
		assertEquals(1, stockEntries.get(0).getCurrentStock());

		service.setEnablePatientStock(patient, false);

		assertEquals(true, stock.isDeleted());
		List<IStockEntry> entries = service.findAllStockEntriesForStock(stock);
		assertTrue(entries.isEmpty());
	}

	@Test
	public void c_reactivateDeletedStock() {
		assertFalse(service.getPatientStock(patient).isPresent());

		service.setEnablePatientStock(patient, true);

		Optional<IStock> patientStock = service.getPatientStock(patient);
		assertTrue(patientStock.isPresent());
		List<IStockEntry> entries = service.findAllStockEntriesForStock(patientStock.get());
		assertTrue(entries.size() == 1);
		for (IStockEntry entry : entries) {
			assertEquals(false, entry.isDeleted());
		}
	}
}
