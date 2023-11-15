package ch.elexis.core.osgi;

import org.eclipse.equinox.console.completion.common.Completer;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;

import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.console.CmdAdvisor;

/**
 * Provides the <code>ds</code> command entry point, giving an overview of all
 * available console components.
 */
@Component(service = { CommandProvider.class, Completer.class }, immediate = true)
public class ConsoleCommandPovider extends AbstractConsoleCommandProvider implements Completer {

	@Reference
	private ServiceComponentRuntime scr;

	@CmdAdvisor(description = "ds")
	public void _ds(CommandInterpreter ci) {
		ci.print(getHelp((String[]) null));
	}

	@CmdAdvisor(description = "list unsatisfied osgi services")
	public String __ds_unsatisfied(String appointmentId, String patientId, String mandatorId) {
		return UnsatisfiedComponentUtil.listUnsatisfiedComponents(scr, FrameworkUtil.getBundle(getClass()));
	}
}
