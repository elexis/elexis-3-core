package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

import ch.elexis.core.model.ch.BillingLaw;

public class BillingLawTest {

	@Test
	public void testBillingNames() {
		assertEquals("KVG", BillingLaw.KVG.name());
		assertEquals("VVG", BillingLaw.VVG.name());
		assertEquals("privat", BillingLaw.privat.name());
		assertEquals("IV", BillingLaw.IV.name());
		assertEquals("MV", BillingLaw.MV.name());
		assertEquals("OTHER", BillingLaw.OTHER.name());
	}

	@Test
	public void testLocalizedBillingNames() {
		if (Locale.getDefault().toString().equals("de_CH")) {
			System.out.println("Billing law Locale german tests");
			assertTrue(BillingLaw.IV.getLocaleText().startsWith("IVG:"));
			assertTrue(BillingLaw.KVG.getLocaleText().startsWith("KVG:"));
			assertTrue(BillingLaw.MV.getLocaleText().startsWith("MVG:"));
			assertTrue(BillingLaw.VVG.getLocaleText().startsWith("VVG:"));
			assertTrue(BillingLaw.privat.getLocaleText().startsWith("privat"));
			assertTrue(BillingLaw.OTHER.getLocaleText().startsWith("Andere"));
			assertTrue(BillingLaw.NONE.getLocaleText().startsWith("Keine"));
		}
		if (Locale.getDefault().toString().equals("fr_CH")) {
			System.out.println("Billing law Locale french tests");
			assertTrue(BillingLaw.IV.getLocaleText().startsWith("LAI:"));
			assertTrue(BillingLaw.KVG.getLocaleText().startsWith("LAMal:"));
			assertTrue(BillingLaw.MV.getLocaleText().startsWith("LAM:"));
			assertTrue(BillingLaw.VVG.getLocaleText().startsWith("LCA:"));
			assertTrue(BillingLaw.privat.getLocaleText().startsWith("privé"));
			assertTrue(BillingLaw.OTHER.getLocaleText().startsWith("Autre"));
			assertTrue(BillingLaw.NONE.getLocaleText().startsWith("Aucun"));
		}
	}

}
