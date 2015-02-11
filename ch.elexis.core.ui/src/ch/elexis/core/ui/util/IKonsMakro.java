package ch.elexis.core.ui.util;

/**
 * Extend with an additional macro for the consultation. If no internal macro is registered for a
 * given String, the list of external macros is queried for the given macro name.
 */
public interface IKonsMakro {
	
	/**
	 * execute the macro with name makro
	 * 
	 * @param makro
	 * @return the value to replace the content in the consultation with
	 */
	public String executeMakro(String makro);
}
