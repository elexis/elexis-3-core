package ch.elexis.core.model;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.Role;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.rgw.tools.PasswordEncryptionService;

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
		getEntityMarkDirty().setId(value);
	}
	
	@Override
	public String getHashedPassword(){
		return getEntity().getHashedPassword();
	}
	
	@Override
	public void setHashedPassword(String value){
		getEntityMarkDirty().setHashedPassword(value);
	}
	
	@Override
	public IContact getAssignedContact(){
		return ModelUtil.getAdapter(getEntity().getKontakt(), IContact.class);
	}
	
	@Override
	public void setAssignedContact(IContact value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty()
				.setKontakt((Kontakt) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setKontakt(null);
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
			getEntityMarkDirty().setRoles(roles);
		}
		return role;
	}
	
	@Override
	public void removeRole(IRole role){
		if (role instanceof AbstractIdDeleteModelAdapter) {
			Set<Role> roles = new HashSet<Role>(getEntity().getRoles());
			roles.remove(((AbstractIdDeleteModelAdapter<?>) role).getEntity());
			getEntityMarkDirty().setRoles(roles);
		}
	}
	
	@Override
	public String getSalt(){
		return getEntity().getSalt();
	}
	
	@Override
	public void setSalt(String value){
		getEntityMarkDirty().setSalt(value);
	}
	
	@Override
	public boolean isActive(){
		return getEntity().isActive();
	}
	
	@Override
	public void setActive(boolean value){
		getEntityMarkDirty().setActive(value);
	}
	
	@Override
	public boolean isAllowExternal(){
		return getEntity().isAllowExternal();
	}
	
	@Override
	public void setAllowExternal(boolean value){
		getEntityMarkDirty().setAllowExternal(value);
		
	}

	@Override
	public boolean isAdministrator(){
		return getEntity().isAdministrator();
	}

	@Override
	public void setAdministrator(boolean value){
		getEntityMarkDirty().setAdministrator(value);
	}
	
	@Override
	public String getLabel(){
		return getId();
	}

	@Override
	public IUser login(String username, char[] password){
		if (isDeleted() || !username.equals(getUsername()) || !isActive()) {
			return null;
		}
		
		try {
			if (!new PasswordEncryptionService().authenticate(password, getHashedPassword(),
				getSalt())) {
				return null;
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
			LoggerFactory.getLogger(IUser.class).error("Error verifying password", e);
		}
		return this;
	}
	
	@Override
	public boolean isInternal(){
		return true;
	}
}
