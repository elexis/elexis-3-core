/*******************************************************************************
 * Copyright (c) 2007-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    MEDEVIT <office@medevit.at> - modifications to 3.1
 *******************************************************************************/
package ch.elexis.core.ac;

import javax.management.relation.Role;

import ch.elexis.core.model.IRole;

/**
 * An ACLContributor declares a List of AccessControlElements it will use. Such Elements will define
 * rights a user has, and will be editable via the administrator UI (Settings tab "groups and
 * rights"). The names of such ACE's can be chosen freely and thus might collide. In such case, the
 * framework will assign them on a first-come-first-serve basis. Thus, the second client requesting
 * the same verb will get a reject.
 * 
 * This interface is used with the ACLContribution extension point
 * 
 * @author gerry
 * @since 3.1 removal of reject method, addition of initializeDefaults
 */
public interface IACLContributor {
	
	/**
	 * return all ACLs to be used by this extension
	 * 
	 * @return an array of all ACE's to insert
	 */
	public ACE[] getACL();
	
	/**
	 * Initialize the default rights to system {@link IRole}s for the ACLs provided in
	 * {@link #getACL()}. Use calls like {@link AbstractAccessControl#grant(String, ACE)} where the
	 * String argument is e.g. {@link Role#SYSTEMROLE_LITERAL_USER}
	 * 
	 * @param ac
	 *            the {@link AbstractAccessControl} instance to grant against
	 */
	public void initializeDefaults(AbstractAccessControl ac);
	
}
