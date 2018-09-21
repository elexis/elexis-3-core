package ch.elexis.core.services;

import ch.elexis.core.model.IUser;

public interface IUserService {
	
	/**
	 * Verify the provided password for the given user
	 * 
	 * @param user
	 * @param attemptedPassword
	 * @return <code>true</code> if password matched
	 */
	public boolean verifyPassword(IUser user, String attemptedPassword);
	
	/**
	 * Set the password for a given user. Will store the password in encrypted format. Performs save
	 * operation.
	 * 
	 * @param user
	 * @param password
	 */
	public void setPasswordForUser(IUser user, String password);
	
}
