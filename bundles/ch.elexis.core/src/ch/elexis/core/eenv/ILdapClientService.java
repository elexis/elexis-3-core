package ch.elexis.core.eenv;

import java.util.Optional;

import ch.elexis.core.model.IUser;

/**
 * Provides a client connection to the Elexis-Environment LDAP service
 */
public interface ILdapClientService {
	
	/**
	 * Find an elexis user by its userid in LDAP. It is found only if an entry in the People
	 * directory exists, that is memberOf <code>cn=ELEXIS_USER,ou=groups,{{LDAP_BASE_DN}}</code>
	 * 
	 * @param userid
	 * @return
	 */
	public Optional<String> findUserDn(String userid);
	
	/**
	 * Bind to the LDAP service using the provided user dn and password. Loads the user and its
	 * attributes if password is correct.
	 * 
	 * @param userDn
	 * @param userPassword
	 * @return an {@link IUser} if the object could be loaded using the userDns bind - which implies
	 *         that the password is correct
	 */
	public Optional<IUser> loadUserByDn(String userDn, char[] userPassword);
	
}
