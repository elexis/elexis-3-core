package ch.elexis.core.eenv;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IXid;
import ch.elexis.core.services.IModelService;

public class KeycloakUser implements IUser {

	private final IModelService coreModelService;

	private final String preferredUsername;
	private final String name;
	private final String familyName;
	private final String assignedContactId;
	private final long issueTime;
	private final long expirationTime;
	private final Set<String> roles;

	/**
	 * Keycloak Represented User. This users data is populated via incoming JTW
	 * token. It maps to the Elexis user world employing the core model service.
	 * 
	 * @param coreModelService
	 * @param preferredUsername jwt preferred_username
	 * @param name              jwt name
	 * @param familyName        jwt family_name
	 * @param issueTime         jwt iat - time (in seconds since Unix epoch)
	 * @param expirationTime    jwt exp - time (in seconds since Unix epoch)
	 * @param roles
	 */
	public KeycloakUser(IModelService coreModelService, String preferredUsername, String name, String familyName,
			long issueTime, long expirationTime, String assignedContactId, Set<String> roles) {

		this.preferredUsername = preferredUsername;
		this.name = name;
		this.familyName = familyName;
		this.assignedContactId = assignedContactId;
		this.issueTime = issueTime;
		this.expirationTime = expirationTime;
		this.roles = roles;

		this.coreModelService = coreModelService;
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
		return name + " " + familyName + " (" + getId() + ")";
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
	public IContact getAssignedContact() {
		return coreModelService.load(assignedContactId, IContact.class).orElse(null);
	}

	@Override
	public void setAssignedContact(IContact value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IRole> getRoles() {
		List<IRole> _roles = new ArrayList<IRole>();
		roles.forEach(rs -> coreModelService.load(rs, IRole.class).ifPresent(r -> _roles.add(r)));
		return _roles;
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
		// TODO Auto-generated method stub
		return false;
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
		return false;
	}

}
