/*******************************************************************************
 * Copyright (c) 2009-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 	  MEDEVIT <office@medevit.at> - major refactorings #2112
 *******************************************************************************/
package ch.elexis.admin;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.data.Query;
import ch.elexis.data.Right;
import ch.elexis.data.Role;

/**
 * AcessControlElement
 * 
 * An item constituting a named right. AccessControlElements are collected hierarchically in ACL's
 * (AccessControlLists). An ACE has a parent, an internal name and a (probably localized) external
 * name that will be shown to the user. <br>
 * <br>
 * ACEs are loaded within {@link AbstractAccessControl#load()}
 * 
 * @since 2.0
 * @author gerry
 * 		
 */
public class ACE implements Serializable {
	private static final long serialVersionUID = 34320020090119L;
	
	public static final String ACE_ROOT_LITERAL = "root";//$NON-NLS-1$	
	public static final ACE ACE_ROOT = new ACE(null, ACE_ROOT_LITERAL, Messages.ACE_root);
	public static final ACE ACE_IMPLICIT = new ACE(ACE.ACE_ROOT, "implicit", Messages.ACE_implicit); //$NON-NLS-1$
	
	private static Map<String, ACE> allDefinedACEs;
	
	private static Logger log = LoggerFactory.getLogger(ACE.class);
	
	private final String name;
	private String localizedName;
	private final ACE parent;
	private List<ACE> children = new ArrayList<ACE>();
	
	/**
	 * initialize all defined ACEs, only performed once
	 * 
	 * @return
	 */
	private static void initAllDefinedACEs(){
		if (allDefinedACEs != null)
			return;
			
		List<ACE> temp = getACLContributionExtensions().stream()
			.flatMap(acl -> Arrays.asList(acl.getACL()).stream()).collect(Collectors.toList());
		allDefinedACEs = temp.stream().collect(Collectors.toMap(a -> a.getCanonicalName(), a -> a));
	}
	
	/**
	 * initialize the default ACE values
	 * 
	 * @param reset
	 *            resets all configured rights before installing the defaults
	 * @since 3.1
	 */
	public static void initializeACEDefaults(boolean reset){
		if (reset) {
			Query<Role> arq = new Query<Role>(Role.class);
			List<Role> allRoles = arq.execute();
			for (Role role : allRoles) {
				role.revokeAllRightsForRole();
			}
			Right.resetTable();
		}
		try {
			getACLContributionExtensions().stream()
				.forEach(ace -> ace.initializeDefaults(CoreHub.acl));
		} catch (Exception e) {
			log.warn("initializeACEDefaults", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static List<IACLContributor> getACLContributionExtensions(){
		return Extensions.getClasses(ExtensionPointConstantsData.ACL_CONTRIBUTION,
			ExtensionPointConstantsData.ACL_CONTRIBUTION_PT_CONTRIBUTOR);
	}
	
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
		if (parent != null)
			parent.addChild(this);
	}
	
	private void addChild(ACE ace){
		children.add(ace);
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
	 * @param deep
	 *            recurse to bottom, or <code>false</code> deliver direct children
	 * @return a list of all children to this, in unspecific order, including self
	 * @since 3.1
	 */
	public List<ACE> getChildren(boolean deep){
		if (deep) {
			return getChildrenRecursive();
		} else {
			return new ArrayList<ACE>(children);
		}
	}
	
	/**
	 * recursively fetch all children, adding self
	 * 
	 * @return
	 */
	private List<ACE> getChildrenRecursive(){
		List<ACE> ret = new ArrayList<ACE>();
		ret.add(this);
		for (ACE ace : children) {
			ret.addAll(ace.getChildrenRecursive());
		}
		return ret;
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
	
	/**
	 * @return a unique hex string for the ACE that is derived by its canonical name and name, if
	 *         {@link #ACE_ROOT} returns <code>root</code>
	 * @since 3.1
	 */
	public String getUniqueHashFromACE(){
		if (ACE_ROOT.equals(this))
			return ACE_ROOT_LITERAL;
		int valCan = Math.abs(getCanonicalName().hashCode());
		int valNam = Math.abs(getName().hashCode());
		BigInteger valI = new BigInteger(valCan + "" + valNam);
		return valI.toString(16);
	}
	
	/**
	 * @return all defined ACE elements
	 * @since 3.1
	 */
	public static @NonNull List<ACE> getAllDefinedACElements(){
		initAllDefinedACEs();
		return new ArrayList<ACE>(allDefinedACEs.values());
	}
	
	/**
	 * @return the root elements of all ACElements
	 * @since 3.1
	 */
	public static @NonNull ACE[] getAllDefinedRootACElements(){
		return ACE.getAllDefinedACElements().stream()
			.filter(p -> ACE.ACE_ROOT.equals(p.getParent())).toArray(size -> new ACE[size]);
	}
	
	/**
	 * @return this element and its entire parent chain
	 */
	public List<ACE> getParentChainIncludingSelf(){
		List<ACE> aces = new ArrayList<ACE>();
		aces.add(this);
		if (this.equals(ACE_ROOT))
			return aces;
		ACE parent = getParent();
		while (parent != ACE_ROOT) {
			aces.add(parent);
			parent = parent.getParent();
		}
		
		return aces;
	}
	
	@Override
	public String toString(){
		return getUniqueHashFromACE() + " " + getName() + " " + getCanonicalName();
	}
	
	/**
	 * @param uniqueHash
	 * @return
	 */
	public static @Nullable ACE getACEByCanonicalName(String canonicalName){
		initAllDefinedACEs();
		return allDefinedACEs.get(canonicalName);
	}
}
