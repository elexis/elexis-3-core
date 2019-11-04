package ch.elexis.core.importer.div.tasks.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
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
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
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
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.core.utils.PlatformHelper;

public class Hl7ImporterTaskIntegrationTest {
	
	private IVirtualFilesystemService vfs =
		OsgiServiceUtil.getService(IVirtualFilesystemService.class).get();
	
	static final String PREFIX_AUTH_WIN2KSRV =
		"smb://unittest:Unit_Test_17@win2k12srv.medelexis.ch/smb_for_unittests/";
	static final String SMB_TEST_DIRECTORY = PREFIX_AUTH_WIN2KSRV + "hl7ImportTest/";
	
	@Test
	public void executionOnLocalFilesystem() throws Exception{
		
		// SMB Share
		//		IVirtualFilesystemHandle tempDirectoryVfs = vfs.of(smbTempDirectory);
		//
		
		// Local FS
		Path tempDirectory = Files.createTempDirectory("hl7ImporterTest");
		tempDirectory.toFile().deleteOnExit();
		IVirtualFilesystemHandle tempDirectoryVfs = vfs.of(tempDirectory.toFile());
		//
		
		tempDirectoryVfs.mkdir();
		
		final IVirtualFilesystemHandle hl7 =
			vfs.of(new File(PlatformHelper.getBasePath("ch.elexis.core.tasks.test"),
				"rsc/5083_LabCube_ABXMicrosEmi_20160217143956_198647.hl7"));
		final IVirtualFilesystemHandle pdf =
			vfs.of(new File(PlatformHelper.getBasePath("ch.elexis.core.tasks.test"),
				"rsc/5083_LabCube_ABXMicrosEmi_20160217143956_198647.pdf"));
		final IVirtualFilesystemHandle hl7Target = tempDirectoryVfs.subFile(hl7.getName());
		final IVirtualFilesystemHandle pdfTarget = tempDirectoryVfs.subFile(pdf.getName());
		final IVirtualFilesystemHandle archiveDir = tempDirectoryVfs.subDir("archive").mkdir();
		final IVirtualFilesystemHandle hl7Archived = archiveDir.subFile(hl7.getName());
		final IVirtualFilesystemHandle pdfArchived = archiveDir.subFile(pdf.getName());
		
		Hl7ImporterTaskIntegrationTestUtil.prepareEnvironment();
		
		Callable<Void> pushFiles = () -> {
			pdf.copyTo(pdfTarget);
			hl7.copyTo(hl7Target);
			return null;
		};
		
		localFilesystemImport(tempDirectoryVfs.toString(), pushFiles);
		
		// import was successful, files was moved to archive
		System.out.println(tempDirectoryVfs.getAbsolutePath());
		assertTrue(hl7Archived.exists());
		assertTrue(pdfArchived.exists());
		hl7Archived.delete();
		pdfArchived.delete();
		archiveDir.delete();
		tempDirectoryVfs.delete();
		//		new File(tempDirectory.toFile() + "/archive").delete();
	}
	
	/**
	 * Test the automatic import of hl7 files. This is realized by chaining tasks. The first task
	 * watches a given directory for changes, and on every file found it starts a subsequent task -
	 * the importer. A third task, bills created LabResults
	 * 
	 * @param pushFiles
	 * @param url
	 * @throws Exception
	 */
	
	public void localFilesystemImport(String urlString, Callable<Void> pushFiles) throws Exception{
		
		IUser activeUser = ContextServiceHolder.get().getActiveUser().get();
		assertNotNull(activeUser);
		
		ILaboratory laboratory = Hl7ImporterTaskIntegrationTestUtil.configureLabAndLabItemBilling();
		
		ITaskDescriptor watcherTaskDescriptor = initDirectoryWatcherTask(activeUser, urlString);
		ITaskDescriptor hl7ImporterTaskDescriptor = initHl7ImporterTask(activeUser);
		ITaskDescriptor billLabResultsTaskDescriptor = initBillLabResultTask(activeUser);
		
		pushFiles.call();
		
		// add a hl7 file with accompanying pdf to the directory
		
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(
			() -> TaskServiceHolder.get().findLatestExecution(watcherTaskDescriptor).isPresent());
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> TaskServiceHolder.get()
			.findLatestExecution(watcherTaskDescriptor).get().isFinished());
		
