package ch.elexis.data;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ch.elexis.core.model.IContact;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class Test_Patient extends AbstractPersistentObjectTest {

	@Test
	public void testNameWithApostrophe() {
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
	public void TestVorname() {
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
	
	@Test
	public void getPostAnschriftPatient() {
		Person person = new Person("Name", "Vorname", "26.07.1979", "m");
		person.set(Person.FLD_STREET, "Strasse 14");
		person.set(Person.FLD_COUNTRY, "CH");
		person.set(Person.FLD_PLACE, "City");
		person.set(Person.TITLE, "Dr.");
		person.set(Person.FLD_ZIP, "4433");
		
		String postAnschrift = person.getPostAnschrift(true);
		
		person.set(Person.FLD_ANSCHRIFT, null);
		
		IContact contact = CoreModelServiceHolder.get().load(person.getId(), IContact.class).orElseThrow();
		String postalAddress = contact.getPostalAddress();
		
		assertEquals(postAnschrift, postalAddress);
		
		person.removeFromDatabase();
	}
	
	@Test
	public void getPostAnschriftOrganisation() {
		Organisation organization = new Organisation("Name", "Zusatz1");
		organization.set(Person.FLD_STREET, "Strasse 14");
		organization.set(Person.FLD_COUNTRY, "CH");
		organization.set(Person.FLD_PLACE, "City");
		organization.set(Person.FLD_ZIP, "4433");
		
		String postAnschrift = organization.getPostAnschrift(true);
		
		organization.set(Person.FLD_ANSCHRIFT, null);
		
		IContact contact = CoreModelServiceHolder.get().load(organization.getId(), IContact.class).orElseThrow();
		String postalAddress = contact.getPostalAddress();
		
		assertEquals(postAnschrift, postalAddress);
		
		organization.removeFromDatabase();
	}
}
