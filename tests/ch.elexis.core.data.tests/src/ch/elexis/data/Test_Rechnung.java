package ch.elexis.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.rgw.tools.Money;

public class Test_Rechnung extends AbstractPersistentObjectTest {

	@Test
	public void testRemoveOpenReminders() {
		Mandant mandant = new Mandant("TestMandant", "TestMandant", "01.01.70", "w");
		Patient patient = new Patient("TestPatient", "TestPatient", "01.01.70", "w");
		Konsultation kons = patient.createFallUndKons();
		Fall fall = kons.getFall();
		// enable removing open reminders
		ConfigServiceHolder.setGlobal(Preferences.RNN_REMOVE_OPEN_REMINDER, true);

		// Rechnung MAHNUNG_1 full amount
		Rechnung rechnung = new Rechnung("1", mandant, fall, kons.getDatum(), kons.getDatum(), new Money(10000),
				RnStatus.OFFEN);
		rechnung.setStatus(RnStatus.MAHNUNG_1);
		rechnung.addZahlung(new Money(10).multiply(-1.0), Messages.Rechnung_Mahngebuehr1, null);
		assertTrue(rechnung.getStatus() == RnStatus.MAHNUNG_1);
		assertTrue(rechnung.hasReminders());

		rechnung.addZahlung(new Money(10000), "initial amount", null);
		assertTrue(rechnung.getStatus() == RnStatus.BEZAHLT);
		assertFalse(rechnung.hasReminders());

		// Rechnung MAHNUNG_2 partial amount
		rechnung = new Rechnung("2", mandant, fall, kons.getDatum(), kons.getDatum(), new Money(10000), RnStatus.OFFEN);
		rechnung.setStatus(RnStatus.MAHNUNG_1);
		rechnung.addZahlung(new Money(10).multiply(-1.0), Messages.Rechnung_Mahngebuehr1, null);
		assertTrue(rechnung.getStatus() == RnStatus.MAHNUNG_1);
		rechnung.setStatus(RnStatus.MAHNUNG_2);
		rechnung.addZahlung(new Money(10).multiply(-1.0), Messages.Rechnung_Mahngebuehr2, null);
		assertTrue(rechnung.getStatus() == RnStatus.MAHNUNG_2);
		assertTrue(rechnung.hasReminders());

		rechnung.addZahlung(new Money(5000), "partial amount", null);
		assertTrue(rechnung.getStatus() == RnStatus.TEILZAHLUNG);
		rechnung.addZahlung(new Money(5000), "partial amount", null);
		assertTrue(rechnung.getStatus() == RnStatus.BEZAHLT);
		assertFalse(rechnung.hasReminders());
	}

	@Test
	public void testDontRemoveOpenReminders() {
		Mandant mandant = new Mandant("TestMandant", "TestMandant", "01.01.70", "w");
		Patient patient = new Patient("TestPatient", "TestPatient", "01.01.70", "w");
		Konsultation kons = patient.createFallUndKons();
		Fall fall = kons.getFall();
		// disable removing open reminders
		ConfigServiceHolder.setGlobal(Preferences.RNN_REMOVE_OPEN_REMINDER, false);

		// Rechnung MAHNUNG_1 full amount
		Rechnung rechnung = new Rechnung("1", mandant, fall, kons.getDatum(), kons.getDatum(), new Money(10000),
				RnStatus.OFFEN);
		rechnung.setStatus(RnStatus.MAHNUNG_1);
		rechnung.addZahlung(new Money(10).multiply(-1.0), Messages.Rechnung_Mahngebuehr1, null);
		assertTrue(rechnung.getStatus() == RnStatus.MAHNUNG_1);
		assertTrue(rechnung.hasReminders());

		rechnung.addZahlung(new Money(10000), "initial amount", null);
		assertFalse(rechnung.getStatus() == RnStatus.BEZAHLT);
		assertTrue(rechnung.hasReminders());
	}
}
