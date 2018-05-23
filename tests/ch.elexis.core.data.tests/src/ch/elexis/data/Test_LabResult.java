package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.LabResultConstants;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription.Description;
import ch.rgw.tools.TimeTool;

public class Test_LabResult {
	
	static Patient patient;
	static LabItem absoluteLabItem;
	static LabItem textLabItem;
	
	public static final String REFVAL = "refVal";
	
	@BeforeClass
	public static void prepareLabItems(){
		patient = new Patient("Mustermann", "Max", "1.1.2000", "m");
		
		Organisation org = new Organisation("orgname", "orgzusatz1");
		absoluteLabItem = new LabItem("kuerzel1", "testname1", org, REFVAL, REFVAL, "",
			LabItemTyp.ABSOLUTE, "gruppe", "0");
		textLabItem = new LabItem("kuerzel2", "testname2", org, REFVAL, REFVAL, "",
			LabItemTyp.ABSOLUTE, "gruppe", "0");
	}
	
	@Test
	public void testEvaluationRulesForTypeAbsolute(){
		CoreHub.globalCfg.set(Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_ABSOLUT
			+ Preferences.LABSETTINGS_CFG_EVAL_REFVAL_NON_EQUAL_RESVAL_MEANS_PATHOLOGIC, false);
		
		LabResult result = new LabResult(patient, new TimeTool(), absoluteLabItem, REFVAL, "");
		assertFalse(result.isFlag(LabResultConstants.PATHOLOGIC));
		assertFalse(result.isPathologicFlagIndetermined(null));
		assertEquals(Description.PATHO_ABSOLUT, result.getPathologicDescription().getDescription());
		
		result = new LabResult(patient, new TimeTool(), absoluteLabItem, "neg", "");
		assertFalse(result.isFlag(LabResultConstants.PATHOLOGIC));
		assertTrue(result.isPathologicFlagIndetermined(null));
		assertEquals(Description.UNKNOWN, result.getPathologicDescription().getDescription());
		
		CoreHub.globalCfg.set(Preferences.LABSETTINGS_CFG_EVAL_PREFIX_TYPE_ABSOLUT
			+ Preferences.LABSETTINGS_CFG_EVAL_REFVAL_NON_EQUAL_RESVAL_MEANS_PATHOLOGIC, true);
		
		result = new LabResult(patient, new TimeTool(), absoluteLabItem, REFVAL, "");
		assertFalse(result.isFlag(LabResultConstants.PATHOLOGIC));
		assertFalse(result.isPathologicFlagIndetermined(null));
		assertEquals(Description.PATHO_ABSOLUT, result.getPathologicDescription().getDescription());
		
		result = new LabResult(patient, new TimeTool(), absoluteLabItem, "neg", "");
		assertTrue(result.isFlag(LabResultConstants.PATHOLOGIC));
		assertFalse(result.isPathologicFlagIndetermined(null));
		assertEquals(Description.PATHO_ABSOLUT, result.getPathologicDescription().getDescription());
	}
	
	@Test
	public void testCreateLabResultAssertLabOrder(){
		LabResult labResult = LabResult.createLabResultAndAssertLabOrder(patient, new TimeTool(),
			absoluteLabItem, REFVAL, "", null, REFVAL, null, "orderId", null, null, "testGroup");
		assertNotNull(labResult.getId());
		assertNotNull(labResult.getLabOrder());
	}
	
}
