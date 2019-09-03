package ch.elexis.core.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.OptimisticLocking;
import org.eclipse.persistence.annotations.OptimisticLockingType;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "config")
@EntityListeners(EntityWithIdListener.class)
@OptimisticLocking(type = OptimisticLockingType.SELECTED_COLUMNS, selectedColumns = {
	@Column(name = "LASTUPDATE")
})
@Cache(expiry = 15000)
public class Config extends AbstractEntityWithId implements EntityWithId {

	// Transparently updated by the EntityListener
	protected Long lastupdate;
	
	@Id
	@Column(unique = true, nullable = false, length = 80)
	private String param;

	@Lob
	private String wert;

	@Override
	public String getId(){
		return getParam();
	}
	
	@Override
	public void setId(String id){
		setParam(id);
	}
	
	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public String getWert() {
		return wert;
	}

	public void setWert(String wert) {
		this.wert = wert;
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
