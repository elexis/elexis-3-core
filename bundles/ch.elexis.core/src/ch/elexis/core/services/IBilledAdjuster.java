package ch.elexis.core.services;

import ch.elexis.core.model.IBilled;

/**
 * Implementations of {@link IBilledAdjuster} can adjust a {@link IBilled} directly after the
 * {@link IBillingService} created it. Implementations are expected to be OSGi service components.
 * 
 * @author thomas
 * 
 */
public interface IBilledAdjuster {
	/**
	 * Adjust the created {@link IBilled}.
	 * 
	 * @param billed
	 *            the IBilled object to adjust
	 */
	public void adjust(IBilled billed);
}
