package ch.elexis.data.views;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.data.AbstractPersistentObjectTest;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.Money;

public class Test_InvoiceBillState extends AbstractPersistentObjectTest {
	
	public Test_InvoiceBillState(JdbcLink link){
		super(link);
	}
	
	@Test
	public void testInitializeSqlView() throws IOException{
		Mandant mandant = new Mandant("TestMandant", "TestMandant", "01.01.70", "w");
		Patient patient = new Patient("TestPatient", "TestPatient", "01.01.70", "w");
		Konsultation kons = patient.createFallUndKons();
		Fall fall = kons.getFall();
		// enable removing open reminders
		CoreHub.globalCfg.set(Preferences.RNN_REMOVE_OPEN_REMINDER, true);
		
		// Rechnung MAHNUNG_1 full amount
		Rechnung rechnung = new Rechnung("1", mandant, fall, kons.getDatum(), kons.getDatum(),
			new Money(10000), RnStatus.OFFEN);
		
		// initialize table
		InvoiceBillState.initializeSqlViewIfRequired();
		
		AtomicInteger countPatients = new AtomicInteger(0);
		AtomicInteger countInvoices = new AtomicInteger(0);
		InvoiceBillState.fetchNumberOfPatientsAndInvoices(countPatients, countInvoices);
		assertEquals(3, countPatients.get());
		assertEquals(4, countInvoices.get());
	}
}
