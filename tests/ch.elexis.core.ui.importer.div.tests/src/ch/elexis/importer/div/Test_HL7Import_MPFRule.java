package ch.elexis.importer.div;

import static ch.elexis.importer.div.Helpers.parseOneHL7file;
import static ch.elexis.importer.div.Helpers.removeAllLaboWerte;
import static ch.elexis.importer.div.Helpers.removeAllPatientsAndDependants;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.importer.div.importers.HL7Parser;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.PathologicDescription.Description;
import ch.elexis.core.ui.importer.div.importers.TestHL7Parser;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Query;

@RunWith(Parameterized.class)
public class Test_HL7Import_MPFRule {
	
	private HL7Parser hlp = new TestHL7Parser("HL7_Test");
	private static Path workDir = null;
	
	private Boolean isMFPRuleActive;
	
	public Test_HL7Import_MPFRule(boolean isMFPRuleActive){
		this.isMFPRuleActive = isMFPRuleActive;
		
		if (isMFPRuleActive) {
			CoreHub.globalCfg.setAsList(
				Preferences.LABSETTINGS_MISSING_PATH_FLAG_MEANS_NON_PATHOLOGIC_FOR_LABORATORIES,
				Collections.singletonList(AllTests.testLab.getId()));
		} else {
			CoreHub.globalCfg.setAsList(
				Preferences.LABSETTINGS_MISSING_PATH_FLAG_MEANS_NON_PATHOLOGIC_FOR_LABORATORIES,
				Collections.emptyList());
		}
		CoreHub.globalCfg.flush();
	}
	
	@Parameterized.Parameters(name = "{0}")
	public static Collection<Boolean> primeNumbers(){
		return Arrays.asList(new Boolean[] {
			true, false
		});
	}
	
	@Before
	public void setup() throws Exception{
		workDir = Helpers.copyRscToTempDirectory();
	}
	
	@After
	public void teardown() throws Exception{
		removeAllPatientsAndDependants();
		if (workDir != null) {
			Helpers.removeTempDirectory(workDir);
		}
	}
	
	@Test
	public void test_ImportOnExistingLabItemRefValue_11114() throws IOException{
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		
		LabItem liKrus = new LabItem("KRUS", "Kreatinin im Urin", AllTests.testLab, "> 60", "> 60",
			"mmol/l", LabItemTyp.NUMERIC, "Urin", "42");
		
		parseOneHL7file(hlp, new File(workDir.toString(), "Analytica/Albumin.hl7"), false, true);
		
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> qrr = qr.execute();
		assertEquals(4, qrr.size());
		for (LabResult labResult : qrr) {
			
			assertEquals(LabItemTyp.NUMERIC, labResult.getItem().getTyp());
			assertEquals(labResult.getOrigin().getLabel(), AllTests.testLab.getId(),
				labResult.getOrigin().getId());
			
			PathologicDescription pathologicDescription = labResult.getPathologicDescription();
			String itemCode = labResult.getItem().getKuerzel();
			switch (itemCode) {
			case "KRUS":
				assertEquals(liKrus.getId(), labResult.getItem().getId());
				assertEquals(
					(isMFPRuleActive) ? Description.PATHO_IMPORT : Description.PATHO_REF_ITEM,
					pathologicDescription.getDescription());
				assertEquals((isMFPRuleActive) ? "Lt. MPF Regel" : "> 60",
					pathologicDescription.getReference());
				assertEquals((isMFPRuleActive) ? 0 : 1, labResult.getFlags());
				assertEquals(LabItemTyp.NUMERIC, labResult.getItem().getTyp());
				assertFalse(labResult.isPathologicFlagIndetermined(null));
				assertEquals("14.5", labResult.getResult());
				break;
			case "MIKA":
				assertEquals((isMFPRuleActive) ? Description.PATHO_IMPORT : Description.PATHO_NOREF,
					pathologicDescription.getDescription());
				assertEquals(0, labResult.getFlags());
				assertEquals((isMFPRuleActive) ? Boolean.FALSE : Boolean.TRUE,
					labResult.isPathologicFlagIndetermined(null));
				assertEquals("404", labResult.getResult());
				break;
			case "MIKAQ":
				assertEquals(Description.PATHO_IMPORT, pathologicDescription.getDescription());
				assertEquals("H", pathologicDescription.getReference());
				assertEquals(1, labResult.getFlags());
				assertFalse(labResult.isPathologicFlagIndetermined(null));
				assertEquals("g/mol", labResult.getUnit());
				assertEquals("27.9", labResult.getResult());
				break;
			case "TSH":
				assertEquals("2.07", labResult.getResult());
				assertEquals("mU/l", labResult.getUnit());
				assertEquals("0.55 - 4.78", labResult.getRefFemale());
				assertEquals(
					(isMFPRuleActive) ? Description.PATHO_IMPORT : Description.PATHO_REF_ITEM,
					pathologicDescription.getDescription());
				assertEquals(0, labResult.getFlags());
				break;
			default:
				break;
			}
		}
	}
	
	@Test
	public void test_ImportConsiderCorrectNPathologicFlag_11231() throws IOException{
		
		removeAllPatientsAndDependants();
		removeAllLaboWerte();
		
		parseOneHL7file(hlp,
			new File(workDir.toString(), "XLabResults/09168648_20150327102125_13382.hl7"), false,
			true);
		
		Query<LabResult> qr = new Query<LabResult>(LabResult.class);
		List<LabResult> qrr = qr.execute();
		assertEquals(26, qrr.size());
		for (LabResult labResult : qrr) {
			assertEquals(LabItemTyp.TEXT, labResult.getItem().getTyp());
			
			PathologicDescription pathologicDescription = labResult.getPathologicDescription();
			String itemCode = labResult.getItem().getKuerzel();
			switch (itemCode) {
			case "na":
				assertEquals("143", labResult.getResult());
				assertEquals("mmol/l", labResult.getUnit());
				assertEquals(0, labResult.getFlags());
				assertEquals(Description.PATHO_IMPORT, pathologicDescription.getDescription());
				break;
			case "rdwsd":
				assertEquals("53.1", labResult.getResult());
				assertEquals("fl", labResult.getUnit());
				assertEquals(1, labResult.getFlags());
				assertEquals(Description.PATHO_IMPORT, pathologicDescription.getDescription());
				break;
			default:
				break;
			}
		}
		
	}
	
}
