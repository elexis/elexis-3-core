package ch.elexis.data;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLinkException;

public class Test_Konsultation extends AbstractPersistentObjectTest {
	
	private static JdbcLink link;
	
	private static Patient pat;
	private static Fall fall;
	private static Konsultation kons;
	
	@BeforeClass
	public static void init(){
		link = initDB();
		
		User user = User.load("Administrator");
		// set user and Mandant in system
		ElexisEventDispatcher.getInstance()
			.fire(new ElexisEvent(user, User.class, ElexisEvent.EVENT_SELECTED));
		Mandant m = new Mandant("Mandant", "Erwin", "26.07.1979", "m");
		CoreHub.setMandant(m);
		
		pat = new Patient("Name", "Vorname", "26.8.2011", "m");
		fall = new Fall(pat.getId(), "Bezeichnung", "Grund", "KVG");
		kons = new Konsultation(fall);
	}
	
	@AfterClass
	public static void tearDown(){
		try {
			if (link == null || !link.isAlive())
				return;
			link.exec("DROP ALL OBJECTS");
			link.disconnect();
		} catch (JdbcLinkException je) {
			// just tell what happend and resume
			// excpetion is allowed for tests which get rid of the connection on their own
			// for example testConnect(), ...
			je.printStackTrace();
		}
	}
	
	@Test
	public void testDiagnosisCreation(){
		FreeTextDiagnose ftd = new FreeTextDiagnose("Text", true);
		kons.addDiagnose(ftd);
		kons.addDiagnose(ftd);
		assertEquals(1, kons.getDiagnosen().size());
		String count = ftd.getDBConnection()
			.queryString("SELECT COUNT(*) FROM DIAGNOSEN WHERE KLASSE="
				+ JdbcLink.wrap(ftd.getClass().getName()) + " AND DG_CODE="
				+ JdbcLink.wrap(ftd.getCode()));
		assertEquals("1", count);
		
		kons.removeDiagnose(ftd);
		assertEquals(0, kons.getDiagnosen().size());
	}
	
}
