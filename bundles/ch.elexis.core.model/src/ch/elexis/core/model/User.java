package ch.elexis.core.model;

import java.util.List;

import ch.elexis.core.jpa.entities.Kontakt;
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
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSalt(){
		return getEntity().getSalt();
	}

	@Override
	public void setSalt(String value){
		getEntity().setSalt(value);
	}
}
