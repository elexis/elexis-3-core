package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.OrderServiceHolder;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.utils.OsgiServiceUtil;

public class Test_OrderService extends AbstractPersistentObjectTest {

	private static IStock stock_A_5_order;
	private static Artikel artikel_A;
	private static IStockEntry stockEntry;

	private static IStockService stockService = OsgiServiceUtil.getService(IStockService.class).get();

	@BeforeClass
	public static void init() {
		stock_A_5_order = CoreModelServiceHolder.get().create(IStock.class);
		stock_A_5_order.setCode("AOD");
		stock_A_5_order.setPriority(5);
		CoreModelServiceHolder.get().save(stock_A_5_order);
		artikel_A = new Artikel("ArtikelAOrder", "Eigenartikel");
		artikel_A.set(Artikel.FLD_TYP, ArticleTyp.EIGENARTIKEL.name());

		stockEntry = stockService.storeArticleInStock(stock_A_5_order, artikel_A.storeToString());
		assertNotNull(stockEntry);
		stockEntry.setMinimumStock(5);
		stockEntry.setCurrentStock(10);
		stockEntry.setMaximumStock(15);

	}

	@AfterClass
	public static void afterClass() {
		CoreModelServiceHolder.get().remove(stockEntry);
		CoreModelServiceHolder.get().remove(stock_A_5_order);
	}

	@Test
	public void testAddRefillForStockEntryToOrderAndFindOpenOrder() {
		Bestellung b = new Bestellung("TestBestellung", CoreHub.getLoggedInContact());
		OrderServiceHolder.get().addRefillForStockEntryToOrder(stockEntry, b.toIOrder(), false);
		assertEquals(1, b.getEntries().size());
		assertEquals(5, b.getEntries().get(0).getCount());

		IOrderEntry ioe = OrderServiceHolder.get().findOpenOrderEntryForStockEntry(stockEntry);
		assertNotNull(ioe);
	}
}
