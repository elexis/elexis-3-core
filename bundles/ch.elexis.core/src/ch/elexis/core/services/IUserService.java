package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;

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
	 * Set the password for a given user. Will store the password in encrypted
	 * format. Performs save operation.
	 *
	 * @param user
	 * @param password
	 */
	public void setPasswordForUser(IUser user, String password);

	/**
	 * Retrieve the set of {@link IMandator}s this user is working for.
	 *
	 * @param user
	 * @return
	 */
	public Set<IMandator> getExecutiveDoctorsWorkingFor(IUser user, boolean includeNonActive);

	/**
	 * Retrieve the set of {@link IMandator}s this user is working for. Non active
	 * {@link IMandator}s are not included.
	 * 
	 * @param user
	 * @return
	 */
	default public Set<IMandator> getExecutiveDoctorsWorkingFor(IUser user) {
		return getExecutiveDoctorsWorkingFor(user, false);
	}

	/**
	 * Retrieve the set of {@link IMandator}s this group is working for.
	 *
	 * @param group
	 * @return
	 */
	public Set<IMandator> getExecutiveDoctorsWorkingFor(IUserGroup group, boolean includeNonActive);

	/**
	 * Retrieve the set of {@link IMandator}s this group is working for. Non active
	 * {@link IMandator}s are not included.
	 * 
	 * @param group
	 * @param includeNonActive
	 * @return
	 */
	default public Set<IMandator> getExecutiveDoctorsWorkingFor(IUserGroup group) {
		return getExecutiveDoctorsWorkingFor(group, false);
	}

	/**
	 * Add or remove the {@link IMandator} to the list of mandators the user
	 * {@link IUser} is working for. Depending on the add parameter add or remove is
	 * performed.
	 * 
	 * @param user
	 * @param mandator
	 * @param add
	 */
	public void addOrRemoveExecutiveDoctorWorkingFor(IUser user, IMandator mandator, boolean add);

	/**
	 * Add or remove the {@link IMandator} to the list of mandators the group
	 * {@link IUserGroup} is working for. Depending on the add parameter add or
	 * remove is performed.
	 * 
	 * @param user
	 * @param mandator
	 * @param add
	 */
	public void addOrRemoveExecutiveDoctorWorkingFor(IUserGroup userGroup, IMandator mandator, boolean add);

	/**
	 * Retrieve the default executive doctor this user is working for.
	 *
	 * @param user contact as retrieved by {@link IUser#getAssignedContact()}
	 * @return
	 */
	public Optional<IMandator> getDefaultExecutiveDoctorWorkingFor(IUser user);

	/**
	 * Set the default executive doctor this user is working for.
	 * 
	 * @param userContact
	 * @param mandator
	 */
	public void setDefaultExecutiveDoctorWorkingFor(IUser userContact, IMandator mandator);

	/**
	 * Retrieve the active users associated with a given contact.
	 *
	 * @param contact
	 * @return active users associated with the contact (excludes users marked as
	 *         deleted and not active)
	 */
	public List<IUser> getUsersByAssociatedContact(IContact contact);

	/**
	 * Verify whether the proposed username is not already in use
	 *
	 * @param username
	 * @return <code>true</code> if the given username may be used
	 */
	public boolean verifyUsernameNotTaken(String username);

	/**
	 * Verify whether the proposed groupname is not already in use
	 *
	 * @param groupname
	 * @return <code>true</code> if the given groupname may be used
	 */
	public boolean verifyGroupnameNotTaken(String groupname);

	/**
	 * Get the {@link IUserGroup}s the {@link IUser} is part of.
	 * 
	 * @param user
	 * @return
	 */
	public List<IUserGroup> getUserGroups(IUser user);

	/**
	 * Get the {@link IRole}s of the {@link IUser}. If the {@link IUser} is part of
	 * one or many {@link IUserGroup}s, the {@link IRole}s of the
	 * {@link IUserGroup}s are returned.
	 * 
	 * @param user
	 * @return
	 */
	public List<IRole> getUserRoles(IUser user);

	/**
	 * Applies the given set of {@link IRole} id entries to the user. Only roles
	 * known to the local system are applied. Effectively overwrites all existing
	 * role assignments.
	 * 
	 * @param user      to apply roleSet to
	 * @param userRoles will be the new total set of roles assigned to user
	 * @return the set effectively applied, differs from userRoles if certain roles
	 *         could not be applied
	 */
	public Set<String> setUserRoles(IUser user, Set<String> userRoles);

	/**
	 * Checks if the given {@link IUser} has the specified {@link IRole}
	 * 
	 * @param user
	 * @param roleId
	 * @return
	 */
	public boolean hasRole(IUser user, String roleId);

}
