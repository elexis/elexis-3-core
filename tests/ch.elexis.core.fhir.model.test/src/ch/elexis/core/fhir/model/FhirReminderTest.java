package ch.elexis.core.fhir.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.hl7.fhir.r4.model.Task;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.fhir.model.adapter.ModelAdapterFactory;
import ch.elexis.core.fhir.model.test.AllPluginTests;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IReminderBuilder;
import ch.elexis.core.model.builder.IUserGroupBuilder;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class FhirReminderTest {

	private static IFhirTransformerRegistry transformerRegistry;

	private static IModelService coreModelService;
	
	@BeforeClass
	public static void beforeClass() {
		transformerRegistry = OsgiServiceUtil.getService(IFhirTransformerRegistry.class).get();
		coreModelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
	}

	@Test
	public void createAdapter() {
		IReminder reminder = new IReminderBuilder(coreModelService, null, Visibility.ALWAYS, ProcessStatus.OPEN,
				"test text").build();
		Optional<Task> fhirReminder = transformerRegistry.getTransformerFor(Task.class, IReminder.class)
				.getFhirObject(reminder);
		assertTrue(fhirReminder.isPresent());
		IReminder fhirModelAdapter = (IReminder) new ModelAdapterFactory().createAdapter(fhirReminder.get());
		assertNotNull(fhirModelAdapter);
		assertEquals(reminder.getId(), fhirModelAdapter.getId());
		assertEquals(Visibility.ALWAYS, fhirModelAdapter.getVisibility());
		assertEquals(ProcessStatus.OPEN, fhirModelAdapter.getStatus());
		assertEquals("test text", fhirModelAdapter.getMessage());
	}

	@Test
	public void loadReminderWithPatient() {
		IPatient patient = new IContactBuilder.PatientBuilder(coreModelService, "Patient", "Test",
				LocalDate.of(2001, 1, 1), Gender.FEMALE).buildAndSave();
		IReminder reminder = new IReminderBuilder(coreModelService, null, Visibility.ALWAYS, ProcessStatus.OPEN,
				"test text").contact(patient).buildAndSave();
		Optional<IReminder> loadedReminder = AllPluginTests.getModelService().load(reminder.getId(), IReminder.class);
		assertTrue(loadedReminder.isPresent());
		assertEquals(reminder.getId(), loadedReminder.get().getId());
		assertEquals(reminder.getVisibility(), loadedReminder.get().getVisibility());
		assertEquals(reminder.getStatus(), loadedReminder.get().getStatus());
		assertEquals(reminder.getMessage(), loadedReminder.get().getMessage());
		assertEquals(reminder.getContact().getId(), loadedReminder.get().getContact().getId());
		assertEquals(reminder.getContact().getDescription1(), loadedReminder.get().getContact().getDescription1());
		assertEquals(reminder.getContact().asIPatient().getDateOfBirth(),
				loadedReminder.get().getContact().asIPatient().getDateOfBirth());

		AllPluginTests.getModelService().delete(loadedReminder.get());
		AllPluginTests.getModelService().delete(loadedReminder.get().getContact());
	}

	@Test
	public void setResponsible() {
		IMandator mandator = new IContactBuilder.MandatorBuilder(coreModelService, "Mandator", "Test").build();
		mandator.setUser(true);
		CoreModelServiceHolder.get().save(mandator);
		IUserGroup group = new IUserGroupBuilder(coreModelService, "TestGroup").buildAndSave();

		IReminder reminder = new IReminderBuilder(coreModelService, null, Visibility.ALWAYS, ProcessStatus.OPEN,
				"test text").buildAndSave();

		Optional<IReminder> loadedReminder = AllPluginTests.getModelService().load(reminder.getId(), IReminder.class);
		assertTrue(loadedReminder.get().getResponsible().isEmpty());
		assertFalse(loadedReminder.get().isResponsibleAll());

		loadedReminder.get().addResponsible(mandator);
		AllPluginTests.getModelService().save(loadedReminder.get());
		loadedReminder = AllPluginTests.getModelService().load(reminder.getId(), IReminder.class);
		assertFalse(loadedReminder.get().getResponsible().isEmpty());

		IReminder editReminder = loadedReminder.get();
		editReminder.getResponsible().forEach(c -> {
			editReminder.removeResponsible(c);
		});
		editReminder.setGroup(group);
		AllPluginTests.getModelService().save(editReminder);
		loadedReminder = AllPluginTests.getModelService().load(reminder.getId(), IReminder.class);
		assertTrue(loadedReminder.get().getResponsible().isEmpty());
		assertNotNull(loadedReminder.get().getGroup());

		AllPluginTests.getModelService().delete(loadedReminder.get());
		AllPluginTests.getModelService().delete(loadedReminder.get().getResponsible());
	}
}
