package ch.elexis.data;

import java.util.List;

import org.junit.Test;

import ch.rgw.tools.JdbcLink;

public class Test_Patient extends AbstractPersistentObjectTest {
	
	public Test_Patient(JdbcLink link){
		super(link);
	}


	@Test
	public void testNameWithApostrophe(){
		final String familyNameWithApostrophe = "D'Andrea";
		Patient male = new Patient("Mustermann", "Max", "1.1.2000", "m");
		male.set(Patient.FLD_NAME, familyNameWithApostrophe);
		assert (male.getName() == familyNameWithApostrophe);
		
		// query it
		Query<Patient> qbe = new Query<Patient>(Patient.class);
		qbe.add(Patient.FLD_NAME, Query.LIKE, familyNameWithApostrophe);
		List<Patient> res = qbe.execute();
		assert (res.size() == 1);
		male.delete();
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
//		System.out.println("Search via " + dbFlavor + " returned " + res.size() + " patients");
		assert (res.size() == 1);
	}
}
