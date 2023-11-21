package ch.elexis.core.tasks.test;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.internal.service.TaskServiceHolder;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;

public class QuarantinedTests {

	private ITaskService taskService;
	private ITaskDescriptor taskDescriptor;

	@Before
	public void before() {
		taskService = TaskServiceHolder.get();
	}

	/**
	 * Use-Case: manual execution, db check, repair, import
	 *
	 * @throws Exception
	 */
	@Ignore("TODO fix test case")
	@Test
	public void triggerManual_HelloWorld() throws Exception {
//		taskDescriptor = taskService.createTaskDescriptor(rwcLogContext);
//		taskDescriptor.setOwner(owner);
//		taskDescriptor.setReferenceId("manual_helloWorld");
//		taskDescriptor.setRunContextParameter("testKey", "testValue");
//		taskService.setActive(taskDescriptor, true);
//
//		Optional<ITask> findExecutions = taskService.findLatestExecution(taskDescriptor);
//		assertTrue(findExecutions.isPresent());
//
//		ITask task = taskService.trigger(taskDescriptor, progressMonitor, TaskTriggerType.MANUAL, null);
//
//		Awaitility.await().atMost(2, TimeUnit.SECONDS).until(taskDone(task));
//		assertEquals(TaskState.COMPLETED, task.getState());
//
//		findExecutions = taskService.findLatestExecution(taskDescriptor);
//		assertTrue(findExecutions.isPresent());
//		assertEquals(TaskState.COMPLETED, findExecutions.get().getState());
//		assertTrue(findExecutions.get().getResult().containsKey("runnableExecDuration"));
	}

	@Ignore("TODO fix test case")
	@Test
	public void triggerManual_Misthios() throws Exception {
//		IIdentifiedRunnable rwcMisthios = taskService.instantiateRunnableById("misthios");
//		taskDescriptor = taskService.createTaskDescriptor(rwcMisthios);
//		taskDescriptor.setOwner(owner);
//		taskDescriptor.setReferenceId("manual_helloWorld_misthios");
//		Map<String, Serializable> context = new HashMap<>();
//		context.put("bundle_url",
//				"https://gitlab.medelexis.ch/mdescher/elexis-misthios/raw/master/sample-misthios-bundle/");
//		taskDescriptor.setRunContext(context);
//		taskService.setActive(taskDescriptor, true);
//
//		ITask task = taskService.trigger(taskDescriptor, progressMonitor, TaskTriggerType.MANUAL, null);
//
//		Awaitility.await().atMost(2, TimeUnit.SECONDS).until(taskDone(task));
//		assertEquals(TaskState.COMPLETED, task.getState());
	}

	@Test
	@Ignore
	public void triggerFilesystemChangeOnlyOnceOnUnchangedFile()
			throws TaskException, IOException, InterruptedException {
//		IIdentifiedRunnable rwcLogResultContext = taskService
//				.instantiateRunnableById(IdentifiedRunnableIdConstants.LOGRESULTCONTEXT);
//		taskDescriptor = taskService.createTaskDescriptor(rwcLogResultContext);
//		taskDescriptor.setOwner(owner);
//		taskDescriptor.setRunContext(runContext);
//		taskDescriptor.setTriggerType(TaskTriggerType.FILESYSTEM_CHANGE);
//		taskDescriptor.setTriggerParameter(IIdentifiedRunnable.RunContextParameter.STRING_URL,
//				tempDirectory.toString());
//		taskDescriptor.setTriggerParameter(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.FILE_EXTENSION_FILTER, "txt");
//		taskService.saveTaskDescriptor(taskDescriptor);
//		taskService.setActive(taskDescriptor, true);
//
//		Path createFile = Files.createTempFile(tempDirectory, "test", ".txt");
//		System.out.println(LocalDateTime.now() + " created " + createFile.toString());
//
//		Thread.sleep(3000);
//
//		IQuery<ITask> query = getTaskModelService().getQuery(ITask.class);
//		query.and(ModelPackage.Literals.ITASK__TASK_DESCRIPTOR, COMPARATOR.EQUALS, taskDescriptor);
//		List<ITask> execute = query.execute();
//		assertEquals(1, execute.size());
	}

	/**
	 * Use-Case: Search for reminders on this patient
	 *
	 * @throws TaskException
	 */
	@Ignore("TODO fix test case")
	@Test
	public void triggerSysEvent_PatientChange() throws TaskException {
//		taskDescriptor = taskService.createTaskDescriptor(rwcLogContext);
//		taskDescriptor.setOwner(owner);
//		taskDescriptor.setTriggerType(TaskTriggerType.SYSTEM_EVENT);
//		taskDescriptor.setTriggerParameter("eventClass", "ch.elexis.data.Patient");
//		taskDescriptor.setTriggerParameter("eventType", "EVENT_SELECTED");
//		taskService.setActive(taskDescriptor, true);
//
//		throw new UnsupportedOperationException();
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
//		taskDescriptor = taskService.createTaskDescriptor(rwcLogContext);
//		taskDescriptor.setOwner(owner);
//		taskDescriptor.setTriggerType(TaskTriggerType.OTHER_TASK);
//		taskDescriptor.setTriggerParameter("referenceId", "otherTaskReferenceId");
//		taskDescriptor.setTriggerParameter("taskState", "COMPLETED");
//		taskService.setActive(taskDescriptor, true);
//
//		throw new UnsupportedOperationException();
	}

}
