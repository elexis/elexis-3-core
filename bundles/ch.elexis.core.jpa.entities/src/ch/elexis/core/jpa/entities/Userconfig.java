package ch.elexis.core.jpa.entities;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.OptimisticLocking;
import org.eclipse.persistence.annotations.OptimisticLockingType;

import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.QueryHint;
import jakarta.persistence.Table;

@Entity
@Table(name = "userconfig")
@EntityListeners(EntityWithIdListener.class)
@OptimisticLocking(type = OptimisticLockingType.SELECTED_COLUMNS, selectedColumns = { @Column(name = "LASTUPDATE") })
@IdClass(UserconfigId.class)
@Cache(expiry = 15000)
@NamedQuery(name = "Userconfig.ownerid.param", query = "SELECT uc FROM Userconfig uc WHERE uc.ownerId = :ownerid AND uc.param = :param", hints = {
		@QueryHint(name = "eclipselink.query-results-cache", value = "true"),
		@QueryHint(name = "eclipselink.query-results-cache.size", value = "500"),
		@QueryHint(name = "eclipselink.query-results-cache.expiry", value = "15000") })
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
	public String getId() {
		return getOwnerId() + "_" + getParam();
	}

	@Override
	public void setId(String id) {
		setParam(id);
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
