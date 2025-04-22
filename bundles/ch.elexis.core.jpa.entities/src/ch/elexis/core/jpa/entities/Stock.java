package ch.elexis.core.jpa.entities;

import java.util.List;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "STOCK")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000)
public class Stock extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	/**
	 * The stock priority. That is, if an article exists in multiple stocks it will
	 * be drawn in order of stock priority - with 0 being highest.
	 *
	 * May be overriden by {@link #owner}
	 */
	@Column(nullable = false, unique = true)
	int priority;

	/**
	 * A short name or code for this stock. If the code matches <code>P[0-9]+</code>
	 * it is a patient located stock.
	 */
	@Column(length = 7, unique = true)
	String code;

	/**
	 * Additional description text.
	 */
	@Column(length = 255)
	String description;

	/**
	 * A description of the physical location of this stock.
	 */
	@Column(length = 255)
	String location;

	/**
	 * If not <code>null</code> defines this as a private stock for a mandator. Only
	 * services accounted to this mandator may draw articles from this stock. If a
	 * private stock exists for a mandator, it will always be preferred in drawing
	 * articles.
	 */
	@OneToOne
	@JoinColumn(name = "OWNER")
	Kontakt owner;

	/**
	 * The contact responsible for managing this stock
	 */
	@OneToOne
	@JoinColumn(name = "RESPONSIBLE", insertable = false)
	Kontakt responsible;

	/**
	 * The UUID of a stock commissioning service for this stock. If not a machine,
	 * <code>null</code>
	 */
	@Column(name = "driver_uuid", length = 64)
	String driverUuid;

	/**
	 * The configuration for the driver identified by its UUID. <code>null</code> if
	 * not a machine
	 */
	@Lob
	@Column(name = "driver_config")
	String driverConfig;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "stock", cascade = CascadeType.REMOVE)
	private List<StockEntry> entries;

	@Override
	public String toString() {
		return super.toString() + " code=[" + getCode() + "] driverUuid=[" + getDriverUuid() + "] driverConfig=["
				+ getDriverConfig() + "]";
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Kontakt getOwner() {
		return owner;
	}

	public void setOwner(Kontakt owner) {
		this.owner = owner;
	}

	public Kontakt getResponsible() {
		return responsible;
	}

	public void setResponsible(Kontakt responsible) {
		this.responsible = responsible;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDriverUuid() {
		return driverUuid;
	}

	public void setDriverUuid(String driverUuid) {
		this.driverUuid = driverUuid;
	}

	public String getDriverConfig() {
		return driverConfig;
	}

	public void setDriverConfig(String driverConfig) {
		this.driverConfig = driverConfig;
	}

	public List<StockEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<StockEntry> entries) {
		this.entries = entries;
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
