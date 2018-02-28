package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.eigenartikel.Constants;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.Money;

public class Test_Verrechnet extends AbstractPersistentObjectTest {
	
	private static Mandant mandant;
	private static Patient patient;
	private static Fall fall;
	
	public Test_Verrechnet(JdbcLink link){
		super(link);
	}

	@BeforeClass
	public static void before(){
		mandant = new Mandant("Mandant", "Erwin", "26.07.1979", "m");
		
		patient = new Patient("Mustermann", "Max", "1.1.2000", "m");
		fall = patient.neuerFall(Fall.getDefaultCaseLabel(), Fall.getDefaultCaseReason(),
			Fall.getDefaultCaseLaw());
	}
	
	@Test
	public void changeAnzahlValidated(){
		Artikel art = new Artikel("TestEigenartikel", Constants.TYPE_NAME, "0815");
		art.setEKPreis(new Money(14.20));
		art.setVKPreis(new Money(20.10));
		
		CoreHub.setMandant(mandant);
		Konsultation cons = new Konsultation(fall);
		
		cons.addLeistung(art);
		List<Verrechnet> leistungen = cons.getLeistungen();
		assertEquals(1, leistungen.size());
		Verrechnet vr = leistungen.get(0);
		assertEquals(1, vr.getZahl());
		
		cons.addLeistung(art);
		cons.addLeistung(art);
		leistungen = cons.getLeistungen();
		assertEquals(1, leistungen.size());
		vr = leistungen.get(0);
		assertEquals(3, vr.getZahl());
		
		IStatus changeAnzahlValidated = vr.changeAnzahlValidated(2);
		assertTrue(changeAnzahlValidated.isOK());
		leistungen = cons.getLeistungen();
		assertEquals(1, leistungen.size());
		vr = leistungen.get(0);
		assertEquals(2, vr.getZahl());
	}
	
	@Test
	public void createAndEscapeChars(){
		Artikel art =
			new Artikel("Vorsorgeuntersuchungen gemäss Empfehlungen {SGP}'93, im 1. Monat",
				Constants.TYPE_NAME, "0816");
		art.setEKPreis(new Money(14.20));
		art.setVKPreis(new Money(20.10));
		
		CoreHub.setMandant(mandant);
		Konsultation cons = new Konsultation(fall);
		
		cons.addLeistung(art);
		List<Verrechnet> leistungen = cons.getLeistungen();
		assertEquals(1, leistungen.size());
		Verrechnet vr = leistungen.get(0);
		assertEquals(1, vr.getZahl());
		
		assertEquals("Vorsorgeuntersuchungen gemäss Empfehlungen {SGP}'93, im 1. Monat",
			vr.getText());
	}
}
