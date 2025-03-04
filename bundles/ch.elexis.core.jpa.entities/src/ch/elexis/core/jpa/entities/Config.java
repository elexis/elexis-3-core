package ch.elexis.core.jpa.entities;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.OptimisticLocking;
import org.eclipse.persistence.annotations.OptimisticLockingType;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "config")
@EntityListeners(EntityWithIdListener.class)
@OptimisticLocking(type = OptimisticLockingType.SELECTED_COLUMNS, selectedColumns = { @Column(name = "LASTUPDATE") })
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
	public String getId() {
		return getParam();
	}

	@Override
	public void setId(String id) {
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
	public Long getLastupdate() {
		return lastupdate;
	}

	@Override
	public void setLastupdate(Long lastupdate) {
		this.lastupdate = lastupdate;
	}
}
