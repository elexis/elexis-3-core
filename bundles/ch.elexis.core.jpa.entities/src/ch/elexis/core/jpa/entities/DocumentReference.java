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
@Table(name = "CH_ELEXIS_CORE_FINDINGS_DOCUMENTREFERENCE")
@Cache(expiry = 15000)
@EntityListeners(EntityWithIdListener.class)
@NamedQuery(name = "DocumentReference.patientid", query = "SELECT dr FROM DocumentReference dr WHERE dr.deleted = false AND dr.patientid = :patientid")
@NamedQuery(name = "DocumentReference.documentid", query = "SELECT dr FROM DocumentReference dr WHERE dr.deleted = false AND dr.documentid = :documentid")
public class DocumentReference extends AbstractEntityWithId implements EntityWithId, EntityWithDeleted {

	// Transparently updated by the EntityListener
	protected Long lastupdate;

	@Id
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert(converter = BooleanCharacterConverterSafe.class)
	protected boolean deleted = false;

	@Column(length = 80)
	private String authorId;

	@Column(length = 512)
	private String keywords;

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
	private String documentstoreid;

	@Column(length = 80)
	private String documentid;

	@Lob
	private String content;

	public String getPatientId() {
		return patientid;
	}

	public void setPatientId(String patientId) {
		this.patientid = patientId;
	}

	public String getDocumentStoreId() {
		return documentstoreid;
	}

	public void setDocumentStoreId(String documentstoreid) {
		this.documentstoreid = documentstoreid;
	}

	public String getDocumentId() {
		return documentid;
	}

	public void setDocumentId(String documentid) {
		this.documentid = documentid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getKeywords() {
		return keywords;
	}
}
