package ch.elexis.core.console;

import java.util.LinkedHashMap;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Provides the <code>lxs</code> command entry point, giving an overview of all available console
 * components.
 */
@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandPovider extends AbstractConsoleCommandProvider {
	
	public void _lxs(CommandInterpreter ci){
		ci.print(getHelp((String[]) null));
	}
	
	@Override
	protected void initializeCommandsHelp(LinkedHashMap<String, String> commandsHelp){
		// not here
	}
	
}
