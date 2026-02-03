package ch.elexis.core.console.internal;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.utils.OsgiServiceUtil;

/**
 * Register a function with the go shell that can be used as prompt by setting
 * <code>prompt = { felix:prompt }</code> in <code>gosh_profile</code>
 */
@Component(service = Prompt.class, immediate = true, property = { "osgi.command.scope=felix",
		"osgi.command.function=prompt" })
public class Prompt {


	private IContextService contextService;

	public String prompt() {

		if (contextService == null) {
			contextService = OsgiServiceUtil.getService(IContextService.class).orElse(null);
		}

		StringBuilder stringBuilder = new StringBuilder();

		if (contextService != null) {
			String userId = contextService.getActiveUser().map(IUser::getId).orElse(null);
			if (userId != null) {
				stringBuilder.append(userId);
			} else {
				stringBuilder.append("no-user");
			}

			String eeHostname = System.getenv("EE_HOSTNAME");
			if (eeHostname != null) {
				stringBuilder.append("@");
				stringBuilder.append(eeHostname.replaceAll(".myelexis.ch", ""));
			}

			// Cisco style enable prompt
			if (AbstractConsoleCommandProvider.isPrivilegedMode()) {
				stringBuilder.append("# ");
			} else {
				stringBuilder.append("> ");
			}
		}

		return stringBuilder.toString();
	}

}
