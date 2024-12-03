package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.model.builder.IArticleBuilder;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IOrderServiceTest extends AbstractServiceTest {

	static IModelService coreModelService = AllServiceTests.getModelService();
	private static IOrderService orderService;
	private static IArticle article;
	private static IStock stock;
	private static IStockEntry stockEntry;
	private static IOrder order;

	@BeforeClass
	public static void beforeClass() {
		orderService = OsgiServiceUtil.getService(IOrderService.class).get();
		article = new IArticleBuilder(coreModelService, "test medication article", "1234567", ArticleTyp.ARTIKELSTAMM)
				.buildAndSave();

		stock = createStock();
		stockEntry = createStockEntry();
		order = createOrder();
	}

	@Test
	public void addRefillForStockEntryToOrder() {
		IOrderEntry newEntry = orderService.addRefillForStockEntryToOrder(stockEntry, order);

		assertNotNull(newEntry);
		assertEquals(stockEntry.getArticle(), newEntry.getArticle());
		assertEquals(2, newEntry.getAmount());
	}

	@Test
	public void findOpenOrderEntryForStockEntry() {
		IOrderEntry entry = orderService.findOpenOrderEntryForStockEntry(stockEntry);

		assertNotNull(entry);
		assertEquals(order.getEntries().get(0), entry);
	}

	@Test
	public void findOrderEntryForStock() {
		IOrderEntry orderEntry = order.getEntries().get(0);
		orderEntry.setState(OrderEntryState.DONE);

		List<IOrderEntry> entries = orderService.findOrderEntryForStock(stock);
		assertFalse(entries.isEmpty());
		assertTrue(entries.contains(orderEntry));
	}

	private static IStock createStock() {
		IStock stock = coreModelService.create(IStock.class);
		stock.setCode("TES");
		stock.setPriority(1);
		stock.setDescription("Test");
		coreModelService.save(stock);
		return stock;
	}

	private static IStockEntry createStockEntry() {
		IStockEntry stockEntry = coreModelService.create(IStockEntry.class);
		stockEntry.setStock(stock);
		stockEntry.setCurrentStock(0);
		stockEntry.setMaximumStock(1);
		stockEntry.setArticle(article);
		return stockEntry;
	}

	private static IOrder createOrder() {
		IOrder order = coreModelService.create(IOrder.class);
		coreModelService.save(order);
		coreModelService.save(order.addEntry(article, stock, null, 1));
		return order;
	}
}
