/*******************************************************************************
 * Copyright (c) 2009-2014, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.admin;

import java.io.Serializable;

/**
 * AcessControlElement
 * 
 * An item constituting a named right. AccessControlElements are collected hierarchically in ACL's
 * (AccessControlLists). An ACE has a parent, an internal name and a (probably localized) external
 * name that will be shown to the user. <br>
 * <br>
 * ACEs are loaded within {@link AccessControl#load()}
 * 
 * @since 2.0
 * @author gerry
 * 
 */
public class ACE implements Serializable {
	private static final long serialVersionUID = 34320020090119L;
	
	public static final ACE ACE_ROOT = new ACE(null, "root", Messages.ACE_root); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ACE_IMPLICIT = new ACE(ACE.ACE_ROOT, "implicit", Messages.ACE_implicit); //$NON-NLS-1$ //$NON-NLS-2$
	
	private final String name;
	private String localizedName;
	private final ACE parent;
	
	/**
	 * Create a new ACE. This is the recommended constructor for most cases.
	 * 
	 * @param parent
	 *            the parent ACE. If this is a top-level ACE, use {@link #ACE_ROOT} as parent.
	 * @param name
	 *            the internal, immutable name of this ACE. Should be unique. Therefore, it is
	 *            recommended to prefix the name with the plugin ID
	 * @param localizedName
	 *            the name that will be presented to the user. This should be a translatable String
	 */
	public ACE(ACE parent, String name, String localizedName){
		this.parent = parent;
		this.name = name;
		this.localizedName = localizedName;
	}
	
	/**
	 * create a new ACE without localized name. The localized name will be the same as the internal
	 * name. So this constructor should <b>not</b> be used for ACE's that will be shown to the user.
	 * 
	 * @param parent
	 *            the parent ACE. If this is a top-evel ACE, use ACE_ROOT as parent.
	 * @param name
	 *            the internal, immutable name of this ACE. Should be unique. Therefore, it is
	 *            recommended to prefix the name with the plugin ID.
	 */
	public ACE(ACE parent, String name){
		this(parent, name, name);
	}
	
	/**
	 * @return the non-translatable name of this ACE
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * @return the localized Name of this ACE
	 */
	public String getLocalizedName(){
		return localizedName;
	}
	
	/**
	 * @return the parent ACE
	 */
	public ACE getParent(){
		return parent;
	}
	
	/**
	 * Change the localized name of this ACE
	 * 
	 * @param lName
	 *            a new name to use as localized name
	 */
	public void setLocalizedName(String lName){
		localizedName = lName;
	}
	
	/**
	 * get the full pathname of an ACE. This Method is internal to the ACL system and should not be
	 * used externally
	 */
	String getCanonicalName(){
		StringBuilder sp = new StringBuilder();
		sp.append(getName());
		ACE parent = getParent();
		while ((parent != null) && (!parent.equals(ACE.ACE_ROOT))) {
			sp.insert(0, parent.getName() + "/"); //$NON-NLS-1$
			parent = parent.getParent();
		}
		return sp.toString();
	}
}
