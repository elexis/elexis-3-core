package ch.elexis.core.tasks.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.RunnableWithContextIdConstants;
import ch.elexis.core.tasks.TaskTriggerTypeParameterConstants;
import ch.elexis.core.tasks.internal.model.service.CoreModelServiceHolder;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskState;
import ch.elexis.core.tasks.model.TaskTriggerType;
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
	
	public TaskServiceTest(){
		taskService = OsgiServiceUtil.getService(ITaskService.class).get();
		IPerson contact = new IContactBuilder.PersonBuilder(CoreModelServiceHolder.get(), "first",
			"last", LocalDate.now(), Gender.MALE).buildAndSave();
		owner = new IUserBuilder(CoreModelServiceHolder.get(), "testUser", contact).buildAndSave();
	}
	
	@BeforeClass
	public static void beforeClass() throws IOException{
		tempDirectory = Files.createTempDirectory("taskServiceTest");
		tempDirectory.toFile().deleteOnExit();
	}
	
	@Before
	public void before() throws TaskException{
		rwcLogContext = taskService
			.instantiateRunnableById(RunnableWithContextIdConstants.RUNNABLE_ID_LOGRESULTCONTEXT);
	}
	
	private Callable<Boolean> taskDone(ITask task){
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception{
				return (TaskState.FAILED == task.getState())
					|| (TaskState.COMPLETED == task.getState());
			}
		};
	}
	
	/**
	 * Use-Case: manual execution, db check, repair, import
	 * 
	 * @throws Exception
	 */
	@Test
	public void triggerManual_HelloWorld() throws Exception{
		taskDescriptor = taskService.createTaskDescriptor(owner, rwcLogContext);
		taskDescriptor.setReferenceId("manual_helloWorld");
		taskDescriptor.setRunContextParameter("testKey", "testValue");
		taskService.setActive(taskDescriptor, true);
		
		ITask task =
			taskService.trigger(taskDescriptor, progressMonitor, TaskTriggerType.MANUAL, null);
		
		Awaitility.await().atMost(2, TimeUnit.SECONDS).until(taskDone(task));
		assertEquals(TaskState.COMPLETED, task.getState());
	}
	
	@Test
	public void triggerManual_Misthios() throws Exception{
		IIdentifiedRunnable rwcMisthios = taskService.instantiateRunnableById("misthios");
		taskDescriptor = taskService.createTaskDescriptor(owner, rwcMisthios);
		taskDescriptor.setReferenceId("manual_helloWorld_misthios");
		Map<String, Serializable> context = new HashMap<>();
		context.put("bundle_url",
			"https://gitlab.medelexis.ch/mdescher/elexis-misthios/raw/master/sample-misthios-bundle/");
		taskDescriptor.setRunContext(context);
		taskService.setActive(taskDescriptor, true);
		
		ITask task =
			taskService.trigger(taskDescriptor, progressMonitor, TaskTriggerType.MANUAL, null);
		
		Awaitility.await().atMost(2, TimeUnit.SECONDS).until(taskDone(task));
		assertEquals(TaskState.COMPLETED, task.getState());
	}
	
	/**
	 * Use-Case: HL7 Import on incoming HL7 file ATTENTION: On OS X picking up changes in the FS can
	 * take up to 10 seconds
	 * 
	 * @throws TaskException
	 * @throws IOException
	 * @throws InterruptedException
	 * @see https://www.reddit.com/r/java/comments/3vtv8i/beware_javaniofilewatchservice_is_subtly_broken/
	 */
	@Test
	public void triggerFSChange() throws TaskException, IOException, InterruptedException{
		IIdentifiedRunnable rwcDeleteFile = taskService
			.instantiateRunnableById(RunnableWithContextIdConstants.RUNNABLE_ID_DELETEFILE);
		taskDescriptor = taskService.createTaskDescriptor(owner, rwcDeleteFile);
		taskDescriptor.setRunContext(runContext);
		taskDescriptor.setTriggerType(TaskTriggerType.FILESYSTEM_CHANGE);
		taskDescriptor.setTriggerParameter(
			TaskTriggerTypeParameterConstants.FILESYSTEM_CHANGE_PARAM_DIRECTORY_PATH,
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
	 * Use-Case: Search for reminders on this patient
	 * 
	 * @throws TaskException
	 */
	public void triggerSysEvent_PatientChange() throws TaskException{
		taskDescriptor = taskService.createTaskDescriptor(owner, rwcLogContext);
		taskDescriptor.setTriggerType(TaskTriggerType.SYSTEM_EVENT);
		taskDescriptor.setTriggerParameter("eventClass", "ch.elexis.data.Patient");
		taskDescriptor.setTriggerParameter("eventType", "EVENT_SELECTED");
		taskService.setActive(taskDescriptor, true);
		
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Use-Case: In CCM -> check for patient reminders
	 * 
	 * @throws TaskException
	 */
	public void triggerTime() throws TaskException{
		taskDescriptor = taskService.createTaskDescriptor(owner, rwcLogContext);
		taskDescriptor.setTriggerType(TaskTriggerType.CRON);
		taskDescriptor.setTriggerParameter("crontab", "* /5 * * * *");
		taskService.setActive(taskDescriptor, true);
		
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Use-Case: Job Workflow
	 * 
	 * @throws TaskException
	 */
	public void triggerJobState() throws TaskException{
		taskDescriptor = taskService.createTaskDescriptor(owner, rwcLogContext);
		taskDescriptor.setTriggerType(TaskTriggerType.OTHER_TASK);
		taskDescriptor.setTriggerParameter("referenceId", "otherTaskReferenceId");
		taskDescriptor.setTriggerParameter("taskState", "COMPLETED");
		taskService.setActive(taskDescriptor, true);
		
		throw new UnsupportedOperationException();
	}
	
}
