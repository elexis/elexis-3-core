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
package ch.elexis.data;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ch.elexis.admin.ACE;
import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;

public class Role extends PersistentObject {
	
	public static final String TABLENAME = "ROLE";
	public static final String FLD_SYSTEM_ROLE = "ISSYSTEMROLE";
	public static final String FLD_EXT_I18N_LABEL = "LAB_" + Locale.getDefault().getLanguage();
	public static final String FLD_JOINT_RIGHTS = "Rights";
	
	public static final String ROLE_LITERAL_USER = "user";
	public static final String ROLE_LITERAL_EXECUTIVE_DOCTOR = "executive_doctor";
	
	static {
		addMapping(TABLENAME, FLD_SYSTEM_ROLE, FLD_EXTINFO, FLD_JOINT_RIGHTS
			+ "=LIST:ROLE_ID:ROLE_RIGHT_JOINT");
		
		if (!tableExists(TABLENAME)) {
			executeDBInitScriptForClass(Role.class, null);
			initBasicRoles();
		}
	}
	
	protected Role(){}
	
	/**
	 * Configure the basic system role user
	 */
	public static void initBasicRoles(){
		Role ur = Role.load(ROLE_LITERAL_USER);
		ACE[] anwender = AccessControlDefaults.getAnwender();
		Arrays.asList(anwender).forEach(ace -> ur.grantAccessRight(ace));
		ACE[] alle = AccessControlDefaults.getAlle();
		Arrays.asList(alle).forEach(ace -> ur.grantAccessRight(ace));
		
		Role ed = Role.load(ROLE_LITERAL_EXECUTIVE_DOCTOR);
		ed.grantAccessRight(AccessControlDefaults.ACE_ACCESS);
	}
	
	protected Role(final String id){
		super(id);
	}
	
	public static Role load(final String id){
		return new Role(id);
	}
	
	@Override
	public String getLabel(){
		return getId();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public boolean isSystemRole(){
		return getBoolean(FLD_SYSTEM_ROLE);
	}
	
	public String getTranslatedLabel(){
		return (String) getExtInfoStoredObjectByKey(FLD_EXT_I18N_LABEL);
	}
	
	public void setTranslatedLabel(String translatedLabel){
		setExtInfoStoredObjectByKey(FLD_EXT_I18N_LABEL, translatedLabel);
	}
	
	public ACE[] getAssignedAccessRights(){
		ACE[] array =
			ACE.getAllDefinedACElements().stream()
				.filter(p -> CoreHub.acl.request(this, p))
				.toArray(size -> new ACE[size]);
		return array;
	}
	
	/**
	 * @return the {@link Right#ID} of all rights permitted to this role
	 */
	private List<String> getAssignedRightsIds(){
		return getList(FLD_JOINT_RIGHTS, false);
	}
	
	/**
	 * @param ace
	 *            grants the respective right, and all associated child rights if applicable
	 */
	public void grantAccessRight(ACE ace){
		// check if ace is already granted via a parent right
		// if it is, just return
		
		Right right = Right.getOrCreateRightByACE(ace);
		if (getAssignedRightsIds().contains(ace.getUniqueHashFromACE()))
			return;
		// We could use INSERT IGNORE on mysql
		addToList(FLD_JOINT_RIGHTS, right.getId());
	}
	
	/**
	 * 
	 * @param ace
	 *            revokes the respective right, all associated child rights if applicable
	 */
	public void revokeAccessRight(ACE ace){		
		ace.getChildren(true).stream().map(p -> Right.getOrCreateRightByACE(p))
			.forEach(r -> removeFromList(FLD_JOINT_RIGHTS, r.getId()));
	}
	
}
