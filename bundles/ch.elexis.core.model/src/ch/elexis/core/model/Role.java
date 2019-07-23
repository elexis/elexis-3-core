package ch.elexis.core.model;

import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;

public class Role extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Role>
		implements IdentifiableWithXid, IRole {
	
	private ExtInfoHandler extInfoHandler;
	
	public Role(ch.elexis.core.jpa.entities.Role entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
	}
	
	@Override
	public void setId(String id){
		getEntityMarkDirty().setId(id);
	}
	
	@Override
	public boolean isSystemRole(){
		return getEntity().isSystemRole();
	}
	
	@Override
	public void setSystemRole(boolean value){
		getEntityMarkDirty().setSystemRole(value);
	}

	@Override
	public List<IRight> getAssignedRights(){
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getRights().parallelStream().filter(f -> !f.isDeleted())
			.map(f -> ModelUtil.getAdapter(f, IRight.class, true)).collect(Collectors.toList());
	}
	
}
