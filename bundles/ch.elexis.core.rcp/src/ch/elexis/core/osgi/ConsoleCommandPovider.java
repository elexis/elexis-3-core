package ch.elexis.core.osgi;

import org.eclipse.equinox.console.completion.common.Completer;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
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
public class ConsoleCommandPovider extends AbstractConsoleCommandProvider {

	@Reference
	private ServiceComponentRuntime scr;

	@Activate
	public void activate() {
		register(this.getClass());
	}

	@CmdAdvisor(description = "declarative services")
	public void _ds(CommandInterpreter ci) {
		executeCommand("ds", ci);
	}

	@CmdAdvisor(description = "list unsatisfied osgi services")
	public String __ds_unsatisfied() {
		return UnsatisfiedComponentUtil.listUnsatisfiedComponents(scr, FrameworkUtil.getBundle(getClass()));
	}
}
