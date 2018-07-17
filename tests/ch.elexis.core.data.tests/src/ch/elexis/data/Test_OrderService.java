package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IOrderEntry;
import ch.elexis.core.data.service.StockService;
import ch.elexis.core.data.services.IOrderService;
import ch.rgw.tools.JdbcLink;


public class Test_OrderService extends AbstractPersistentObjectTest {
		
	public Test_OrderService(JdbcLink link){
		super(link);
	}

	private static IOrderService orderService = CoreHub.getOrderService();
	
	private static Stock stock_A_5_order;
	private static Artikel artikel_A;
	private static StockEntry stockEntry;
	
	private static StockService stockService = CoreHub.getStockService();
	
	@BeforeClass
	public static void init(){
		stock_A_5_order = new Stock("AOD", 5);
		artikel_A = new Artikel("ArtikelAOrder", "Eigenartikel");
		
		stockEntry = (StockEntry) stockService.storeArticleInStock(stock_A_5_order,
			artikel_A.storeToString());
		stockEntry.setMinimumStock(5);
		stockEntry.setCurrentStock(10);
		stockEntry.setMaximumStock(15);
		
	}
	
	@AfterClass
	public static void afterClass() {
		stock_A_5_order.removeFromDatabase();
	}
	
	@Test
	public void testAddRefillForStockEntryToOrderAndFindOpenOrder() {
		Bestellung b = new Bestellung("TestBestellung", CoreHub.actUser);
		orderService.addRefillForStockEntryToOrder(stockEntry, b);
		assertEquals(1, b.getEntries().size());
		assertEquals(5,  b.getEntries().get(0).getCount());
	
		IOrderEntry ioe = orderService.findOpenOrderEntryForStockEntry(stockEntry);
		assertNotNull(ioe);
	}
}
