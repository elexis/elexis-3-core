package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.test.AbstractTest;
import ch.elexis.core.types.Gender;

public class PersonTest extends AbstractTest {

	@Override
	@Before
	public void before() {
		super.before();
		super.createUserSetActiveInContext();
	}

	@Override
	@After
	public void after() {
		super.after();
	}

	@Test
	public void modifyPerson() {
		person.setMaritalStatus(MaritalStatus.MARRIED);
		coreModelService.save(person);
		assertTrue(person.isPerson());
		assertFalse(person.isPatient());
		assertFalse(person.isMandator());
		assertFalse(person.isOrganization());
		assertFalse(person.isLaboratory());

		String id = person.getId();
		assertNotNull(id);
		IPerson findById = coreModelService.load(id, IPerson.class).get();
		assertNotNull(findById);
		assertEquals(MaritalStatus.MARRIED, findById.getMaritalStatus());
	}

	@Test
	public void searchPersonByBirthDate() {
		IQuery<IPerson> query = coreModelService.getQuery(IPerson.class);
		Date theBirthDate = new GregorianCalendar(1979, 6, 26).getTime();
		LocalDate localDate = Instant.ofEpochMilli(theBirthDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
		query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS, localDate);
		List<IPerson> execute = query.execute();
		assertEquals(1, execute.size());
	}

	@Test
	public void personGetPostalAddressNonPersisted() {
		IPatient patient = new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), "Firstname", "Lastname",
				LocalDate.of(2000, 1, 1), Gender.MALE).build();
		patient.setCity("City");
		patient.setZip("123");

		patient.setStreet("Street");
		String address = patient.getPostalAddress();
		assertNotNull(address);
	}
}
