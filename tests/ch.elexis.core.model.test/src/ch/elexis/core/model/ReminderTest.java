package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class ReminderTest extends AbstractTest {

	@Override
	@Before
	public void before() {
		super.before();
		super.createMandator();
		super.createPatient();
		super.createPerson();
	}

	@Test
	public void createDeleteReminder() throws IOException {
		IReminder reminder = coreModelService.create(IReminder.class);
		reminder.setContact(patient);
		reminder.setCreator(mandator);
		reminder.addResponsible(mandator);
		reminder.addResponsible(person);
		reminder.setSubject("test");
		coreModelService.save(reminder);

		reminder = coreModelService.load(reminder.getId(), IReminder.class).orElse(null);
		assertNotNull(reminder);

		assertEquals(coreModelService.load(patient.getId(), IContact.class).get(), reminder.getContact());

		assertEquals(2, reminder.getResponsible().size());
		assertTrue(reminder.getResponsible().contains(coreModelService.load(person.getId(), IContact.class).get()));

		coreModelService.delete(reminder);
		coreModelService.remove(reminder);
	}
}
