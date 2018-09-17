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
	private IModelService modelSerice;
	
	private IArticle article, article1;
	
	private IStock stock;
	
	private IOrder order;
	
	private LocalDateTime orderTimestamp;
	
	@Before
	public void before(){
		modelSerice = OsgiServiceUtil.getService(IModelService.class).get();
		
		article = modelSerice.create(IArticle.class);
		article.setName("test article");
		article.setCode("123456789");
		article.setTyp(ArticleTyp.EIGENARTIKEL);
		article.setGtin("0000001111111");
		article.setPackageSize(12);
		article.setSellingSize(12);
		modelSerice.save(article);
		
		article1 = modelSerice.create(IArticle.class);
		article1.setName("test article 1");
		article1.setCode("987654321");
		article1.setTyp(ArticleTyp.EIGENARTIKEL);
		article1.setGtin("1111112222222");
		article1.setPackageSize(24);
		article1.setSellingSize(24);
		modelSerice.save(article1);
		
		stock = modelSerice.create(IStock.class);
		stock.setCode("TST");
		stock.setPriority(5);
		modelSerice.save(stock);
		
		orderTimestamp = LocalDateTime.now();
		
		order = modelSerice.create(IOrder.class);
		order.setTimestamp(orderTimestamp);
		order.setName("TEST");
		modelSerice.save(order);
	}
	
	@After
	public void after(){
		modelSerice.remove(order);
		modelSerice.remove(article);
		modelSerice.remove(article1);
		modelSerice.remove(stock);
		
		OsgiServiceUtil.ungetService(modelSerice);
		modelSerice = null;
	}
	
	@Test
	public void create(){
		IOrderEntry entry = modelSerice.create(IOrderEntry.class);
		assertNotNull(entry);
		assertTrue(entry instanceof IOrderEntry);
		
		entry.setOrder(order);
		entry.setStock(stock);
		entry.setArticle(article);
		entry.setAmount(5);
		modelSerice.save(entry);
		order.addEntry(entry);
		assertFalse(order.getEntries().isEmpty());
		
		Optional<IOrderEntry> loaded = modelSerice.load(entry.getId(), IOrderEntry.class);
		assertTrue(loaded.isPresent());
		assertFalse(entry == loaded.get());
		assertEquals(entry, loaded.get());
		assertEquals(entry.getArticle(), loaded.get().getArticle());
		assertEquals(entry.getStock(), loaded.get().getStock());
		assertEquals(entry.getOrder(), loaded.get().getOrder());
		assertEquals(entry.getOrder().getTimestamp(), loaded.get().getOrder().getTimestamp());
		
		order.removeEntry(entry);
	}
	
	@Test
	public void query(){
		IOrderEntry entry = modelSerice.create(IOrderEntry.class);
		entry.setOrder(order);
		entry.setStock(stock);
		entry.setArticle(article);
		entry.setAmount(5);
		modelSerice.save(entry);
		order.addEntry(entry);
		
		IOrderEntry entry1 = modelSerice.create(IOrderEntry.class);
		entry1.setOrder(order);
		entry1.setStock(stock);
		entry1.setArticle(article1);
		entry1.setAmount(2);
		modelSerice.save(entry1);
		order.addEntry(entry1);
		
		String storeToString = StoreToStringServiceHolder.getStoreToString(article);
		String[] articleParts = storeToString.split(IStoreToStringContribution.DOUBLECOLON);
		
		IQuery<IOrderEntry> query = modelSerice.getQuery(IOrderEntry.class, true, false);
		query.and("articleId", COMPARATOR.EQUALS, articleParts[1]);
		query.and("articleType", COMPARATOR.EQUALS, articleParts[0]);
		List<IOrderEntry> existing = query.execute();
		assertEquals(1, existing.size());
		assertEquals(2, order.getEntries().size());
		assertEquals(2, existing.get(0).getOrder().getEntries().size());
		
		order.removeEntry(entry);
		order.removeEntry(entry1);
	}
}
