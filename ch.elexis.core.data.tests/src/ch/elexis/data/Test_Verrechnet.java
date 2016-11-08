package ch.elexis.data;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.eigenartikel.Constants;
import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public class Test_Verrechnet extends AbstractPersistentObjectTest {
	
	private JdbcLink link;
	
	@Before
	public void setUp(){
		link = initDB();
	}
	
	@After
	public void tearDown(){
		if (link != null) {
			link.exec("DROP ALL OBJECTS");
			link.disconnect();
		}
	}
	
	@Test
	public void changeAnzahlValidated(){
		
		new Anwender("testAnwender", "pass", true);
		Anwender.login("testAnwender", "pass");
		
		Mandant m = new Mandant("Mandant", "Erwin", "26.07.1979", "m");
		CoreHub.setMandant(m);
		
		new Patient("Mustermann", "Max", "1.1.2000", "m");
		Fall fall = new Fall();
		Konsultation cons = new Konsultation(fall);
		Artikel art = new Artikel("TestEigenartikel", Constants.TYPE_NAME, "0815");
		art.setEKPreis(new Money(14.20));
		art.setVKPreis(new Money(20.10));
		
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
	
}
