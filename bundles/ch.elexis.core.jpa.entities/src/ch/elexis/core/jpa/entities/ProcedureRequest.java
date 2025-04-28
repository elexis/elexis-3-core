package ch.elexis.core.jpa.entities;

import org.eclipse.persistence.annotations.Cache;

import ch.elexis.core.jpa.entities.converter.BooleanCharacterConverterSafe;
import ch.elexis.core.jpa.entities.listener.EntityWithIdListener;
import ch.elexis.core.model.util.ElexisIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_PROCEDUREREQUEST")
@Cache(expiry = 15000)
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "ProcedureRequest.patientid", query = "SELECT pr FROM ProcedureRequest pr WHERE pr.deleted = false AND pr.patientid = :patientid")
@NamedQuery(name = "ProcedureRequest.encounterid", query = "SELECT pr FROM ProcedureRequest pr WHERE pr.deleted = false AND pr.encounterid = :encounterid")
public class ProcedureRequest extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
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
	private String encounterid;

	@Lob
	private String content;

	public String getPatientId() {
		return patientid;
	}

	public void setPatientId(String patientId) {
		this.patientid = patientId;
	}

	public String getEncounterId() {
		return encounterid;
	}

	public void setEncounterId(String encounterId) {
		this.encounterid = encounterId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
