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

import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.utils.OsgiServiceUtil;

public class StockTest {
	private IModelService modelService;

	private IArticle article, article1;

	private IStock stock;

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

	}

	@After
	public void after() {
		modelService.remove(article);
		modelService.remove(article1);

		modelService.remove(stock);

		OsgiServiceUtil.ungetService(modelService);
		modelService = null;
	}

	@Test
	public void create() {
		stock = modelService.create(IStock.class);
		assertNotNull(stock);
		assertTrue(stock instanceof IStock);

		stock.setCode("code");
		stock.setDescription("description");
		modelService.save(stock);

		modelService.refresh(stock);
		assertNotNull(stock.getId());

		Optional<IStock> loaded = modelService.load(stock.getId(), IStock.class);
		assertTrue(loaded.isPresent());
		assertFalse(stock == loaded.get());
		assertEquals(stock, loaded.get());
		assertEquals("code", stock.getCode());
		assertEquals("description", stock.getDescription());

	}

	@Test
	public void getStockEntries() {
		stock = modelService.create(IStock.class);
		stock.setCode("TST2");
		stock.setPriority(5);
		modelService.save(stock);


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
		List<IStockEntry> execute = query.execute();
		assertEquals(2, execute.size());

		List<IStockEntry> stockEntries = stock.getStockEntries();
		assertEquals(2, stockEntries.size());

		modelService.remove(entry);
		modelService.remove(entry1);
	}
}
