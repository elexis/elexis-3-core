package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;

public interface IUserService {

	public static String ACTIVE_USER_WORKING_FOR = "ActiveExecutiveDoctorsWorkingFor";

	/**
	 * Verify the provided password for the given user
	 *
	 * @param user
	 * @param attemptedPassword
	 * @return <code>true</code> if password matched
	 */
	public boolean verifyPassword(IUser user, char[] attemptedPassword);

	/**
	 * Set the password for a given user. Will store the password in encrypted
	 * format. Performs save operation.
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
	public Set<IMandator> getExecutiveDoctorsWorkingFor(IContact user);

	/**
	 * Add or remove the {@link IMandator} to the list of mandators the user
	 * {@link IContact} is working for. Depending on the add parameter add or remove
	 * is performed.
	 * 
	 * @param user
	 * @param mandator
	 * @param add
	 */
	public void addOrRemoveExecutiveDoctorWorkingFor(IContact user, IMandator mandator, boolean add);

	/**
	 * Retrieve the default executive doctor this user is working for.
	 *
	 * @param user contact as retrieved by {@link IUser#getAssignedContact()}
	 * @return
	 */
	public Optional<IMandator> getDefaultExecutiveDoctorWorkingFor(IContact user);

	/**
	 * Set the default executive doctor this user is working for.
	 * 
	 * @param userContact
	 * @param mandator
	 */
	public void setDefaultExecutiveDoctorWorkingFor(IContact userContact, IMandator mandator);

	/**
	 * Retrieve the active users associated with a given contact.
	 *
	 * @param contact
	 * @return active users associated with the contact (excludes users marked as
	 *         deleted and not active)
	 */
	public List<IUser> getUsersByAssociatedContact(IContact contact);

}
