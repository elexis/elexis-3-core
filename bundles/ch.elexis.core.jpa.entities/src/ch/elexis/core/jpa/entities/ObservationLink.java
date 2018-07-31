package ch.elexis.core.jpa.entities;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_OBSERVATIONLINK")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "ObservationLink.sourceid.type", query = "SELECT ol FROM ObservationLink ol WHERE ol.deleted = false AND ol.sourceid = :sourceid AND ol.type = :type")
@NamedQuery(name = "ObservationLink.targetid.type", query = "SELECT ol FROM ObservationLink ol WHERE ol.deleted = false AND ol.targetid = :targetid AND ol.type = :type")
@NamedQuery(name = "ObservationLink.targetid.sourceid.type", query = "SELECT ol FROM ObservationLink ol WHERE ol.deleted = false AND ol.targetid = :targetid AND ol.sourceid = :sourceid AND ol.type = :type")
public class ObservationLink implements EntityWithId, EntityWithDeleted {
	
	// Transparently updated by the EntityListener
	protected BigInteger lastupdate;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();
	
	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;
	
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

	@Column(length = 80)
	private String sourceid;

	@Column(length = 80)
	private String targetid;

	@Column(length = 8)
	private String type;

	@Column(length = 255)
	private String description;

	public String getSourceid() {
		return sourceid;
	}

	public void setSourceid(String sourceid) {
		this.sourceid = sourceid;
	}

	public String getTargetid() {
		return targetid;
	}

	public void setTargetid(String targetid) {
		this.targetid = targetid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
