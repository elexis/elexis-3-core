package ch.elexis.core.console.internal;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.console.AbstractConsoleCommandProvider;

/**
 * Provides the <code>lxs</code> command entry point, giving an overview of all available console
 * components.
 */
@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandPovider extends AbstractConsoleCommandProvider {
	
	public void _lxs(CommandInterpreter ci){
		ci.print(getHelp((String[]) null));
	}
	
}
