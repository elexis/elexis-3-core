package ch.elexis.core.ui.tests.views.invoicelist;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListSqlQuery;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.Money;

public class Test_InvoiceBillState {

	@Test
	public void testInitializeSqlView() throws IOException {
		Mandant mandant = new Mandant("TestMandant", "TestMandant", "01.01.70", "w");
		Patient patient = new Patient("TestPatient", "TestPatient", "01.01.70", "w");
		Konsultation kons = patient.createFallUndKons();
		Fall fall = kons.getFall();
		// enable removing open reminders
		ConfigServiceHolder.setGlobal(Preferences.RNN_REMOVE_OPEN_REMINDER, true);

		// Rechnung MAHNUNG_1 full amount
		Rechnung rechnung = new Rechnung("1", mandant, fall, kons.getDatum(), kons.getDatum(), new Money(10000),
				InvoiceState.OPEN.getState());

		AtomicInteger countPatients = new AtomicInteger(0);
		AtomicInteger countInvoices = new AtomicInteger(0);
		InvoiceListSqlQuery.fetchNumberOfPatientsAndInvoices(countPatients, countInvoices);
		assertEquals(1, countPatients.get());
		assertEquals(1, countInvoices.get());
	}
}
