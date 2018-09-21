package ch.elexis.core.model;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;

public class Role extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Role>
		implements IdentifiableWithXid, IRole {
	
	private ExtInfoHandler extInfoHandler;
	
	public Role(ch.elexis.core.jpa.entities.Role entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
	}
	
	@Override
	public void setId(String id){
		getEntity().setId(id);
	}
	
	@Override
	public boolean isSystemRole(){
		return getEntity().isSystemRole();
	}
	
	@Override
	public void setSystemRole(boolean value){
		getEntity().setSystemRole(value);
	}
	
}
