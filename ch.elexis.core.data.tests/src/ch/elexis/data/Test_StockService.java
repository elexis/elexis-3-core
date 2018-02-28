package ch.elexis.data;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.StockService;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IStockService.Availability;
import ch.rgw.tools.JdbcLink;

public class Test_StockService extends AbstractPersistentObjectTest {
	
	public Test_StockService(JdbcLink link){
		super(link);
	}
	
	private static Stock defaultStock;
	private static Stock stock_A_5_public;
	private static Stock stock_B_10_private;
	private static Mandant stock_B_10_owner;
	
	private static Artikel artikel_A;
	private static Artikel artikel_B;
	private static Artikel artikel_C;
	
	private static StockService stockService = CoreHub.getStockService();
	
	@Before
	public void before(){		
		Query<Stock> qre = new Query<Stock>(Stock.class);
		qre.add(Stock.FLD_CODE, Query.EQUALS, "A");
		List<Stock> execute = qre.execute();
		if (execute.size() > 0) {
			stock_A_5_public = execute.get(0);
		} else {
			stock_A_5_public = new Stock("A", 5);
		}
		
		qre.clear();
		qre.add(Stock.FLD_CODE, Query.EQUALS, "PRV");
		execute = qre.execute();
		if (execute.size() > 0) {
			stock_B_10_private = execute.get(0);
		} else {
			stock_B_10_private = new Stock("PRV", 10);
		}
		
		stock_B_10_owner = new Mandant("Mandant", "Musterfrau", "26081950", "s");
		stock_B_10_private.setOwner(stock_B_10_owner);
		
		artikel_A = new Artikel("ArtikelA", "Eigenartikel");
		artikel_B = new Artikel("ArtikelB", "Eigenartikel");
		artikel_C = new Artikel("ArtikelC", "Eigenartikel");
		
		defaultStock = Stock.load("STD");
		assertEquals("STD", defaultStock.getId());
		
		IStockEntry stockEntry_A =
			stockService.storeArticleInStock(defaultStock, artikel_A.storeToString());
		stockEntry_A.setMinimumStock(5);
		stockEntry_A.setCurrentStock(10);
		stockEntry_A.setMaximumStock(15);
		IStockEntry stockEntry_A_5 =
			stockService.storeArticleInStock(stock_A_5_public, artikel_A.storeToString());
		IStockEntry stockEntry_A_PRIV =
			stockService.storeArticleInStock(stock_B_10_private, artikel_A.storeToString());
		IStockEntry stockEntry_B =
			stockService.storeArticleInStock(defaultStock, artikel_B.storeToString());
		IStockEntry stockEntry_B_5 =
			stockService.storeArticleInStock(stock_A_5_public, artikel_B.storeToString());
	}
	
	@Test
	public void testCreateEditAndDeleteStock(){
		int size = stockService.getAllStocks(true).size();
		Stock stock = new Stock("TMP", 3);
		List<Stock> allStocks = stockService.getAllStocks(true);
		assertEquals(size + 1, allStocks.size());
		for (int i = 0; i < allStocks.size(); i++) {
			Stock s = allStocks.get(i);
			if (i == 0) {
				assertEquals("STD", s.getCode());
				assertEquals("STD", s.getId());
				assertEquals(Integer.valueOf(0), Integer.valueOf(s.getPriority()));
			} else if (i == 1) {
				assertEquals(Integer.valueOf(3), Integer.valueOf(s.getPriority()));
				assertEquals("TMP", s.getCode());
			} else if (i == 2) {
				assertEquals(Integer.valueOf(5), Integer.valueOf(s.getPriority()));
				assertEquals("A", s.getCode().trim()); // trim() needed for postgres
			} else if (i == 3) {
				assertEquals(Integer.valueOf(10), Integer.valueOf(s.getPriority()));
				assertEquals("PRV", s.getCode());
				assertEquals(stock_B_10_owner.getId(), s.getOwner().getId());
			}
		}
		stock.delete();
		allStocks = stockService.getAllStocks(true);
		assertEquals(size, allStocks.size());
	}
	
	@Test
	public void testDenyStockDeleteOnExistingStockEntries(){
		Stock stock = new Stock("TMP", 15);
		Artikel artikel = new Artikel("ArtikelC", "Eigenartikel");
		stockService.storeArticleInStock(stock, artikel.storeToString());
		stock.delete();
	}
	
