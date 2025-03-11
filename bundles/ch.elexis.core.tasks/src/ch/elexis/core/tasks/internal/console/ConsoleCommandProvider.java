package ch.elexis.core.tasks.internal.console;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.console.CmdAdvisor;
import ch.elexis.core.console.CmdParam;
import ch.elexis.core.console.ConsoleProgressMonitor;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.tasks.internal.model.service.CoreModelServiceHolder;
import ch.elexis.core.tasks.internal.service.TaskDescriptor;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.ModelPackage;
import ch.elexis.core.tasks.model.OwnerTaskNotification;
import ch.elexis.core.tasks.model.TaskTriggerType;
import ch.elexis.core.time.TimeUtil;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {

	@Reference
	private ITaskService taskService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.tasks.model)")
	private IModelService taskModelService;

	@Reference
	private IContextService contextService;

	@Reference
	private IVirtualFilesystemService vfsService;

	@Activate
	public void activate() {
		register(this.getClass());
	}

	@CmdAdvisor(description = "task management")
	public void _task(CommandInterpreter ci) {
		executeCommand("task", ci);
	}

	@CmdAdvisor(description = "show last executed tasks")
	public void __task_last(@CmdParam(required = false, description = "max: default=20") String maxEntries,
			@CmdParam(required = false, description = "tdIdOrRefId") String tdIdOrRefId) {

		IQuery<ITask> query = taskModelService.getQuery(ITask.class);
		if (maxEntries != null) {
			query.limit(Integer.valueOf(maxEntries));
		} else {
			query.limit(20);
		}
		if (tdIdOrRefId != null) {
			ITaskDescriptor taskDescriptor = taskService.findTaskDescriptorByIdOrReferenceId(tdIdOrRefId).orElse(null);
			if (taskDescriptor == null) {
				fail("Unknown descriptorId or descriptorReferenceId");
				return;
			} else {
				query.and(ModelPackage.Literals.ITASK__TASK_DESCRIPTOR, COMPARATOR.EQUALS, taskDescriptor);
			}
		}
		query.orderBy(ModelPackage.Literals.ITASK__FINISHED_AT, ORDER.DESC);

		List<ITask> finishedTasks = query.execute();
		prflp("State", 8);
		prflp("Descriptor Id/RefId", 27);
		prflp("ID", 27);
		prflp("FinishTime", 25);
		prflp("CreateTime", 25);
		ci.print("Result\n");

		finishedTasks.stream().forEach(t -> {
			prflp(t.getState().name(), 8);
			prflp(t.getTaskDescriptor().getReferenceId(), 27);
			prflp(t.getId(), 27);
			prflp(TimeUtil.formatSafe(t.getFinishedAt()), 25);
			prflp(TimeUtil.formatSafe(t.getCreatedAt()), 25);
			ci.print(t.getResult() + StringUtils.LF);
		});
	}

	@CmdAdvisor(description = "list tasks and current state")
	public void __task_list(
			@CmdParam(required = false, description = "boolean: show system tasks") String showSystemTasks) {
		List<ITask> runningTasks = taskService.getRunningTasks();
		// Trigger ID Descriptor Id/RefId StartTime Progress (%)
		prflp("State", 8);
		prflp("Trigger", 11);
		prflp("ID", 27);
		prflp("Descriptor Id/RefId", 27);
		prflp("StartTime", 25);
		prflp("Owner / Runner / Runnable", 70, true);

		runningTasks.stream().forEach(t -> {
			ITaskDescriptor td = t.getTaskDescriptor();
			prflp("RUN", 8);
			prflp(td.getTriggerType().getName(), 11);
			prflp(t.getId(), 27);
			prflp(td.getReferenceId(), 27);
			prflp(TimeUtil.formatSafe(t.getRunAt()), 25);
			String owner = (td.getOwner() != null) ? td.getOwner().getId() : "null";
			prflp(owner + " / " + formatRunner(td.getRunner()) + " / " + td.getIdentifiedRunnableId(), 70, true);
		});

		List<ITaskDescriptor> incurredTasks = taskService.getIncurredTasks();
		incurredTasks.stream().forEach(td -> {
			prflp("INC", 8);
			prflp(td.getTriggerType().getName(), 11);
			prflp(StringUtils.EMPTY, 27);
			prflp(td.getReferenceId(), 27);
			prflp("NR " + (String) td.getTransientData().get("cron-next-exectime"), 25);
			String owner = (td.getOwner() != null) ? td.getOwner().getId() : "null";
			prflp(owner + " / " + formatRunner(td.getRunner()) + " / " + td.getIdentifiedRunnableId(), 70, true);
		});

		IQuery<ITaskDescriptor> tdQuery = taskModelService.getQuery(ITaskDescriptor.class, true, false);
		if (!"true".equalsIgnoreCase(showSystemTasks)) {
			tdQuery.and(ModelPackage.Literals.ITASK_DESCRIPTOR__SYSTEM, COMPARATOR.EQUALS, false);
		}
		tdQuery.orderBy(ModelPackage.Literals.ITASK_DESCRIPTOR__ACTIVE, ORDER.DESC);
		tdQuery.orderBy(ModelPackage.Literals.ITASK_DESCRIPTOR__RUNNER, ORDER.DESC);
		List<ITaskDescriptor> taskDescriptors = tdQuery.execute();
		taskDescriptors.removeAll(incurredTasks);
		taskDescriptors.stream().forEach(td -> {

			// **ACT active on this machine
			// --ACT active but not on this machine
			// INACT inactive
			String state = td.isActive() ? (boundForThisStation(td.getRunner()) ? "**ACT" : "--ACT") : "INACT";
			prflp(state, 8);
			prflp(td.getTriggerType().getName(), 11);
			prflp(StringUtils.EMPTY, 27);
			prflp((td.isSystem() ? "S-" : StringUtils.EMPTY) + td.getReferenceId(), 27);
			prflp(StringUtils.EMPTY, 25);
			String owner = (td.getOwner() != null) ? td.getOwner().getId() : "null";
			prflp(owner + " / " + formatRunner(td.getRunner()) + " / " + td.getIdentifiedRunnableId(), 70, true);
		});
	}

	private String formatRunner(String runner) {
		if (boundForThisStation(runner)) {
			return "*" + runner.toUpperCase();
		} else {
			return runner.toLowerCase();
		}
	}

	private boolean boundForThisStation(String runner) {
		return contextService.getStationIdentifier().equalsIgnoreCase(runner);
	}

	@CmdAdvisor(description = "Activate a task descriptor for execution")
	public String __task_activate(@CmdParam(description = "taskId | tdIdOrTdRefId") String idOrReferenceId)
			throws TaskException {
		Optional<ITaskDescriptor> taskDescriptor = taskService.findTaskDescriptorByIdOrReferenceId(idOrReferenceId);
		if (!taskDescriptor.isPresent()) {
			return "Invalid or ambiguous id argument";
		}
		taskService.setActive(taskDescriptor.get(), true);
		return ok();
	}

	@CmdAdvisor(description = "Refresh system state on a taskDescriptor and act on it")
	public String __task_refresh(@CmdParam(description = "taskId | tdIdOrTdRefId") String idOrReferenceId)
			throws TaskException {
		Optional<ITaskDescriptor> taskDescriptor = taskService.findTaskDescriptorByIdOrReferenceId(idOrReferenceId);
		if (!taskDescriptor.isPresent()) {
			return "Invalid or ambiguous id argument";
		}
		taskService.refresh(taskDescriptor.get());
		return ok();
	}

	@CmdAdvisor(description = "Deactivate a task descriptor for execution")
	public String __task_deactivate(@CmdParam(description = "taskId | tdIdOrTdRefId") String idOrReferenceId)
			throws TaskException {
		Optional<ITaskDescriptor> taskDescriptor = taskService.findTaskDescriptorByIdOrReferenceId(idOrReferenceId);
		if (!taskDescriptor.isPresent()) {
			return "Invalid or ambiguous id argument";
		}
		taskService.setActive(taskDescriptor.get(), false);
		return ok();
	}

	@CmdAdvisor(description = "Gracefully cancel a running task")
	public void __task_cancel(@CmdParam(description = "taskId | tdIdOrTdRefId") String id) {
		List<ITask> activeTasks = taskService.getRunningTasks();
		for (ITask task : activeTasks) {
			if (id.equals(task.getId()) || id.equals(task.getTaskDescriptor().getId())
					|| id.equalsIgnoreCase(task.getTaskDescriptor().getReferenceId())) {

				task.getProgressMonitor().setCanceled(true);
				ok("Sent setCanceled to Task " + task.getId());
				return;
			}
		}
		fail("No matching task for given id");
	}

	@CmdAdvisor(description = "create or modify a task descriptor from a json file")
	public void __task_descriptor_url(@CmdParam(description = "url") String urlString)
			throws IOException, TaskException {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		IVirtualFilesystemHandle of = vfsService.of(urlString);
		String content = new String(of.readAllBytes(), StandardCharsets.UTF_8);
		TaskDescriptor fromJson = gson.fromJson(content, TaskDescriptor.class);
		taskService.saveTaskDescriptor(fromJson);
	}

	@CmdAdvisor(description = "serialize a task descriptor to a json string")
	public void __task_descriptor_json(@CmdParam(description = "tdIdOrTdRefId") String idOrReferenceId) {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Optional<ITaskDescriptor> taskDescriptor = taskService.findTaskDescriptorByIdOrReferenceId(idOrReferenceId);
		if (taskDescriptor.isPresent()) {
			ITaskDescriptor iTaskDescriptor = taskDescriptor.get();
			String json = gson.toJson(iTaskDescriptor, TaskDescriptor.class);
			ci.println(json);
		} else {
			fail("taskDescriptor not found");
		}
	}

	@CmdAdvisor(description = "set attributes on a task descriptor")
	public String __task_descriptor_set(@CmdParam(description = "tdIdOrTdRefId") String idOrReferenceId,
			@CmdParam(description = "(owner | runner | referenceid | trigger | otn)") String key,
			@CmdParam(description = "value ") String value) {
		ITaskDescriptor taskDescriptor = taskService.findTaskDescriptorByIdOrReferenceId(idOrReferenceId).orElse(null);
		if (taskDescriptor != null) {
			switch (key.toLowerCase()) {
			case "owner":
				IUser user = CoreModelServiceHolder.get().load(value, IUser.class).orElse(null);
				if (user != null) {
					taskDescriptor.setOwner(user);
				} else {
					return "userId not found";
				}
				break;
			case "runner":
				taskDescriptor.setRunner(value);
				break;
			case "referenceid":
				taskDescriptor.setReferenceId(value);
				break;
			case "trigger":
				TaskTriggerType ttt = TaskTriggerType.getByName(value.toUpperCase());
				if (ttt != null) {
					taskDescriptor.setTriggerType(ttt);
				} else {
					return "TaskTriggerType not found";
				}
				break;
			case "otn":
				OwnerTaskNotification otn = OwnerTaskNotification.getByName(value.toUpperCase());
				if (otn != null) {
					taskDescriptor.setOwnerNotification(otn);
				} else {
					return "OwnerTaskNotification not found";
				}
				break;
			default:
				if (key.toLowerCase().startsWith("runcontext.")) {
					String keyVal = key.substring("runContext.".length());
					taskDescriptor.setRunContextParameter(keyVal, value);
				}
				break;
			}
			taskModelService.save(taskDescriptor);
		} else {
			return "taskDescriptor not found";
		}
		return ok();
	}

	@CmdAdvisor(description = "manually trigger execution of a task descriptor")
	public void __task_trigger(@CmdParam(description = "tdIdOrTdRefId") String idOrReferenceId) throws TaskException {
		Optional<ITaskDescriptor> taskDescriptor = taskService.findTaskDescriptorByIdOrReferenceId(idOrReferenceId);
		if (taskDescriptor.isPresent()) {
			String result = taskService
					.trigger(taskDescriptor.get(), new ConsoleProgressMonitor(), TaskTriggerType.MANUAL, null)
					.toString();
			ok(result);
		} else {
			fail("Invalid or ambiguous id argument");
		}
	}

	@CmdAdvisor(description = "directly execute a runnable using its default context")
	public void __task_singleshot(@CmdParam(description = "runnableId") String runnableId) throws TaskException {
		IIdentifiedRunnable runnable = taskService.instantiateRunnableById(runnableId);
		ITaskDescriptor taskDescriptor = taskService.createTaskDescriptor(runnable);
		String referenceId = taskDescriptor.getReferenceId();
		taskDescriptor.setReferenceId("sshot_" + referenceId);
		ITask task = taskService.triggerSync(taskDescriptor, new ConsoleProgressMonitor(), TaskTriggerType.MANUAL,
				Collections.emptyMap());
		Map<String, ?> result = task.getResult();
		Set<String> keySet = result.keySet();
		for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			ci.println(key + ":" + result.get(key));
		}
		taskModelService.delete(taskDescriptor);
	}

	@CmdAdvisor(description = "deactivate and remove a task descriptor")
	public void __task_descriptor_remove(@CmdParam(description = "tdIdOrTdRefId") String idOrReferenceId)
			throws TaskException {
		Optional<ITaskDescriptor> taskDescriptor = taskService.findTaskDescriptorByIdOrReferenceId(idOrReferenceId);
		if (taskDescriptor.isPresent()) {
			boolean result = taskService.removeTaskDescriptor(taskDescriptor.get());
			ok(result);
		} else {
			fail("Invalid or ambiguous id argument");
		}
	}

	@CmdAdvisor(description = "list all available identified runnables")
	public void __task_runnable_list() {
		List<IIdentifiedRunnable> availableRunnables = taskService.getIdentifiedRunnables();

		prflp("Runnable ID", 30);
		prflp("Bundle", 60);
		ci.print("Description\n");

		availableRunnables.stream().sorted(Comparator.comparing(ii -> ii.getId())).forEach(ii -> {
			prflp(ii.getId(), 30);
			String classNameShortened = abbreviatePackageNames(ii.getClass().getName());
			prflp(classNameShortened, 60);
			ci.print(ii.getLocalizedDescription() + StringUtils.LF);
		});
	}

	private String abbreviatePackageNames(String name) {
		return name.replace("ch.elexis", "c.e").replace("ch.medelexis", "c.m").replace("at.medevit", "a.m")
				.replace("core", "c");
	}

}
