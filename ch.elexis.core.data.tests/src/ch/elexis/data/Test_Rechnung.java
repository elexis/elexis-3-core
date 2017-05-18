package ch.elexis.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.Money;

public class Test_Rechnung extends AbstractPersistentObjectTest {
	
	public Test_Rechnung(JdbcLink link){
		super(link);
	}
	
	@Test
	public void testRemoveOpenReminders(){
		Mandant mandant = new Mandant("TestMandant", "TestMandant", "01.01.70", "w");
		Patient patient = new Patient("TestPatient", "TestPatient", "01.01.70", "w");
		Konsultation kons = patient.createFallUndKons();
		Fall fall = kons.getFall();
		// enable removing open reminders
		CoreHub.globalCfg.set(Preferences.RNN_REMOVE_OPEN_REMINDER, true);
		
		// Rechnung MAHNUNG_1 full amount
		Rechnung rechnung = new Rechnung("1", mandant, fall, kons.getDatum(), kons.getDatum(),
			new Money(10000), RnStatus.OFFEN);
		rechnung.setStatus(RnStatus.MAHNUNG_1);
		rechnung.addZahlung(new Money(10).multiply(-1.0),
				Messages.Rechnung_Mahngebuehr1, null);
		assertTrue(rechnung.getStatus() == RnStatus.MAHNUNG_1);
		assertTrue(rechnung.hasOpenReminders());
		
		rechnung.addZahlung(new Money(10000), "initial amount", null);
		assertTrue(rechnung.getStatus() == RnStatus.BEZAHLT);
		assertFalse(rechnung.hasOpenReminders());
		
		// Rechnung MAHNUNG_2 partial amount
		rechnung = new Rechnung("2", mandant, fall, kons.getDatum(), kons.getDatum(),
			new Money(10000), RnStatus.OFFEN);
		rechnung.setStatus(RnStatus.MAHNUNG_1);
		rechnung.addZahlung(new Money(10).multiply(-1.0), Messages.Rechnung_Mahngebuehr1, null);
		assertTrue(rechnung.getStatus() == RnStatus.MAHNUNG_1);
		rechnung.setStatus(RnStatus.MAHNUNG_2);
		rechnung.addZahlung(new Money(10).multiply(-1.0), Messages.Rechnung_Mahngebuehr2, null);
		assertTrue(rechnung.getStatus() == RnStatus.MAHNUNG_2);
		assertTrue(rechnung.hasOpenReminders());
		
		rechnung.addZahlung(new Money(5000), "partial amount", null);
		assertTrue(rechnung.getStatus() == RnStatus.TEILZAHLUNG);
		rechnung.addZahlung(new Money(5000), "partial amount", null);
		assertTrue(rechnung.getStatus() == RnStatus.BEZAHLT);
		assertFalse(rechnung.hasOpenReminders());
	}
	
	@Test
	public void testDontRemoveOpenReminders(){
		Mandant mandant = new Mandant("TestMandant", "TestMandant", "01.01.70", "w");
		Patient patient = new Patient("TestPatient", "TestPatient", "01.01.70", "w");
		Konsultation kons = patient.createFallUndKons();
		Fall fall = kons.getFall();
		// disable removing open reminders
		CoreHub.globalCfg.set(Preferences.RNN_REMOVE_OPEN_REMINDER, false);
		
		// Rechnung MAHNUNG_1 full amount
		Rechnung rechnung = new Rechnung("1", mandant, fall, kons.getDatum(), kons.getDatum(),
			new Money(10000), RnStatus.OFFEN);
		rechnung.setStatus(RnStatus.MAHNUNG_1);
		rechnung.addZahlung(new Money(10).multiply(-1.0), Messages.Rechnung_Mahngebuehr1, null);
		assertTrue(rechnung.getStatus() == RnStatus.MAHNUNG_1);
		assertTrue(rechnung.hasOpenReminders());
		
		rechnung.addZahlung(new Money(10000), "initial amount", null);
		assertFalse(rechnung.getStatus() == RnStatus.BEZAHLT);
		assertTrue(rechnung.hasOpenReminders());
	}
}
