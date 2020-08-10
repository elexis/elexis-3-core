package ch.elexis.core.mail.ui.handlers;

import java.util.HashMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.tasks.model.ITaskDescriptor;

public class OutboxUtil {
	
	private static Command getCommand(){
		ICommandService commandService =
			(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		
		return commandService
			.getCommand("at.medevit.elexis.outbox.ui.command.getOrCreateElementNoUi");
	}
	
	public static boolean isOutboxAvailable(){
		return getCommand() != null && getCommand().isEnabled();
	}
	
	public static Object getOrCreateElement(ITaskDescriptor iTaskDescriptor, boolean sent){
		// now try to call the create outbox command, is not part of core ...
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("at.medevit.elexis.outbox.ui.command.getOrCreateElementNoUi.dburi",
				StoreToStringServiceHolder.getStoreToString(iTaskDescriptor));
			params.put("at.medevit.elexis.outbox.ui.command.getOrCreateElementNoUi.sent",
				Boolean.toString(sent));
			ParameterizedCommand parametrizedCommmand =
				ParameterizedCommand.generateCommand(getCommand(), params);
			return PlatformUI.getWorkbench().getService(IHandlerService.class)
				.executeCommand(parametrizedCommmand, null);
		} catch (Exception ex) {
			LoggerFactory.getLogger(OutboxUtil.class)
				.warn("Create OutboxElement command not available");
		}
		return null;
	}
}
