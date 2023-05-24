package ch.elexis.core.ui.contacts.command;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.data.Patient;

public class SendMailCommand extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Patient patient = ElexisEventDispatcher.getSelectedPatient();
		if (patient != null) {
			ICommandService commandService = (ICommandService) HandlerUtil.getActiveWorkbenchWindow(event)
					.getService(ICommandService.class);
			try {
				Command sendMailCommand = commandService.getCommand("ch.elexis.core.mail.ui.sendMail"); //$NON-NLS-1$

				HashMap<String, String> params = new HashMap<String, String>();

				params.put("ch.elexis.core.mail.ui.sendMail.to", StringUtils.SPACE + patient.getMailAddress()); //$NON-NLS-1$

				ParameterizedCommand parametrizedCommmand = ParameterizedCommand.generateCommand(sendMailCommand,
						params);
				PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(parametrizedCommmand, null);
			} catch (Exception ex) {
				throw new RuntimeException("ch.elexis.core.mail.ui.sendMail not found", ex); //$NON-NLS-1$
			}
		}
		return null;
	}

}
