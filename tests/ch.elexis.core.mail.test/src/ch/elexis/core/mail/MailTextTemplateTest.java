package ch.elexis.core.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IImage;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.ITextReplacementService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;

public class MailTextTemplateTest {

	private ITextReplacementService textReplacement;

	@Before
	public void before() {
		IQuery<ITextTemplate> query = CoreModelServiceHolder.get().getQuery(ITextTemplate.class);
		query.execute().forEach(t -> CoreModelServiceHolder.get().remove(t));

		textReplacement = OsgiServiceUtil.getService(ITextReplacementService.class).get();
	}

	@After
	public void after() {
		OsgiServiceUtil.ungetService(textReplacement);
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

	@Test
	public void imageTemplates() {
		ContextServiceHolder.get().setNamed("ch.elexis.core.mail.image.elexismailappointmentqr",
				new Supplier<IImage>() {
					@Override
					public IImage get() {
						IImage ret = CoreModelServiceHolder.get().create(IImage.class);
						ret.setImage(new byte[1]);
						ret.setTitle("test");
						return ret;
					}
				});

		String text = "text\n[Image.MailAppointmentQr]\n\n[Image.MailPraxisLogo]";
		String replacedText = textReplacement.performReplacement(ContextServiceHolder.get().getRootContext(), text);

		MailMessage message = new MailMessage().to("receiver@there.com").subject("subject").text(replacedText);
		assertTrue(message.hasImage());
		assertEquals(2, message.getImageStrings().size());
		assertNotNull(message.getImage("cid:elexismailappointmentqr"));
	}
}
