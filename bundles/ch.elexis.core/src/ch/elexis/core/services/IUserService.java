package ch.elexis.core.services;

import java.util.Optional;
import java.util.Set;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;

public interface IUserService {
	
	/**
	 * Verify the provided password for the given user
	 * 
	 * @param user
	 * @param attemptedPassword
	 * @return <code>true</code> if password matched
	 */
	public boolean verifyPassword(IUser user, char[] attemptedPassword);
	
	/**
	 * Set the password for a given user. Will store the password in encrypted format. Performs save
	 * operation.
	 * 
	 * @param user
	 * @param password
	 */
	public void setPasswordForUser(IUser user, String password);
	
	/**
	 * Retrieve the set of mandators this user is working for.
	 * 
	 * @param user contact as retrieved by {@link IUser#getAssignedContact()}
	 * @return
	 */
	Set<IMandator> getExecutiveDoctorsWorkingFor(IContact user);
	
	/**
	 * Retrieve the default executive doctor this user is working for.
	 * 
	 * @param user contact as retrieved by {@link IUser#getAssignedContact()}
	 * @return
	 */
	Optional<IMandator> getDefaultExecutiveDoctorWorkingFor(IContact user);
	
}
