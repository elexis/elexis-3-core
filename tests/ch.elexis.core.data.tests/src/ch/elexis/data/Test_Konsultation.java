package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.rgw.tools.JdbcLink;

public class Test_Konsultation extends AbstractPersistentObjectTest {

	public Test_Konsultation(JdbcLink link) {
		super(link);
	}

	private static Patient pat;
	private static Fall fall;
	private static Konsultation kons;

	@Before
	public void before() {
		User user = User.load(testUserName);
		// set user and Mandant in system
		ContextServiceHolder.get()
				.setActiveUser(CoreModelServiceHolder.get().load(user.getId(), IUser.class).orElse(null));
		Mandant m = new Mandant("Mandant", "Erwin", "26.07.1979", "m");
		CoreHub.setMandant(m);

		pat = new Patient("Name", "Vorname", "26.8.2011", "m");
		fall = new Fall(pat.getId(), "Bezeichnung", "Grund", "KVG");
		assertEquals(BillingLaw.KVG, fall.getConfiguredBillingSystemLaw());
		kons = new Konsultation(fall);

		FreeTextDiagnose.checkInitTable();
	}

	@After
	public void after() {
		kons.delete();
		fall.delete();
		pat.delete();
	}

	@Test
	public void testConsultationCreation() {
		// #5612 test default diagnosis
		FreeTextDiagnose ftd = new FreeTextDiagnose("TextDefault", true);
		ConfigServiceHolder.setUser(Preferences.USR_DEFDIAGNOSE, ftd.storeToString());
		Konsultation kons = new Konsultation(fall);
		assertEquals(1, kons.getDiagnosen().size());
		assertEquals(ftd.getId(), kons.getDiagnosen().get(0).getId());
		ConfigServiceHolder.setUser(Preferences.USR_DEFDIAGNOSE, "");
	}

	@Test
	public void testConsultationOrderingDateTime() throws InterruptedException {
		Konsultation kons = new Konsultation(fall);
		Thread.sleep(1050);
		Konsultation kons2 = new Konsultation(fall);
		assertTrue(kons2.getDateTime().isAfter(kons.getDateTime()));
		assertEquals(1, kons2.compareTo(kons));
	}

	@Test
	public void testDiagnosisCreation() {
		long currentTimeMillis = System.currentTimeMillis();
		FreeTextDiagnose ftd = new FreeTextDiagnose("Text", true);
		kons.addDiagnose(ftd);
		kons.addDiagnose(ftd);
		assertEquals(1, kons.getDiagnosen().size());
		String count = ftd.getDBConnection().queryString("SELECT COUNT(*) FROM DIAGNOSEN WHERE KLASSE="
				+ JdbcLink.wrap(ftd.getClass().getName()) + " AND DG_CODE=" + JdbcLink.wrap(ftd.getCode()));
		assertEquals(Integer.toString(1), count);
		String lastUpdateSet = ftd.getDBConnection().queryString("SELECT LASTUPDATE FROM DIAGNOSEN WHERE KLASSE="
				+ JdbcLink.wrap(ftd.getClass().getName()) + " AND DG_CODE=" + JdbcLink.wrap(ftd.getCode()));
		assertTrue(currentTimeMillis <= Long.valueOf(lastUpdateSet));

		kons.removeDiagnose(ftd);
		assertEquals(0, kons.getDiagnosen().size());
	}

}
