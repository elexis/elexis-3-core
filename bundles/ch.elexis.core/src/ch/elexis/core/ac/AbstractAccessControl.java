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
package ch.elexis.core.ac;

import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.RoleConstants;

/**
 * @since 3.1 replaced the original AccessControl, which is kept in AccessControlImpl
 * @since 3.8 removed AccessControlImpl
 */
public abstract class AbstractAccessControl {
	
	public static final String USER_GROUP = RoleConstants.SYSTEMROLE_LITERAL_USER;
	public static final String ADMIN_GROUP = RoleConstants.SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR;
	
	public abstract boolean request(ACE ace);
	
	/**
	 * Request a right for a given {@link Role}
	 * 
	 * @param r
	 * @param ace
	 * @return <code>true</code> if allowed
	 */
	public abstract boolean request(@NonNull IRole r, ACE ace);
	
	/**
	 * Request a right for a given {@link User}. The rights allowed for this user is the joint
	 * rights for each {@link Role} this user is allocated to.
	 * 
	 * @param u
	 * @param ace
	 * @return <code>true</code> if allowed
	 */
	public abstract boolean request(IUser u, ACE ace);
	
	/**
	 * Grant a specific right to a {@link Role}
	 * 
	 * @param r
	 * @param ace
	 */
	public abstract void grant(IRole r, ACE ace);
	
	/**
	 * Revoke a specific right from a {@link Role}
	 * 
	 * @param r
	 * @param ace
	 */
	public abstract void revoke(IRole r, ACE ace);
	
	/**
	 * @deprecated 3.1 for compatibility only
	 */
	public void flush(){}
	
}
