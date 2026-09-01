package ch.elexis.core.ui.tests.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import ch.elexis.core.ui.views.DeprecatedViewInfo;
import ch.elexis.core.ui.views.DeprecatedViews;
import ch.elexis.core.ui.views.DeprecatedViews.Entry;
import ch.elexis.core.ui.views.DeprecatedViews.Stage;

/**
 * Tests for the registry of deprecated views, see ticket 28206.
 */
public class Test_DeprecatedViews {

	@Test
	public void testKnownViewIsFound() {
		Entry entry = DeprecatedViews.get("ch.elexis.views.SearchView");
		assertNotNull(entry);
		assertEquals(Stage.NOTICE, entry.getStage());
		assertEquals("Spotlight", entry.getSuccessor());
	}

	@Test
	public void testUnknownViewIsNotDeprecated() {
		assertNull(DeprecatedViews.get("ch.elexis.core.ui.views.PatientenListeView"));
		assertNull(DeprecatedViews.get(""));
		assertNull(DeprecatedViews.get(null));
	}

	@Test
	public void testSecondaryIdResolvesToSameEntry() {
		Entry plain = DeprecatedViews.get("ch.elexis.BBSView");
		Entry withSecondary = DeprecatedViews.get("ch.elexis.BBSView:secondary");
		assertNotNull(withSecondary);
		assertEquals(plain.getSuccessor(), withSecondary.getSuccessor());
	}

	@Test
	public void testNormalizeId() {
		assertEquals("ch.elexis.BBSView", DeprecatedViews.normalizeId("ch.elexis.BBSView"));
		assertEquals("ch.elexis.BBSView", DeprecatedViews.normalizeId("ch.elexis.BBSView:2"));
		assertNull(DeprecatedViews.normalizeId(null));
	}

	@Test
	public void testViewWithoutSuccessor() {
		Entry entry = DeprecatedViews.get("ch.elexis.TarmedTimer");
		assertNotNull(entry);
		assertNull(entry.getSuccessor());
	}

	@Test
	public void testHiddenViewIdsOnlyContainHiddenStage() {
		List<String> hidden = DeprecatedViews.getHiddenViewIds();
		assertFalse(hidden.isEmpty());
		for (String viewId : hidden) {
			assertEquals(viewId, Stage.HIDDEN, DeprecatedViews.get(viewId).getStage());
		}
	}

	@Test
	public void testEclipseViewsHaveNoSuccessor() {
		assertNull(DeprecatedViews.get("org.eclipse.ui.views.ProgressView").getSuccessor());
	}

	@Test
	public void testEclipseViewsAreHidden() {
		List<String> hidden = DeprecatedViews.getHiddenViewIds();
		assertTrue(hidden.contains("org.eclipse.ui.internal.introview"));
		assertTrue(hidden.contains("org.eclipse.ui.browser.view"));
		assertTrue(hidden.contains("org.eclipse.ui.views.PropertySheet"));
		assertTrue(hidden.contains("org.eclipse.ui.views.ContentOutline"));
		assertTrue(hidden.contains("org.eclipse.ui.views.ProgressView"));
	}

	@Test
	public void testNoticeViewIsNotHidden() {
		assertFalse(DeprecatedViews.getHiddenViewIds().contains("ch.elexis.views.SearchView"));
	}

	@Test
	public void testInfoIsShownOnlyOnce() {
		DeprecatedViewInfo info = new DeprecatedViewInfo("Spotlight");
		assertFalse(info.isShown());
		info.markShown();
		assertTrue(info.isShown());
	}

	@Test
	public void testInfoAcceptsMissingSuccessor() {
		assertFalse(new DeprecatedViewInfo(null).isShown());
		assertFalse(new DeprecatedViewInfo("").isShown());
	}

	@Test
	public void testMessageContainsViewAndSuccessor() {
		String message = new DeprecatedViewInfo("Spotlight").getMessage("Suchen");
		assertTrue(message, message.contains("Suchen"));
		assertTrue(message, message.contains("Spotlight"));
		// a single apostrophe in a translation would make MessageFormat drop the
		// placeholders instead of filling them
		assertFalse(message, message.contains("{0}"));
		assertFalse(message, message.contains("{1}"));
	}

	@Test
	public void testMessageWithoutSuccessorMentionsNoNull() {
		String message = new DeprecatedViewInfo(null).getMessage("Stoppuhr");
		assertTrue(message, message.contains("Stoppuhr"));
		assertFalse(message, message.contains("null"));
		assertFalse(message, message.contains("{0}"));
	}
}
