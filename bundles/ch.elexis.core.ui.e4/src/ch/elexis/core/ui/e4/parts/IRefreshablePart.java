package ch.elexis.core.ui.e4.parts;

import java.util.Collections;
import java.util.Map;

/**
 * Mixin interface to declare that the implementing part is refreshable. Parts
 * implementing this may use the command
 * <code>ch.elexis.core.ui.command.part.refresh</code> to call for refresh
 */
public interface IRefreshablePart {

	default void refresh() {
		refresh(Collections.emptyMap());
	}

	/**
	 * Refresh the part potentially considering the provided filter parameters
	 *
	 * @param filterParameters
	 */
	abstract void refresh(Map<Object, Object> filterParameters);

}
