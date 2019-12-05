package ch.elexis.core.services;

import javax.security.auth.login.LoginException;

import ch.elexis.core.model.IUser;

public interface IExternalLoginService {
	
	/**
	 * Returns the {@link IUser} after an external login or {@link LoginException} if the login
	 * fails
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws LoginException
	 */
	public IUser login(String username, char[] password) throws LoginException;
	
}
