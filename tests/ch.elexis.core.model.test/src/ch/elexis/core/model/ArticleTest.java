package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;
import ch.elexis.core.types.ArticleTyp;

public class ArticleTest extends AbstractTest {
	
	@Before
	public void before(){
		super.before();
		createLocalArticle();
	}
	
	@After
	public void after(){
		super.after();
	}
	
	@Test
	public void create(){
		assertNotNull(localArticle);
		assertTrue(localArticle instanceof IArticle);
		
		Optional<IArticle> loaded = coreModelService.load(localArticle.getId(), IArticle.class);
		assertTrue(loaded.isPresent());
		assertEquals("123456789", loaded.get().getCode());
		assertEquals(ArticleTyp.EIGENARTIKEL, loaded.get().getTyp());
		assertEquals("0000001111111", loaded.get().getGtin());
		assertEquals(12, loaded.get().getPackageSize());
		assertEquals(12, loaded.get().getSellingSize());
	}
	
	@Test
	public void product(){
		IArticle product = coreModelService.create(IArticle.class);
		product.setName("test product");
		product.setTyp(ArticleTyp.ARTIKEL);
		
		IArticle article = coreModelService.create(IArticle.class);
		article.setName("test article 1");
		article.setCode("123456789");
		article.setTyp(ArticleTyp.ARTIKEL);
		article.setGtin("0000001111111");
		article.setPackageSize(12);
		article.setSellingSize(12);
		article.setProduct(product);
		
		IArticle article1 = coreModelService.create(IArticle.class);
		article1.setName("test article 2");
		article1.setCode("987654321");
		article1.setTyp(ArticleTyp.ARTIKEL);
		article1.setGtin("1111112222222");
		article1.setPackageSize(24);
		article1.setSellingSize(24);
		article1.setProduct(product);
		
		coreModelService.save(Arrays.asList(product, article, article1));
		
		Optional<IArticle> loaded = coreModelService.load(product.getId(), IArticle.class);
		assertFalse(loaded.get().getPackages().isEmpty());
		assertTrue(loaded.get().getPackages().contains(article));
		assertTrue(loaded.get().getPackages().contains(article1));
		assertEquals(loaded.get(), article.getProduct());
		assertEquals(loaded.get(), article1.getProduct());
		
		// must clear product references before removing product
		product.getPackages().forEach(p -> p.setProduct(null));
		coreModelService.save(Arrays.asList(article, article1));
		coreModelService.remove(product);
		loaded = coreModelService.load(article.getId(), IArticle.class);
		assertTrue(loaded.isPresent());
		assertTrue(loaded.get().getProduct() == null);
		
		coreModelService.remove(article);
		coreModelService.remove(article1);
	}
	
	@Test
	public void query(){
		
		IArticle article1 = coreModelService.create(IArticle.class);
		article1.setName("test article 1");
		article1.setCode("987654321");
		article1.setTyp(ArticleTyp.EIGENARTIKEL);
		article1.setGtin("1111112222222");
		article1.setPackageSize(24);
		article1.setSellingSize(24);
		coreModelService.save(article1);
		
		IQuery<IArticle> query = coreModelService.getQuery(IArticle.class);
		query.and(ModelPackage.Literals.IARTICLE__GTIN, COMPARATOR.EQUALS, "0000001111111");
		List<IArticle> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(localArticle, existing.get(0));
		
		query = coreModelService.getQuery(IArticle.class);
		query.and(ModelPackage.Literals.IARTICLE__TYP, COMPARATOR.EQUALS,
			ArticleTyp.EIGENARTIKEL);
		existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(2, existing.size());
		
		coreModelService.remove(localArticle);
		coreModelService.remove(article1);
	}
}
