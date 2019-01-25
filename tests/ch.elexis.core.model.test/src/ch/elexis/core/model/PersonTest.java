package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class PersonTest extends AbstractTest {

	@Before
	public void before() {
		super.before();
		super.createPerson();
	}

	@After
	public void after() {
		super.after();
	}

	@Test
	public void createDeletePatient() {
		person.setMaritalStatus(MaritalStatus.MARRIED);
		coreModelService.save(person);
		assertTrue(person.isPerson());
		assertFalse(person.isPatient());
		assertFalse(person.isMandator());
		assertFalse(person.isOrganization());
		assertFalse(person.isLaboratory());

		String id = person.getId();
		assertNotNull(id);
		assertNotNull(person.getCode());
		IPerson findById = coreModelService.load(id, IPerson.class).get();
		assertNotNull(findById);
		assertEquals(MaritalStatus.MARRIED, findById.getMaritalStatus());
		coreModelService.delete(person);
	}

}
