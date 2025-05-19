package ch.elexis.core.fhir.model.impl;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.CareTeam;

import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;

public class FhirUserGroup extends AbstractFhirModelAdapter<CareTeam> implements IUserGroup {

	public FhirUserGroup(CareTeam fhirResource) {
		super(fhirResource);
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public void setDeleted(boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getExtInfo(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Object, Object> getMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IUser> getUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRole> getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGroupname() {
		return getFhirResource().getName();
	}

	@Override
	public void setGroupname(String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public IRole addRole(IRole role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeRole(IRole role) {
		// TODO Auto-generated method stub

	}

	@Override
	public IUser addUser(IUser user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeUser(IUser user) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getLabel() {
		return getGroupname();
	}

	@Override
	public Class<CareTeam> getFhirType() {
		return CareTeam.class;
	}

	@Override
	public Class<?> getModelType() {
		return IUserGroup.class;
	}

}
