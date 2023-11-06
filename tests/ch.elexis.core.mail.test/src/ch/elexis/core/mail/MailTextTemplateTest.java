package ch.elexis.core.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class MailTextTemplateTest {

	@Before
	public void before() {
		IQuery<ITextTemplate> query = CoreModelServiceHolder.get().getQuery(ITextTemplate.class);
		query.execute().forEach(t -> CoreModelServiceHolder.get().remove(t));
	}

	@Test
	public void saveAndLoadAll() {
		List<ITextTemplate> loadedList = MailTextTemplate.load();
		assertTrue(loadedList.isEmpty());
		Optional<ITextTemplate> loadedTemplate = MailTextTemplate.load("testTemplate");
		assertFalse(loadedTemplate.isPresent());

		new MailTextTemplate.Builder().name("testTemplate").text("Test Template\n[Test.Template]").buildAndSave();
		new MailTextTemplate.Builder().name("testTemplate1").text("Test Template\n[Test.Template]").buildAndSave();

		loadedList = MailTextTemplate.load();
		assertEquals(2, loadedList.size());
		loadedTemplate = MailTextTemplate.load("testTemplate");
		assertTrue(loadedTemplate.isPresent());
	}
}
