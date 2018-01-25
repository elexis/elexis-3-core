package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeTool;

public class Test_LabItem extends AbstractPersistentObjectTest {
	
	public Test_LabItem(JdbcLink link){
		super(link);
	}

	private Organisation org;
	
	private Patient formulaPat;
	private LabResult formulaResult;
	private LabItem formulaItem;
	
	private static final String REF_ITEM_KUERZEL = "kuerzel";
	private static final String REF_ITEM_NAME = "testname";
	private static final String REF_ITEM_UNIT = "mg/dl";
	private static final String REF_ITEM_REFM = "0-1";
	private static final String REF_ITEM_REFW = "0-2";
	private static final String REF_ITEM_GROUP = "G gruppe";
	
	private LabItem currentLabItem;
	
	@Before
	public void setUp(){
		// create a instance of an PersistentObject ex. Organisation to test the query
		org = new Organisation("orgname", "orgzusatz1");
		currentLabItem = new LabItem(REF_ITEM_KUERZEL, REF_ITEM_NAME, org, REF_ITEM_REFM, REF_ITEM_REFW,
			REF_ITEM_UNIT, LabItemTyp.NUMERIC, REF_ITEM_GROUP, "0");
	}
	
	@After
	public void after() {
		currentLabItem.delete();
		org.delete();
	}
	
	@Test
	public void testGetLabItems(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
	}
	
	@Test
	public void testGetLabItemsSelective(){
		// create a second lab item to select
		LabItem item =
			new LabItem("kuerzel1", "testname1", org, "0-1", "0-2", "mg/dl", LabItemTyp.NUMERIC,
				"gruppe", "0");
		assertEquals(StringConstants.ONE, item.get(LabItem.VISIBLE));
		
		List<LabItem> items = LabItem.getLabItems(org.getId(), "kuerzel1", "0-1", "0-2", "mg/dl");
		assertEquals(1, items.size());
		assertEquals(item.getId(), items.get(0).getId());
		
		items = LabItem.getLabItems(org.getId(), null, "0-1", "0-2", "mg/dl");
		assertEquals(2, items.size());
		
		items = LabItem.getLabItems(org.getId(), null, null, null, null);
		assertEquals(2, items.size());

		items = LabItem.getLabItems(org.getId(), REF_ITEM_KUERZEL, null, null, null);
		assertEquals(1, items.size());

		items = LabItem.getLabItems(org.getId(), REF_ITEM_KUERZEL + "_dummy", null, null, null);
		assertEquals(0, items.size());
		
		item.delete();
	}
	
	@Test
	public void testGetEinheit(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertEquals(REF_ITEM_UNIT, loc.getEinheit());
	}
	
	@Test
	public void testSetEinheit(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		loc.setEinheit("l");
		assertEquals("l", loc.getEinheit());
	}
	
	@Test
	public void testGetGroup(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertEquals(REF_ITEM_GROUP, loc.getGroup());
	}
	
	@Test
	public void testGetRefM(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertEquals(REF_ITEM_REFM, loc.getRefM());
	}
	
	@Test
	public void testSetRefM(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		loc.setRefM("1-2");
		assertEquals("1-2", loc.getRefM());
	}
	
	@Test
	public void testGetRefW(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertEquals(REF_ITEM_REFW, loc.getRefW());
	}
	
	@Test
	public void testSetRefW(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		loc.setRefW("1-2");
		assertEquals("1-2", loc.getRefW());
	}
	
	@Test
	public void testGetLabor(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertEquals(org, loc.getLabor());
	}
	
	@Test
	public void testGetLabel(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertNotNull(loc.getLabel());
	}
	
	@Test
	public void testGetShortLabel(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertNotNull(loc.getShortLabel());
	}
	
	@Test
	public void testGetTyp(){
		List<LabItem> items = LabItem.getLabItems();
		assertEquals(1, items.size());
		LabItem loc = items.get(0);
		assertEquals(LabItemTyp.NUMERIC, loc.getTyp());
	}
	
	// @Test TODO: does not work under Elexis 3.0
	public void testSetFormula(){
		createFormulaEnv();
		assertEquals("G_1*2", formulaItem.getFormula());
	}
	
	// @Test TODO: does not work under Elexis 3.0
	public void testEvaluate(){
		createFormulaEnv();
		// a null pointer will be thrown when looking for the script interpreter
		// TODO write a plugin test including the interpreter
		try {
			formulaItem.evaluate(formulaPat, new TimeTool("01.01.00"));
		} catch (NullPointerException e) {
			
		} catch (ElexisException e) {
			fail();
		}
	}
	
	private void createFormulaEnv(){
		// create a second lab item to select
		LabItem item =
			new LabItem("kuerzel1", "testname1", org, "0-1", "0-2", "mg/dl", LabItemTyp.NUMERIC,
				"G gruppe", "1");
		// create a lab item made up by a formula
		formulaItem =
			new LabItem("formula", "formulatest", org, "0-2", "0-4", "mg/dl", LabItemTyp.FORMULA,
				"G gruppe", "2");
		
		formulaPat = new Patient("testName", "testVorname", "01.01.79", "m");
		
		formulaResult = new LabResult(formulaPat, new TimeTool("01.01.00"), item, "0.5", "comment");
		
		formulaItem.setFormula("G_1*2");
	}
}
