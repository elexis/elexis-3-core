package ch.elexis.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.rgw.tools.JdbcLink;

@RunWith(Parameterized.class)
public class Test_Patient extends AbstractPersistentObjectTest {
	
	private static String dbFlavor = null;
	private static String savedDbFlavor = null;
	private static JdbcLink link = null;
	
	public Test_Patient(String flavor){
		this.dbFlavor = flavor;
		if (savedDbFlavor != flavor) {
			savedDbFlavor = flavor;
			cleanupDb();
			link = initDB(dbFlavor);
		}
	}
	
	@Parameters
	public static Collection<Object[]> data(){
		Object[][] data = null;
		String tstIt = "elexis.run.dbtests";
		System.out.println("Test_Patient:" + tstIt + " is " + System.getProperty(tstIt));
		if ("true".equals(System.getProperty(tstIt))) {
			data = new Object[][] {
				{
					"postgresql",
				}, {
					"mysql"
				}, {
					"h2"
				},
			};
		} else {
			data = new Object[][] {
				{
					"h2"
				},
			};
		}		
		return Arrays.asList(data);
	}
	
	@Test
	public void testNameWithApostrophe(){
		final String familyNameWithApostrophe = "D'Andrea";
		Patient male = new Patient("Mustermann", "Max", "1.1.2000", "m");
		male.set(Patient.FLD_NAME, familyNameWithApostrophe);
		System.out.println("male.getName() is " + male.getName());
		assert (male.getName() == familyNameWithApostrophe);
		
		// query it
		Query<Patient> qbe = new Query<Patient>(Patient.class);
		qbe.add(Patient.FLD_NAME, Query.LIKE, familyNameWithApostrophe);
		List<Patient> res = qbe.execute();
		System.out.println("Search via " + dbFlavor + " returned " + res.size() + " patients");
		assert (res.size() == 1);
	}
	
	@Test
	public void TestVorname(){
		final String givenName = "Maria";
		Patient female = new Patient("Musterfrau", "Erika", "1.1.2000", "f");
		female.set(Patient.FLD_NAME, givenName);
		System.out.println("female.getVorname() is " + female.getVorname());
		assert (female.getVorname() == givenName);
		
		// query it
		Query<Patient> qbe = new Query<Patient>(Patient.class);
		qbe.add(Patient.FLD_NAME, Query.LIKE, givenName);
		List<Patient> res = qbe.execute();
		System.out.println("Search via " + dbFlavor + " returned " + res.size() + " patients");
		assert (res.size() == 1);
	}
	
	private static void cleanupDb(){
		if (link != null) {
			PersistentObject.deleteAllTables();
			link.disconnect();
			link = null;
		}
	}
	
	@AfterClass
	public static void logout(){
		cleanupDb();
	}
	
}
