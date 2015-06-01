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

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;

import ch.elexis.core.jdt.Nullable;
import ch.rgw.tools.PasswordEncryptionService;

public class User extends PersistentObject {
	
	public static final String TABLENAME = "USER_";
	public static final String USERNAME_ADMINISTRATOR = "Administrator";
	
	public static final String FLD_IS_ACTIVE = "IS_ACTIVE";
	public static final String FLD_IS_ADMINISTRATOR = "IS_ADMINISTRATOR";
	public static final String FLD_ASSOC_CONTACT = "KONTAKT_ID";
	public static final String FLD_HASHED_PASSWORD = "HASHED_PASSWORD";
	public static final String FLD_SALT = "SALT";
	public static final String FLD_KEYSTORE = "KEYSTORE";
	public static final String FLD_JOINT_ROLES = "Roles";
	
	private static PasswordEncryptionService pes = new PasswordEncryptionService();
	
	static {
		addMapping(TABLENAME, 
			FLD_IS_ACTIVE, 
			FLD_IS_ADMINISTRATOR, 
			FLD_ASSOC_CONTACT, 
			FLD_HASHED_PASSWORD, 
			FLD_SALT, 
			FLD_KEYSTORE,
			FLD_JOINT_ROLES + "=LIST:USER_ID:USER_ROLE_JOINT");
		
		if(!tableExists(TABLENAME)) {
			executeDBInitScriptForClass(User.class, null);
			User.migrateToNewStructure();
		}
	}
	
	public User(){}
	
	/**
	 * Every new {@link User} is assigned the {@link Role#ROLE_LITERAL_USER}
	 * 
	 * @param anw
	 * @param username
	 * @param password
	 */
	public User(Anwender anw, String username, String password){
		create(username);
		setAssignedContact(anw);
		setPassword(password);
		
		setAssignedRole(Role.load(Role.ROLE_LITERAL_USER), true);
	}
	
	protected User(final String id){
		super(id);
	}
	
	public static User load(final String id){
		return new User(id);
	}
	
	/**
	 * Transfer existing users into the new separated table.<br>
	 * Every {@link Anwender} is automatically assigned to the role {@link Role#ROLE_LITERAL_USER}.
	 * Every {@link Mandant} is additionally assigned to the role
	 * {@link Role#ROLE_LITERAL_EXECUTIVE_DOCTOR}.
	 * 
	 * @see https://redmine.medelexis.ch/issues/771
	 */
	private static void migrateToNewStructure(){
		new Role(); // to call the static init header
		
		Query<Anwender> qbe = new Query<Anwender>(Anwender.class);
		List<Anwender> users = qbe.execute();
		for (Anwender anwender : users) {
			String username = anwender.get(Kontakt.FLD_NAME3);
			String password = (String) anwender.getExtInfoStoredObjectByKey("UsrPwd");
			User u;
			if (username.equals(USERNAME_ADMINISTRATOR)) {
				u = User.load(USERNAME_ADMINISTRATOR);
				u.setPassword(password);
				u.setAssignedContact(anwender);
			} else {
				u = new User(anwender, username, password);
			}
			boolean isMandator = anwender.getBoolean(Anwender.FLD_IS_MANDATOR);
			if (isMandator) {
				u.setAssignedRole(Role.load(Role.ROLE_LITERAL_EXECUTIVE_DOCTOR), true);
			}
			
			// TODO delete the information from contact table?
		}
	}
	
	/**
	 * 
	 * @return
	 * @since 3.1
	 */
	public List<Role> getAssignedRoles() {
		List<String> roles = getList(FLD_JOINT_ROLES, false);
		return roles.stream().map(p -> Role.load(p)).collect(Collectors.toList());
	}
	
	/**
	 * 
	 * @param role
	 * @since 3.1
	 */
	public void setAssignedRole(Role role, boolean isAssigned) {
		List<Role> assignedRoles = getAssignedRoles();
		if(isAssigned) {
			if(assignedRoles.contains(role)) return;
			addToList(FLD_JOINT_ROLES, role.getId());
		} else {
			if(!assignedRoles.contains(role)) return;
			removeFromList(FLD_JOINT_ROLES, role.getId());
		}
	}
	
	/**
	 * 
	 * @param attemptedPassword
	 * @return
	 */
	public boolean verifyPassword(String attemptedPassword) {
		boolean ret = false;
		String[] values = get(false, FLD_HASHED_PASSWORD, FLD_SALT);
		try {
			ret = pes.authenticate(attemptedPassword, values[0], values[1]);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
			log.error("Error verifying password", e);
		}
		return ret;
	}
	
	public String getUsername() {
		return getId();
	}
	
	public void setUsername(String username) {
		set(FLD_ID, username);
	}
	
	/** Passwort setzen */
	public void setPassword(final String pwd){
		try {
			String salt = pes.generateSaltAsHexString();
			String hashed_pw = pes.getEncryptedPasswordAsHexString(pwd, salt);
			set(new String[] {
				FLD_SALT, FLD_HASHED_PASSWORD
			}, salt, hashed_pw);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
			log.error("Error setting password for contact", e);
		}
	}
	
	public void setAssignedContact(Kontakt contact){
		set(FLD_ASSOC_CONTACT, contact.getId());
	}
	
	/**
	 * @return contact, castable to {@link Anwender}
	 */
	public @Nullable String getAssignedContactId() {
		return get(FLD_ASSOC_CONTACT);
	}
	
	public boolean isAdministrator() {
		return getBoolean(FLD_IS_ADMINISTRATOR);
	}
	
	public void setAdministrator(boolean val) {
		set(FLD_IS_ADMINISTRATOR, ts(val));
	}
	
	@Override
	public String getLabel(){
		return getId();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}

	public @Nullable Anwender getAssignedContact(){
		String assocId = getAssignedContactId();
		if(assocId!=null && assocId.length()>1) {
			return Anwender.load(assocId); 
		}
		return null;
	}

	public boolean isActive(){
		return getBoolean(FLD_IS_ACTIVE);
	}
	
	public void setActive(boolean val) {
		set(FLD_IS_ACTIVE, ts(val));
	}
}
