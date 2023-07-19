package ch.elexis.core.services;

import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.exceptions.AccessControlException;

public interface IAccessControlService {

	/**
	 * Test if the provided {@link EvaluatableACE} is permitted for the current user
	 * provided by the {@link IContextService}.
	 * 
	 * @param evaluatableAce
	 * @return
	 * @throws AccessControlException
	 */
	public boolean evaluate(EvaluatableACE evaluatableAce) throws AccessControlException;

	/**
	 * Execute the runnable in privileged mode. The runnable is executed privileged
	 * in the calling thread.
	 * 
	 * @param runnable
	 */
	public void doPrivileged(Runnable runnable);

}
