package ch.elexis.core.findings.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.hl7.fhir.r4.model.Narrative;
import org.junit.Test;

public class ModelUtilTest {
	
	@Test
	public void setNarrativeText() {
		String value = "\n"
				+ "[ 13.07.2016 Rezept 13.07.2016 ](JW) neues Dauerrezept + Rezept für panotile habe chron Ohrenentz. braucht nur wenn akut";
		Narrative narrative = new Narrative();
		ModelUtil.setNarrativeFromString(narrative, value);
		Optional<String> string = ModelUtil.getNarrativeAsString(narrative);
		assertTrue(string.isPresent());
		assertEquals(value, string.get());
	}
}
