package ch.elexis.core.ee;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IXid;

public class OpenIdUser implements IUser {

	private final String preferredUsername;
	private final String name;
	private final String familyName;
	private final String associatedContactId;
	private final long issueTime;
	private final long expirationTime;
	private final Set<String> roles;

	/**
	 * This users data is populated via incoming Json Web Token.
	 *
	 * @param coreModelService
	 * @param preferredUsername   jwt preferred_username
	 * @param name                jwt name
	 * @param familyName          jwt family_name
	 * @param issueTime           jwt iat - time (in seconds since Unix epoch)
	 * @param expirationTime      jwt exp - time (in seconds since Unix epoch)
	 * @param associatedContactId the IContact this user is associated with
	 * @param roles
	 */
	public OpenIdUser(String preferredUsername, String name, String familyName, long issueTime, long expirationTime,
			String associatedContactId, Set<String> roles) {

		this.preferredUsername = preferredUsername;
		this.name = name;
		this.familyName = familyName;
		this.associatedContactId = associatedContactId;
		this.issueTime = issueTime;
		this.expirationTime = expirationTime;
		this.roles = roles;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public void setDeleted(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getId() {
		return preferredUsername;
	}

	@Override
	public String getLabel() {
		return name + StringUtils.SPACE + familyName + " (" + getId() + ")";
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		return false;
	}

	@Override
	public IXid getXid(String domain) {
		return null;
	}

	@Override
	public Long getLastupdate() {
		return issueTime * 1000;
	}

	@Override
	public String getUsername() {
		return preferredUsername;
	}

	@Override
	public void setUsername(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getHashedPassword() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setHashedPassword(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getSalt() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSalt(String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAssociatedContactId() {
		return associatedContactId;
	}

	@Override
	public IContact getAssignedContact() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAssignedContact(IContact value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getRoleIds() {
		return new ArrayList<String>(roles);
	}

	@Deprecated
	@Override
	public List<IRole> getRoles() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isActive() {
		return expirationTime > (System.currentTimeMillis() / 1000);
	}

	@Override
	public void setActive(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAllowExternal() {
		return true;
	}

	@Override
	public void setAllowExternal(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAdministrator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAdministrator(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IRole addRole(IRole role) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeRole(IRole role) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IUser login(String username, char[] password) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInternal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + getLabel() + " assignedContactId=" + associatedContactId + ", roles="
				+ roles;
	}

	@Override
	public void setRoles(List<IRole> roles) {
		throw new UnsupportedOperationException();
	}

}