		assertEquals(true,
			TaskServiceHolder.get().findLatestExecution(hl7ImporterTaskDescriptor).isPresent());
		Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> TaskServiceHolder.get()
			.findLatestExecution(hl7ImporterTaskDescriptor).get().isFinished());
		
		assertEquals(TaskState.COMPLETED, TaskServiceHolder.get()
			.findLatestExecution(hl7ImporterTaskDescriptor).get().getState());
		
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
		
		assertEquals(true,
			TaskServiceHolder.get().findLatestExecution(billLabResultsTaskDescriptor).isPresent());
		// It was tried to bill EAL 1371.00 - but this will not succeed as the required
		// code is available in elexis-3-base only, the task was however executed and
		// failed correctly
		ITask billingTask =
			TaskServiceHolder.get().findLatestExecution(billLabResultsTaskDescriptor).get();
		assertEquals(TaskState.FAILED, billingTask.getState());
		String result =
			(String) billingTask.getResult().get(ReturnParameter.FAILED_TASK_EXCEPTION_MESSAGE);
		assertTrue(result.contains("EAL tarif [1371.00] does not exist"));
		
		// TODO test fail message
		// TODO partial result?
		
		TaskServiceHolder.get().removeTaskDescriptor(watcherTaskDescriptor);
		TaskServiceHolder.get().removeTaskDescriptor(billLabResultsTaskDescriptor);
		TaskServiceHolder.get().removeTaskDescriptor(hl7ImporterTaskDescriptor);
	}
	
	private ITaskDescriptor initBillLabResultTask(IUser activeUser) throws TaskException{
		IIdentifiedRunnable runnable = TaskServiceHolder.get()
			.instantiateRunnableById(BillLabResultOnCreationIdentifiedRunnable.RUNNABLE_ID);
		assertNotNull(runnable);
		
		ITaskDescriptor taskDescriptor = TaskServiceHolder.get().createTaskDescriptor(runnable);
		taskDescriptor.setOwner(activeUser);
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
			TaskServiceHolder.get().createTaskDescriptor(hl7ImporterRunnable);
		hl7ImporterTaskDescriptor.setOwner(activeUser);
		hl7ImporterTaskDescriptor.setSingleton(true);
		hl7ImporterTaskDescriptor.setTriggerType(TaskTriggerType.OTHER_TASK);
		hl7ImporterTaskDescriptor.setReferenceId("hl7Importer_a");
		hl7ImporterTaskDescriptor.setOwnerNotification(OwnerTaskNotification.WHEN_FINISHED_FAILED);
		TaskServiceHolder.get().setActive(hl7ImporterTaskDescriptor, true);
		return hl7ImporterTaskDescriptor;
	}
	
	private ITaskDescriptor initDirectoryWatcherTask(IUser activeUser, String url)
		throws TaskException{
		// create watcher taskdescriptor
		IIdentifiedRunnable watcherRunnable = TaskServiceHolder.get()
			.instantiateRunnableById(IdentifiedRunnableIdConstants.TRIGGER_TASK_FOR_EVERY_FILE);
		assertNotNull(watcherRunnable);
		Map<String, Serializable> watcherRunContext = watcherRunnable.getDefaultRunContext();
		watcherRunContext.put(RunContextParameter.STRING_URL, url);
		watcherRunContext.put(RunContextParameter.TASK_DESCRIPTOR_REFID, "hl7Importer_a");
		watcherRunContext.put("fileExtensionFilter", "hl7");
		
		ITaskDescriptor watcherTaskDescriptor =
			TaskServiceHolder.get().createTaskDescriptor(watcherRunnable);
		watcherTaskDescriptor.setOwner(activeUser);
		watcherTaskDescriptor.setTriggerType(TaskTriggerType.CRON);
		watcherTaskDescriptor.setReferenceId("watch_hl7_files");
		watcherTaskDescriptor.setSingleton(true);
		watcherTaskDescriptor.setTriggerParameter("cron", "0/5 * * * * ?");
		watcherTaskDescriptor.setRunContext(watcherRunContext);
		TaskServiceHolder.get().setActive(watcherTaskDescriptor, true);
		
		return watcherTaskDescriptor;
	}
}
