/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.admin;

import ch.elexis.data.Role;
import ch.elexis.data.User;

/**
 * @since 3.1 replaced the original AccessControl, which is kept in {@link AccessControlImpl}
 */
public abstract class AbstractAccessControl {
	
	public static final String USER_GROUP = Role.SYSTEMROLE_LITERAL_USER;
	public static final String ADMIN_GROUP = Role.SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR;
	
	public abstract boolean request(ACE ace);
	
	/**
	 * Request a right for the current user by providing the {@link ACE#getCanonicalName()}
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
	public abstract boolean request(Role r, ACE ace);
	
	/**
	 * Request a right for a given {@link User}. The rights allowed for this user is the joint
	 * rights for each {@link Role} this user is allocated to.
	 * 
	 * @param u
	 * @param ace
	 * @return <code>true</code> if allowed
	 */
	public abstract boolean request(User u, ACE ace);
	
	/**
	 * Grant a specific right to a {@link Role}
	 * 
	 * @param r
	 * @param ace
	 */
	public abstract void grant(Role r, ACE ace);
	
	/**
	 * Revoke a specific right from a {@link Role}
	 * 
	 * @param r
	 * @param ace
	 */
	public abstract void revoke(Role r, ACE ace);
	
	/**
	 * Grant a specific right to a Role id
	 * @param s the id, resolvable to a {@link Role}
	 * @param ace
	 */
	public abstract void grant(String s, ACE ace);
	
	/**
	 * @deprecated 3.1 for compatibility only
	 */
	public void flush(){}
	
}
