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
import org.apache.commons.lang.StringUtils;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;

import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.RoleConstants;
import ch.rgw.tools.PasswordEncryptionService;
import ch.rgw.tools.StringTool;

public class User extends PersistentObject {
	
	public static final String TABLENAME = "USER_";
	public static final String USERNAME_ADMINISTRATOR = "Administrator";
	
	public static final String FLD_IS_ACTIVE = "IS_ACTIVE";
	public static final String FLD_IS_ADMINISTRATOR = "IS_ADMINISTRATOR";
	public static final String FLD_ALLOW_EXTERNAL = "ALLOW_EXTERNAL";
	public static final String FLD_ASSOC_CONTACT = "KONTAKT_ID";
	public static final String FLD_HASHED_PASSWORD = "HASHED_PASSWORD";
	public static final String FLD_SALT = "SALT";
	public static final String FLD_KEYSTORE = "KEYSTORE";
	public static final String FLD_TOTP = "TOTP";
	public static final String FLD_JOINT_ROLES = "Roles";
	
	private static PasswordEncryptionService pes = new PasswordEncryptionService();
	
	static {
		addMapping(TABLENAME, FLD_ID, FLD_IS_ACTIVE, FLD_IS_ADMINISTRATOR, FLD_ASSOC_CONTACT,
			FLD_HASHED_PASSWORD, FLD_SALT, FLD_KEYSTORE, FLD_TOTP, FLD_ALLOW_EXTERNAL,
			FLD_JOINT_ROLES + "=LIST:USER_ID:USER_ROLE_JOINT");
			
		initTables();
	}
	
	protected static void initTables() {
		if (!tableExists(TABLENAME)) {
			executeDBInitScriptForClass(User.class, null);
			User.migrateToNewStructure();
		}
	}
	
	public User(){}
	
	/**
	 * Every new {@link User} is assigned the {@link Role#SYSTEMROLE_LITERAL_USER}
	 * 
	 * @param anw
	 * @param username
	 * @param password
	 */
	public User(Anwender anw, String username, String password){
		create(username);
		setAssignedContact(anw);
		if (password == null || password.length() == 0) {
			password = StringTool.unique("pswd");
		}
		setPassword(password);
		
		setAssignedRole(Role.load(RoleConstants.SYSTEMROLE_LITERAL_USER), true);
	}
	
	protected User(final String id){
		super(id);
	}
	
	/**
	 * Generates a user object, which is not necessarily backed with a real db
	 * object, use {@link #exists()} to check.
	 * 
	 * @param id
	 * @return
	 */
	public static @NonNull User load(final String id) {
		return new User(id);
	}
	
	/**
	 * Transfer existing users into the new separated table.<br>
	 * Every {@link Anwender} is automatically assigned to the role {@link Role#SYSTEMROLE_LITERAL_USER}.
	 * Every {@link Mandant} is additionally assigned to the role
	 * {@link Role#SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR}.
	 * 
	 * @see https://redmine.medelexis.ch/issues/771
	 */
	private static void migrateToNewStructure(){
		Role.initTables();
		
		log.info("Starting migration to new user structure");
		
		Query<Anwender> qbe = new Query<Anwender>(Anwender.class);
		List<Anwender> users = qbe.execute();
		for (Anwender anwender : users) {
			String username = anwender.get(Kontakt.FLD_NAME3);
			if(username==null || username.length()==0) {
				log.warn("Username for Anwender "+anwender.getLabel()+" not set. Skipping user creation.");
				continue;
			}
			
			String password = (String) anwender.getExtInfoStoredObjectByKey("UsrPwd");
			boolean setActive=true;
			if(password==null || password.length()==0) {
				password = "pass";
				log.warn("Password for Anwender "+anwender.getLabel()+" is empty, setting 'pass' and deactivating user.");
				setActive = false;
			}
			
			User u;
			if (username.equals(USERNAME_ADMINISTRATOR)) {
				u = User.load(USERNAME_ADMINISTRATOR);
				u.setAssignedContact(anwender);
				u.setPassword(password);
				log.info("Overriding Administrator password with password from anwender [{}]",
					anwender.getLabel());
			} else {
				u = new User(anwender, username, password);
			}
			u.setActive(setActive);
			
			boolean isMandator = anwender.getBoolean(Anwender.FLD_IS_MANDATOR);
			if (isMandator) {
				u.setAssignedRole(Role.load(RoleConstants.SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR), true);
				u.setAssignedRole(Role.load(RoleConstants.SYSTEMROLE_LITERAL_DOCTOR), true);
			}
			
			log.info("Migrated anwender [{}] to new user structure with id [{}]",
				anwender.getLabel(), u.getId());
			
			// TODO delete the information from contact table?
		}
	}
	
