package ch.elexis.core.services;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IEncounter;

/**
 * Implementations of {@link IBillableAdjuster} can adjust a {@link IBillable} before it is
 * attempted to be billed. Implementations are expected to be OSGi service components.
 * 
 * @author thomas
 * 
 */
public interface IBillableAdjuster {
	/**
	 * Adjust the {@link IBillable} before it is attempted to be billed. The adjusted
	 * {@link IBillable}, possibly a different object than billable, is returned. If no adjustment
	 * is performed, the same object is returned.
	 * 
	 * @param verrechenbar
	 * @return the adjusted {@link IVerrechenbar}
	 */
	public IBillable adjust(IBillable billable, IEncounter encounter);
}
