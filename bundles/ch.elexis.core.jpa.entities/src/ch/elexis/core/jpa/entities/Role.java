package ch.elexis.core.jpa.entities;

import java.util.Collection;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ROLE")
@EntityListeners(EntityWithIdListener.class)
public class Role extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted, EntityWithExtInfo {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Lob
	protected byte[] extInfo;

	@Convert(converter = BooleanCharacterConverterSafe.class)
	@Column(name = "ISSYSTEMROLE")
	protected boolean systemRole;

	@ManyToMany
	@JoinTable(name = "ROLE_RIGHT_JOINT", joinColumns = @JoinColumn(name = "ROLE_ID"), inverseJoinColumns = @JoinColumn(name = "ID"))
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
	public byte[] getExtInfo() {
		return extInfo;
	}

	@Override
	public void setExtInfo(byte[] extInfo) {
		this.extInfo = extInfo;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Long getLastupdate() {
		return lastupdate;
	}

	@Override
	public void setLastupdate(Long lastupdate) {
		this.lastupdate = lastupdate;
	}
}
