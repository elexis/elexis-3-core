package ch.elexis.core.importer.div.tasks.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.importer.div.tasks.BillLabResultOnCreationIdentifiedRunnable;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnable.ReturnParameter;
import ch.elexis.core.model.tasks.IIdentifiedRunnable.RunContextParameter;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.LabServiceHolder;
import ch.elexis.core.tasks.IdentifiedRunnableIdConstants;
import ch.elexis.core.tasks.internal.service.TaskServiceHolder;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.OwnerTaskNotification;
import ch.elexis.core.tasks.model.TaskState;
import ch.elexis.core.tasks.model.TaskTriggerType;
import ch.elexis.core.utils.PlatformHelper;

public class Hl7ImporterTaskIntegrationTest {
	
	static Path tempDirectory;
	
	@BeforeClass
	public static void beforeClass() throws IOException{
		tempDirectory = Files.createTempDirectory("hl7ImporterTest");
		tempDirectory.toFile().deleteOnExit();
		
		Hl7ImporterTaskIntegrationTestUtil.prepareEnvironment();
	}
	
	/**
	 * Test the automatic import of hl7 files. This is realized by chaining tasks. The first task
	 * watches a given directory for changes, and on every file found it starts a subsequent task -
	 * the importer. A third task, bills created LabResults
	 * 
	 * @throws TaskException
	 * @throws IOException
	 */
	@Test
	public void localFilesystemImport() throws TaskException, IOException{
		
		IUser activeUser = ContextServiceHolder.get().getActiveUser().get();
		assertNotNull(activeUser);
		
		ILaboratory laboratory = Hl7ImporterTaskIntegrationTestUtil.configureLabAndLabItemBilling();
		
		ITaskDescriptor watcherTaskDescriptor = initDirectoryWatcherTask(activeUser);
		ITaskDescriptor hl7ImporterTaskDescriptor = initHl7ImporterTask(activeUser);
		ITaskDescriptor billLabResultsTaskDescriptor = initBillLabResultTask(activeUser);
		
		// add a hl7 file with accompanying pdf to the directory
		File src = new File(PlatformHelper.getBasePath("ch.elexis.core.tasks.test"),
			"rsc/5083_LabCube_ABXMicrosEmi_20160217143956_198647.hl7");
		File pdf = new File(PlatformHelper.getBasePath("ch.elexis.core.tasks.test"),
			"rsc/5083_LabCube_ABXMicrosEmi_20160217143956_198647.pdf");
		Files.copy(pdf.toPath(), new File(tempDirectory.toFile(), pdf.getName()).toPath(),
			StandardCopyOption.REPLACE_EXISTING);
		Files.copy(src.toPath(), new File(tempDirectory.toFile(), src.getName()).toPath(),
			StandardCopyOption.REPLACE_EXISTING);
		
		Awaitility.await().atMost(10, TimeUnit.SECONDS)
			.until(() -> TaskServiceHolder.get().findExecutions(watcherTaskDescriptor).size() > 0);
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> TaskServiceHolder.get()
			.findExecutions(watcherTaskDescriptor).get(0).isFinished());
		
