package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

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

public class IStockServiceTest extends AbstractServiceTest {

	static IModelService coreModelService = AllServiceTests.getModelService();
	private IStockService service;
	private IPatient patient;
	private IArticle article;

	@Before
	public void before() {
		service = OsgiServiceUtil.getService(IStockService.class).get();
		patient = new IContactBuilder.PatientBuilder(coreModelService, "test", "patient",
				LocalDate.of(2000, 1, 1), Gender.MALE).buildAndSave();
		article = new IArticleBuilder(coreModelService, "test medication article", "1234567", ArticleTyp.ARTIKELSTAMM)
				.build();
	}

	private IStock preparePatientStock() {
		service.setEnablePatientStock(patient, true);
		return service.getPatientStock(patient).orElseThrow(() -> new AssertionError("No stock found"));
	}

	@Test
	public void createStock() {
		service.setEnablePatientStock(patient, true);
		IStock stock = service.getPatientStock(patient).get();
		assertEquals(0, stock.getPriority());
		assertEquals("P" + patient.getPatientNr(), stock.getCode());
		assertEquals("patient test", stock.getDescription());
		assertFalse(stock.isDeleted());
		
		service.storeArticleInStock(stock, StoreToStringServiceHolder.getStoreToString(article));
		coreModelService.save(article);
	}

	@Test
	public void deleteStock() {
		IStock stock = preparePatientStock();
		service.setEnablePatientStock(patient, false);
		List<IStockEntry> entries = service.findAllStockEntriesForStock(stock);
		assertEquals(true, stock.isDeleted());
		for (IStockEntry entry : entries) {
			assertEquals(true, entry.isDeleted());
		}
	}

	@Test
	public void activateDeletedStock() {
		IStock stock = preparePatientStock();
		stock.setDeleted(true);
		service.setEnablePatientStock(patient, true);
		assertEquals(false, stock.isDeleted());
		List<IStockEntry> entries = service.findAllStockEntriesForStock(stock);
		for (IStockEntry entry : entries) {
			assertEquals(true, entry.isDeleted());
		}
	}
}
