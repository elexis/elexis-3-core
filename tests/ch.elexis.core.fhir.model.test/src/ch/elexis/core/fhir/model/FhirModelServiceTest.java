package ch.elexis.core.fhir.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.gclient.IQuery;
import ch.elexis.core.fhir.model.test.AllPluginTests;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IReminderBuilder;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class FhirModelServiceTest {

	private static IModelService coreModelService;

	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		coreModelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
	}

	@Test
	public void getConnectionStatus() {
		assertEquals(ConnectionStatus.REMOTE, AllPluginTests.getModelService().getConnectionStatus());
	}

	@Test
	public void loadPatient() {
		IPatient patient = new IContactBuilder.PatientBuilder(coreModelService, "Patient", "Test",
				LocalDate.of(2001, 1, 1), Gender.FEMALE).buildAndSave();
		Optional<IPatient> loadedPatient = AllPluginTests.getModelService().load(patient.getId(), IPatient.class);
		assertTrue(loadedPatient.isPresent());
		assertEquals(patient.getId(), loadedPatient.get().getId());
		assertEquals(patient.getDateOfBirth(), loadedPatient.get().getDateOfBirth());
		assertEquals(patient.getGender(), loadedPatient.get().getGender());
		assertEquals(patient.getLabel(), loadedPatient.get().getLabel());

		AllPluginTests.getModelService().delete(loadedPatient.get());
	}

	@Test
	public void loadMandator() {
		IMandator mandator = new IContactBuilder.MandatorBuilder(coreModelService, "Mandator", "Test").build();
		mandator.setUser(true);
		CoreModelServiceHolder.get().save(mandator);
		Optional<IMandator> loadedMandator = AllPluginTests.getModelService().load(mandator.getId(), IMandator.class);
		assertTrue(loadedMandator.isPresent());
		assertEquals(mandator.getId(), loadedMandator.get().getId());

		AllPluginTests.getModelService().delete(loadedMandator.get());
	}

	@Test
	public void loadReminder() {
		IReminder reminder = new IReminderBuilder(coreModelService, null, Visibility.ALWAYS, ProcessStatus.OPEN,
				"test text").buildAndSave();
		Optional<IReminder> loadedReminder = AllPluginTests.getModelService().load(reminder.getId(), IReminder.class);
		assertTrue(loadedReminder.isPresent());
		assertEquals(reminder.getId(), loadedReminder.get().getId());
		assertEquals(reminder.getVisibility(), loadedReminder.get().getVisibility());
		assertEquals(reminder.getStatus(), loadedReminder.get().getStatus());
		assertEquals(reminder.getMessage(), loadedReminder.get().getMessage());

		AllPluginTests.getModelService().delete(loadedReminder.get());
	}

	@Test
	public void queryReminder() {
		for (int i = 0; i < 10; i++) {
			IReminder reminder = new IReminderBuilder(coreModelService, null, Visibility.ALWAYS, ProcessStatus.OPEN,
					"test text " + i).build();
			if (i % 2 == 0) {
				reminder.setStatus(ProcessStatus.CLOSED);
			}
			reminder.setDue(LocalDate.now().plusDays(i));
			coreModelService.save(reminder);
		}
		IQuery<IBaseBundle> query = AllPluginTests.getModelService().getQuery(IReminder.class);
		query.and(Task.STATUS.exactly().code(TaskStatus.COMPLETED.name()));
		List<IReminder> closedResults = AllPluginTests.getModelService().getQueryResults(query, IReminder.class);
		assertNotNull(closedResults);
		assertFalse(closedResults.isEmpty());
		assertEquals(5, closedResults.size());

		query = AllPluginTests.getModelService().getQuery(IReminder.class);
		query.and(Task.STATUS.exactly().code(TaskStatus.ACCEPTED.name()));
		List<IReminder> openResults = AllPluginTests.getModelService().getQueryResults(query, IReminder.class);
		assertNotNull(openResults);
		assertFalse(openResults.isEmpty());
		assertEquals(5, openResults.size());

		assertFalse(openResults.stream().map(r -> r.getId()).toList().contains(closedResults.get(0).getId()));
	}
}
