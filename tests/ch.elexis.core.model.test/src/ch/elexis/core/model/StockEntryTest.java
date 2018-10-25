package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.test.CoreModelServiceHolder;
import ch.elexis.core.model.test.StoreToStringServiceHolder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IStoreToStringContribution;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.utils.OsgiServiceUtil;

public class StockEntryTest {
	private IModelService modelService;
	
	private IArticle article, article1;
	
	private IStock stock;
	
	@Before
	public void before(){
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
		
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
	}
	
	@After
	public void after(){
		modelService.remove(article);
		modelService.remove(article1);
		modelService.remove(stock);
		
		OsgiServiceUtil.ungetService(modelService);
		modelService = null;
	}
	
	@Test
	public void create(){
		IStockEntry entry = modelService.create(IStockEntry.class);
		assertNotNull(entry);
		assertTrue(entry instanceof IStockEntry);
		
		entry.setArticle(article);
		entry.setStock(stock);
		entry.setMinimumStock(2);
		entry.setCurrentStock(1);
		entry.setMaximumStock(5);
		modelService.save(entry);
		
		Optional<IStockEntry> loaded = modelService.load(entry.getId(), IStockEntry.class);
		assertTrue(loaded.isPresent());
		assertFalse(entry == loaded.get());
		assertEquals(entry, loaded.get());
		assertEquals(entry.getArticle(), loaded.get().getArticle());
		assertEquals(entry.getStock(), loaded.get().getStock());
		assertEquals(entry.getCurrentStock(), loaded.get().getCurrentStock());
		
		modelService.remove(entry);
	}
	
	@Test
	public void query(){
		IStockEntry entry = modelService.create(IStockEntry.class);
		entry.setArticle(article);
		entry.setStock(stock);
		entry.setMinimumStock(2);
		entry.setCurrentStock(1);
		entry.setMaximumStock(5);
		modelService.save(entry);
		
		IStockEntry entry1 = modelService.create(IStockEntry.class);
		entry1.setArticle(article1);
		entry1.setStock(stock);
		entry1.setMinimumStock(3);
		entry1.setCurrentStock(2);
		entry1.setMaximumStock(4);
		modelService.save(entry1);
		
		IQuery<IStockEntry> query = modelService.getQuery(IStockEntry.class);
		query.and(ModelPackage.Literals.ISTOCK_ENTRY__STOCK, COMPARATOR.EQUALS, stock);
		List<IStockEntry> existing = query.execute();
		assertEquals(2, existing.size());

		INamedQuery<Long> currentStock = CoreModelServiceHolder.get().getNamedQueryByName(
			Long.class, IStockEntry.class, "StockEntry_SumCurrentStock.articleId.articleType");
		String storeToString = StoreToStringServiceHolder.getStoreToString(article);
		String[] parts = storeToString.split(IStoreToStringContribution.DOUBLECOLON);
		List<Long> results =
			currentStock.executeWithParameters(
				currentStock.getParameterMap(
				"articleId", parts[1], "articleType", parts[0]));
		assertEquals((Long) results.get(0), (Long) 1L);
		
		modelService.remove(entry);
		modelService.remove(entry1);
	}
}
