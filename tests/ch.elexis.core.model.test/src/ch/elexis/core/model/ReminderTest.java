package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IUserGroupBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.ISubQuery;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
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

	@Test
	public void createDeleteReminderGroup() throws IOException {
		IUserGroup group = new IUserGroupBuilder(CoreModelServiceHolder.get(), "testGroup").buildAndSave();

		IReminder reminder = coreModelService.create(IReminder.class);
		reminder.setContact(patient);
		reminder.setCreator(mandator);
		reminder.setGroup(group);
		reminder.addResponsible(mandator);
		reminder.addResponsible(person);
		reminder.setSubject("test");
		coreModelService.save(reminder);

		reminder = coreModelService.load(reminder.getId(), IReminder.class).orElse(null);
		assertNotNull(reminder);

		assertEquals(group, reminder.getGroup());

		coreModelService.delete(reminder);
		coreModelService.remove(reminder);
	}

	@Test
	public void createQuery() throws IOException {
		IReminder reminder = coreModelService.create(IReminder.class);
		reminder.setContact(patient);
		reminder.setCreator(mandator);
		reminder.addResponsible(mandator);
		reminder.addResponsible(person);
		reminder.setSubject("test");
		coreModelService.save(reminder);

		IQuery<IReminder> query = coreModelService.getQuery(IReminder.class);
		query.and(ModelPackage.Literals.IREMINDER__CONTACT, COMPARATOR.EQUALS, patient);
		assertEquals(1, query.execute().size());

		IReminder otherReminder = coreModelService.create(IReminder.class);
		otherReminder.setContact(patient);
		otherReminder.setCreator(mandator);
		otherReminder.addResponsible(person);
		otherReminder.setSubject("test other");
		coreModelService.save(otherReminder);

		IReminder allReminder = coreModelService.create(IReminder.class);
		allReminder.setContact(patient);
		allReminder.setCreator(person);
		allReminder.setResponsibleAll(true);
		allReminder.setSubject("test all");
		allReminder.setDue(LocalDate.now().minusDays(2));
		coreModelService.save(allReminder);

		query = coreModelService.getQuery(IReminder.class);
		query.and(ModelPackage.Literals.IREMINDER__CONTACT, COMPARATOR.EQUALS, patient);
		ISubQuery<IReminderResponsibleLink> subQuery = query.createSubQuery(IReminderResponsibleLink.class,
				coreModelService);
		subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
		subQuery.and("responsible", COMPARATOR.EQUALS, mandator);
		query.exists(subQuery);

		assertEquals(1, query.execute().size());

		query = coreModelService.getQuery(IReminder.class);
		query.and(ModelPackage.Literals.IREMINDER__CONTACT, COMPARATOR.EQUALS, patient);

		query.startGroup();
		subQuery = query.createSubQuery(IReminderResponsibleLink.class, coreModelService);
		subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
		subQuery.and("responsible", COMPARATOR.EQUALS, mandator);
		query.exists(subQuery);
		query.or("responsibleValue", COMPARATOR.EQUALS, "ALL");
		query.andJoinGroups();
		assertEquals(2, query.execute().size());

		query = coreModelService.getQuery(IReminder.class);
		query.and(ModelPackage.Literals.IREMINDER__CREATOR, COMPARATOR.EQUALS, mandator);
		query.startGroup();
		subQuery = query.createSubQuery(IReminderResponsibleLink.class, coreModelService);
		subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
		subQuery.and("responsible", COMPARATOR.EQUALS, mandator);
		query.exists(subQuery);
		query.or("responsibleValue", COMPARATOR.EQUALS, "ALL");
		query.orJoinGroups();
		assertEquals(3, query.execute().size());

		query = coreModelService.getQuery(IReminder.class);
		query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.LESS_OR_EQUAL, LocalDate.now());
		query.startGroup();
		subQuery = query.createSubQuery(IReminderResponsibleLink.class, coreModelService);
		subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
		subQuery.and("responsible", COMPARATOR.EQUALS, mandator);
		query.exists(subQuery);
		query.or("responsibleValue", COMPARATOR.EQUALS, "ALL");
		query.andJoinGroups();
		assertEquals(1, query.execute().size());

		coreModelService.remove(reminder);
		coreModelService.remove(otherReminder);
		coreModelService.remove(allReminder);
	}
}
