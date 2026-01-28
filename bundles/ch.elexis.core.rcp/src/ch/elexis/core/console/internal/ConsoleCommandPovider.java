package ch.elexis.core.console.internal;

import org.eclipse.equinox.console.completion.common.Completer;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.console.AbstractConsoleCommandProvider;

/**
 * Provides the <code>lxs</code> command entry point, giving an overview of all
 * available console components.
 */
@Component(service = { CommandProvider.class, Completer.class }, immediate = true)
public class ConsoleCommandPovider extends AbstractConsoleCommandProvider implements Completer {

	public void _lxs(CommandInterpreter ci) {
		ci.print(getHelp((String[]) null));
	}

}
