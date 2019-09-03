package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.OptimisticLocking;
import org.eclipse.persistence.annotations.OptimisticLockingType;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "userconfig")
@EntityListeners(EntityWithIdListener.class)
@OptimisticLocking(type = OptimisticLockingType.SELECTED_COLUMNS, selectedColumns = {
	@Column(name = "LASTUPDATE")
})
@IdClass(UserconfigId.class)
@Cache(expiry = 15000)
@NamedQuery(name = "Userconfig.ownerid.param", query = "SELECT uc FROM Userconfig uc WHERE uc.ownerId = :ownerid AND uc.param = :param")
public class Userconfig extends AbstractEntityWithId implements EntityWithId {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@Column(name = "UserID")
	private String ownerId;
	
	@Id
	@Column(unique = true, nullable = false, length = 80)
	private String param;
	
	@Lob
	private String value;

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String getId(){
		return getOwnerId() + "_" + getParam();
	}
	
	@Override
	public void setId(String id){
		setParam(id);
	}
	
	@Override
	public Long getLastupdate(){
		return lastupdate != null ? lastupdate : 0L;
	}
	
	@Override
	public void setLastupdate(Long lastupdate){
		this.lastupdate = lastupdate;
	}
}
