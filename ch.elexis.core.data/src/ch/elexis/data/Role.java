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
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.jdt.Nullable;
import ch.rgw.tools.JdbcLink.Stm;

public class Role extends PersistentObject {
	
	public static final String TABLENAME = "ROLE";
	public static final String FLD_SYSTEM_ROLE = "ISSYSTEMROLE";
	public static final String FLD_EXT_I18N_LABEL = "LAB_" + Locale.getDefault().getLanguage();
	public static final String FLD_JOINT_RIGHTS = "Rights";
	
	public static final String SYSTEMROLE_LITERAL_USER = "user";
	public static final String SYSTEMROLE_LITERAL_DOCTOR = "doctor";
	public static final String SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR = "executive_doctor";
	public static final String SYSTEMROLE_LITERAL_ASSISTANT = "assistant";
	public static final String SYSTEMROLE_LITERAL_USER_EXTERNAL = "user_external";
	public static final String SYSTEMROLE_LITERAL_PATIENT = "patient";
	
	static {
		addMapping(TABLENAME, FLD_ID, FLD_SYSTEM_ROLE, FLD_EXTINFO,
			FLD_JOINT_RIGHTS + "=LIST:ROLE_ID:ROLE_RIGHT_JOINT");
			
		if (!tableExists(TABLENAME)) {
			executeDBInitScriptForClass(Role.class, null);
			ACE.initializeACEDefaults(false);
		}
	}
	
	public Role(){}
	
	public Role(boolean isSystemRole) {
		create(null);
		
		setSystemRole(false);
	}
	
	protected Role(final String id){
		super(id);
	}
	
	public static Role load(final String id){
		return new Role(id);
	}
	
	@Override
	public String getLabel(){
		return get(FLD_ID);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public boolean isSystemRole(){
		return getBoolean(FLD_SYSTEM_ROLE);
	}
	
	public void setSystemRole(boolean val) {
		// ignored, for databinding only
	}
	
	public String getRoleName(){
		return get(FLD_ID);
	}
	
	/**
	 * renames the role, which effectively changes the id resulting in a new object returned
	 * 
	 * @param rolename
	 * @return
	 */
	public @Nullable Role setRoleName(String rolename){
		if (verifyRoleNameNotTaken(rolename)) {
			List<ACE> ar = Arrays.asList(getAssignedAccessRights());
			ar.stream().forEachOrdered(a -> revokeAccessRight(a));
			
			set(FLD_ID, rolename);
			Role r = Role.load(rolename);
			ar.stream().forEachOrdered(a -> r.grantAccessRight(a));
			return r;
		}
		return null;
	}
	
	/**
	 * verify whether the proposed rolename is not already in use
	 * 
	 * @param rolename
	 * @return <code>true</code> if the given rolename is available for use
	 */
	public static boolean verifyRoleNameNotTaken(String rolename){
		return new Query<Role>(Role.class, FLD_ID, rolename).execute().size() == 0;
	}
	
	public String getTranslatedLabel(){
		return (String) getExtInfoStoredObjectByKey(FLD_EXT_I18N_LABEL);
	}
	
	public void setTranslatedLabel(String translatedLabel){
		setExtInfoStoredObjectByKey(FLD_EXT_I18N_LABEL, translatedLabel);
	}
	
	public ACE[] getAssignedAccessRights(){
		ACE[] array = ACE.getAllDefinedACElements().stream()
			.filter(p -> CoreHub.acl.request(this, p)).toArray(size -> new ACE[size]);
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
			.forEachOrdered(r -> removeFromList(FLD_JOINT_RIGHTS, r.getId()));
	}
	
	@Override
	public boolean delete(){
		if (isSystemRole())
			return false;
			
		Arrays.asList(getAssignedAccessRights()).stream().forEachOrdered(a -> revokeAccessRight(a));
		
		Stm stm = getConnection().getStatement();
		int res = stm.exec("DELETE FROM " + TABLENAME + " WHERE ID=" + getWrappedId());
		getConnection().releaseStatement(stm);
		return res == 1;
	}

	/**
	 * Revokes all rights of this role
	 */
	public void revokeAllRightsForRole(){
		Stm stm = getConnection().getStatement();
		stm.exec("DELETE FROM ROLE_RIGHT_JOINT WHERE ROLE_ID=" + getWrappedId());
		getConnection().releaseStatement(stm);
	}
}