		assertEquals(1, TaskServiceHolder.get().findExecutions(hl7ImporterTaskDescriptor).size());
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> TaskServiceHolder.get()
			.findExecutions(hl7ImporterTaskDescriptor).get(0).isFinished());
		
		assertEquals(TaskState.COMPLETED,
			TaskServiceHolder.get().findExecutions(hl7ImporterTaskDescriptor).get(0).getState());
		
		// 18 labResults + 1 pdf
		List<ILabResult> labResults =
			CoreModelServiceHolder.get().getQuery(ILabResult.class).execute();
		assertEquals(19, labResults.size());
		for (ILabResult iLabResult : labResults) {
			assertEquals(laboratory.getId(), iLabResult.getOrigin().getId());
			assertNotNull(iLabResult.getItem());
			Optional<ILabMapping> mapping = LabServiceHolder.get()
				.getLabMappingByContactAndItem(iLabResult.getOrigin(), iLabResult.getItem());
			assertTrue(mapping.isPresent());
		}
		
		assertEquals(1,
			TaskServiceHolder.get().findExecutions(billLabResultsTaskDescriptor).size());
		// It was tried to bill EAL 1371.00 - but this will not succeed as the required
		// code is available in elexis-3-base only, the task was however executed and failed correctly
		ITask billingTask =
			TaskServiceHolder.get().findExecutions(billLabResultsTaskDescriptor).get(0);
		assertEquals(TaskState.FAILED, billingTask.getState());
		String result = (String) billingTask.getResult().get(ReturnParameter.FAILED_TASK_EXCEPTION_MESSAGE);
		assertTrue(result.contains("EAL tarif [1371.00] does not exist"));
		
		// import was successful, files was moved to archive
		File archivedHl7 = new File(tempDirectory.toFile() + "/archive", src.getName());
		File archivedPdf = new File(tempDirectory.toFile() + "/archive", pdf.getName());
		assertTrue(archivedHl7.exists());
		assertTrue(archivedPdf.exists());
		archivedHl7.delete();
		archivedPdf.delete();
		new File(tempDirectory.toFile() + "/archive").delete();
		
		// TODO should file be locked during process by task?
		System.out.println(tempDirectory.toAbsolutePath());
		
		// TODO test fail message
		// TODO partial result?
	}
	
	private ITaskDescriptor initBillLabResultTask(IUser activeUser) throws TaskException{
		IIdentifiedRunnable runnable = TaskServiceHolder.get()
			.instantiateRunnableById(BillLabResultOnCreationIdentifiedRunnable.RUNNABLE_ID);
		assertNotNull(runnable);
		
		ITaskDescriptor taskDescriptor =
			TaskServiceHolder.get().createTaskDescriptor(activeUser, runnable);
		taskDescriptor.setTriggerType(TaskTriggerType.SYSTEM_EVENT);
		taskDescriptor.setTriggerParameter("topic", ElexisEventTopics.PERSISTENCE_EVENT_CREATE);
		taskDescriptor.setTriggerParameter(ElexisEventTopics.PROPKEY_CLASS,
			"ch.elexis.data.LabResult");
		taskDescriptor.setTriggerParameter("origin", "self");
		TaskServiceHolder.get().setActive(taskDescriptor, true);
		
		return taskDescriptor;
	}
	
	private ITaskDescriptor initHl7ImporterTask(IUser activeUser) throws TaskException{
		// create hl7importer taskdescriptor
		IIdentifiedRunnable hl7ImporterRunnable =
			TaskServiceHolder.get().instantiateRunnableById("hl7importer");
		assertNotNull(hl7ImporterRunnable);
		
		ITaskDescriptor hl7ImporterTaskDescriptor =
			TaskServiceHolder.get().createTaskDescriptor(activeUser, hl7ImporterRunnable);
		hl7ImporterTaskDescriptor.setSingleton(true);
		hl7ImporterTaskDescriptor.setTriggerType(TaskTriggerType.OTHER_TASK);
		hl7ImporterTaskDescriptor.setReferenceId("hl7Importer_a");
		hl7ImporterTaskDescriptor.setOwnerNotification(OwnerTaskNotification.WHEN_FINISHED_FAILED);
		TaskServiceHolder.get().setActive(hl7ImporterTaskDescriptor, true);
		return hl7ImporterTaskDescriptor;
	}
	
	private ITaskDescriptor initDirectoryWatcherTask(IUser activeUser) throws TaskException{
		// create watcher taskdescriptor
		IIdentifiedRunnable watcherRunnable = TaskServiceHolder.get()
			.instantiateRunnableById(IdentifiedRunnableIdConstants.TRIGGER_TASK_FOR_EVERY_FILE);
		assertNotNull(watcherRunnable);
		Map<String, Serializable> watcherRunContext = watcherRunnable.getDefaultRunContext();
		String url = tempDirectory.toString();
		watcherRunContext.put(RunContextParameter.STRING_URL, url);
		watcherRunContext.put(RunContextParameter.TASK_DESCRIPTOR_REFID, "hl7Importer_a");
		watcherRunContext.put("fileExtensionFilter", "hl7");
		
		ITaskDescriptor watcherTaskDescriptor =
			TaskServiceHolder.get().createTaskDescriptor(activeUser, watcherRunnable);
		watcherTaskDescriptor.setTriggerType(TaskTriggerType.CRON);
		watcherTaskDescriptor.setReferenceId("watch_hl7_files");
		watcherTaskDescriptor.setSingleton(true);
		watcherTaskDescriptor.setTriggerParameter("cron", "0/5 * * * * ?");
		watcherTaskDescriptor.setRunContext(watcherRunContext);
		TaskServiceHolder.get().setActive(watcherTaskDescriptor, true);
		
		return watcherTaskDescriptor;
	}
}
