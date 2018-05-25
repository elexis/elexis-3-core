package ch.elexis.core.jpa.entities;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;

@Entity
@Table(name = "ROLE")
public class Role extends AbstractDBObjectIdDeletedExtInfo {

	@Convert(converter = BooleanCharacterConverterSafe.class)
	@Column(name = "ISSYSTEMROLE")
	protected boolean systemRole;

	@ManyToMany
    @JoinTable(name="ROLE_RIGHT_JOINT",
                joinColumns=
                     @JoinColumn(name="ID"),
                inverseJoinColumns=
                     @JoinColumn(name="ROLE_ID")
    )
	protected Collection<Right> rights;
	
	public boolean isSystemRole() {
		return systemRole;
	}

	public void setSystemRole(boolean systemRole) {
		this.systemRole = systemRole;
	}
	
	public Collection<Right> getRights() {
		return rights;
	}
	
	public void setRights(Collection<Right> rights) {
		this.rights = rights;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
