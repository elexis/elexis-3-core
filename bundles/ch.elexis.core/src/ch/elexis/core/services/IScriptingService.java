package ch.elexis.core.services;

import ch.elexis.core.exceptions.ElexisException;

public interface IScriptingService {
	
	/**
	 * Execute the script using an Interpreter available to Elexis (mostly beanshell).
	 * 
	 * @param script
	 * @return the result of evaluating the script
	 * @throws ElexisException
	 *             on script execution error
	 */
	public Object execute(String script) throws ElexisException;
}
