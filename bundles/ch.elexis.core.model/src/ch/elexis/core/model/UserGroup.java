package ch.elexis.core.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.elexis.core.jpa.entities.Role;
import ch.elexis.core.jpa.entities.User;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;

public class UserGroup extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.UserGroup>
		implements IdentifiableWithXid, IUserGroup {

	public UserGroup(ch.elexis.core.jpa.entities.UserGroup entity) {
		super(entity);
	}

	@Override
	public List<IRole> getRoles() {
		return getEntity().getRoles().parallelStream().map(r -> ModelUtil.getAdapter(r, IRole.class))
				.collect(Collectors.toList());
	}

	@Override
	public IRole addRole(IRole role) {
		if (role instanceof AbstractIdDeleteModelAdapter) {
			Set<Role> roles = new HashSet<Role>(getEntity().getRoles());
			roles.add((Role) ((AbstractIdDeleteModelAdapter<?>) role).getEntity());
			getEntityMarkDirty().setRoles(roles);
		}
		return role;
	}

	@Override
	public void removeRole(IRole role) {
		if (role instanceof AbstractIdDeleteModelAdapter) {
			Set<Role> roles = new HashSet<Role>(getEntity().getRoles());
			roles.remove(((AbstractIdDeleteModelAdapter<?>) role).getEntity());
			getEntityMarkDirty().setRoles(roles);
		}
	}

	@Override
	public List<IUser> getUsers() {
		return getEntity().getUsers().parallelStream().map(r -> ModelUtil.getAdapter(r, IUser.class))
				.collect(Collectors.toList());
	}

	@Override
	public IUser addUser(IUser user) {
		if (user instanceof AbstractIdDeleteModelAdapter) {
			Set<User> users = new HashSet<User>(getEntity().getUsers());
			users.add((User) ((AbstractIdDeleteModelAdapter<?>) user).getEntity());
			getEntityMarkDirty().setUsers(users);
		}
		return user;
	}

	@Override
	public void removeUser(IUser user) {
		if (user instanceof AbstractIdDeleteModelAdapter) {
			Set<User> users = new HashSet<User>(getEntity().getUsers());
			users.remove(((AbstractIdDeleteModelAdapter<?>) user).getEntity());
			getEntityMarkDirty().setUsers(users);
		}
	}

	@Override
	public String getGroupname() {
		return getEntity().getId();
	}

	@Override
	public void setGroupname(String value) {
		getEntityMarkDirty().setId(value);
	}

	@Override
	public String getLabel() {
		return getId();
	}

	@Override
	public Object getExtInfo(Object key) {
		return extInfoHandler.getExtInfo(key);
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		extInfoHandler.setExtInfo(key, value);
	}

	@Override
	public Map<Object, Object> getMap() {
		return extInfoHandler.getMap();
	}
}
