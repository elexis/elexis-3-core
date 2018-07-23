package ch.elexis.core.jpa.entities;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "STOCK")
@EntityListeners(EntityWithIdListener.class)
public class Stock implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected BigInteger lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
	/**
	 * The stock priority. That is, if an article exists in multiple stocks it
	 * will be drawn in order of stock priority - with 0 being highest.
	 * 
	 * May be overriden by {@link #owner}
	 */
	@Column(nullable = false, unique = true)
	int priority;

	/**
	 * A short name or code for this stock
	 */
	@Column(length = 3)
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
	 * If not <code>null</code> defines this as a private stock for a mandator.
	 * Only services accounted to this mandator may draw articles from this
	 * stock. If a private stock exists for a mandator, it will always be
	 * preferred in drawing articles.
	 */
	@OneToOne
	@JoinColumn(name = "OWNER", insertable = false)
	Kontakt owner;

	/**
	 * The contact responsible for managing this stock
	 */
	@OneToOne
	@JoinColumn(name = "RESPONSIBLE", insertable = false)
	Kontakt responsible;

	/**
	 * The UUID of a stock commissioning service for this stock. If not a
	 * machine, <code>null</code>
	 */
	@Column(name = "driver_uuid", length = 64)
	String driverUuid;

	/**
	 * The configuration for the driver identified by its UUID.
	 * <code>null</code> if not a machine
	 */
	@Lob
	@Column(name = "driver_config")
	String driverConfig;

	@OneToMany(cascade = CascadeType.REMOVE)
	@JoinColumn(name = "STOCK", insertable = false, updatable = false)
	protected List<StockEntry> entries;

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
	public boolean isDeleted(){
		return deleted;
	}
	
	@Override
	public void setDeleted(boolean deleted){
		this.deleted = deleted;
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public void setId(String id){
		this.id = id;
	}
	
	@Override
	public BigInteger getLastupdate(){
		return lastupdate;
	}
	
	@Override
	public void setLastupdate(BigInteger lastupdate){
		this.lastupdate = lastupdate;
	}
}
