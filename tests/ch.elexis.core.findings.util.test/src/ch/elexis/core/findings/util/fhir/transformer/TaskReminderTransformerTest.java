package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.builder.IReminderBuilder;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;

public class TaskReminderTransformerTest {

	private static IFhirTransformer<Task, IReminder> transformer;

	@BeforeClass
	public static void beforeClass() throws Exception {
		IFhirTransformerRegistry transformerRegistry = OsgiServiceUtil.getService(IFhirTransformerRegistry.class).get();
		transformer = transformerRegistry.getTransformerFor(Task.class, IReminder.class);
	}

	private IReminder setupLocalReminder(Visibility visibility, ProcessStatus status, String subject, String message,
			LocalDate due) {
		IReminderBuilder reminderBuilder = new IReminderBuilder(AllTransformerTests.getCoreModelService(),
				ContextServiceHolder.get(), visibility, status, message).subject(subject);
		IReminder ret = reminderBuilder.build();
		ret.setDue(due);
		CoreModelServiceHolder.get().save(ret);
		return ret;
	}

	@Test
	public void getFhirObject() {
		IReminder localObject = setupLocalReminder(Visibility.ALWAYS, ProcessStatus.OPEN, "test to fhir object",
				"additional info", LocalDate.of(2020, 2, 2));
		Optional<Task> fhirObject = transformer.getFhirObject(localObject);
		assertTrue(fhirObject.isPresent());
		assertEquals(Visibility.ALWAYS.name(), FhirUtil.getCodesFromCodingList("http://www.elexis.info/task/visibility",
				fhirObject.get().getCode().getCoding()).get(0));
		assertEquals(TaskStatus.ACCEPTED, fhirObject.get().getStatus());
		assertEquals("test to fhir object", fhirObject.get().getDescription());
		assertEquals("additional info", fhirObject.get().getNoteFirstRep().getText());
		assertEquals(Date.from(LocalDate.of(2020, 2, 2).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()),
				fhirObject.get().getExecutionPeriod().getEnd());
		CoreModelServiceHolder.get().remove(localObject);
	}

	@Test
	public void getLocalObject() {
		IReminder localObject = setupLocalReminder(Visibility.ALWAYS, ProcessStatus.OPEN, "test get local object",
				"additional info", LocalDate.of(2020, 2, 2));
		Optional<Task> fhirObject = transformer.getFhirObject(localObject);
		assertTrue(fhirObject.isPresent());
		Optional<IReminder> loadedLocalObject = transformer.getLocalObject(fhirObject.get());
		assertTrue(loadedLocalObject.isPresent());
		assertEquals(localObject, loadedLocalObject.get());
		CoreModelServiceHolder.get().remove(localObject);
	}

	@Test
	public void createLocalObject() {
		IReminder localObject = setupLocalReminder(Visibility.ALWAYS, ProcessStatus.OPEN, "test create local object",
				"additional info", LocalDate.of(2020, 2, 2));
		Optional<Task> fhirObject = transformer.getFhirObject(localObject);
		assertTrue(fhirObject.isPresent());
		CoreModelServiceHolder.get().remove(localObject);
		Optional<IReminder> createdLocalObject = transformer.createLocalObject(fhirObject.get());
		assertTrue(createdLocalObject.isPresent());
		localObject = createdLocalObject.get();

		assertEquals(Visibility.ALWAYS, localObject.getVisibility());
		assertEquals(ProcessStatus.OPEN, localObject.getStatus());
		assertEquals("test create local object", localObject.getSubject());
		assertEquals("additional info", localObject.getMessage());
		assertEquals(LocalDate.of(2020, 2, 2), localObject.getDue());

		CoreModelServiceHolder.get().remove(localObject);
	}

	@Test
	public void updateLocalObject() {
		IReminder localObject = setupLocalReminder(Visibility.ALWAYS, ProcessStatus.OPEN, "test to fhir object",
				"additional info", LocalDate.of(2020, 2, 2));
		Optional<Task> fhirObject = transformer.getFhirObject(localObject);
		assertTrue(fhirObject.isPresent());

		fhirObject.get().getExecutionPeriod()
				.setEnd(Date.from(LocalDate.of(2030, 3, 3).atStartOfDay(ZoneId.systemDefault()).toInstant()));
		fhirObject.get().setDescription("test update local object");

		transformer.updateLocalObject(fhirObject.get(), localObject);

		assertEquals(Visibility.ALWAYS, localObject.getVisibility());
		assertEquals(ProcessStatus.OPEN, localObject.getStatus());
		assertEquals("test update local object", localObject.getSubject());
		assertEquals("additional info", localObject.getMessage());
		assertEquals(LocalDate.of(2030, 3, 3), localObject.getDue());
	}
}
