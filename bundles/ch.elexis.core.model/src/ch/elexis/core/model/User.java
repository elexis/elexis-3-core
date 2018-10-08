package ch.elexis.core.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.Role;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.util.ModelUtil;

public class User extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.User>
		implements IdentifiableWithXid, IUser {
	
	public User(ch.elexis.core.jpa.entities.User entity){
		super(entity);
	}
	
	@Override
	public String getUsername(){
		return getEntity().getId();
	}
	
	@Override
	public void setUsername(String value){
		getEntity().setId(value);
	}
	
	@Override
	public String getHashedPassword(){
		return getEntity().getHashedPassword();
	}
	
	@Override
	public void setHashedPassword(String value){
		getEntity().setHashedPassword(value);
	}
	
	@Override
	public IContact getAssignedContact(){
		return ModelUtil.getAdapter(getEntity().getKontakt(), IContact.class);
	}
	
	@Override
	public void setAssignedContact(IContact value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntity().setKontakt((Kontakt) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntity().setKontakt(null);
		}
	}
	
	@Override
	public List<IRole> getRoles(){
		return getEntity().getRoles().parallelStream()
			.map(r -> ModelUtil.getAdapter(r, IRole.class)).collect(Collectors.toList());
	}
	
	@Override
	public IRole addRole(IRole role){
		if (role instanceof AbstractIdDeleteModelAdapter) {
			Set<Role> roles = new HashSet<Role>(getEntity().getRoles());
			roles.add((Role) ((AbstractIdDeleteModelAdapter<?>) role).getEntity());
			getEntity().setRoles(roles);
		}
		return role;
	}
	
	@Override
	public void removeRole(IRole role){
		if (role instanceof AbstractIdDeleteModelAdapter) {
			Set<Role> roles = new HashSet<Role>(getEntity().getRoles());
			roles.remove((Role) ((AbstractIdDeleteModelAdapter<?>) role).getEntity());
			getEntity().setRoles(roles);
		}
	}
	
	@Override
	public String getSalt(){
		return getEntity().getSalt();
	}
	
	@Override
	public void setSalt(String value){
		getEntity().setSalt(value);
	}
	
	@Override
	public boolean isActive(){
		return getEntity().isActive();
	}
	
	@Override
	public void setActive(boolean value){
		getEntity().setActive(value);
	}
	
	@Override
	public boolean isAllowExternal(){
		return getEntity().isAllowExternal();
	}
	
	@Override
	public void setAllowExternal(boolean value){
		getEntity().setAllowExternal(value);
		
	}

	@Override
	public boolean isAdministrator(){
		return getEntity().isAdministrator();
	}

	@Override
	public void setAdministrator(boolean value){
		getEntity().setAdministrator(value);
	}
}
