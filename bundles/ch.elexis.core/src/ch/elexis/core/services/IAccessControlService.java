package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.ac.ACEAccessBitMapConstraint;
import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.exceptions.AccessControlException;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;

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
	 * Execute the runnable privileged (all rights, no user needed) in the calling
	 * thread.
	 * 
	 * @param runnable
	 */
	public void doPrivileged(Runnable runnable);

	/**
	 * Test if the current thread is running in privileged mode.
	 * 
	 * @return
	 */
	boolean isPrivileged();

	/**
	 * Refresh the ACL information for the user.
	 * 
	 * @param user
	 */
	void refresh(IUser user);

	/**
	 * Test if the current user provided by the {@link IContextService} has rights
	 * with acts on behalf of (aobo) or self attribute for the provided
	 * {@link ObjectEvaluatableACE}. If more than one right is provided in the
	 * {@link ObjectEvaluatableACE} the first found aobo or self right returns.
	 * 
	 * @param clazz
	 * @return
	 */
	public Optional<ACEAccessBitMapConstraint> isAoboOrSelf(ObjectEvaluatableACE evaluatableAce);

	/**
	 * Get a list of all ids of the {@link IMandator}s the current user provided by
	 * the {@link IContextService} acts on behalf of (aobo).
	 * 
	 * @return
	 */
	public List<String> getAoboMandatorIds();

	/**
	 * Get a list of all ids of the {@link IMandator}s the current user provided by
	 * the {@link IContextService} acts on behalf of (aobo). List is never empty and
	 * starts with "-1" element for SQL WHERE IN.
	 * 
	 * @return
	 */
	public List<String> getAoboMandatorIdsForSqlIn();

	/**
	 * Get the id of the {@link IMandator} of the current user.
	 * 
	 * @return
	 */
	public String getSelfMandatorId();
}
