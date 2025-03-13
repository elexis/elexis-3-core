package ch.elexis.core.jpa.entities;

import java.time.LocalDate;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * <!-- begin-user-doc --> JPA entity representing the OUTPUT_LOG table.
 * 
 * This class represents a log entry in the database and contains metadata
 * related to various output operations. <!-- end-user-doc -->
 */
@Entity
@Table(name = "OUTPUT_LOG")
@EntityListeners(EntityWithIdListener.class)
@Cache(expiry = 15000, alwaysRefresh = false, refreshOnlyIfNewer = true)
public class OutputLogEntity extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	/**
	 * Soft delete flag to mark the entity as deleted without actually removing it
	 * from the database.
	 */
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	/**
	 * Corresponds to "ObjectType" - the type of the logged object.
	 */
	@Column(name = "OBJECTTYPE")
	private String objectType;

	/**
	 * Corresponds to "ObjectID" - the unique identifier of the logged object.
	 */
	@Column(name = "OBJECTID")
	private String objectId;

	/**
	 * Stores the creator ID, indicating who generated the log entry.
	 */
	@Column(name = "CREATORID", length = 80)
	private String creatorId;

	/**
	 * Corresponds to "Outputter" - identifies the entity responsible for the output
	 * process.
	 */
	@Column(name = "OUTPUTTER")
	private String outputter;

	/**
	 * Corresponds to "DATE" (previously "FLD_DATE") - stores the date of the log
	 * entry. Stored as a string, but could be changed to a Timestamp or LocalDate.
	 */

	@Column(length = 8)
	private LocalDate datum;

	/**
	 * Corresponds to "EXTINFO" - additional metadata related to the output process.
	 */
	@Lob
	@Column(name = "EXTINFO")
	protected byte[] extInfo;

	/**
	 * Stores the status of the output process.
	 */
	@Lob
	@Column(name = "OUTPUTTERSTATUS")
	private String outputterStatus;

	/**
	 * Timestamp of the last update, managed by an entity listener.
	 */
	protected Long lastupdate;

	//
	// GETTERS & SETTERS
	//

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getOutputter() {
		return outputter;
	}

	public void setOutputter(String outputter) {
		this.outputter = outputter;
	}

	public LocalDate getDate() {
		return datum;
	}

	public void setDate(LocalDate datum) {
		this.datum = datum;
	}

	public byte[] getExtInfo() {
		return extInfo;
	}

	public void setExtInfo(byte[] extInfo) {
		this.extInfo = extInfo;
	}

	/**
	 * Returns the outputter status.
	 *
	 * @return The current outputter status.
	 */
	public String getOutputterStatus() {
		return outputterStatus;
	}

	/**
	 * Sets the outputter status.
	 *
	 * @param outputterStatus The new outputter status.
	 */
	public void setOutputterStatus(String outputterStatus) {
		this.outputterStatus = outputterStatus;

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
