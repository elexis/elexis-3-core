package ch.elexis.data;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.types.LabItemTyp;
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
	public void testCreateLabResultAssertLabOrder(){
		LabResult labResult = LabResult.createLabResultAndAssertLabOrder(patient, new TimeTool(),
			absoluteLabItem, REFVAL, "", null, REFVAL, null, "orderId", null, null, "testGroup");
		assertNotNull(labResult.getId());
		assertNotNull(labResult.getLabOrder());
	}
	
}
