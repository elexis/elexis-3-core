package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.test.StoreToStringServiceHolder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IStoreToStringContribution;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.utils.OsgiServiceUtil;

public class OrderEntryTest {
	private IModelService modelService;

	private IArticle article, article1;

	private IStock stock;

	private IOrder order;

	private LocalDateTime orderTimestamp;

	@Before
	public void before() {
		modelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();

		article = modelService.create(IArticle.class);
		article.setName("test article");
		article.setCode("123456789");
		article.setTyp(ArticleTyp.EIGENARTIKEL);
		article.setGtin("0000001111111");
		article.setPackageSize(12);
		article.setSellingSize(12);
		modelService.save(article);

		article1 = modelService.create(IArticle.class);
		article1.setName("test article 1");
		article1.setCode("987654321");
		article1.setTyp(ArticleTyp.EIGENARTIKEL);
		article1.setGtin("1111112222222");
		article1.setPackageSize(24);
		article1.setSellingSize(24);
		modelService.save(article1);

		stock = modelService.create(IStock.class);
		stock.setCode("TST");
		stock.setPriority(5);
		modelService.save(stock);

		orderTimestamp = LocalDateTime.now();

		order = modelService.create(IOrder.class);
		order.setTimestamp(orderTimestamp);
		order.setName("TEST");
		modelService.save(order);
	}

	@After
	public void after() {
		modelService.remove(order);
		modelService.remove(article);
		modelService.remove(article1);
		modelService.remove(stock);

		OsgiServiceUtil.ungetService(modelService);
		modelService = null;
	}

	@Test
	public void create() {
		IOrderEntry entry = modelService.create(IOrderEntry.class);
		assertNotNull(entry);
		assertTrue(entry instanceof IOrderEntry);

		entry.setOrder(order);
		entry.setStock(stock);
		entry.setArticle(article);
		entry.setAmount(5);
		modelService.save(entry);
		assertFalse(order.getEntries().isEmpty());

		Optional<IOrderEntry> loaded = modelService.load(entry.getId(), IOrderEntry.class);
		assertTrue(loaded.isPresent());
		assertFalse(entry == loaded.get());
		assertEquals(entry, loaded.get());
		assertEquals(entry.getArticle(), loaded.get().getArticle());
		assertEquals(entry.getStock(), loaded.get().getStock());
		assertEquals(entry.getOrder(), loaded.get().getOrder());
		assertEquals(entry.getOrder().getTimestamp(), loaded.get().getOrder().getTimestamp());

		IOrderEntry entry2 = modelService.create(IOrderEntry.class);
		entry2.setStock(stock);
		entry2.setArticle(article1);
		entry2.setAmount(1);
		entry2.setOrder(order);
		modelService.save(entry2);
		assertEquals(2, order.getEntries().size());

		IOrderEntry entry3 = order.addEntry(article1, stock, null, 3);
		modelService.save(entry3);
		assertEquals(4, entry3.getAmount());
		assertEquals(entry2, entry3);

		// delete not allowed as FK prevents order removal
		modelService.remove(entry);
		modelService.remove(entry2);
		modelService.refresh(order, true);
		assertEquals(0, order.getEntries().size());
	}

	@Test
	public void query() {
		IOrderEntry entry = modelService.create(IOrderEntry.class);
		entry.setOrder(order);
		entry.setStock(stock);
		entry.setArticle(article);
		entry.setAmount(5);
		modelService.save(entry);

		IOrderEntry entry1 = modelService.create(IOrderEntry.class);
		entry1.setOrder(order);
		entry1.setStock(stock);
		entry1.setArticle(article1);
		entry1.setAmount(2);
		modelService.save(entry1);

		String storeToString = StoreToStringServiceHolder.getStoreToString(article);
		String[] articleParts = storeToString.split(IStoreToStringContribution.DOUBLECOLON);

		IQuery<IOrderEntry> query = modelService.getQuery(IOrderEntry.class, true, false);
		query.and("articleId", COMPARATOR.EQUALS, articleParts[1]);
		query.and("articleType", COMPARATOR.EQUALS, articleParts[0]);
		List<IOrderEntry> existing = query.execute();
		assertEquals(1, existing.size());
		assertEquals(2, order.getEntries().size());
		assertEquals(2, existing.get(0).getOrder().getEntries().size());

		// delete not allowed as FK prevents order removal
		modelService.remove(entry);
		modelService.remove(entry1);
		modelService.refresh(order, true);
		assertEquals(0, order.getEntries().size());
	}
}
