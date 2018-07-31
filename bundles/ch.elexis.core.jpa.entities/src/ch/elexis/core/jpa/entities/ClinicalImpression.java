package ch.elexis.core.jpa.entities;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.id.ElexisIdGenerator;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_CLINICALIMPRESSION")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "ClinicalImpression.patientid", query = "SELECT cl FROM ClinicalImpression cl WHERE cl.deleted = false AND cl.patientid = :patientid")
@NamedQuery(name = "ClinicalImpression.encounterid", query = "SELECT cl FROM ClinicalImpression cl WHERE cl.deleted = false AND cl.encounterid = :encounterid")
public class ClinicalImpression implements EntityWithId, EntityWithDeleted {

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
	private String patientid;

	@Column(length = 80)
	private String encounterid;

	@Lob
	private String content;

	public String getPatientid() {
		return patientid;
	}

	public void setPatientid(String patientid) {
		this.patientid = patientid;
	}

	public String getEncounterid() {
		return encounterid;
	}

	public void setEncounterid(String encounterid) {
		this.encounterid = encounterid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
