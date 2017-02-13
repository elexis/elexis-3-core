package ch.elexis.data;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.Money;

public class Test_Verrechnet extends AbstractPersistentObjectTest {

	public Test_Verrechnet(JdbcLink link) {
		super(link);
	}

	@Test
	public void changeAnzahlValidated() {

		new Anwender("testAnwender", "pass", true);
		Anwender.login("testAnwender", "pass");

		Mandant m = new Mandant("Mandant", "Erwin", "26.07.1979", "m");
		CoreHub.setMandant(m);

		new Patient("Mustermann", "Max", "1.1.2000", "m");
		Fall fall = new Fall();
		Konsultation cons = new Konsultation(fall);
		Artikel art = new Artikel("TestEigenartikel", "Eigenartikel", "0815");
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

		vr.changeAnzahl(2);
		leistungen = cons.getLeistungen();
		assertEquals(1, leistungen.size());
		vr = leistungen.get(0);
		assertEquals(2, vr.getZahl());
	}

}
