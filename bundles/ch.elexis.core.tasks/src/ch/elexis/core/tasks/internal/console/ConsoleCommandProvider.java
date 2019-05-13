package ch.elexis.core.tasks.internal.console;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;

import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.console.ConsoleProgressMonitor;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {
	
	@Reference
	private ITaskService taskService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.tasks.model)")
	private IModelService taskModelService;
	
	@Reference
	private IContextService contextService;
	
	@Activate
	public void activate() {
		register(this.getClass());
	}
	
	@Override
	protected void initializeCommandsHelp(LinkedHashMap<String, String> commandsHelp){
		commandsHelp.put("task", "task management");
	}

	public void _task(CommandInterpreter ci){
		executeCommand("task", ci);
	}
	
	public void __task_descriptor_list(){
		List<ITaskDescriptor> taskDescriptors =
			taskModelService.getQuery(ITaskDescriptor.class, true, false).execute();
		taskDescriptors.forEach(e -> ci.println(e));
	}
	
	public String __task_descriptor_add(String identifiedRunnableId) throws TaskException{
		if (StringUtils.isBlank(identifiedRunnableId)) {
			return missingArgument("identifiedRunnableId");
		}
		
		IIdentifiedRunnable runnable = taskService.instantiateRunnableById(identifiedRunnableId);
		
		ITaskDescriptor taskDescriptor = taskModelService.create(ITaskDescriptor.class);
		taskDescriptor.setIdentifiedRunnableId(runnable.getId());
		taskDescriptor.setRunner(contextService.getStationIdentifier());
		taskModelService.save(taskDescriptor);
		return taskDescriptor.toString();
	}
	
	public String __task_descriptor_set(List<String> arguments){
		if (arguments.size() < 2) {
			ci.println("usage: task descriptor set id | referenceId [property=value ...]");
			ci.println("| properties: runContext<Map> | triggerType<EnumLiteral> ");
			return "";
		}
		
		String idOrReferenceId = arguments.get(0);
		
		Optional<ITaskDescriptor> taskDescriptor =
			taskService.findTaskDescriptorByIdOrReferenceId(idOrReferenceId);
		if (!taskDescriptor.isPresent()) {
			return "Invalid or ambiguous id argument";
		}
		
		arguments = arguments.subList(0, arguments.size());
		
		for (String arg : arguments) {
			ci.println(arg);
			String[] split = arg.split("=");
			if (split.length == 2) {	
				try {
					Object value;
					if (split[1].startsWith("{") && split[1].endsWith("}")) {
						// json map
						value = new Gson().fromJson(split[1], Map.class);
					} else {
						value = split[1];
					}
					Class<?> propertyType = PropertyUtils.getPropertyType(taskDescriptor.get(), split[0]);
					if(Objects.equals(TaskTriggerType.class, propertyType)) {
						value = TaskTriggerType.valueOf(split[1].toUpperCase());
					}
					PropertyUtils.setProperty(taskDescriptor.get(), split[0], value);
				} catch (IllegalAccessException | InvocationTargetException
						| NoSuchMethodException e) {
					ci.println(e);
				}
			}
		}
		taskModelService.save(taskDescriptor.get());
		return ok();
	}
	
	public String __task_descriptor_trigger(String taskDescriptorIdOrReferenceId)
		throws TaskException{
		Optional<ITaskDescriptor> taskDescriptor =
			taskService.findTaskDescriptorByIdOrReferenceId(taskDescriptorIdOrReferenceId);
		if (!taskDescriptor.isPresent()) {
			return "Invalid or ambiguous id argument";
		}
		
		return taskService.trigger(taskDescriptor.get(), new ConsoleProgressMonitor(),
			TaskTriggerType.MANUAL, null).toString();
	}
	
	public void __task_runnable_list(){
		Map<String, String> availableRunnables = taskService.listAvailableRunnables();
		availableRunnables.entrySet().stream()
			.forEach(e -> ci.println(e.getKey() + " " + e.getValue()));
	}
	
	public String __task_descriptor_activate(List<String> arguments) throws TaskException {
		if (arguments.size() != 2) {
			ci.println("usage: task descriptor set activate true | false");
		}
		
		Optional<ITaskDescriptor> taskDescriptor =
				taskService.findTaskDescriptorByIdOrReferenceId(arguments.get(0));
		if (!taskDescriptor.isPresent()) {
			return "Invalid or ambiguous id argument";
		}
		Boolean active = Boolean.parseBoolean(arguments.get(1));
		
		taskService.setActive(taskDescriptor.get(), active);
		return ok();
	}
	
}
