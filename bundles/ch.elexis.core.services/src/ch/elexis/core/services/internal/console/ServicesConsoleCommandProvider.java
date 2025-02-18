package ch.elexis.core.services.internal.console;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.console.CmdAdvisor;
import ch.elexis.core.console.CmdParam;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IMessageService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.status.ObjectStatus;

@Component(service = CommandProvider.class, immediate = true)
public class ServicesConsoleCommandProvider extends AbstractConsoleCommandProvider {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IContextService contextService;

	@Reference
	private IMessageService messageService;

	@Activate
	public void activate() {
		register(this.getClass());
	}

	@CmdAdvisor(description = "Core Services")
	public void _srv(CommandInterpreter ci) {
		executeCommand("srv", ci);
	}

	@CmdAdvisor(description = "send an internal message")
	public void __srv_message(@CmdParam(required = true, description = "contactid of receiver") String contactid,
			@CmdParam(required = true, description = "message to send") String message) {

		if (StringUtils.isEmpty(contactid)) {
			missingArgument("contactid");
			return;
		}

		if (StringUtils.isEmpty(message)) {
			missingArgument("message");
			return;
		}

		TransientMessage _message = messageService.prepare(contextService.getStationIdentifier(),
				IMessageService.INTERNAL_MESSAGE_URI_SCHEME + ":" + contactid);
		_message.setMessageText(message);
		ObjectStatus status = messageService.send(_message);
		ok(status);
	}

}
