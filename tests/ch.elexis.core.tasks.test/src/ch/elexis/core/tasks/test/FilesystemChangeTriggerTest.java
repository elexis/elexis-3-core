package ch.elexis.core.tasks.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.IdentifiedRunnableIdConstants;
import ch.elexis.core.tasks.internal.service.TaskServiceHolder;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskState;
import ch.elexis.core.tasks.model.TaskTriggerType;
import ch.elexis.core.tasks.model.TaskTriggerTypeParameter;
import ch.elexis.core.tasks.test.runnable.TestExecutionContextRunnable;

public class FilesystemChangeTriggerTest {

	private ITaskService taskService;
	private ITaskDescriptor taskDescriptor;

	static Path tempDirectory;

	@BeforeClass
	public static void beforeClass() throws IOException {
		tempDirectory = Files.createTempDirectory("FilesystemChangeTriggerTest");
		tempDirectory.toFile().deleteOnExit();
	}

	@Before
	public void before() {
		taskService = TaskServiceHolder.get();
	}

	@Test
	public void onTemporaryDirectory() throws TaskException, IOException, InterruptedException {
		IIdentifiedRunnable rwcDeleteFile = TaskServiceHolder.get()
				.instantiateRunnableById(IdentifiedRunnableIdConstants.DELETEFILE);
		taskDescriptor = taskService.createTaskDescriptor(rwcDeleteFile);
		taskDescriptor.setReferenceId("onTemporaryDirectory");
		taskDescriptor.setOwner(AllTests.getOwner());
		taskDescriptor.setRunContext(new HashMap<>());
		taskDescriptor.setTriggerType(TaskTriggerType.FILESYSTEM_CHANGE);
		taskDescriptor.setTriggerParameter(IIdentifiedRunnable.RunContextParameter.STRING_URL,
				tempDirectory.toString());
		System.out.println(tempDirectory.toString());
		taskDescriptor.setTriggerParameter(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.FILE_EXTENSION_FILTER, "txt");
		taskService.saveTaskDescriptor(taskDescriptor);

		assertEquals(0, AllTests.getTaskServiceTestUtil().getTasks(taskDescriptor).size());

		taskService.setActive(taskDescriptor, true);
		assertTrue(taskService.getIncurredTasks().contains(taskDescriptor));

		Path createFile = Files.createTempFile(tempDirectory, "test", ".txt");
		System.out.println(LocalDateTime.now() + " created " + createFile.toString());

		Callable<Boolean> c = () -> !createFile.toFile().exists();
		Awaitility.await().atMost(30, TimeUnit.SECONDS).until(c);

		List<ITask> result = AllTests.getTaskServiceTestUtil().getTasks(taskDescriptor);
		assertEquals(1, result.size());
		assertEquals(TaskState.COMPLETED, result.get(0).getState());

		taskService.setActive(taskDescriptor, false);
		assertFalse(taskService.getIncurredTasks().contains(taskDescriptor));
	}

	@Test(expected = TaskException.class)
	public void onTemporaryFile() throws TaskException, IOException, InterruptedException {

		Path createFile = Files.createTempFile(tempDirectory, "test", ".pdf");

		IIdentifiedRunnable rwcDeleteFile = TaskServiceHolder.get()
				.instantiateRunnableById(TestExecutionContextRunnable.ID);
		taskDescriptor = taskService.createTaskDescriptor(rwcDeleteFile);
		taskDescriptor.setReferenceId("onTemporaryFile");
		taskDescriptor.setOwner(AllTests.getOwner());
		taskDescriptor.setRunContext(new HashMap<>());
		taskDescriptor.setTriggerType(TaskTriggerType.FILESYSTEM_CHANGE);
		taskDescriptor.setTriggerParameter(IIdentifiedRunnable.RunContextParameter.STRING_URL,
				createFile.toString());
		System.out.println(createFile.toString());
		taskDescriptor.setTriggerParameter(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.FILE_EXTENSION_FILTER, "pdf");
		taskService.saveTaskDescriptor(taskDescriptor);

		assertEquals(0, AllTests.getTaskServiceTestUtil().getTasks(taskDescriptor).size());

		taskService.setActive(taskDescriptor, true);
	}

	@Test
	public void handleActivateDeactivate() {

	}

}
