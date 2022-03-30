package ch.elexis.core.jpa.entities;

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
@Table(name = "CH_ELEXIS_CORE_FINDINGS_ENCOUNTER")
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "Encounter.patientid", query = "SELECT en FROM Encounter en WHERE en.deleted = false AND en.patientid = :patientid")
@NamedQuery(name = "Encounter.consultationid", query = "SELECT en FROM Encounter en WHERE en.deleted = false AND en.consultationid = :consultationid")
public class Encounter extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

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

	@Column(length = 80)
	private String patientid;

	@Column(length = 80)
	private String mandatorid;

	@Column(length = 80)
	private String consultationid;

	@Lob
	private String content;

	public String getPatientId() {
		return patientid;
	}

	public void setPatientId(String patientId) {
		this.patientid = patientId;
	}

	public String getConsultationId() {
		return consultationid;
	}

	public void setConsultationId(String consultationId) {
		this.consultationid = consultationId;
	}

	public String getMandatorId() {
		return mandatorid;
	}

	public void setMandatorId(String serviceProviderId) {
		this.mandatorid = serviceProviderId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
