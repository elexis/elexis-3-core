package ch.elexis.core.services;

import javax.security.auth.login.LoginException;

import ch.elexis.core.model.IUser;

/**
 * Contribute a login method. Methods are consulted ordered by {@link #getPriority()}. If
 * {@link #performLogin(Object)} returns an {@link IUser} no further login contributors are
 * consulted.
 * 
 * @since 3.8
 */
public interface ILoginContributor {
	
	/**
	 * 
	 * @return the priority - higher precedence in login process
	 */
	public int getPriority();
	
	/**
	 * Try to login a user. The respective login tokens to be queried, and the way to query them are
	 * subject to the service. The service has to take care that {@link IUser#getAssignedContact()}
	 * is valid - if this is not the case, it MUST return <code>null</code>
	 * 
	 * @return an {@link IUser} object if login was successful, else <code>null</code>
	 * @param shell
	 *            an object castable to <code>org.eclipse.swt.widgets.Shell</code> or
	 *            <code>null</code>
	 * @throws LoginException
	 *             if there was an error in employing this login-service - that is, login success
	 *             could not be determined due to a certain reason
	 */
	public IUser performLogin(Object shell) throws LoginException;
	
}
