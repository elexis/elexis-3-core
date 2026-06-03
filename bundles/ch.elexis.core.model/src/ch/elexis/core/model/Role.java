package ch.elexis.core.model;

import java.util.Map;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;

public class Role extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Role>
		implements IdentifiableWithXid, IRole {

	public Role(ch.elexis.core.jpa.entities.Role entity) {
		super(entity);
	}

	@Override
	public void setId(String id) {
		getEntityMarkDirty().setId(id);
	}

	@Override
	public boolean isSystemRole() {
		return getEntity().isSystemRole();
	}

	@Override
	public void setSystemRole(boolean value) {
		getEntityMarkDirty().setSystemRole(value);
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
