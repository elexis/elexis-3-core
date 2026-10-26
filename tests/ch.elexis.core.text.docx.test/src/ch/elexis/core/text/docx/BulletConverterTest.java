package ch.elexis.core.text.docx;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.elexis.core.text.BulletConverter;

public class BulletConverterTest {

	@Test
	public void convertDashLinesToList() {
		String html = BulletConverter.toHtmlLists("Intro\n- one\n- two");

		assertTrue(html.contains("<p>Intro</p>"));
		assertTrue(html.contains("<ul><li>one</li><li>two</li></ul>"));
		assertFalse(html.contains("<li>-"));
	}

	@Test
	public void escapePlainText() {
		String html = BulletConverter.toHtmlLists("one < two & three > two");

		assertTrue(html.contains("one &lt; two &amp; three &gt; two"));
	}

	@Test
	public void plainTextWithoutDashLines() {
		String html = BulletConverter.toHtmlLists("Intro\nPlain text");

		assertTrue(html.contains("<p>Intro</p>"));
		assertTrue(html.contains("<p>Plain text</p>"));
		assertFalse(html.contains("<ul>"));
	}
}
