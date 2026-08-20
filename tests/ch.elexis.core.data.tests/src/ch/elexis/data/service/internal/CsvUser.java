package ch.elexis.data.service.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IXid;

public class CsvUser implements IUser {

	private static Logger logger = LoggerFactory.getLogger(IUser.class);

	private String username;
	private String password;
	private IContact userContact;
	private List<IRole> roles = new ArrayList<>();

	private String[] csvRows;

	public CsvUser(String[] csvRows) {
		this.csvRows = csvRows;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(String username) {
		// not relevant
	}

	@Override
	public String getHashedPassword() {
		return password;
	}

	@Override
	public void setHashedPassword(String password) {
		// not relevant
	}

	@Override
	public String getAssociatedContactId() {
		return userContact != null ? userContact.getId() : null;
	}

	@Override
	public IContact getAssignedContact() {
		return userContact;
	}

	@Override
	public void setAssignedContact(IContact contact) {
		// not relevant
	}

	@Override
	public List<IRole> getRoles() {
		return roles;
	}

	@Override
	public List<String> getRoleIds() {
		return getRoles().stream().map(IRole::getId).toList();
	}

	@Override
	public IRole addRole(IRole role) {
		// not relevant
		return null;
	}

	@Override
	public void removeRole(IRole role) {
		// not relevant
	}

	@Override
	public String getSalt() {
		// not relevant
		return null;
	}

	@Override
	public void setSalt(String salt) {
		// not needed
	}

	@Override
	public boolean isActive() {
		// not relevant
		return true;
	}

	@Override
	public void setActive(boolean active) {
		// not relevant
	}

	@Override
	public boolean isAllowExternal() {
		// not relevant
		return true;
	}

	@Override
	public void setAllowExternal(boolean allowExternal) {
		// not relevant

	}

	@Override
	public boolean isAdministrator() {
		// not relevant
		return false;
	}

	@Override
	public void setAdministrator(boolean value) {
		// not relevant
	}

	@Override
	public String getLabel() {
		return getId();
	}

	@Override
	public boolean isDeleted() {
		// not relevant
		return false;
	}

	@Override
	public void setDeleted(boolean value) {
		// not relevant
	}

	@Override
	public String getId() {
		return username;
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// not relevant
		return true;
	}

	@Override
	public IXid getXid(String domain) {
		// not relevant
		return null;
	}

	@Override
	public Long getLastupdate() {
		// not relevant
		return null;
	}

	@Override
	public IUser login(String inUsername, char[] inPassword) {
		if (csvRows != null) {
			for (String row : csvRows) {
				String splits[] = row.split(":");
				if (splits.length > 3) {
					if (splits[0].equals(inUsername) && Arrays.equals(splits[1].toCharArray(), inPassword)) {
						IContact contact = CoreModelServiceHolder.get().load(splits[2], IContact.class).orElse(null);
						if (contact != null) {
							for (String roleName : splits[3].split(",")) {
								CoreModelServiceHolder.get().load(roleName, IRole.class).ifPresent(this.roles::add);
							}
							this.username = splits[0];
							this.password = splits[1];
							this.userContact = contact;
							logger.info("Csv login successful");
							return this;
						} else {
							logger.info("Csv contact not found for id: " + splits[3]);
						}
					}
				}
			}
			logger.info("Csv user not found");
		}
		return null;
	}

	@Override
	public boolean isInternal() {
		return false;
	}

	@Override
	public void setRoles(List<IRole> roles) {
		// TODO Auto-generated method stub

	}

}
