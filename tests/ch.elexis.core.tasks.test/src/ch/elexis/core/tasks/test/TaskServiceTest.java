package ch.elexis.core.tasks.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnable.ReturnParameter;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.tasks.IdentifiedRunnableIdConstants;
import ch.elexis.core.tasks.internal.model.service.CoreModelServiceHolder;
import ch.elexis.core.tasks.internal.runnables.LogResultContextIdentifiedRunnable;
import ch.elexis.core.tasks.internal.service.TaskServiceHolder;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskState;
import ch.elexis.core.tasks.model.TaskTriggerType;
import ch.elexis.core.tasks.test.runnable.TestExecutionContextRunnable;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class TaskServiceTest {

	ITaskService taskService;
	IIdentifiedRunnable rwcLogContext;
	IUser owner;
	ITaskDescriptor taskDescriptor;
	IProgressMonitor progressMonitor;
	Map<String, Serializable> runContext = new HashMap<>();

	static Path tempDirectory;

	public TaskServiceTest() {
		taskService = TaskServiceHolder.get();

		IPerson contact = new IContactBuilder.PersonBuilder(CoreModelServiceHolder.get(), "first", "last",
				LocalDate.now(), Gender.MALE).mandator().buildAndSave();
		owner = new IUserBuilder(CoreModelServiceHolder.get(), "testUser", contact).buildAndSave();
	}

	@BeforeClass
	public static void beforeClass() throws IOException {
		tempDirectory = Files.createTempDirectory("taskServiceTest");
		tempDirectory.toFile().deleteOnExit();
	}

	@Before
	public void before() throws TaskException {
		rwcLogContext = taskService.instantiateRunnableById(IdentifiedRunnableIdConstants.LOGRESULTCONTEXT);
		assertTrue(rwcLogContext instanceof LogResultContextIdentifiedRunnable);
	}

	private IModelService getTaskModelService() {
		return OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.tasks.model)")
				.orElse(null);
	}

	private ITaskDescriptor taskDescriptorOf(String id) throws TaskException {
		IIdentifiedRunnable testExecContextRunnable = taskService.instantiateRunnableById(id);
		if (TestExecutionContextRunnable.ID.equals(id)) {
			assertTrue(testExecContextRunnable instanceof TestExecutionContextRunnable);
		}
		return taskService.createTaskDescriptor(testExecContextRunnable);
	}

	private Callable<Boolean> taskDone(ITask task) {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return task.isFinished();
			}
		};
	}

	// Check the context the runnable is executed in
	@Test
	public void runnableExecutionContext() throws TaskException {
		taskDescriptor = taskDescriptorOf(TestExecutionContextRunnable.ID);

		taskDescriptor.setSingleton(true);
		taskDescriptor.setOwner(owner);
		taskService.setActive(taskDescriptor, true);

		ITask task = taskService.trigger(taskDescriptor, progressMonitor, TaskTriggerType.MANUAL, null);
		Awaitility.await().atMost(2, TimeUnit.SECONDS).until(taskDone(task));
		assertEquals(TaskState.COMPLETED, task.getState());
	}

	// Check COMPLETED_WARN manual resolve
	@Test
	public void completedWarnWithManualResolution() throws TaskException {
		taskDescriptor = taskDescriptorOf(TestExecutionContextRunnable.ID);

		taskDescriptor.setSingleton(true);
		taskDescriptor.setOwner(owner);
		taskDescriptor.setRunContext(Collections.singletonMap(ReturnParameter.MARKER_WARN, Boolean.TRUE));
		taskService.setActive(taskDescriptor, true);
		ITask task = taskService.trigger(taskDescriptor, progressMonitor, TaskTriggerType.MANUAL, null);
		Awaitility.await().atMost(2, TimeUnit.SECONDS).until(taskDone(task));
		assertEquals(TaskState.COMPLETED_WARN, task.getState());

		task.setStateCompletedManual("test");
		assertEquals(TaskState.COMPLETED_MANUAL, task.getState());
		String manualMessage = (String) task.getResult().get(TaskState.COMPLETED_MANUAL.name());
		assertTrue(manualMessage.endsWith(":test"));
	}

	@Test(expected = TaskException.class)
	public void reloadOnTaskDescriptorChangeDeleted() throws TaskException {

		taskDescriptor = taskDescriptorOf(TestExecutionContextRunnable.ID);
		taskDescriptor.setSingleton(true);
		taskDescriptor.setOwner(owner);
		taskService.setActive(taskDescriptor, true);
		taskService.saveTaskDescriptor(taskDescriptor);

		// side-lined modification of the taskdescriptor
		String SQL = "UPDATE TASKDESCRIPTOR SET DELETED = '1', LASTUPDATE='" + System.currentTimeMillis()
				+ "' WHERE (ID='" + taskDescriptor.getId() + "')";
		int executeNativeUpdate = getTaskModelService().executeNativeUpdate(SQL);
		assertEquals(1, executeNativeUpdate);

		taskService.trigger(taskDescriptor, progressMonitor, TaskTriggerType.MANUAL, null);
	}

	/**
	 * Use-Case: manual execution, db check, repair, import
	 *
	 * @throws Exception
	 */
	@Ignore("TODO fix test case")
	@Test
	public void triggerManual_HelloWorld() throws Exception {
		taskDescriptor = taskService.createTaskDescriptor(rwcLogContext);
		taskDescriptor.setOwner(owner);
		taskDescriptor.setReferenceId("manual_helloWorld");
		taskDescriptor.setRunContextParameter("testKey", "testValue");
		taskService.setActive(taskDescriptor, true);

		Optional<ITask> findExecutions = taskService.findLatestExecution(taskDescriptor);
		assertTrue(findExecutions.isPresent());

		ITask task = taskService.trigger(taskDescriptor, progressMonitor, TaskTriggerType.MANUAL, null);

		Awaitility.await().atMost(2, TimeUnit.SECONDS).until(taskDone(task));
		assertEquals(TaskState.COMPLETED, task.getState());

		findExecutions = taskService.findLatestExecution(taskDescriptor);
		assertTrue(findExecutions.isPresent());
		assertEquals(TaskState.COMPLETED, findExecutions.get().getState());
		assertTrue(findExecutions.get().getResult().containsKey("runnableExecDuration"));
	}

	@Ignore("TODO fix test case")
	@Test
	public void triggerManual_Misthios() throws Exception {
		IIdentifiedRunnable rwcMisthios = taskService.instantiateRunnableById("misthios");
		taskDescriptor = taskService.createTaskDescriptor(rwcMisthios);
		taskDescriptor.setOwner(owner);
		taskDescriptor.setReferenceId("manual_helloWorld_misthios");
		Map<String, Serializable> context = new HashMap<>();
		context.put("bundle_url",
				"https://gitlab.medelexis.ch/mdescher/elexis-misthios/raw/master/sample-misthios-bundle/");
		taskDescriptor.setRunContext(context);
		taskService.setActive(taskDescriptor, true);

		ITask task = taskService.trigger(taskDescriptor, progressMonitor, TaskTriggerType.MANUAL, null);

		Awaitility.await().atMost(2, TimeUnit.SECONDS).until(taskDone(task));
		assertEquals(TaskState.COMPLETED, task.getState());
	}

	/**
	 * Use-Case: HL7 Import on incoming HL7 file ATTENTION: On OS X picking up
	 * changes in the FS can take up to 10 seconds
	 *
	 * @throws TaskException
	 * @throws IOException
	 * @throws InterruptedException
	 * @see https://www.reddit.com/r/java/comments/3vtv8i/beware_javaniofilewatchservice_is_subtly_broken/
	 */
	@Test
	@Ignore
	public void triggerFSChange() throws TaskException, IOException, InterruptedException {
		IIdentifiedRunnable rwcDeleteFile = taskService
				.instantiateRunnableById(IdentifiedRunnableIdConstants.DELETEFILE);
		taskDescriptor = taskService.createTaskDescriptor(rwcDeleteFile);
		taskDescriptor.setOwner(owner);
		taskDescriptor.setRunContext(runContext);
		taskDescriptor.setTriggerType(TaskTriggerType.FILESYSTEM_CHANGE);
		taskDescriptor.setTriggerParameter(IIdentifiedRunnable.RunContextParameter.STRING_URL,
				tempDirectory.toString());
		taskService.setActive(taskDescriptor, true);

		Path createFile = Files.createTempFile(tempDirectory, "test", "txt");
		System.out.println(LocalDateTime.now() + " created " + createFile.toString());

		Callable<Boolean> c = () -> {
			return !createFile.toFile().exists();
		};
		Awaitility.await().atMost(15, TimeUnit.SECONDS).until(c);
	}

	/**
	 * Use-Case: In CCM -> check for patient reminders
	 *
	 * @throws TaskException
	 * @throws IOException
	 */
	@Test
	public void triggerCron() throws TaskException, IOException {

		IIdentifiedRunnable rwcDeleteFile = taskService
				.instantiateRunnableById(IdentifiedRunnableIdConstants.DELETEFILE);
		Path createFile = Files.createTempFile(tempDirectory, "test", "txt");
		createFile.toFile().deleteOnExit();

		taskDescriptor = taskService.createTaskDescriptor(rwcDeleteFile);
		taskDescriptor.setOwner(owner);
		taskDescriptor.setTriggerType(TaskTriggerType.CRON);
		taskDescriptor.setRunContextParameter(IIdentifiedRunnable.RunContextParameter.STRING_URL,
				createFile.toString());
		// job will run every 5 seconds
		taskDescriptor.setTriggerParameter("cron", "0/5 * * * * ?");
		taskService.setActive(taskDescriptor, true);

		Callable<Boolean> c = () -> {
			return !createFile.toFile().exists();
		};

		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(c);
		Optional<ITask> execution = taskService.findLatestExecution(taskDescriptor);
		assertEquals(TaskState.COMPLETED, execution.get().getState());
	}

	/**
	 * Use-Case: Search for reminders on this patient
	 *
	 * @throws TaskException
	 */
	@Ignore("TODO fix test case")
	@Test
	public void triggerSysEvent_PatientChange() throws TaskException {
		taskDescriptor = taskService.createTaskDescriptor(rwcLogContext);
		taskDescriptor.setOwner(owner);
		taskDescriptor.setTriggerType(TaskTriggerType.SYSTEM_EVENT);
		taskDescriptor.setTriggerParameter("eventClass", "ch.elexis.data.Patient");
		taskDescriptor.setTriggerParameter("eventType", "EVENT_SELECTED");
		taskService.setActive(taskDescriptor, true);

		throw new UnsupportedOperationException();
	}

	/**
	 * Use-Case: Bill a labresult on an existing encounter when result was created
	 *
	 * @see at.medevit.elexis.roche.labor.billing.AddLabToKons
	 *
	 * @throws TaskException
	 * @throws InterruptedException
	 */
	@Test
	public void triggerSysEvent_LabItemCreate() throws TaskException, InterruptedException {
		taskDescriptor = taskService.createTaskDescriptor(rwcLogContext);
		taskDescriptor.setOwner(owner);
		taskDescriptor.setTriggerType(TaskTriggerType.SYSTEM_EVENT);
		taskDescriptor.setTriggerParameter("topic", ElexisEventTopics.PERSISTENCE_EVENT_CREATE);
		taskDescriptor.setTriggerParameter(ElexisEventTopics.PROPKEY_CLASS, "ch.elexis.data.Patient");
		taskDescriptor.setTriggerParameter("origin", "self");
		taskService.setActive(taskDescriptor, true);

		assertEquals(false, taskService.findLatestExecution(taskDescriptor).isPresent());

		Thread.sleep(1000);

		new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), "Mister", "Rigoletti", LocalDate.now(),
				Gender.MALE).buildAndSave();

		Thread.sleep(1500);

		assertEquals(true, taskService.findLatestExecution(taskDescriptor).isPresent());

	}

	/**
	 * Use-Case: Job Workflow, passive trigger by another task reaching a specific
	 * state
	 *
	 * @throws TaskException
	 */
	@Ignore("TODO fix test case")
	@Test
	public void triggerJobState() throws TaskException {
		taskDescriptor = taskService.createTaskDescriptor(rwcLogContext);
		taskDescriptor.setOwner(owner);
		taskDescriptor.setTriggerType(TaskTriggerType.OTHER_TASK);
		taskDescriptor.setTriggerParameter("referenceId", "otherTaskReferenceId");
		taskDescriptor.setTriggerParameter("taskState", "COMPLETED");
		taskService.setActive(taskDescriptor, true);

		throw new UnsupportedOperationException();
	}

}
