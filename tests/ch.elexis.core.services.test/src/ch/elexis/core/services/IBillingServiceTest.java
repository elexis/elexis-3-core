package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IFreeTextDiagnosis;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.InvoiceServiceHolder;
import ch.elexis.core.services.holder.StockServiceHolder;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public class IBillingServiceTest extends AbstractServiceTest {
	
	private IBillingService billingService =
		OsgiServiceUtil.getService(IBillingService.class).get();
	
	private static ICustomService customService;
	private static IArticle customArticle;
	private static IEncounter encounter;
	private static IStockEntry customArticleStockEntry;
	
	@BeforeClass
	public static void beforeClass(){
		customService = coreModelService.create(ICustomService.class);
		customService.setText("test service");
		customService.setCode("1234");
		customService.setNetPrice(new Money(512));
		customService.setPrice(new Money(1024));
		coreModelService.save(customService);
		
		customArticle = coreModelService.create(IArticle.class);
		customArticle.setText("test article");
		customArticle.setGtin("0123456789012");
		customArticle.setTyp(ArticleTyp.EIGENARTIKEL);
		coreModelService.save(customArticle);
		
		customArticleStockEntry = StockServiceHolder.get()
			.storeArticleInStock(StockServiceHolder.get().getDefaultStock(), customArticle);
		assertEquals(1, customArticleStockEntry.getCurrentStock());
		
		encounter = new IEncounterBuilder(CoreModelServiceHolder.get(),
			AllServiceTests.getCoverage(), AllServiceTests.getMandator()).buildAndSave();
	}
	
	@AfterClass
	public static void afterClass(){
		CoreModelServiceHolder.get().remove(encounter);
	}
	
	@Before
	public void before(){
		createTestMandantPatientFallBehandlung();
		ContextServiceHolder.get().setActiveMandator(testMandators.get(0));
	}
	
	@After
	public void after(){
		ContextServiceHolder.get().setActiveMandator(null);
		customArticleStockEntry.setCurrentStock(1);
		coreModelService.save(customArticleStockEntry);
		
		cleanup();
	}
	
	@Test
	public void billCustomService() throws InterruptedException{
		Result<IBilled> billed = billingService.bill(customService, encounter, 1.0);
		assertTrue(billed.getMessages().get(0).getText(), billed.isOK());
		List<IBilled> billedList = encounter.getBilled();
		
		assertEquals(1, billedList.size());
		assertEquals(1, billedList.get(0).getAmount(), 0d);
		assertEquals(1024, billedList.get(0).getPrice().getCents());
		
		// Import - wird verrechnet
		// Leistung wird deleted = 1
		// neuerliche verrechnung aber keine anzeige
		EntityManager em = (EntityManager) OsgiServiceUtil.getService(IElexisEntityManager.class)
			.get().getEntityManager(true);
		em.getTransaction().begin();
		int executeUpdate =
			em.createNativeQuery("UPDATE LEISTUNGEN SET DELETED='1' WHERE DELETED='0'")
				.executeUpdate();
		assertEquals(1, executeUpdate);
		em.getTransaction().commit();
		
		billed = billingService.bill(customService, encounter, 1.0);
		
		billedList = encounter.getBilled();
		assertFalse(billedList.get(0).isDeleted());
		assertEquals(1, billedList.size());
		assertEquals(1, billedList.get(0).getAmount(), 0d);
		assertEquals(1024, billedList.get(0).getPrice().getCents());
		
		coreModelService.remove(billed.get());
	}
	
	@Test
	public void isNonEditableCoverageHasEndDate(){
		ContextServiceHolder.get().setActiveMandator(testMandators.get(0));
		
		Result<IEncounter> isEditable = billingService.isEditable(testEncounters.get(0));
		assertTrue(isEditable.toString(), isEditable.isOK());
		
		ICoverage fall = testEncounters.get(0).getCoverage();
		fall.setDateTo(LocalDate.now());
		coreModelService.save(fall);
		
		Result<IEncounter> result = billingService.isEditable(testEncounters.get(0));
		assertFalse(result.toString(), result.isOK());
	}
	
	@Test
	public void isNonEditableAsInvoiceIsSet(){
		ContextServiceHolder.get().setActiveUser(AllServiceTests.getUser());
		ContextServiceHolder.get().setActiveMandator(testMandators.get(0));
		ConfigServiceHolder.get().set(ContextServiceHolder.get().getActiveUserContact().get(),
			ch.elexis.core.constants.Preferences.LEISTUNGSCODES_BILLING_STRICT, false);
		
		Result<IEncounter> isEditable = billingService.isEditable(testEncounters.get(0));
		assertTrue(isEditable.toString(), isEditable.isOK());
		
		IFreeTextDiagnosis diagnosis = coreModelService.create(IFreeTextDiagnosis.class);
		diagnosis.setDescription("test");
		diagnosis.setText("testText");
		coreModelService.save(diagnosis);
		testEncounters.get(0).addDiagnosis(diagnosis);
		coreModelService.save(testEncounters.get(0));
		Result<IInvoice> invoice = InvoiceServiceHolder.get().invoice(testEncounters);
		assertTrue(invoice.toString(), invoice.isOK());
		
		Result<IEncounter> result = billingService.isEditable(testEncounters.get(0));
		assertFalse(result.toString(), result.isOK());
	}
	
	@Test
	public void billArticleAndDecrementStock(){
		Result<IBilled> billed = billingService.bill(customArticle, encounter, 1.0);
		assertTrue(billed.getMessages().get(0).getText(), billed.isOK());
		CoreModelServiceHolder.get().remove(billed.get());
		
		IStockEntry stockEntry = StockServiceHolder.get().findStockEntryForArticleInStock(
			StockServiceHolder.get().getDefaultStock(), customArticle);
		assertEquals(0, stockEntry.getCurrentStock());
	}
	
	@Test
	public void changeAmountCorrectlyModifiesStock(){
		customArticleStockEntry.setCurrentStock(8);
		coreModelService.save(customArticleStockEntry);
		
		Result<IBilled> billed = billingService.bill(customArticle, encounter, 1.0);
		assertTrue(billed.getMessages().get(0).getText(), billed.isOK());
		
		billingService.changeAmountValidated(billed.get(), 4);
		IStockEntry stockEntry = StockServiceHolder.get().findStockEntryForArticleInStock(
			StockServiceHolder.get().getDefaultStock(), customArticle);
		assertEquals(4, stockEntry.getCurrentStock());
		
		billingService.changeAmountValidated(billed.get(), 3);
		stockEntry = StockServiceHolder.get().findStockEntryForArticleInStock(
			StockServiceHolder.get().getDefaultStock(), customArticle);
		assertEquals(5, stockEntry.getCurrentStock());
		
		CoreModelServiceHolder.get().remove(billed.get());
	}
	
	@Test
	public void billSellingSizeSmallerPackageSize(){
		IArticle customArticle_2 = coreModelService.create(IArticle.class);
		customArticle_2.setText("Actimove Gilchrist Plus");
		customArticle_2.setTyp(ArticleTyp.EIGENARTIKEL);
		customArticle_2.setPackageSize(10);
		customArticle_2.setSellingPrice(new Money(6350));
		customArticle_2.setSellingSize(1);
		coreModelService.save(customArticle_2);
		
		Result<IBilled> billed = billingService.bill(customArticle_2, encounter, 1.0);
		assertTrue(billed.getMessages().get(0).getText(), billed.isOK());
		double billed_amount = billed.get().getAmount();
		double billed_price = billed.get().getPrice().getCents();
		
		coreModelService.remove(billed.get());
		coreModelService.remove(customArticle_2);
		
		assertEquals(1, billed_amount, 0.1);
		assertEquals(new Money(635).getCents(), billed_price, 0.1);
	}
	
}