	/**
	 * Get the time-based One-time Password secret (initializes a secret if none yet set)
	 * 
	 * @return
	 * @since 3.6
	 */
	public String getTotp(){
		String totp = get(FLD_TOTP);
		if (StringUtils.isEmpty(totp)) {
			resetTotp();
		}
		return totp;
	}
	
	/**
	 * Verify an otp token for this user
	 * @param otp
	 * @return
	 * @since 3.6
	 */
	public boolean verifyTotp(String otp) {
		Totp totp = new Totp(getTotp());
		return totp.verify(otp);
	}
	
	/**
	 * Reset the time-based One-time Password secret
	 * @since 3.6
	 */
	public void resetTotp() {
		String totp = Base32.random();
		set(FLD_TOTP, totp);
	}
	
	/**
	 * 
	 * @return
	 * @since 3.1
	 */
	public List<Role> getAssignedRoles(){
		List<String> roles = getList(FLD_JOINT_ROLES, false);
		return roles.stream().map(p -> Role.load(p)).collect(Collectors.toList());
	}
	
	/**
	 * 
	 * @param role
	 * @since 3.1
	 */
	public void setAssignedRole(Role role, boolean isAssigned){
		List<Role> assignedRoles = getAssignedRoles();
		if (isAssigned) {
			if (assignedRoles.contains(role))
				return;
			addToList(FLD_JOINT_ROLES, role.getId());
		} else {
			if (!assignedRoles.contains(role))
				return;
			removeFromList(FLD_JOINT_ROLES, role.getId());
		}
	}
	
	/**
	 * 
	 * @param attemptedPassword
	 * @return
	 */
	public boolean verifyPassword(String attemptedPassword){
		boolean ret = false;
		String[] values = get(false, FLD_HASHED_PASSWORD, FLD_SALT);
		try {
			ret = pes.authenticate(attemptedPassword, values[0], values[1]);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
			log.error("Error verifying password", e);
		}
		return ret;
	}
	
	public String getUsername(){
		return get(FLD_ID);
	}
	
	/**
	 * verify whether the proposed username is not already in use
	 * 
	 * @param username
	 * @return <code>true</code> if the given username may be used
	 */
	public static boolean verifyUsernameNotTaken(String username){
		Query<User> qbe = new Query<User>(User.class);
		qbe.clear(true);
		qbe.add(User.FLD_ID, Query.EQUALS, username);
		return qbe.execute().size() == 0;
	}
	
	/**
	 * set the new username, where the username is equivalent to the ID, invalidates the given
	 * object returning the new one
	 * 
	 * @param username
	 * @return the new {@link User} object as a result of the rename
	 */
	public @Nullable User setUsername(String username){
		if (verifyUsernameNotTaken(username)) {
			// we have to re-target the assigned roles to the new username
			List<Role> assignedRoles = getAssignedRoles();
			assignedRoles.stream().forEachOrdered(r -> setAssignedRole(r, false));
			set(FLD_ID, username);
			User u = User.load(username);
			assignedRoles.stream().forEachOrdered(r -> u.setAssignedRole(r, true));
			return u;
		}
		return null;
	}
	
	/**
	 * set the password
	 * @param pwd
	 */
	public void setPassword(@NonNull final String pwd){
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
	
	public void setAssignedContact(@Nullable Kontakt contact){
		if (contact == null)
			return;
		set(FLD_ASSOC_CONTACT, contact.getId());
	}
	
	/**
	 * @return contact, castable to {@link Anwender}
	 */
	public @Nullable String getAssignedContactId(){
		return get(FLD_ASSOC_CONTACT);
	}
	
	public boolean isAdministrator(){
		return getBoolean(FLD_IS_ADMINISTRATOR);
	}
	
	public void setAdministrator(boolean val){
		set(FLD_IS_ADMINISTRATOR, ts(val));
	}
	
	@Override
	public String getLabel(){
		return getUsername();
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public @Nullable Anwender getAssignedContact(){
		String assocId = getAssignedContactId();
		if (assocId != null && assocId.length() > 1) {
			return Anwender.load(assocId);
		}
		return null;
	}
	
	public boolean isActive(){
		return getBoolean(FLD_IS_ACTIVE);
	}
	
	public void setActive(boolean val){
		set(FLD_IS_ACTIVE, ts(val));
	}
	
	@Override
	public boolean delete(){
		getAssignedRoles().stream().forEachOrdered(r -> setAssignedRole(r, false));
		return super.delete();
	}
}
