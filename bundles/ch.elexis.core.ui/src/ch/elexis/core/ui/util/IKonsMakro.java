package ch.elexis.core.ui.util;

/**
 * Extend with an additional macro for the consultation. If no internal macro is registered for a
 * given String, the list of external macros is queried for the given macro name.
 */
public interface IKonsMakro {
	
	/**
	 * Execute the macro with name makro. If no makro is found null should be returned.
	 * 
	 * @param makro
	 * @return the value to replace the content in the consultation with or null if no makro is
	 *         found
	 */
	public String executeMakro(String makro);
}
