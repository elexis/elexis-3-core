package ch.elexis.core.data.interfaces.text;

import java.util.Optional;

public interface ITextResolver {
	
	/**
	 * Resolve text from the object.
	 * 
	 * @param object
	 * @return
	 */
	public Optional<String> resolve(Object object);
}
