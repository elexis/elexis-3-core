package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

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
				InvoiceState.OPEN.getState());
		rechnung.setStatus(InvoiceState.DEMAND_NOTE_1);
		rechnung.addZahlung(new Money(10).multiply(-1.0), Messages.Rechnung_Mahngebuehr1, null);
		assertTrue(rechnung.getInvoiceState() == InvoiceState.DEMAND_NOTE_1);
		assertTrue(rechnung.hasReminders());

		rechnung.addZahlung(new Money(10000), "initial amount", null);
		assertTrue(rechnung.getInvoiceState() == InvoiceState.PAID);
		assertFalse(rechnung.hasReminders());

		// Rechnung MAHNUNG_2 partial amount
		rechnung = new Rechnung("2", mandant, fall, kons.getDatum(), kons.getDatum(), new Money(10000),
				InvoiceState.OPEN.getState());
		rechnung.setStatus(InvoiceState.DEMAND_NOTE_1);
		rechnung.addZahlung(new Money(10).multiply(-1.0), Messages.Rechnung_Mahngebuehr1, null);
		assertTrue(rechnung.getInvoiceState() == InvoiceState.DEMAND_NOTE_1);
		rechnung.setStatus(InvoiceState.DEMAND_NOTE_2);
		rechnung.addZahlung(new Money(10).multiply(-1.0), Messages.Rechnung_Mahngebuehr2, null);
		assertTrue(rechnung.getInvoiceState() == InvoiceState.DEMAND_NOTE_2);
		assertTrue(rechnung.hasReminders());

		rechnung.addZahlung(new Money(5000), "partial amount", null);
		assertTrue(rechnung.getInvoiceState() == InvoiceState.PARTIAL_PAYMENT);
		rechnung.addZahlung(new Money(5000), "partial amount", null);
		assertTrue(rechnung.getInvoiceState() == InvoiceState.PAID);
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
				InvoiceState.OPEN.getState());
		rechnung.setStatus(InvoiceState.DEMAND_NOTE_1);
		rechnung.addZahlung(new Money(10).multiply(-1.0), Messages.Rechnung_Mahngebuehr1, null);
		assertTrue(rechnung.getInvoiceState() == InvoiceState.DEMAND_NOTE_1);
		assertTrue(rechnung.hasReminders());

		rechnung.addZahlung(new Money(10000), "initial amount", null);
		assertFalse(rechnung.getInvoiceState() == InvoiceState.PAID);
		assertTrue(rechnung.hasReminders());
	}

	@Test
	public void testSetStatusUpdatesStateDateOnlyOnChange() {
		Mandant mandant = new Mandant("TestMandant", "TestMandant", "01.01.70", "w");
		Patient patient = new Patient("TestPatient", "TestPatient", "01.01.70", "w");
		Konsultation kons = patient.createFallUndKons();
		Fall fall = kons.getFall();

		Rechnung rechnung = new Rechnung("1", mandant, fall, kons.getDatum(), kons.getDatum(), new Money(10000),
				InvoiceState.OPEN.getState());
		// the constructor sets BILL_STATE directly, no trace entry yet
		assertEquals(0, rechnung.getTrace(Rechnung.STATUS_CHANGED).size());

		// pin the state date to a known past date
		rechnung.set(Rechnung.BILL_STATE_DATE, "01.01.2020");

		// setting the same state again must not touch the state date
		rechnung.setStatus(InvoiceState.OPEN);
		assertEquals("01.01.2020", rechnung.get(Rechnung.BILL_STATE_DATE));
		// but the trace entry is still recorded
		assertEquals(1, rechnung.getTrace(Rechnung.STATUS_CHANGED).size());

		// changing the state must update the state date
		rechnung.setStatus(InvoiceState.OPEN_AND_PRINTED);
		assertEquals(new TimeTool().toString(TimeTool.DATE_GER), rechnung.get(Rechnung.BILL_STATE_DATE));
		assertEquals(2, rechnung.getTrace(Rechnung.STATUS_CHANGED).size());
	}
}
