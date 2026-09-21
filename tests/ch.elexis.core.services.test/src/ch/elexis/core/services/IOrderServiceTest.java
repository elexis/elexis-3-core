package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.model.builder.IArticleBuilder;
import ch.elexis.core.rcp.utils.OsgiServiceUtil;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.types.ArticleTyp;

public class IOrderServiceTest extends AbstractServiceTest {

	static IModelService coreModelService = OsgiServiceUtil
			.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
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

	@Test
	public void calculateDailyDifferences_shouldCalculateCorrectDiff() {
		IMandator mandator = AllServiceTests.getMandator();
		ContextServiceHolder.get().setActiveMandator(mandator);
		IArticle testArticle = new IArticleBuilder(coreModelService, "Testartikel", "9999999", ArticleTyp.EIGENARTIKEL)
				.buildAndSave();
		IEncounter encounter = coreModelService.create(IEncounter.class);
		encounter.setDate(LocalDate.now());
		encounter.setMandator(mandator);
		encounter.setBillable(true);
		coreModelService.save(encounter);
		IBillingService billingService = OsgiServiceUtil.getService(IBillingService.class).get();
		billingService.bill(testArticle, encounter, 5.0);
		IOrder order = coreModelService.create(IOrder.class);
		IOrderEntry entry = order.addEntry(testArticle, stock, null, 2);
		entry.setState(OrderEntryState.OPEN);
		coreModelService.save(order);
		coreModelService.save(entry);
		Map<IArticle, Integer> differences = orderService.calculateDailyDifferences(LocalDate.now(), List.of(mandator));
		assertNotNull(differences);
		assertTrue(differences.containsKey(testArticle));
		assertEquals(Integer.valueOf(3), differences.get(testArticle));
	}

	@Test
	public void reduceOpenEntries() {
		IOrder freshOrder = coreModelService.create(IOrder.class);
		coreModelService.save(freshOrder);
		IOrderEntry entry = freshOrder.addEntry(article, stock, null, 4);
		entry.setState(OrderEntryState.OPEN);
		coreModelService.save(entry);
		orderService.reduceOpenEntries(List.of(freshOrder), article, 3);
		IOrderEntry updated = freshOrder.getEntries().stream().filter(e -> e.getId().equals(entry.getId())).findFirst()
				.orElse(null);
		assertNotNull(updated);
		assertEquals(1, updated.getAmount());
	}

	@Test
	public void createOrderEntries() {
		order.getEntries().forEach(e -> {
			e.setState(OrderEntryState.DONE);
			coreModelService.save(e);
		});
		Map<IArticle, Integer> toCreate = new LinkedHashMap<>();
		toCreate.put(article, 3);
		int initialCount = order.getEntries().size();
		orderService.addOrCreateOrderEntries(List.of(order), order, toCreate, null);
		assertEquals(initialCount + 1, order.getEntries().size());
		IOrderEntry added = order.getEntries().get(order.getEntries().size() - 1);
		assertEquals(article, added.getArticle());
		assertEquals(3, added.getAmount());
	}

	@Test
	public void syncMediorderArticleReplacement_movesReservationOnPatientStock() {
		IStockService stockService = OsgiServiceUtil.getService(IStockService.class).get();
		IStock patientStock = createPatientStock("PatientStock-99001");
		IArticle requested = new IArticleBuilder(coreModelService, "requested article", "8000001",
				ArticleTyp.ARTIKELSTAMM).buildAndSave();
		IArticle alternative = new IArticleBuilder(coreModelService, "alternative article", "8000002",
				ArticleTyp.ARTIKELSTAMM).buildAndSave();
		createStockEntry(patientStock, requested, 1, 1);
		IOrder patientOrder = coreModelService.create(IOrder.class);
		coreModelService.save(patientOrder);
		IOrderEntry entry = patientOrder.addEntry(requested, patientStock, null, 1);
		coreModelService.save(entry);

		entry.setArticle(alternative);
		coreModelService.save(entry);
		orderService.syncMediorderArticleReplacement(entry, requested);

		assertEquals(alternative, entry.getArticle());
		assertNull(stockService.findStockEntryForArticleInStock(patientStock, requested));
		IStockEntry moved = stockService.findStockEntryForArticleInStock(patientStock, alternative);
		assertNotNull(moved);
		assertEquals(1, moved.getMinimumStock());
		assertEquals(1, moved.getMaximumStock());
		assertEquals(0, moved.getCurrentStock());
		assertEquals(entry, orderService.findOpenOrderEntryForStockEntry(moved));
	}

	@Test
	public void syncMediorderArticleReplacement_keepsThresholdsOnRegularStock() {
		IStockService stockService = OsgiServiceUtil.getService(IStockService.class).get();
		IArticle stocked = new IArticleBuilder(coreModelService, "stocked article", "8000003", ArticleTyp.ARTIKELSTAMM)
				.buildAndSave();
		IArticle alternative = new IArticleBuilder(coreModelService, "stocked alternative", "8000004",
				ArticleTyp.ARTIKELSTAMM).buildAndSave();
		createStockEntry(stock, stocked, 2, 5);
		IOrder regularOrder = coreModelService.create(IOrder.class);
		coreModelService.save(regularOrder);
		IOrderEntry entry = regularOrder.addEntry(stocked, stock, null, 3);
		coreModelService.save(entry);

		entry.setArticle(alternative);
		coreModelService.save(entry);
		orderService.syncMediorderArticleReplacement(entry, stocked);

		IStockEntry unchanged = stockService.findStockEntryForArticleInStock(stock, stocked);
		assertNotNull(unchanged);
		assertEquals(2, unchanged.getMinimumStock());
		assertEquals(5, unchanged.getMaximumStock());
		assertNull(stockService.findStockEntryForArticleInStock(stock, alternative));
	}

	@Test
	public void syncMediorderAmount_followsReducedAmountOnPatientStock() {
		IStockService stockService = OsgiServiceUtil.getService(IStockService.class).get();
		IStock patientStock = createPatientStock("PatientStock-99002");
		IArticle requested = new IArticleBuilder(coreModelService, "partially deliverable article", "8000005",
				ArticleTyp.ARTIKELSTAMM).buildAndSave();
		createStockEntry(patientStock, requested, 2, 2);
		IOrder patientOrder = coreModelService.create(IOrder.class);
		coreModelService.save(patientOrder);
		IOrderEntry entry = patientOrder.addEntry(requested, patientStock, null, 2);
		coreModelService.save(entry);

		entry.setAmount(1);
		coreModelService.save(entry);
		orderService.syncMediorderAmount(entry, 2);

		assertEquals(1, entry.getAmount());
		IStockEntry reserved = stockService.findStockEntryForArticleInStock(patientStock, requested);
		assertNotNull(reserved);
		assertEquals(1, reserved.getMinimumStock());
		assertEquals(2, reserved.getMaximumStock());
	}

	private static IStock createPatientStock(String id) {
		IStock patientStock = coreModelService.create(IStock.class);
		patientStock.setId(id);
		patientStock.setCode(id);
		patientStock.setPriority(0);
		patientStock.setDescription("Test patient stock");
		coreModelService.save(patientStock);
		return patientStock;
	}

	private static IStockEntry createStockEntry(IStock targetStock, IArticle targetArticle, int minimum, int maximum) {
		IStockEntry entry = coreModelService.create(IStockEntry.class);
		entry.setStock(targetStock);
		entry.setArticle(targetArticle);
		entry.setCurrentStock(0);
		entry.setMinimumStock(minimum);
		entry.setMaximumStock(maximum);
		coreModelService.save(entry);
		return entry;
	}

}
