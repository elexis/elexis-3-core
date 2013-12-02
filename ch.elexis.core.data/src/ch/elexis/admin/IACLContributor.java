/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

// This interface is to be used with the ACLContribution expension point

package ch.elexis.admin;

/**
 * An ACLContributor declares a List of AccessControlElements it will use. Such Elements will define
 * rights a user has, and will be editable via the administrator UI (Settings tab
 * "groups and rights"). The names of such ACE's can be chosen freely and thus might collide. In
 * such case, the framework will assign them on a first-come-first-serve basis. Thus, the second
 * client requesting the same verb will get a reject.
 * 
 * @author gerry
 * 
 */
public interface IACLContributor {
	
	/**
	 * return the ACL to be used by this extension
	 * 
	 * @return an array of all ACE's to insert
	 */
	public ACE[] getACL();
	
	/**
	 * The framework will call this method if one ore more of the transmitted ACL's could not be
	 * integrated (illegal name or duplicate)
	 * 
	 * @param acl
	 *            array of all rejected acls (these have not been integrated)
	 * @return the plugin can return an array of corrected acls or null.
	 */
	public ACE[] reject(ACE[] acl);
	
}