	@Test
	public void testStoreUnstoreFindPreferredArticleInStock(){
		List<? extends IStockEntry> a_entries =
			stockService.findAllStockEntriesForArticle(artikel_A.storeToString());
		assertEquals(3, a_entries.size());
		List<? extends IStockEntry> b_entries =
			stockService.findAllStockEntriesForArticle(artikel_B.storeToString());
		assertEquals(2, b_entries.size());
		IStockEntry stockEntry_A_STD =
			stockService.findStockEntryForArticleInStock(defaultStock, artikel_A.storeToString());
		assertEquals("STD", stockEntry_A_STD.getStock().getCode());
		IStockEntry stockEntry_A_PUB = stockService
			.findStockEntryForArticleInStock(stock_A_5_public, artikel_A.storeToString());
		assertEquals("A", stockEntry_A_PUB.getStock().getCode().trim()); // trim() needed for postgres
		
		IStockEntry stockEntry = stockService
			.findPreferredStockEntryForArticle(artikel_A.storeToString(), stock_B_10_owner.getId());
		assertEquals("PRV", stockEntry.getStock().getCode());
		IStockEntry stockEntry_PUB =
			stockService.findPreferredStockEntryForArticle(artikel_A.storeToString(), null);
		assertEquals("STD", stockEntry_PUB.getStock().getCode());
		
		stockService.unstoreArticleFromStock(stock_B_10_private, artikel_A.storeToString());
		IStockEntry stockEntry_unstoredPriv = stockService
			.findPreferredStockEntryForArticle(artikel_A.storeToString(), stock_B_10_owner.getId());
		assertEquals("STD", stockEntry_unstoredPriv.getStock().getCode());
	}
	
	@Test
	public void testStockAvailabilities(){
		Availability availability_A = stockService.getCumulatedAvailabilityForArticle(artikel_A);
		assertEquals(Availability.IN_STOCK, availability_A);
		Availability availability_B = stockService.getCumulatedAvailabilityForArticle(artikel_B);
		assertEquals(Availability.OUT_OF_STOCK, availability_B);
		
		assertEquals(Availability.OUT_OF_STOCK, stockService
			.getArticleAvailabilityForStock(stock_A_5_public, artikel_A.storeToString()));
		assertEquals(Availability.IN_STOCK,
			stockService.getArticleAvailabilityForStock(defaultStock, artikel_A.storeToString()));
		
		Stock stock = new Stock("TMP", 20);
		IStockEntry se = stockService.storeArticleInStock(stock, artikel_C.storeToString());
		se.setMinimumStock(5);
		se.setCurrentStock(3);
		assertEquals(Availability.CRITICAL_STOCK,
			stockService.getCumulatedAvailabilityForArticle(artikel_C));
		assertEquals(Availability.CRITICAL_STOCK,
			stockService.getArticleAvailabilityForStock(stock, artikel_C.storeToString()));
		stock.removeFromDatabase();
	}
	
	@Test
	public void testPerformDisposalAndReturnOfArticle(){
		Stock stock = new Stock("TMP", 20);
		IStockEntry se = stockService.storeArticleInStock(stock, artikel_C.storeToString());
		se.setMinimumStock(15);
		se.setCurrentStock(13);
		
		stockService.performSingleDisposal(artikel_C, 5);
		IStockEntry prefSE =
			stockService.findPreferredStockEntryForArticle(artikel_C.storeToString(), null);
		assertEquals(8, prefSE.getCurrentStock());
		assertEquals(15, prefSE.getMinimumStock());
		
		stockService.performSingleReturn(artikel_C, 3);
		prefSE = stockService.findPreferredStockEntryForArticle(artikel_C.storeToString(), null);
		assertEquals(11, prefSE.getCurrentStock());
		assertEquals(15, prefSE.getMinimumStock());
		
		stock.removeFromDatabase();
	}
	
	@Test
	public void testPerformMultipleStoreOnSingleStockForArticle(){
		stockService.storeArticleInStock(defaultStock, artikel_C.storeToString());
		assertEquals(1,
			stockService.findAllStockEntriesForArticle(artikel_C.storeToString()).size());
		stockService.storeArticleInStock(defaultStock, artikel_C.storeToString());
		assertEquals(1,
			stockService.findAllStockEntriesForArticle(artikel_C.storeToString()).size());
	}
	
	@Test
	public void testQueryMappedExpressionNumeric(){
		Stock stock = new Stock("TST", 20);
		Artikel art = new Artikel("TestARtikel", "Eigenartikel");
		Artikel art2 = new Artikel("TestARtikel2", "Eigenartikel");
		Artikel art3 = new Artikel("TestARtikel3", "Eigenartikel");
		IStockEntry se = CoreHub.getStockService().storeArticleInStock(stock, art.storeToString());
		se.setCurrentStock(12);
		se.setMaximumStock(10);
		se.setMinimumStock(5);
		se = CoreHub.getStockService().storeArticleInStock(stock, art2.storeToString());
		se.setCurrentStock(12);
		se.setMaximumStock(12);
		se.setMinimumStock(5);
		se = CoreHub.getStockService().storeArticleInStock(stock, art3.storeToString());
		se.setCurrentStock(4);
		se.setMaximumStock(12);
		se.setMinimumStock(5);
		Query<StockEntry> qbe = new Query<StockEntry>(StockEntry.class);
		qbe.add(StockEntry.FLD_STOCK, Query.EQUALS, stock.getId());
		qbe.add(StockEntry.FLD_CURRENT, Query.LESS_OR_EQUAL, StockEntry.FLD_MIN);
		List<StockEntry> execute = qbe.execute();
		assertEquals(1, execute.size());
		
		stock.removeFromDatabase();
	}
}
