package ch.elexis.importer.div;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Locale;

import org.junit.Test;

import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription.Description;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class Test_Import_LabItemInconclusiveRefValue {
	
	@Test
	public void testLabItemInconclusiveManualCreation(){
		
		Patient pat = new Patient("Tester", "Joachim", "24081933", "m");
		LabItem inconclusiveLabitem =
			new LabItem("EYECOL", "Eye Color", AllTests.testLab, LabItem.REFVAL_INCONCLUSIVE,
				LabItem.REFVAL_INCONCLUSIVE, "color", LabItemTyp.TEXT, "Test", "AB");
		LabResult eyeColor =
			new LabResult(pat, new TimeTool(), inconclusiveLabitem, "blue", "with greyish shades");
		
		assertEquals(0, eyeColor.getFlags());
		assertEquals(Description.PATHO_REF_ITEM,
			eyeColor.getPathologicDescription().getDescription());
		assertFalse(eyeColor.isPathologicFlagIndetermined(null));
		Locale locale = Locale.getDefault();
		if (locale.getLanguage().equals("de")) {
			// Our most common case
			assertEquals("Keine Referenzwerte", eyeColor.getPathologicDescription().getReference());
		} else if (locale.getLanguage().equals("en")) {
			// This is the case when running under CI via gitlab/travis
			assertEquals("No reference values", eyeColor.getPathologicDescription().getReference());
		} else {
			System.out.println(String.format("Skipping test for language %s produced %s",
				locale.getLanguage(), eyeColor.getPathologicDescription().getReference()));
		}
		assertEquals("blue", eyeColor.getResult());
		
	}
	
}
