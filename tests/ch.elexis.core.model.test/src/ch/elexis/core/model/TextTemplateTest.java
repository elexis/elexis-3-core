package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;
import ch.elexis.core.types.TextTemplateCategory;

public class TextTemplateTest extends AbstractTest {
	
	@Before
	public void before(){
		super.before();
		createMandator();
	}
	
	@After
	public void after(){
		super.after();
	}
	
	@Test
	public void create(){
		ITextTemplate template = coreModelService.create(ITextTemplate.class);
		template.setCategory(TextTemplateCategory.MAIL);
		template.setMandator(mandator);
		template.setName("TestTemplate");
		template.setTemplate("Test Template\n[test.text]");
		
		coreModelService.save(template);
		
		Optional<ITextTemplate> loaded =
			coreModelService.load(template.getId(), ITextTemplate.class);
		assertTrue(loaded.isPresent());
		assertFalse(template == loaded.get());
		assertEquals(template, loaded.get());
		assertEquals(template.getMandator(), loaded.get().getMandator());
		assertEquals(template.getCategory(), loaded.get().getCategory());
		assertEquals(template.getName(), loaded.get().getName());
		assertEquals(template.getTemplate(), loaded.get().getTemplate());
		
		coreModelService.remove(template);
	}
	
	@Test
	public void query(){
		ITextTemplate template = coreModelService.create(ITextTemplate.class);
		template.setCategory(TextTemplateCategory.MAIL);
		template.setMandator(mandator);
		template.setName("TestTemplate");
		template.setTemplate("Test Template\n[test.text]");
		coreModelService.save(template);
		
		template = coreModelService.create(ITextTemplate.class);
		template.setCategory(TextTemplateCategory.MAIL);
		template.setMandator(mandator);
		template.setName("TestTemplate1");
		template.setTemplate("Test Template1\n[test.text]");
		coreModelService.save(template);
		
		template = coreModelService.create(ITextTemplate.class);
		template.setCategory(TextTemplateCategory.MAIL);
		template.setName("TestTemplate2");
		template.setTemplate("Test Template2\n[test.text]");
		coreModelService.save(template);
		
		IQuery<ITextTemplate> query = coreModelService.getQuery(ITextTemplate.class);
		assertEquals(3, query.execute().size());
		query.and(ModelPackage.Literals.ITEXT_TEMPLATE__MANDATOR, COMPARATOR.EQUALS, mandator);
		assertEquals(2, query.execute().size());
		
		query = coreModelService.getQuery(ITextTemplate.class);
		query.and(ModelPackage.Literals.ITEXT_TEMPLATE__MANDATOR, COMPARATOR.EQUALS, null);
		assertEquals(1, query.execute().size());
		
		query = coreModelService.getQuery(ITextTemplate.class);
		for (ITextTemplate found : query.execute()) {
			coreModelService.remove(found);
		}
	}
}
