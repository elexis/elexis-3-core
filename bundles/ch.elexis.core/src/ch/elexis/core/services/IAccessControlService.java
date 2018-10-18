package ch.elexis.core.services;

import javax.management.relation.Role;

import ch.elexis.core.ac.ACE;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;

public interface IAccessControlService {
	
	/**
	 * Initializes the default access control set, including all contributions made by "currently"
	 * available plugins.
	 */
	public void initializeDefaults();
	
	/**
	 * convenience method calling {@link #request(User, ACE)}
	 * 
	 * @param ace
	 * @return
	 * @see #request(User, IACE)
	 */
	public boolean request(ACE ace);
	
	/**
	 * Request a right for the current user by providing the {@link IACE#getCanonicalName()}
	 * 
	 * @param canonicalName
	 * @return <code>true</code> if allowed
	 */
	public abstract boolean request(String canonicalName);
	
	/**
	 * Request a right for a given {@link Role}
	 * 
	 * @param r
	 * @param ace
	 * @return <code>true</code> if allowed
	 */
	public abstract boolean request(@NonNull IRole role, ACE ace);
	
	/**
	 * Request a right for a given {@link User}. The rights allowed for this user is the joint
	 * rights for each {@link Role} this user is allocated to.
	 * 
	 * @param u
	 * @param ace
	 * @return <code>true</code> if allowed
	 */
	public abstract boolean request(IUser user, ACE ace);
	
	/**
	 * Grant a specific right to a {@link Role}
	 * 
	 * @param r
	 * @param ace
	 */
	public abstract void grant(IRole role, ACE ace);
	
	/**
	 * Grant a specific right to a Role id
	 * 
	 * @param roledId
	 *            the id, resolvable to a {@link Role}
	 * @param ace
	 * @deprecated please use {@link #grant(IRole, IACE)}
	 */
	public abstract void grant(String roledId, ACE ace);
	
	/**
	 * Revoke a specific right from a {@link Role}
	 * 
	 * @param r
	 * @param ace
	 */
	public abstract void revoke(IRole role, ACE ace);
	
}
