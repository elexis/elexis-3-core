package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.test.AbstractTest;
import ch.elexis.core.types.AddressType;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.RelationshipType;

public class ContactTest extends AbstractTest {

	@Override
	@Before
	public void before() {
		super.before();
		super.createPerson();
	}

	@Override
	@After
	public void after() {
		super.after();
	}

	@Test
	public void createAndRemoveIContact() throws InstantiationException, IllegalAccessException {
		assertNotNull(person.getId());
		assertTrue(person.isPerson());
		assertFalse(person.isPatient());
		assertFalse(person.isMandator());
		assertFalse(person.isOrganization());
		assertFalse(person.isLaboratory());
	}

	@Test
	public void getAgeInYears() {
		IPerson findById = coreModelService.load(person.getId(), IPerson.class).get();
		assertEquals(person.getId(), findById.getId());
		assertEquals(LocalDate.of(1979, 7, 26), person.getDateOfBirth().toLocalDate());
		int ageInYears = findById.getAgeInYears();
		assertTrue(ageInYears >= 2);
	}

	@Test
	public void createRemoveAddress() {
		IAddress nursingHome = coreModelService.create(IAddress.class);
		nursingHome.setType(AddressType.NURSING_HOME);
		nursingHome.setStreet2("Street2");
		nursingHome.setZip("6840");
		nursingHome.setCountry(Country.AT);
		nursingHome.setContact(person);
		coreModelService.save(Arrays.asList(person, nursingHome));

		assertTrue(person.getAddress().contains(nursingHome));
		coreModelService.delete(nursingHome);
		assertFalse(person.getAddress().contains(nursingHome));
	}

	@Test
	public void createUpdateRemoveContactImage() throws IOException {
		IImage image = CoreModelServiceHolder.get().create(IImage.class);
		image.setDate(LocalDate.now());
		image.setTitle("RandomImage");
		image.setMimeType(MimeType.png);
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("./elexis.png")) {
			byte[] byteArray = IOUtils.toByteArray(is);
			image.setImage(byteArray);
		}
		person.setImage(image);

		IPerson findById = coreModelService.load(person.getId(), IPerson.class).get();
		IImage _image = findById.getImage();
		assertTrue(Arrays.equals(image.getImage(), _image.getImage()));
		assertEquals(MimeType.png, _image.getMimeType());
		coreModelService.delete(image);
	}

	@Test
	public void createWithDefaultAddress() {
		IOrganization employer = new IContactBuilder.OrganizationBuilder(coreModelService, "MEDEVIT")
				.defaultAddress("street", "zip", "city", Country.NDF).buildAndSave();

		assertNotNull(employer);
		assertFalse(employer.getAddress().isEmpty());
	}

	@Test
	public void createRemoveRelatedContact() {
		IOrganization employer = new IContactBuilder.OrganizationBuilder(coreModelService, "MEDEVIT").buildAndSave();
		IPerson findById = coreModelService.load(person.getId(), IPerson.class).get();

		IRelatedContact relatedContact = coreModelService.create(IRelatedContact.class);
		relatedContact.setOtherContact(employer);
		relatedContact.setMyType(RelationshipType.BUSINESS_EMPLOYEE);
		relatedContact.setOtherType(RelationshipType.BUSINESS_EMPLOYER);
		relatedContact.setRelationshipDescription("blabla");
		relatedContact.setMyContact(findById);
		coreModelService.save(relatedContact);

		IRelatedContact iRelatedContact = findById.getRelatedContacts().get(0);
		assertEquals(relatedContact.getId(), iRelatedContact.getId());
		assertEquals(employer.getId(), iRelatedContact.getOtherContact().getId());
		assertEquals(RelationshipType.BUSINESS_EMPLOYEE, iRelatedContact.getMyType());
		assertEquals(RelationshipType.BUSINESS_EMPLOYER, iRelatedContact.getOtherType());
		assertEquals("blabla", iRelatedContact.getRelationshipDescription());
		coreModelService.delete(relatedContact);
		assertFalse(findById.getRelatedContacts().contains(relatedContact));
	}

	@Test
	public void extInfo() {
		IPerson testPerson = new IContactBuilder.PersonBuilder(coreModelService, "firstname", "lastname",
				LocalDate.of(2000, 1, 1), Gender.FEMALE).buildAndSave();
		assertTrue(testPerson.getExtInfo("testKey") == null);
		testPerson.setExtInfo("testKey", "testValue");
		coreModelService.save(testPerson);
		Optional<IPerson> loaded = coreModelService.load(testPerson.getId(), IPerson.class);
		assertTrue(loaded.isPresent());
		assertEquals("testValue", loaded.get().getExtInfo("testKey"));
		coreModelService.remove(testPerson);
	}
}
